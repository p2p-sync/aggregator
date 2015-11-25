package org.rmatil.sync.event.aggregator.core.pathwatcher;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

/**
 * The interface for the path watcher factory
 *
 * @see APathWatcherFactory
 * @see PerlockPathWatcherFactory
 */
public interface IPathWatcherFactory {

    /**
     * Creates a watcher recursively watching all children of the given root path
     *
     * @param executorService The executor service used to manage the watcher
     * @param rootPath The root path to watch
     * @param changeListener The listener which should be notified on events
     *
     * @return The created PathWatcher
     */
    PathWatcher createRecursiveWatcher(ExecutorService executorService, Path rootPath, PathChangeListener changeListener);

    /**
     * Creates a non recursive path watcher, i.e. only watches for changes
     * in the root path but not in any of its subfolders
     *
     * @param executorService The executor service used to manage the watcher
     * @param rootPath The root path to watch
     * @param changeListener The listener which should be notified on events
     *
     * @return The created PathWatcher
     */
    PathWatcher createNonRecursiveWatcher(ExecutorService executorService, Path rootPath, PathChangeListener changeListener);
}
