package org.rmatil.sync.event.aggregator.test.core;

import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Use this base class for creating and removing
 * the test directory on disk in setUp resp. tearDown
 */
public abstract class APathTest {

    /**
     * The root folder used to test
     */
    public static final Path ROOT_TEST_DIR = Config.DEFAULT.getRootTestDir();

    /**
     * Push interval for event aggregator
     */
    public static final long TIME_GAP_PUSH_INTERVAL = Config.DEFAULT.getTimeGapPushInterval();

    /**
     * Creates the test directory if not existing yet
     */
    public static void setUp() {
        try {
            // create test dir
            if (!Files.exists(ROOT_TEST_DIR)) {
                Files.createDirectory(ROOT_TEST_DIR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the test directory and all its contents
     */
    public static void tearDown() {
        FileUtil.delete(ROOT_TEST_DIR.toFile());
    }
}
