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
    private static IgnorePathsModifier ignorePathsModifier2;

    private static final Path ROOT_TEST_DIR = Paths.get("/tmp/dir/");

    private static final Path IGNORED_DIR = ROOT_TEST_DIR.resolve(".sync");

    @BeforeClass
    public static void setUp() {
        List<Path> ignoredPaths = new ArrayList<>();
        ignoredPaths.add(IGNORED_DIR);
        ignorePathsModifier = new IgnorePathsModifier(ignoredPaths);

        List<String> ignoredPatterns = new ArrayList<>();
        ignoredPatterns.add("*.java"); // ignore all files ending with .java
        ignoredPatterns.add("**_*"); // ignore all files having a underline in the name
        ignoredPatterns.add(".DS_Store"); // ignore all .DS_Store files
        ignoredPatterns.add("**.swx"); // ignore swap files of vim (?)
        ignoredPatterns.add("**.swp"); // ignore swap files of vim
        ignoredPatterns.add("**Thumbs.db");

        ignorePathsModifier2 = new IgnorePathsModifier(ignoredPaths, ignoredPatterns);
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

        assertEquals("to less events are ignored", 2, modifiedEvents.size());

        assertThat("First event is delete event", modifiedEvents.get(0), instanceOf(DeleteEvent.class));
        assertThat("2nd event is move event", modifiedEvents.get(1), instanceOf(MoveEvent.class));
    }

    @Test
    public void testWithIgnoreGlobs() {
        IEvent ev1 = new CreateEvent(ROOT_TEST_DIR.resolve(".sync/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev2 = new ModifyEvent(ROOT_TEST_DIR.resolve(".sync/someDir/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev3 = new DeleteEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), "someFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev4 = new MoveEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), ROOT_TEST_DIR.resolve("/tmp/dir/newFile.txt"), "newFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev5 = new MoveEvent(ROOT_TEST_DIR.resolve(".sync/someFile.txt"), ROOT_TEST_DIR.resolve("/tmp/dir/newFile.txt"), "newFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev6 = new MoveEvent(ROOT_TEST_DIR.resolve("/tmp/dir/someFile.txt"), ROOT_TEST_DIR.resolve(".sync/newFile.txt"), "newFile.txt", "someHash", System.currentTimeMillis());
        IEvent ev7 = new CreateEvent(ROOT_TEST_DIR.resolve(".DS_Store"), ".DS_Store", "someHash", System.currentTimeMillis());
        IEvent ev8 = new CreateEvent(Paths.get("myFile.java"), "myFile.java", "someHash", System.currentTimeMillis());
        IEvent ev9 = new CreateEvent(ROOT_TEST_DIR.resolve("path/to/myFile.java"), "myFile.java", "someHash", System.currentTimeMillis()); // should not be ignored -> glob pattern not crossing dir boundaries
        IEvent ev10 = new CreateEvent(ROOT_TEST_DIR.resolve("path/to/file_with_underlines.txt"), "file_with_underlines.txt", "someHash", System.currentTimeMillis());
        IEvent ev11 = new MoveEvent(ROOT_TEST_DIR.resolve("path/to/file.txt"), ROOT_TEST_DIR.resolve("path/to/file.swp"), "file.txt", "someHash", System.currentTimeMillis());
        IEvent ev12 = new MoveEvent(ROOT_TEST_DIR.resolve("path/to/file.swp"), ROOT_TEST_DIR.resolve("path/to/file.txt"), "file.txt", "someHash", System.currentTimeMillis());
        IEvent ev13 = new CreateEvent(ROOT_TEST_DIR.resolve("path/to/.myFileUser2.txt.swp"), ".myFileUser2.txt.swp", "someHash", System.currentTimeMillis());
        IEvent ev14 = new CreateEvent(Paths.get("Thumbs.db"), "Thumbs.db", "someHash", System.currentTimeMillis());
        IEvent ev15 = new CreateEvent(ROOT_TEST_DIR.resolve("path/to/Thumbs.db"), "Thumbs.db", "someHash", System.currentTimeMillis());
        IEvent ev16 = new MoveEvent(ROOT_TEST_DIR.resolve(".myFile.swp"), ROOT_TEST_DIR.resolve("path/to/.myFile.swp"), ".myFile.swp", "someHash", System.currentTimeMillis());

        List<IEvent> eventList = new ArrayList<>();
        eventList.add(ev1);
        eventList.add(ev2);
        eventList.add(ev3);
        eventList.add(ev4);
        eventList.add(ev5);
        eventList.add(ev6);
        eventList.add(ev7);
        eventList.add(ev8);
        eventList.add(ev9);
        eventList.add(ev10);
        eventList.add(ev11);
        eventList.add(ev12);
        eventList.add(ev13);
        eventList.add(ev14);
        eventList.add(ev15);
        eventList.add(ev16);


        List<IEvent> modifiedEvents = ignorePathsModifier2.modify(eventList);

        assertEquals("to less events are ignored", 3, modifiedEvents.size());

        assertThat("First event is delete event", modifiedEvents.get(0), instanceOf(DeleteEvent.class));
        assertThat("2nd event is move event", modifiedEvents.get(1), instanceOf(MoveEvent.class));
        assertThat("3rd event is create event", modifiedEvents.get(2), instanceOf(CreateEvent.class));
    }
}
