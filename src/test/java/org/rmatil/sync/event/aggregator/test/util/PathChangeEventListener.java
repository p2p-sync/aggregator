package org.rmatil.sync.event.aggregator.test.util;

import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.api.IEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The event listener which is used
 * to test the event aggregator.
 *
 * <i>Note</i>: Events are always overwritten on a notification
 */
public class PathChangeEventListener implements IEventListener {

    final static Logger logger = LoggerFactory.getLogger(PathChangeEventListener.class);

    protected List<IEvent> events;

    public PathChangeEventListener() {
        this.events = new ArrayList<IEvent>();
    }

    public void onChange(List<IEvent> changes) {
        this.events = changes;
    }

    public List<IEvent> getEvents() {
        return this.events;
    }
}
