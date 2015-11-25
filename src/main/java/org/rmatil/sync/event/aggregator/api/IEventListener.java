package org.rmatil.sync.event.aggregator.api;

import org.rmatil.sync.event.aggregator.core.events.IEvent;

import java.util.List;

/**
 * The interface to use for an event listener
 * which may be registered with the event aggregator
 *
 * @see IEventAggregator The aggregator to which event listener can be registered
 */
public interface IEventListener {

    /**
     * Called, if a new path element is created
     *
     * @param changes The aggregated changes in the filesystem
     */
    void onChange(List<IEvent> changes);
}
