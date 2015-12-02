package org.rmatil.sync.event.aggregator.test.core;

import org.junit.*;
import org.rmatil.sync.event.aggregator.api.IEventAggregator;
import org.rmatil.sync.event.aggregator.core.EventAggregator;
import org.rmatil.sync.event.aggregator.core.aggregator.HistoryMoveAggregator;
import org.rmatil.sync.event.aggregator.core.aggregator.IAggregator;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.DeleteEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.modifier.IModifier;
import org.rmatil.sync.event.aggregator.core.modifier.IgnorePathsModifier;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.mocks.MockPathWatcherFactory;
import org.rmatil.sync.event.aggregator.test.mocks.ObjectManagerMock;
import org.rmatil.sync.event.aggregator.test.mocks.PathWatcherMock;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;
import org.rmatil.sync.event.aggregator.test.util.PathChangeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

/**
 * The test class for EventAggregator.
 * Creates a temporary folder in /tmp to test file event
 * aggregation.
 *
 * @see EventAggregator
 * @see APathTest#ROOT_TEST_DIR The root test directory
 */
public class EventAggregatorTest {

    private static final Logger logger = LoggerFactory.getLogger(EventAggregatorTest.class);

    private static final Path ROOT_TEST_DIR = Config.DEFAULT.getRootTestDir();

    protected static IEventAggregator eventAggregator;

    protected static PathChangeEventListener eventListener;

    protected static MockPathWatcherFactory mockPathWatcherFactory;

    protected static PathWatcherMock pathWatcher;

    @BeforeClass
    public static void setUp() {
        APathTest.setUp();

        IAggregator moveAggregator = new HistoryMoveAggregator(new ObjectManagerMock());
        eventListener = new PathChangeEventListener();
        mockPathWatcherFactory = new MockPathWatcherFactory();
        eventAggregator = new EventAggregator(APathTest.ROOT_TEST_DIR, mockPathWatcherFactory);
        eventAggregator.setAggregationInterval(APathTest.TIME_GAP_PUSH_INTERVAL);
        eventAggregator.addAggregator(moveAggregator);
        eventAggregator.addListener(eventListener);

        try {
            logger.debug("Starting event aggregator");
            eventAggregator.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pathWatcher = (PathWatcherMock) mockPathWatcherFactory.getPathWatcherInstance();
    }

    @AfterClass
    public static void tearDown() {
        logger.debug("Stopping event aggregator");
        eventAggregator.stop();

        APathTest.tearDown();
    }

    @Before
    public void before() {
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
    }

    @After
    public void after() {
        FileUtil.deleteTestFile(ROOT_TEST_DIR);
    }


    @Test
    public void aggregateCreationEvent() {
        assertTrue("Failed to assure that eventBag are empty", eventListener.getEvents().isEmpty());

        // create file
        pathWatcher.mockFileCreation(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_PUSH_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Failed to assert that the eventBag is holding only the creation event", 1, eventListener.getEvents().size());
    }

    @Test
    public void aggregateModifyEvent() {
        // create file
        pathWatcher.mockFileCreation(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_PUSH_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // modify file
        pathWatcher.mockFileModify(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_PUSH_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Failed to assert that the eventBag is holding only the modify event", 1, eventListener.getEvents().size());
    }

    @Test
    public void aggregateDeleteEvent() {
        // create file
        pathWatcher.mockFileCreation(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_PUSH_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // delete file
        pathWatcher.mockFileDelete(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_PUSH_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Failed to assert that the eventBag is holding only the delete event", 1, eventListener.getEvents().size());
    }

    /**
     * Currently, a move is not detectable, since the delete event
     * contains only a null value for the hash.
     * We have to enrich the hash for a delete event by using our
     * local stored history about the filesystem.
     */
    @Test
    public void aggregateMoveEvent() {
        pathWatcher.mockFileDelete(APathTest.ROOT_TEST_DIR);
        pathWatcher.mockFileCreation(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_PUSH_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<IEvent> events = eventListener.getEvents();

        assertEquals("Failed to assert that the eventBag is holding both non aggregated events", 2, events.size());
        assertThat("Failed to assert that the eventBag is holding the delete event", events, hasItem(isA(DeleteEvent.class)));
        assertThat("Failed to assert that the eventBag is holding the create event", events, hasItem(isA(CreateEvent.class)));
    }

    @Test
    public void testAccessor() {
        assertEquals("Event listener are not correctly registered", 1, eventAggregator.getListeners().size());
        eventAggregator.removeListener(eventListener);
        assertEquals("Listeners are not correctly removed", 0, eventAggregator.getListeners().size());
        eventAggregator.addListener(eventListener);

        IModifier modifier = new IgnorePathsModifier(new ArrayList<>());
        eventAggregator.addModifier(modifier);
        assertEquals("Modifier are not correctly registered", 1, eventAggregator.getModifiers().size());
        eventAggregator.removeModifier(modifier);
        assertEquals("Modifier are not correctly removed", 0, eventAggregator.getModifiers().size());

        assertEquals("AggregationInterval is not correctly set", APathTest.TIME_GAP_PUSH_INTERVAL, eventAggregator.getAggregationInterval());
    }

}
