package org.rmatil.sync.event.aggregator.core.events;

import java.nio.file.Path;

/**
 * The common base class used for eventBag
 */
public abstract class AEvent implements IEvent {

    /**
     * The path element which was added/changed/removed
     */
    protected Path path;

    /**
     * The name of the path element
     */
    protected String name;

    /**
     * The hash of the path element
     */
    protected String hash;

    /**
     * A timestamp in milliseconds indicating the time
     * in which this event occurred
     */
    protected long timestamp;

    public Path getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public String getHash() {
        return this.hash;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int compareTo(IEvent anotherEvent) {
        if (this.timestamp == anotherEvent.getTimestamp()) {
            return 0;
        }

        // -1 if this object is less than anotherEvent
        return this.timestamp < anotherEvent.getTimestamp() ? - 1 : 1;
    }

    public abstract String getEventName();
}
