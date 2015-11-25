package org.rmatil.sync.event.aggregator.core.events;

import java.nio.file.Path;

public class ModifyEvent extends AEvent {

    public static final String EVENT_NAME = "event.modify";

    /**
     * @param path The path which is modified
     * @param name The name of the path which is modified
     * @param hash The hash of the path content
     */
    public ModifyEvent(Path path, String name, String hash) {
        this.path = path;
        this.name = name;
        this.hash = hash;
    }

    public String getEventName() {
        return EVENT_NAME;
    }
}
