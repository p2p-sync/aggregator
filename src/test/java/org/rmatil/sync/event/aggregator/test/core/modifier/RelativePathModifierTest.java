package org.rmatil.sync.event.aggregator.test.core.modifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.event.aggregator.core.events.*;
import org.rmatil.sync.event.aggregator.core.modifier.RelativePathModifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RelativePathModifierTest {

    private static RelativePathModifier relativePathModifier;

    private static final Path ROOT_TEST_DIR = Paths.get("/tmp/dir/");

    @BeforeClass
    public static void setUp() {
        relativePathModifier = new RelativePathModifier(ROOT_TEST_DIR);
    }

    @Test
    public void testModifyCreateEvent() {
        IEvent ev1 = new CreateEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev2 = new ModifyEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev3 = new DeleteEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev4 = new MoveEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), ROOT_TEST_DIR.resolve("newFile.txt"), "newFile.txt", "someHash", System.currentTimeMillis());

        List<IEvent> eventList = new ArrayList<>();
        eventList.add(ev1);
        eventList.add(ev2);
        eventList.add(ev3);
        eventList.add(ev4);

        List<IEvent> modifiedEvents = relativePathModifier.modify(eventList);

        assertEquals("Not all events are contained", 4, modifiedEvents.size());

        assertThat("First event is create event", modifiedEvents.get(0), instanceOf(CreateEvent.class));
        assertEquals("Path is not the expected", "someFile.txt", modifiedEvents.get(0).getPath().toString());

        assertThat("2nd event is modify event", modifiedEvents.get(1), instanceOf(ModifyEvent.class));
        assertEquals("Path is not the expected", "someFile.txt", modifiedEvents.get(1).getPath().toString());

        assertThat("3rd event is create event", modifiedEvents.get(2), instanceOf(DeleteEvent.class));
        assertEquals("Path is not the expected", "someFile.txt", modifiedEvents.get(2).getPath().toString());

        assertThat("4th event is create event", modifiedEvents.get(3), instanceOf(MoveEvent.class));
        assertEquals("Path is not the expected", "someFile.txt", modifiedEvents.get(3).getPath().toString());
        MoveEvent mvEvent = (MoveEvent) modifiedEvents.get(3);
        assertEquals("New Path is not the expected", "newFile.txt",mvEvent.getNewPath().toString());
    }

    @Test
    public void testDir() {
        String file = "/tmp/dir/myDir/someFile.txt";
        String newFile = "/tmp/dir/myDir/newFile.txt";
        String fileName = "someFile.txt";
        String newFileName = "newFile.txt";
        IEvent ev1 = new CreateEvent(ROOT_TEST_DIR.resolve(file), fileName, "someHash", System.currentTimeMillis());
        IEvent ev2 = new ModifyEvent(ROOT_TEST_DIR.resolve(file), fileName, "someHash", System.currentTimeMillis());
        IEvent ev3 = new DeleteEvent(ROOT_TEST_DIR.resolve(file), fileName, "someHash", System.currentTimeMillis());
        IEvent ev4 = new MoveEvent(ROOT_TEST_DIR.resolve(file), ROOT_TEST_DIR.resolve(newFile), newFileName, "someHash", System.currentTimeMillis());

        List<IEvent> eventList = new ArrayList<>();
        eventList.add(ev1);
        eventList.add(ev2);
        eventList.add(ev3);
        eventList.add(ev4);

        List<IEvent> modifiedEvents = relativePathModifier.modify(eventList);

        assertEquals("Not all events are contained", 4, modifiedEvents.size());

        assertThat("First event is create event", modifiedEvents.get(0), instanceOf(CreateEvent.class));
        assertEquals("Path is not the expected", "myDir/someFile.txt", modifiedEvents.get(0).getPath().toString());

        assertThat("2nd event is modify event", modifiedEvents.get(1), instanceOf(ModifyEvent.class));
        assertEquals("Path is not the expected", "myDir/someFile.txt", modifiedEvents.get(1).getPath().toString());

        assertThat("3rd event is create event", modifiedEvents.get(2), instanceOf(DeleteEvent.class));
        assertEquals("Path is not the expected", "myDir/someFile.txt", modifiedEvents.get(2).getPath().toString());

        assertThat("4th event is create event", modifiedEvents.get(3), instanceOf(MoveEvent.class));
        assertEquals("Path is not the expected", "myDir/someFile.txt", modifiedEvents.get(3).getPath().toString());
        MoveEvent mvEvent = (MoveEvent) modifiedEvents.get(3);
        assertEquals("New Path is not the expected", "myDir/newFile.txt",mvEvent.getNewPath().toString());
    }
}
