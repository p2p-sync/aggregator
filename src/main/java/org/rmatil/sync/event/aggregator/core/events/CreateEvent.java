package org.rmatil.sync.event.aggregator.core.events;

import java.nio.file.Path;

/**
 * The event fired when a new file is created
 */
public class CreateEvent extends AEvent {

    public static final String EVENT_NAME = "event.create";

    /**
     * @param path The path which is created
     * @param name The name of the path which is created
     * @param hash The hash of the path content
     * @param timestamp The timestamp in milliseconds of this event
     */
    public CreateEvent(Path path, String name, String hash, long timestamp) {
        super.path = path;
        super.name = name;
        super.hash = hash;
        super.timestamp = timestamp;
    }

    public CreateEvent(CreateEvent createEvent) {
        this(createEvent.getPath(), createEvent.getName(), createEvent.getHash(), createEvent.getTimestamp());
    }

    public String getEventName() {
        return EVENT_NAME;
    }
}
