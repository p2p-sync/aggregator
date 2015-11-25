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
     */
    public CreateEvent(Path path, String name, String hash) {
        super.path = path;
        super.name = name;
        super.hash = hash;
    }

    public String getEventName() {
        return EVENT_NAME;
    }
}
