package org.rmatil.sync.event.aggregator.test.core.aggregator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.commons.path.Naming;
import org.rmatil.sync.event.aggregator.core.aggregator.HistoryMoveAggregator;
import org.rmatil.sync.event.aggregator.core.events.*;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.mocks.ObjectManagerMock;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.api.IObjectManager;
import org.rmatil.sync.version.api.PathType;
import org.rmatil.sync.version.core.model.PathObject;
import org.rmatil.sync.version.core.model.Version;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HistoryMoveAggregatorTest {

    protected static HistoryMoveAggregator moveAggregator;

    protected static IObjectManager objectManagerMock;

    protected static List<IEvent> forceMoveEventList;

    protected static List<IEvent> forceNoMoveEventList;

    protected static Path oldPath = Config.DEFAULT.getRootTestDir().resolve("modify.txt");

    protected static Path newPath = Config.DEFAULT.getRootTestDir().resolve("created/modify.txt");

    protected static String fileName = "modify.txt";

    protected static String fileHash = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

    protected static long firstTimestamp;

    protected static long secondTimestamp;


    @BeforeClass
    public static void setUp() {
        objectManagerMock = new ObjectManagerMock();
        moveAggregator = new HistoryMoveAggregator(
                objectManagerMock
        );
    }

    @Before
    public void before() {
        forceMoveEventList = new ArrayList<>();
        forceNoMoveEventList = new ArrayList<>();

        firstTimestamp = System.currentTimeMillis();
        // this must be greater than HistoryMoveAggregator#EVENT_TIMESTAMP_TOLERANCE
        secondTimestamp = firstTimestamp + 1100;

        DeleteEvent deleteEvent = new DeleteEvent(oldPath, fileName, fileHash, firstTimestamp);
        CreateEvent createEvent = new CreateEvent(newPath, fileName, fileHash, secondTimestamp);

        forceMoveEventList.add(deleteEvent);
        forceMoveEventList.add(createEvent);

        // order the events the other way
        CreateEvent createEvent2 = new CreateEvent(newPath, fileName, fileHash, firstTimestamp);
        DeleteEvent deleteEvent2 = new DeleteEvent(oldPath, fileName, fileHash, secondTimestamp);

        forceNoMoveEventList.add(deleteEvent2);
        forceNoMoveEventList.add(createEvent2);
    }

    @Test
    public void testMultipleDirCreation() {
        List<IEvent> events = new ArrayList<>();

        CreateEvent c1 = new CreateEvent(Config.DEFAULT.getRootTestDir().resolve(Paths.get("myDir1")), "myDir1", null, System.currentTimeMillis());
        CreateEvent c2 = new CreateEvent(Config.DEFAULT.getRootTestDir().resolve(Paths.get("myDir2")), "myDir2", null, System.currentTimeMillis());
        CreateEvent c3 = new CreateEvent(Config.DEFAULT.getRootTestDir().resolve(Paths.get("myDir3")), "myDir3", null, System.currentTimeMillis());
        CreateEvent c4 = new CreateEvent(Config.DEFAULT.getRootTestDir().resolve(Paths.get("myDir4")), "myDir4", null, System.currentTimeMillis());

        events.add(c1);
        events.add(c2);
        events.add(c3);
        events.add(c4);

        List<IEvent> results = moveAggregator.aggregate(events);
        assertEquals("Results do not contain all dir creation events", 4, results.size());
    }

    @Test
    public void testMoveEvent() {
        List<IEvent> results = moveAggregator.aggregate(forceMoveEventList);

        assertEquals("Result does not only contain the move event", 1, results.size());

        IEvent event = results.get(0);
        assertThat("Event is not instance of CreateEvent", event, instanceOf(MoveEvent.class));

        MoveEvent moveEvent = (MoveEvent) event;

        assertEquals("Old path is not the same", oldPath, moveEvent.getPath());
        assertEquals("New path is not the same", newPath, moveEvent.getNewPath());
        assertEquals("New name is not the same", fileName, moveEvent.getName());
        assertEquals("New hash is not the same", fileHash, moveEvent.getHash());
        assertEquals("New timestamp is not the expected one", secondTimestamp, moveEvent.getTimestamp());
    }

    @Test
    public void testNoMoveEvent() {
        List<IEvent> results = moveAggregator.aggregate(forceNoMoveEventList);

        assertEquals("Result does not contain both events", 2, results.size());

        IEvent firstEvent = results.get(0);
        IEvent secondEvent = results.get(1);

        assertThat("First event is not the create event", firstEvent, instanceOf(CreateEvent.class));
        assertThat("Second event is not the delete event", secondEvent, instanceOf(DeleteEvent.class));

        assertEquals("Create event's path is not the same", newPath, firstEvent.getPath());
        assertEquals("Create event's file name is not the same", fileName, firstEvent.getName());
        assertEquals("Create event's hash is not the same", fileHash, firstEvent.getHash());
        assertEquals("Create event's timestamp is not the same", firstTimestamp, firstEvent.getTimestamp());

        assertEquals("Delete event's path is not the same", oldPath, secondEvent.getPath());
        assertEquals("Delete event's file name is not the same", fileName, secondEvent.getName());
        assertEquals("Delete event's hash is not the same", fileHash, secondEvent.getHash());
        assertEquals("Delete event's timestamp is not the same", secondTimestamp, secondEvent.getTimestamp());
    }

    @Test
    public void testSingleEvent() {
        List<IEvent> singleEvent = new ArrayList<>();
        IEvent event = new DeleteEvent(oldPath, fileName, fileHash, secondTimestamp);
        singleEvent.add(event);

        List<IEvent> results = moveAggregator.aggregate(singleEvent);

        assertEquals("Result does not contain the single event", 1, results.size());
        assertEquals("Result does not contain the same single event", event, results.get(0));
    }

    @Test
    public void testMultipleUnrelatedEvents() {
        long beforeFirstTimestamp = firstTimestamp - 1000L;
        ModifyEvent modifyEvent = new ModifyEvent(oldPath, fileName, fileHash, beforeFirstTimestamp);
        forceMoveEventList.add(0, modifyEvent);

        long afterSecondTimestamp = secondTimestamp + 1000L;
        ModifyEvent modifyEvent2 = new ModifyEvent(newPath, fileName, fileHash, afterSecondTimestamp);
        forceMoveEventList.add(modifyEvent2);

        List<IEvent> results = moveAggregator.aggregate(forceMoveEventList);

        assertEquals("Results do not contain all events", 3, results.size());

        IEvent firstEvent = results.get(0);
        assertThat("First event is not modify event", firstEvent, instanceOf(ModifyEvent.class));
        assertEquals("First event is not the same instance of modify event", modifyEvent, firstEvent);

        IEvent event = results.get(1);
        assertThat("Event is not instance of MoveEvent", event, instanceOf(MoveEvent.class));

        MoveEvent moveEvent = (MoveEvent) event;

        assertEquals("Old path is not the same", oldPath, moveEvent.getPath());
        assertEquals("New path is not the same", newPath, moveEvent.getNewPath());
        assertEquals("New name is not the same", fileName, moveEvent.getName());
        assertEquals("New hash is not the same", fileHash, moveEvent.getHash());
        assertEquals("New timestamp is not the expected one", secondTimestamp, moveEvent.getTimestamp());

        IEvent thirdEvent = results.get(2);
        assertThat("Third event is not modify event", thirdEvent, instanceOf(ModifyEvent.class));
        assertEquals("Third event is not the same instance of modify event", modifyEvent2, thirdEvent);

    }

    @Test
    public void testMultipleDeletionWithSameHash() {
        // add a second create event
        long beforeFirstTimestamp = firstTimestamp - 1000L;
        DeleteEvent delete = new DeleteEvent(Config.DEFAULT.getRootTestDir().resolve("created2/newFile.txt"), "newFile.txt", fileHash, beforeFirstTimestamp);
        forceMoveEventList.add(0, delete);

        List<IEvent> results = moveAggregator.aggregate(forceMoveEventList);

        assertEquals("Result does not contain all unmodified events", 3, results.size());
        assertEquals("First event is not the 2nd delete event", delete, results.get(0));
        assertThat("Second event is not the delete event", results.get(1), instanceOf(DeleteEvent.class));
        assertThat("Third event is not the 1st create event", results.get(2), instanceOf(CreateEvent.class));
    }

    @Test
    public void testMultipleCreationWithSameHash() {
        // add a second create event
        long beforeFirstTimestamp = firstTimestamp - 1000L;
        CreateEvent createEvent = new CreateEvent(Config.DEFAULT.getRootTestDir().resolve("created2/newFile.txt"), "newFile.txt", fileHash, beforeFirstTimestamp);
        forceMoveEventList.add(0, createEvent);

        List<IEvent> results = moveAggregator.aggregate(forceMoveEventList);

        assertEquals("Result does not contain all unmodified events", 3, results.size());
        assertEquals("First event is not the 2nd create event", createEvent, results.get(0));
        assertThat("Second event is not the delete event", results.get(1), instanceOf(DeleteEvent.class));
        assertThat("Third event is not the 1st create event", results.get(2), instanceOf(CreateEvent.class));
    }

    @Test
    public void testMoveEventWithHistory()
            throws InputOutputException {

        Version v1 = new Version("hashOfV1");
        ArrayList<Version> versions = new ArrayList<>();
        versions.add(v1);

        PathObject testOldPath = new PathObject(
                fileName,
                Naming.getPathWithoutFileName(fileName, oldPath.toString()),
                PathType.FILE,
                false,
                false,
                null,
                new HashSet<>(),
                versions
        );

        objectManagerMock.writeObject(testOldPath);
        DeleteEvent deleteEvent = new DeleteEvent(oldPath, fileName, null, firstTimestamp);
        CreateEvent createEvent = new CreateEvent(newPath, fileName, v1.getHash(), secondTimestamp);

        List<IEvent> forceLookupInHistoryList = new ArrayList<>();
        forceLookupInHistoryList.add(deleteEvent);
        forceLookupInHistoryList.add(createEvent);

        List<IEvent> results = moveAggregator.aggregate(forceLookupInHistoryList);

        assertEquals("Result does not only contain the move event", 1, results.size());
        assertThat("Event is not move event", results.get(0), instanceOf(MoveEvent.class));
    }
}
