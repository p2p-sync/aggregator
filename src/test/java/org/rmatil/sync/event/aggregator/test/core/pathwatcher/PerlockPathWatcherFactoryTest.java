package org.rmatil.sync.event.aggregator.test.core.pathwatcher;

import name.mitterdorfer.perlock.PathWatcher;
import name.mitterdorfer.perlock.PathWatcherFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.event.aggregator.core.PathEventListener;
import org.rmatil.sync.event.aggregator.core.pathwatcher.PerlockPathWatcherFactory;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.core.APathTest;

import java.nio.file.Paths;
import java.util.concurrent.Executors;
import static org.junit.Assert.*;


public class PerlockPathWatcherFactoryTest {

    protected static PerlockPathWatcherFactory pathWatcherFactory;

    @BeforeClass
    public static void setUp() {
        pathWatcherFactory = new PerlockPathWatcherFactory();

        APathTest.setUp();
    }

    @Test
    public void testCreateWatchers() {
        PathWatcher watcher1 = pathWatcherFactory.createNonRecursiveWatcher(Executors.newFixedThreadPool(1), Config.DEFAULT.getRootTestDir(), new PathEventListener());
        assertFalse("Watcher is already running", watcher1.isRunning());

        PathWatcher watcher2 = pathWatcherFactory.createRecursiveWatcher(Executors.newFixedThreadPool(1), Config.DEFAULT.getRootTestDir(), new PathEventListener());
        assertFalse("Watcher 2 is alreay running", watcher2.isRunning());
    }

}
