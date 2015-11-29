package org.rmatil.sync.event.aggregator.api;

import org.rmatil.sync.event.aggregator.core.aggregator.IAggregator;
import org.rmatil.sync.event.aggregator.core.modifier.IModifier;

import java.io.IOException;

/**
 * Enables to add multiple event listeners to subscribe
 * to basic file system eventBag.
 * An event aggregator aggregates multiple filesystem event
 * to a single one.
 * <p>
 * Additionally, it allows to detect move or rename
 * eventBag which are composited by a delete and an
 * add event.
 *
 * @see IEventListener For the interface of the listener
 */
public interface IEventAggregator {

    /**
     * Add an event listener
     *
     * @param eventListener The event listener which should be called on various eventBag
     */
    void addListener(IEventListener eventListener);

    /**
     * Remove the given event listener
     *
     * @param eventListener The event listener which should be removed
     */
    void removeListener(IEventListener eventListener);

    /**
     * Add an aggregator to the event listener
     *
     * @param aggregator The aggregator to add
     */
    void addAggregator(IAggregator aggregator);

    /**
     * Removes the given aggregator from the list
     *
     * @param aggregator The aggregator to remove
     */
    void removeAggregator(IAggregator aggregator);

    /**
     * Adds the given modifier
     *
     * @param modifier The modifier to add
     */
    void addModifier(IModifier modifier);

    /**
     * Removes the given modifier
     *
     * @param modifier The modifier to remove
     */
    void removeModifier(IModifier modifier);

    /**
     * Set the interval in which eventBag are aggregated
     *
     * @param milliSeconds The interval in milliseconds
     */
    void setAggregationInterval(long milliSeconds);

    /**
     * Returns the interval in which eventBag are aggregated
     *
     * @return The interval in milliseconds
     */
    long getAggregationInterval();

    /**
     * Start the event aggregator.
     * <p>
     * <i>Note</i>: No eventBag are propagated to the event listener
     * if not started
     *
     * @throws IOException If the event aggregator could not be started
     */
    void start()
            throws IOException;

    /**
     * Stop the event aggregator.
     * <p>
     * <i>Note</i>: No eventBag are propagated to the registered event listener
     * anymore after this call
     */
    void stop();
}
