package org.rmatil.sync.event.aggregator.test.core;

import org.junit.*;
import org.rmatil.sync.event.aggregator.api.IEventListener;
import org.rmatil.sync.event.aggregator.core.PathEventListener;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.DeleteEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;
import org.rmatil.sync.event.aggregator.test.util.PathChangeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class PathEventListenerTest {
    private static final Logger logger = LoggerFactory.getLogger(PathEventListenerTest.class);

    private static final Path ROOT_TEST_DIR        = Config.DEFAULT.getRootTestDir();
    private static final long AGGREGATION_INTERVAL = Config.DEFAULT.getTimeGapPushInterval();

    private static PathEventListener        listener;
    private static PathChangeEventListener  eventListener;
    private static ScheduledExecutorService aggregationExecutorService;

    @BeforeClass
    public static void setUp() {
        APathTest.setUp();

        listener = new PathEventListener();
        eventListener = new PathChangeEventListener();
        listener.addListener(eventListener);
    }

    @AfterClass
    public static void tearDown() {
        APathTest.tearDown();
        aggregationExecutorService.shutdownNow();
    }

    @Before
    public void before()
            throws InterruptedException {
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
        eventListener.getEvents().clear();
        // we have to recreate the executor otherwise we would attempt
        // to create new threads on a terminated executor service
        aggregationExecutorService = Executors.newSingleThreadScheduledExecutor();
        aggregationExecutorService.scheduleAtFixedRate(listener, 0, AGGREGATION_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @After
    public void after()
            throws InterruptedException {
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
        eventListener.getEvents().clear();

        aggregationExecutorService.awaitTermination(AGGREGATION_INTERVAL, TimeUnit.MILLISECONDS);
        aggregationExecutorService.shutdownNow();
    }

    @Test
    public void testOnPathCreated()
            throws InterruptedException {

        assertTrue("Event bag is not empty at the beginning", eventListener.getEvents().isEmpty());

        Path file = FileUtil.createTestFile(ROOT_TEST_DIR);
        listener.onPathCreated(file);

        // wait until our listener is notified
        Thread.sleep(AGGREGATION_INTERVAL + 100L);

        assertFalse("Event bag does not contain the new creation event", eventListener.getEvents().isEmpty());
        assertEquals("Event bag does not contain only one element", 1, eventListener.getEvents().size());

        IEvent event = eventListener.getEvents().get(0);

        assertThat("Event is not instance of CreateEvent", event, instanceOf(CreateEvent.class));
        assertEquals("Event name is not equals", event.getEventName(), CreateEvent.EVENT_NAME);
        assertEquals("CreateEvent does not contain the same path element", file, event.getPath());
    }


    @Test
    public void testOnPathModified()
            throws InterruptedException {
        logger.info("Starting testOnPathModified");
        assertTrue("Event bag is not empty at the beginning", eventListener.getEvents().isEmpty());

        logger.info("Creating test file");
        Path file = FileUtil.createTestFile(ROOT_TEST_DIR);
        listener.onPathCreated(file);

        Thread.sleep(100L);

        logger.info("Modifying Test file");
        Path fileModify = FileUtil.modifyTestFile(ROOT_TEST_DIR);
        listener.onPathModified(fileModify);

        // wait until our listener is notified
        Thread.sleep(AGGREGATION_INTERVAL + 100L);

        assertEquals("Event bag does not contain create and modify event", 2, eventListener.getEvents().size());

        IEvent createEvent = eventListener.getEvents().get(0);

        assertThat("Event is not instance of CreateEvent", createEvent, instanceOf(CreateEvent.class));
        assertEquals("CreateEvent does not contain the same path element", file, createEvent.getPath());

        IEvent modifyEvent = eventListener.getEvents().get(1);

        assertThat("Event is not instance of ModifyEvent", modifyEvent, instanceOf(ModifyEvent.class));
        assertEquals("Event name is not equals", modifyEvent.getEventName(), ModifyEvent.EVENT_NAME);
        assertEquals("ModifyEvent does not contain the same path element", file, modifyEvent.getPath());
    }

    @Test
    public void testOnPathDelete()
            throws InterruptedException {
        assertTrue("Event bag is not empty at the beginning", eventListener.getEvents().isEmpty());

        Path file = FileUtil.createTestFile(ROOT_TEST_DIR);
        listener.onPathCreated(file);

        Path fileDelete = FileUtil.deleteTestFile(ROOT_TEST_DIR);
        listener.onPathDeleted(fileDelete);

        // wait until our listener is notified
        Thread.sleep(AGGREGATION_INTERVAL + 100L);

        assertEquals("Event bag does not contain create and delete event", 2, eventListener.getEvents().size());

        IEvent createEvent = eventListener.getEvents().get(0);

        assertThat("Event is not instance of CreateEvent", createEvent, instanceOf(CreateEvent.class));
        assertEquals("CreateEvent does not contain the same path element", file, createEvent.getPath());

        IEvent deleteEvent = eventListener.getEvents().get(1);

        assertThat("Event is not instance of ModifyEvent", deleteEvent, instanceOf(DeleteEvent.class));
        assertEquals("Event name is not equals", deleteEvent.getEventName(), DeleteEvent.EVENT_NAME);
        assertEquals("DeleteEvent does not contain the same path element", file, deleteEvent.getPath());
        assertNotNull("Filename is null", deleteEvent.getName());
        assertNull("Hash is not null", deleteEvent.getHash());
    }

    @Test
    public void testAccessors() {
        assertEquals("listeners are not correctly registered", 1, listener.getListener().size());

        IEventListener l = new PathChangeEventListener();
        listener.addListener(l);
        assertEquals("listeners are not correctly registered after adding", 2, listener.getListener().size());

        listener.removeListener(l);
        assertEquals("listeners are not correctly removed", 1, listener.getListener().size());
    }
}
