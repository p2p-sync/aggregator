package org.rmatil.sync.event.aggregator.test.mocks;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;
import org.rmatil.sync.event.aggregator.core.pathwatcher.APathWatcherFactory;
import org.rmatil.sync.event.aggregator.core.pathwatcher.PerlockPathWatcherFactory;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class MockPathWatcherFactory extends APathWatcherFactory {

    protected PathWatcher pathWatcherInstance;

    public PathWatcher createRecursiveWatcher(ExecutorService pathWatcherExecutorService, Path rootPath, PathChangeListener changeListener) {
        if (null == this.pathWatcherInstance) {
            PathWatcherFactoryMock pathWatcherFactoryMock = new PathWatcherFactoryMock(pathWatcherExecutorService);
            this.pathWatcherInstance = pathWatcherFactoryMock.createRecursiveWatcher(rootPath, changeListener);
        }

        return this.pathWatcherInstance;
    }

    public PathWatcher createNonRecursiveWatcher(ExecutorService pathWatcherExecutorService, Path rootPath, PathChangeListener changeListener) {
        if (null == this.pathWatcherInstance) {
            PathWatcherFactoryMock pathWatcherFactoryMock = new PathWatcherFactoryMock(pathWatcherExecutorService);
            this.pathWatcherInstance = pathWatcherFactoryMock.createNonRecursiveWatcher(rootPath, changeListener);
        }

        return this.pathWatcherInstance;
    }

    public PathWatcher getPathWatcherInstance() {
        return this.pathWatcherInstance;
    }
}
