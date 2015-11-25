package org.rmatil.sync.event.aggregator.test.core;

import java.io.File;

public class FileUtil {

    public static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();

            if (null != contents) {
                for (File child : contents) {
                    delete(child);
                }
            }

            file.delete();

            return true;
        } else {
            return file.delete();
        }
    }
}
