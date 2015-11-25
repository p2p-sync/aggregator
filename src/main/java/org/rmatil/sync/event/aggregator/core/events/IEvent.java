package org.rmatil.sync.event.aggregator.core.events;

import java.nio.file.Path;

/**
 * An interface specifying an event which is
 * fired when the file tree has changed
 */
public interface IEvent {

    /**
     * The path element which has changed
     *
     * @return The element for which the event was triggered
     */
    Path getPath();

    /**
     * The name of the path element which changed
     *
     * @return The name
     */
    String getName();

    /**
     * The hash of the path element
     *
     * @return The hash of the path element
     */
    String getHash();

    /**
     * Returns the name of the event
     *
     * @return The name of the event
     */
    String getEventName();
}
