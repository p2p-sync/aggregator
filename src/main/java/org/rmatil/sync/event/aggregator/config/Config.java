package org.rmatil.sync.event.aggregator.config;

/**
 * The configuration for this component
 */
public enum Config {

    /**
     * The default configuration. It
     * uses sha256 to generate hashes from files
     */
    DEFAULT(HashingAlgorithm.SHA_256);

    /**
     * The hashing algorithm
     */
    private HashingAlgorithm hashingAlgorithm;

    /**
     * @param hashingAlgorithm The hashing algorithm to use
     */
    Config(HashingAlgorithm hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    /**
     * Returns the default configuration
     *
     * @return The default configuration
     */
    public static Config getDefaultConfiguration() {
        return DEFAULT;
    }

    /**
     * Returns the used hashing algorithm
     *
     * @return The used hashing algorithm
     */
    public HashingAlgorithm getHashingAlgorithm() {
        return hashingAlgorithm;
    }
}
