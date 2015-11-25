package org.rmatil.sync.event.aggregator.core.pathwatcher;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;
import name.mitterdorfer.perlock.PathWatcherFactory;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

/**
 * A PathWatcherFactory using the Perlock Path Watcher
 * to watch a specified folder
 */
public class PerlockPathWatcherFactory extends APathWatcherFactory {

    public PathWatcher createRecursiveWatcher(ExecutorService executorService, Path rootPath, PathChangeListener changeListener) {
        PathWatcherFactory pathWatcherFactory = new PathWatcherFactory(executorService);
        return pathWatcherFactory.createRecursiveWatcher(rootPath, changeListener);
    }

    public PathWatcher createNonRecursiveWatcher(ExecutorService executorService, Path rootPath, PathChangeListener changeListener) {
        PathWatcherFactory pathWatcherFactory = new PathWatcherFactory(executorService);
        return pathWatcherFactory.createNonRecursiveWatcher(rootPath, changeListener);
    }

}
