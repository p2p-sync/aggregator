package org.rmatil.sync.event.aggregator.core.aggregator;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;

import java.util.*;

/**
 * Aggregates the combination of a CreateEvent having
 * an empty hash with a modify event not having an empty hash
 * whereas emptyHash = hash("")
 */
public class EmptyHashAggregator implements IAggregator {

    @Override
    public List<IEvent> aggregate(List<IEvent> events) {
        Collections.sort(events);

        Set<IEvent> aggregatedEvents = new HashSet<>();
        List<IEvent> eventsToIgnore = new ArrayList<>();

        for (IEvent event : events) {
            if (event instanceof CreateEvent && event.getHash().equals(Hash.EMPTY_SHA256_HASH)) {
                // find a modify event for this create event with a different hash
                for (IEvent modifyEvent : events) {
                    if (modifyEvent instanceof ModifyEvent &&
                            event.getPath().toString().equals(modifyEvent.getPath().toString()) &&
                            ! modifyEvent.getHash().equals(Hash.EMPTY_SHA256_HASH)) {
                        // use the hash of the corresponding modify event
                        aggregatedEvents.add(
                                new CreateEvent(
                                        event.getPath(),
                                        event.getName(),
                                        modifyEvent.getHash(),
                                        event.getTimestamp()
                                )
                        );

                        eventsToIgnore.add(event);
                        eventsToIgnore.add(modifyEvent);

                        // avoid using different modify events for the same create event multiple times
                        break;
                    }
                }
            } else {
                aggregatedEvents.add(event);
            }
        }

        aggregatedEvents.removeAll(eventsToIgnore);

        List<IEvent> uniqueEvents = new ArrayList<>(aggregatedEvents);
        Collections.sort(uniqueEvents);

        return uniqueEvents;
    }
}
