package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Modifies the list of events in such a way, that notifications
 * about a change in a directory do not get propagated further.
 */
public class IgnoreDirectoryModifier implements IModifier {

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        List<IEvent> modifiedEvents = new ArrayList<>();

        for (IEvent event : events) {
            if (! (event instanceof ModifyEvent && event.getPath().toFile().isDirectory())) {
                modifiedEvents.add(event);
            }
        }

        return modifiedEvents;
    }
}
