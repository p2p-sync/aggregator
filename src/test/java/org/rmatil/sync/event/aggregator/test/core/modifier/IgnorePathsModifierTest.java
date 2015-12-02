package org.rmatil.sync.event.aggregator.test.core.modifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.event.aggregator.core.events.*;
import org.rmatil.sync.event.aggregator.core.modifier.IgnorePathsModifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IgnorePathsModifierTest {

    private static IgnorePathsModifier ignorePathsModifier;

    private static final Path ROOT_TEST_DIR = Paths.get("/tmp/dir/");

    private static final Path IGNORED_DIR = ROOT_TEST_DIR.resolve(".sync");

    @BeforeClass
    public static void setUp() {
        List<Path> ignoredPaths = new ArrayList<>();
        ignoredPaths.add(IGNORED_DIR);
        ignorePathsModifier = new IgnorePathsModifier(ignoredPaths);
    }

    @Test
    public void testIgnore() {
        IEvent ev1 = new CreateEvent(ROOT_TEST_DIR.resolve(".sync/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev2 = new ModifyEvent(ROOT_TEST_DIR.resolve(".sync/someDir/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev3 = new DeleteEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev4 = new MoveEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), ROOT_TEST_DIR.resolve("/tmp/dir/newFile.txt"), "newFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev5 = new MoveEvent(ROOT_TEST_DIR.resolve(".sync/someFile.txt"), ROOT_TEST_DIR.resolve("/tmp/dir/newFile.txt"), "newFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev6 = new MoveEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), ROOT_TEST_DIR.resolve(".sync/newFile.txt"), "newFile.txt", "someHash", System.currentTimeMillis());

        List<IEvent> eventList = new ArrayList<>();
        eventList.add(ev1);
        eventList.add(ev2);
        eventList.add(ev3);
        eventList.add(ev4);
        eventList.add(ev5);
        eventList.add(ev6);

        List<IEvent> modifiedEvents = ignorePathsModifier.modify(eventList);

        assertEquals("to less events are ignored", 2,  modifiedEvents.size());

        assertThat("First event is delete event", modifiedEvents.get(0), instanceOf(DeleteEvent.class));
        assertThat("2nd event is move event", modifiedEvents.get(1), instanceOf(MoveEvent.class));
    }
}
