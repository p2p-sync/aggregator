package org.rmatil.sync.event.aggregator.test.mocks;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;

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
        Path testFile = rootDir.resolve("file1.txt");
        try {
            testFile = Files.createFile(testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.changeListener.onPathCreated(testFile);
    }

    public void mockFileModify(Path rootDir) {
        Path testFilePath = rootDir.resolve("file1.txt");

        try {
            String s = "Hello World! ";
            byte data[] = s.getBytes();
            OutputStream out = new BufferedOutputStream(Files.newOutputStream(testFilePath, CREATE, APPEND));
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }

        this.changeListener.onPathModified(testFilePath);
    }

    public void mockFileDelete(Path rootDir) {
        Path testFilePath = rootDir.resolve("file1.txt");

        File file = testFilePath.toFile();

        if (file.exists()) {
            file.delete();
        }

        this.changeListener.onPathDeleted(testFilePath);
    }
}
