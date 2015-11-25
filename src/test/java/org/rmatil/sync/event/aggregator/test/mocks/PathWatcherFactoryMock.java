package org.rmatil.sync.event.aggregator.test.mocks;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class PathWatcherFactoryMock {

    protected ExecutorService executorService;

    public PathWatcherFactoryMock(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public PathWatcher createRecursiveWatcher(Path rootPath, PathChangeListener changeListener) {
        return new PathWatcherMock(changeListener);
    }

    public PathWatcher createNonRecursiveWatcher(Path rootPath, PathChangeListener changeListener) {
        return new PathWatcherMock(changeListener);
    }
}
