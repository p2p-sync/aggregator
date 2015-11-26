package org.rmatil.sync.event.aggregator.test.core;

import org.junit.*;
import org.rmatil.sync.event.aggregator.core.PathEventListener;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.DeleteEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class PathEventListenerTest {

    private static final Path ROOT_TEST_DIR = Config.DEFAULT.getRootTestDir();
    private static PathEventListener listener;

    @BeforeClass
    public static void setUp() {
        APathTest.setUp();

        listener = new PathEventListener();
    }

    @AfterClass
    public static void tearDown() {
        APathTest.tearDown();
    }

    @Before
    public void before() {
        listener.clearEvents();
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
    }

    @After
    public void after() {
        listener.clearEvents();
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
    }

    @Test
    public void testOnPathCreated() {
        assertTrue("Event bag is not empty at the beginning", listener.getEventBag().isEmpty());

        Path file = FileUtil.createTestFile(ROOT_TEST_DIR);
        listener.onPathCreated(file);

        assertFalse("Event bag does not contain the new creation event", listener.getEventBag().isEmpty());
        assertEquals("Event bag does not contain only one element", 1, listener.getEventBag().size());

        IEvent event = listener.getEventBag().get(0);

        assertThat("Event is not instance of CreateEvent", event, instanceOf(CreateEvent.class));
        assertEquals("CreateEvent does not contain the same path element", file, event.getPath());
    }


    @Test
    public void testOnPathModified() {
        assertTrue("Event bag is not empty at the beginning", listener.getEventBag().isEmpty());

        Path file = FileUtil.createTestFile(ROOT_TEST_DIR);
        listener.onPathCreated(file);

        Path fileModify = FileUtil.modifyTestFile(ROOT_TEST_DIR);
        listener.onPathModified(fileModify);

        assertEquals("Event bag does not contain create and modify event", 2, listener.getEventBag().size());

        IEvent createEvent = listener.getEventBag().get(0);

        assertThat("Event is not instance of CreateEvent", createEvent, instanceOf(CreateEvent.class));
        assertEquals("CreateEvent does not contain the same path element", file, createEvent.getPath());

        IEvent modifyEvent = listener.getEventBag().get(1);

        assertThat("Event is not instance of ModifyEvent", modifyEvent, instanceOf(ModifyEvent.class));
        assertEquals("ModifyEvent does not contain the same path element", file, modifyEvent.getPath());
    }

    @Test
    public void testOnPathDelete() {
        assertTrue("Event bag is not empty at the beginning", listener.getEventBag().isEmpty());

        Path file = FileUtil.createTestFile(ROOT_TEST_DIR);
        listener.onPathCreated(file);

        Path fileDelete = FileUtil.deleteTestFile(ROOT_TEST_DIR);
        listener.onPathDeleted(fileDelete);

        assertEquals("Event bag does not contain create and delete event", 2, listener.getEventBag().size());

        IEvent createEvent = listener.getEventBag().get(0);

        assertThat("Event is not instance of CreateEvent", createEvent, instanceOf(CreateEvent.class));
        assertEquals("CreateEvent does not contain the same path element", file, createEvent.getPath());

        IEvent deleteEvent = listener.getEventBag().get(1);

        assertThat("Event is not instance of ModifyEvent", deleteEvent, instanceOf(DeleteEvent.class));
        assertEquals("DeleteEvent does not contain the same path element", file, deleteEvent.getPath());
        assertNull("Filename is not null", deleteEvent.getName());
        assertNull("Hash is not null", deleteEvent.getHash());
    }
}
