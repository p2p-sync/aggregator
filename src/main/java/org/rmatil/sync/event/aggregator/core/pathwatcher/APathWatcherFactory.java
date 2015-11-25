package org.rmatil.sync.event.aggregator.core.pathwatcher;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public abstract class APathWatcherFactory implements IPathWatcherFactory {

    public abstract PathWatcher createRecursiveWatcher(ExecutorService executorService, Path rootPath, PathChangeListener changeListener);

    public abstract PathWatcher createNonRecursiveWatcher(ExecutorService executorService, Path rootPath, PathChangeListener changeListener);
}
