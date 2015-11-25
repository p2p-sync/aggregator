package org.rmatil.sync.event.aggregator.test.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.event.aggregator.api.IEventAggregator;
import org.rmatil.sync.event.aggregator.core.EventAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The test class for EventAggregator.
 * Creates a temporary folder in /tmp to test file event
 * aggregation.
 *
 * @see EventAggregator
 * @see EventAggregatorTest#ROOT_TEST_DIR The root test directory
 */
public class EventAggregatorTest {

    private static final Logger logger = LoggerFactory.getLogger(EventAggregatorTest.class);

    /**
     * as starting/stop happens in
     * the background we might miss eventBag. Wait a bit...
     */
    private static final long TIME_GAP_LIFE_CYCLE    = 200L;

    /**
     * Jimfs polls only every 5 seconds...
     */
    private static final long TIME_GAP_POLL_INTERVAL = 5500L;

    /**
     * The root folder used to test
     */
    private static final Path ROOT_TEST_DIR          = Paths.get("/tmp/org.rmatil.sync.event.aggregator.test.dir");

    protected static IEventAggregator eventAggregator;

    protected static PathChangeEventListener eventListener;

    @BeforeClass
    public static void setUp() {

        try {
            // create test dir
            if (!Files.exists(ROOT_TEST_DIR)) {
                Files.createDirectory(ROOT_TEST_DIR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        eventListener = new PathChangeEventListener();
        eventAggregator = new EventAggregator(ROOT_TEST_DIR);
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

        FileUtil.delete(ROOT_TEST_DIR.toFile());
    }


    @Test
    public void aggregateEventsTest() {
        assertTrue("Failed to assure that eventBag are empty", eventListener.getEvents().isEmpty());

        Path testFile = ROOT_TEST_DIR.resolve("file1.txt");

        try {
            Files.createFile(testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse("Failed to assert that eventBag are not empty", eventListener.getEvents().isEmpty());
        assertEquals("Failed to assert that the eventBag is holding only the creation event", 1, eventListener.getEvents().size());
    }

}
