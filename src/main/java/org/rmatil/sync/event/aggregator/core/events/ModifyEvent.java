package org.rmatil.sync.event.aggregator.core.events;

import java.nio.file.Path;

public class ModifyEvent extends AEvent {

    public static final String EVENT_NAME = "event.modify";

    /**
     * @param path The path which is modified
     * @param name The name of the path which is modified
     * @param hash The hash of the path content
     * @param timestamp The timestamp in milliseconds of this event
     */
    public ModifyEvent(Path path, String name, String hash, long timestamp) {
        super.path = path;
        super.name = name;
        super.hash = hash;
        super.timestamp = timestamp;
    }

    public String getEventName() {
        return EVENT_NAME;
    }
}
