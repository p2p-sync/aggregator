package org.rmatil.sync.event.aggregator.test.core;

import org.junit.*;
import org.rmatil.sync.event.aggregator.api.IEventAggregator;
import org.rmatil.sync.event.aggregator.core.EventAggregator;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.mocks.MockPathWatcherFactory;
import org.rmatil.sync.event.aggregator.test.mocks.PathWatcherMock;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;
import org.rmatil.sync.event.aggregator.test.util.PathChangeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

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

    private static final Logger logger        = LoggerFactory.getLogger(EventAggregatorTest.class);

    private static final Path   ROOT_TEST_DIR = Config.DEFAULT.getRootTestDir();

    protected static IEventAggregator eventAggregator;

    protected static PathChangeEventListener eventListener;

    protected static MockPathWatcherFactory mockPathWatcherFactory;

    protected static PathWatcherMock pathWatcher;

    @BeforeClass
    public static void setUp() {
        APathTest.setUp();

        eventListener = new PathChangeEventListener();
        mockPathWatcherFactory = new MockPathWatcherFactory();
        eventAggregator = new EventAggregator(APathTest.ROOT_TEST_DIR, mockPathWatcherFactory);
        eventAggregator.setAggregationInterval(APathTest.TIME_GAP_PUSH_INTERVAL);
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

}
