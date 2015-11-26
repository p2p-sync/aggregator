package org.rmatil.sync.event.aggregator.test.mocks;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * This class is used to allow testing file notifications
 * on continuous integration systems which do not always
 * allow filesystem event notifications.
 *
 * Since the file must finally exist on disk to create
 * a hash of it, we still have to create on temporarily,
 * but can trigger the filesystem notifications by our own.
 */
public class PathWatcherMock implements PathWatcher {

    protected boolean isRunning;

    protected PathChangeListener changeListener;

    public PathWatcherMock(PathChangeListener changeListener) {
        this.isRunning = false;
        this.changeListener = changeListener;
    }

    public void start()
            throws IOException {
        this.isRunning = true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void stop() {
        this.isRunning = false;
    }

    public void mockFileCreation(Path rootDir) {
        Path testFile = FileUtil.createTestFile(rootDir);
        this.changeListener.onPathCreated(testFile);
    }

    public void mockFileModify(Path rootDir) {
        Path testFile = FileUtil.modifyTestFile(rootDir);
        this.changeListener.onPathModified(testFile);
    }

    public void mockFileDelete(Path rootDir) {
        Path testFile = FileUtil.deleteTestFile(rootDir);
        this.changeListener.onPathDeleted(testFile);
    }
}
