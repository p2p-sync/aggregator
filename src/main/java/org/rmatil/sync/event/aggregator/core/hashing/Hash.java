package org.rmatil.sync.event.aggregator.core.hashing;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.rmatil.sync.event.aggregator.config.HashingAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Hashing utilities for files
 */
public class Hash {

    /**
     * Hashes the given file with the given hashing algorithm
     *
     * @param hashingAlgorithm The hashing algorithm to use
     * @param file             The file to hash
     *
     * @return The hash
     *
     * @throws IOException           If the hashing fails
     * @throws FileNotFoundException If the given file does not exist
     */
    public static String hash(HashingAlgorithm hashingAlgorithm, File file)
            throws IOException {

        if (! file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " (No such file or directory)");
        }

        if (file.isDirectory()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " (Is a directory)");
        }

        HashCode hc = null;

        switch (hashingAlgorithm) {
            case SHA_1:
                hc = Files.hash(file, Hashing.sha1());
                break;
            case SHA_256:
                hc = Files.hash(file, Hashing.sha256());
                break;
            case SHA_512:
                hc = Files.hash(file, Hashing.sha512());
                break;
        }

        // TODO: how is this applied to directories

        return hc.toString();
    }
}
