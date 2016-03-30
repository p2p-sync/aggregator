package org.rmatil.sync.event.aggregator.test.core.modifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.rmatil.sync.event.aggregator.core.modifier.IgnoreSameHashModifier;
import org.rmatil.sync.event.aggregator.test.mocks.ObjectManagerMock;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.api.DeleteType;
import org.rmatil.sync.version.api.IObjectManager;
import org.rmatil.sync.version.api.PathType;
import org.rmatil.sync.version.core.model.Delete;
import org.rmatil.sync.version.core.model.PathObject;
import org.rmatil.sync.version.core.model.Version;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IgnoreSameHashModifierTest {

    protected static IObjectManager         objectManagerMock;
    protected static IgnoreSameHashModifier modifier;
    protected static List<IEvent>           events;
    protected static List<IEvent>           events2;

    @BeforeClass
    public static void setUp()
            throws InputOutputException {
        List<String> deleteHistory = new ArrayList<>();
        deleteHistory.add("someDeleteHash");

        List<Version> versions = new ArrayList<>();
        versions.add(new Version("someInitialHash"));

        objectManagerMock = new ObjectManagerMock();
        objectManagerMock.writeObject(new PathObject(
                "myFile.txt",
                "path/to",
                PathType.FILE,
                null,
                false,
                new Delete(
                        DeleteType.EXISTENT,
                        deleteHistory
                ),
                null,
                new HashSet<>(),
                versions
        ));

        objectManagerMock.writeObject(new PathObject(
                "myFile2.txt",
                "path/to",
                PathType.FILE,
                null,
                false,
                new Delete(
                        DeleteType.EXISTENT,
                        deleteHistory
                ),
                null,
                new HashSet<>(),
                versions
        ));

        objectManagerMock.writeObject(new PathObject(
                "4thFile.txt",
                "path/to",
                PathType.FILE,
                null,
                false,
                new Delete(
                        DeleteType.EXISTENT,
                        deleteHistory
                ),
                null,
                new HashSet<>(),
                new ArrayList<>()
        ));

        modifier = new IgnoreSameHashModifier(objectManagerMock);

        events = new ArrayList<>();
        events.add(
                new CreateEvent(
                        Paths.get("path/to/myFile.txt"),
                        "myFile.txt",
                        "someInitialHash",
                        System.currentTimeMillis()
                )
        );

        // event which should be ignored due to the same hash
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/myFile.txt"),
                        "myFile.txt",
                        "someInitialHash",
                        System.currentTimeMillis()
                )
        );

        // event which should be ignored due to the same hash of another modify event
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/myFile.txt"),
                        "myFile.txt",
                        "someInitialHash",
                        System.currentTimeMillis()
                )
        );

        // event which should not be ignored due to a different hash
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/myFile.txt"),
                        "myFile.txt",
                        "some2ndHash",
                        System.currentTimeMillis()
                )
        );

        // event which should be ignored due to a same hash of another modify event
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/myFile.txt"),
                        "myFile.txt",
                        "some3rdHash",
                        System.currentTimeMillis()
                )
        );

        // event which should be ignored due to a same hash of another modify event
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/myFile.txt"),
                        "myFile.txt",
                        "some3rdHash",
                        System.currentTimeMillis()
                )
        );

        // event which should not be ignored since its the last modify event with that version
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/myFile.txt"),
                        "myFile.txt",
                        "some3rdHash",
                        System.currentTimeMillis()
                )
        );


        // file 2

        // event which should be ignored due to the same hash
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/myFile2.txt"),
                        "myFile2.txt",
                        "someInitialHash",
                        System.currentTimeMillis()
                )
        );

        // file 3

        // event which should throw an exception on comparing
        events.add(
                new ModifyEvent(
                        Paths.get("path/to/nonExistentFile.txt"),
                        "nonExistentFile.txt",
                        "someInitialHash",
                        System.currentTimeMillis()
                )
        );

        // file 4
        // one of the following three events must pass the modification

        events.add(
                new ModifyEvent(
                        Paths.get("path/to/4thFile.txt"),
                        "4thFile.txt",
                        "4thHash",
                        System.currentTimeMillis()
                )
        );

        events.add(
                new ModifyEvent(
                        Paths.get("path/to/4thFile.txt"),
                        "4thFile.txt",
                        "4thHash",
                        System.currentTimeMillis()
                )
        );

        events.add(
                new ModifyEvent(
                        Paths.get("path/to/4thFile.txt"),
                        "4thFile.txt",
                        "4thHash",
                        System.currentTimeMillis()
                )
        );

        events2 = new ArrayList<>();

        events2.add(
                new ModifyEvent(
                        Paths.get("path/to/5thFile.txt"),
                        "5thFile.txt",
                        "5thHash",
                        System.currentTimeMillis()
                )
        );

        events2.add(
                new ModifyEvent(
                        Paths.get("path/to/5thFile.txt"),
                        "5thFile.txt",
                        "5thHash",
                        System.currentTimeMillis()
                )
        );
    }

    @Test
    public void test() {
        List<IEvent> modifiedEvents = modifier.modify(events);

        assertEquals("5 events should have passed the modifier", 5, modifiedEvents.size());
        assertThat("First passed event should be create event", modifiedEvents.get(0), is(instanceOf(CreateEvent.class)));
        assertThat("2nd passed event should be modify event", modifiedEvents.get(1), is(instanceOf(ModifyEvent.class)));
        assertEquals("2nd passed event should have 2nd hash", "some2ndHash", modifiedEvents.get(1).getHash());
        assertThat("3rd passed event should be modify event", modifiedEvents.get(2), is(instanceOf(ModifyEvent.class)));
        assertEquals("3rd passed event should have 3rd hash", "some3rdHash", modifiedEvents.get(2).getHash());
        assertThat("4th passed event should be modify event", modifiedEvents.get(3), is(instanceOf(ModifyEvent.class)));
        assertEquals("4th passed event should be for non existent file", "path/to/nonExistentFile.txt", modifiedEvents.get(3).getPath().toString());
        assertThat("5th passed event should be modify event", modifiedEvents.get(4), is(instanceOf(ModifyEvent.class)));
        assertEquals("5th passed event should be for be for 4th file", "path/to/4thFile.txt", modifiedEvents.get(4).getPath().toString());
        assertEquals("5th passed event should have 4th hash", "4thHash", modifiedEvents.get(4).getHash());
    }


    @Test
    public void test2() {
        List<IEvent> modifiedEvents = modifier.modify(events2);

        assertEquals("size should be 1", 1, modifiedEvents.size());
    }
}
