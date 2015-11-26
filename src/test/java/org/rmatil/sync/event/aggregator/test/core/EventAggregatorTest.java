package org.rmatil.sync.event.aggregator.test.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.event.aggregator.api.IEventAggregator;
import org.rmatil.sync.event.aggregator.core.EventAggregator;
import org.rmatil.sync.event.aggregator.test.mocks.MockPathWatcherFactory;
import org.rmatil.sync.event.aggregator.test.mocks.PathWatcherMock;
import org.rmatil.sync.event.aggregator.test.util.PathChangeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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

    protected static IEventAggregator eventAggregator;

    protected static PathChangeEventListener eventListener;

    protected static MockPathWatcherFactory mockPathWatcherFactory;

    @BeforeClass
    public static void setUp() {
        APathTest.setUp();

        eventListener = new PathChangeEventListener();
        mockPathWatcherFactory = new MockPathWatcherFactory();
        eventAggregator = new EventAggregator(APathTest.ROOT_TEST_DIR, mockPathWatcherFactory);
        eventAggregator.addListener(eventListener);

        try {
            logger.debug("Starting event aggregator");
            eventAggregator.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() {
        logger.debug("Stopping event aggregator");
        eventAggregator.stop();

        APathTest.tearDown();
    }


    @Test
    public void aggregateEventsTest() {
        assertTrue("Failed to assure that eventBag are empty", eventListener.getEvents().isEmpty());

        PathWatcherMock pathWatcher = (PathWatcherMock) mockPathWatcherFactory.getPathWatcherInstance();

        // create file
        pathWatcher.mockFileCreation(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_POLL_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse("Failed to assert that eventBag is not empty after creation", eventListener.getEvents().isEmpty());
        assertEquals("Failed to assert that the eventBag is holding only the creation event", 1, eventListener.getEvents().size());

        // modify file
        pathWatcher.mockFileModify(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_POLL_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse("Failed to assert that eventBag is not empty after modifying", eventListener.getEvents().isEmpty());
        assertEquals("Failed to assert that the eventBag is holding only the modify event", 1, eventListener.getEvents().size());

        // delete file
        pathWatcher.mockFileDelete(APathTest.ROOT_TEST_DIR);

        try {
            Thread.sleep(APathTest.TIME_GAP_POLL_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse("Failed to assert that eventBag is not empty after deleting", eventListener.getEvents().isEmpty());
        assertEquals("Failed to assert that the eventBag is holding only the delete event", 1, eventListener.getEvents().size());
    }

}
