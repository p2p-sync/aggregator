package org.rmatil.sync.event.aggregator.core.aggregator;

import org.rmatil.sync.event.aggregator.core.events.IEvent;

import java.util.List;

/**
 * An interface for aggregators which eventually
 * aggregate some events in the given list to one or
 * multiple other events.
 *
 * <i>Note</i>: The aggregator is required to return a sorted list
 * containing all events which he did not aggregate.
 */
public interface IAggregator {

    /**
     * Aggregates events of the given list
     * into one, if possible. If not possible,
     * the aggregator is required to return all
     * events which he received. Furthermore, he is
     * required to return a sorted collection again
     *
     * @param events The events to aggregate
     *
     * @return The modified, sorted list of events
     */
    List<IEvent> aggregate(List<IEvent> events);

}
