package org.rmatil.sync.event.aggregator.core.events;

import java.nio.file.Path;

/**
 * The common base class used for eventBag
 */
public abstract class AEvent implements IEvent {

    /**
     * The path element which was added/changed/removed
     */
    protected Path   path;

    /**
     * The name of the path element
     */
    protected String name;

    /**
     * The hash of the path element
     */
    protected String hash;

    public Path getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public String getHash() {
        return this.hash;
    }

    public abstract String getEventName();
}
