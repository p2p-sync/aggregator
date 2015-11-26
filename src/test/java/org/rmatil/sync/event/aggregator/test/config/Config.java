package org.rmatil.sync.event.aggregator.test.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum Config {
    DEFAULT();

    private Path rootTestDir;

    private String testFileName1 = "file1.txt";

    private long timeGapPollInterval;

    Config() {
        rootTestDir = Paths.get("./org.rmatil.sync.event.aggregator.test.dir");
        timeGapPollInterval = 5500L;
    }

    public Path getRootTestDir() {
        return this.rootTestDir;
    }

    public long getTimeGapPollInterval() {
        return this.timeGapPollInterval;
    }

    public String getTestFileName1() {
        return this.testFileName1;
    }
}
