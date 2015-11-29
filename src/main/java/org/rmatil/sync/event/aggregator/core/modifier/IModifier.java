package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.event.aggregator.core.events.IEvent;

import java.util.List;

/**
 * An implementation of this interface may
 * modify the given list of events to its needs.
 */
public interface IModifier {

    /**
     * Modify the given list of events
     *
     * @param events The list of events to modify
     *
     * @return The modified list of events
     */
    List<IEvent> modify(List<IEvent> events);

}
