package org.rmatil.sync.event.aggregator.test.core.modifier;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.rmatil.sync.event.aggregator.core.modifier.IgnoreDirectoryModifier;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.core.APathTest;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class IgnoreDirectoryModifierTest {

    protected static IgnoreDirectoryModifier modifier;

    protected static Path ROOT_DIR = Config.DEFAULT.getRootTestDir();

    @BeforeClass
    public static void setUp() {
        APathTest.setUp();
        modifier = new IgnoreDirectoryModifier(ROOT_DIR);
    }

    @AfterClass
    public static void tearDown() {
        APathTest.tearDown();
    }

    @Test
    public void testModify() {
        Path testFile = FileUtil.createTestFile(Config.DEFAULT.getRootTestDir());
        Path testDir = FileUtil.createTestDir(Config.DEFAULT.getRootTestDir());

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        IEvent ev1 = new CreateEvent(ROOT_DIR.relativize(testFile), testFile.getFileName().toString(), "someHash", System.currentTimeMillis());
        IEvent ev2 = new CreateEvent(ROOT_DIR.relativize(testDir), testDir.getFileName().toString(), null, System.currentTimeMillis());

        // modify event
        IEvent ev3 = new ModifyEvent(ROOT_DIR.relativize(testDir), testDir.getFileName().toString(), null, System.currentTimeMillis());
        IEvent ev4 = new ModifyEvent(ROOT_DIR.relativize(testFile), testFile.getFileName().toString(), "someHash2", System.currentTimeMillis());

        List<IEvent> events = new ArrayList<>();
        events.add(ev1);
        events.add(ev2);
        events.add(ev3);
        events.add(ev4);


        List<IEvent> modifiedEvents = modifier.modify(events);

        assertEquals("Events do not contain expected nr of events", 3, modifiedEvents.size());
        assertThat("CreateEvents are not contained", modifiedEvents, hasItems(ev1, ev2));
        assertThat("ModifyEvent for File is not contained", modifiedEvents, hasItem(ev4));
    }
}
