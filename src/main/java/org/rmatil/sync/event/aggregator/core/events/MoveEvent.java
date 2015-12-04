package org.rmatil.sync.event.aggregator.core.events;

import java.nio.file.Path;

public class MoveEvent extends AEvent {

    public static final String EVENT_NAME = "event.move";

    protected Path newPath;

    /**
     * @param oldPath The path which is modified
     * @param newPath The new path to wich the oldPath was moved
     * @param name The name of the path which is modified
     * @param hash The hash of the path content
     * @param timestamp The timestamp in milliseconds of this event
     */
    public MoveEvent(Path oldPath, Path newPath, String name, String hash, long timestamp) {
        super.path = oldPath;
        this.newPath = newPath;
        super.name = name;
        super.hash = hash;
        super.timestamp = timestamp;
    }

    public MoveEvent(MoveEvent moveEvent) {
        this(moveEvent.getPath(), moveEvent.getNewPath(), moveEvent.getName(), moveEvent.getHash(), moveEvent.getTimestamp());
    }

    public Path getNewPath() {
        return this.newPath;
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }
}
