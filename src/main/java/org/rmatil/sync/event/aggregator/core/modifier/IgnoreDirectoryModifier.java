package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modifies the list of events in such a way, that notifications
 * about a change in a directory do not get propagated further.
 */
public class IgnoreDirectoryModifier implements IModifier {

    final static Logger logger = LoggerFactory.getLogger(IgnoreDirectoryModifier.class);

    protected Path rootDir;

    public IgnoreDirectoryModifier(Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        Collections.sort(events);
        List<IEvent> modifiedEvents = new ArrayList<>();

        for (IEvent event : events) {
            if (! (event instanceof ModifyEvent && this.rootDir.resolve(event.getPath()).toFile().isDirectory())) {
                modifiedEvents.add(event);
            } else {
                logger.trace("Ignoring modify event for directory " + event.getPath());
            }
        }

        Collections.sort(modifiedEvents);
        return modifiedEvents;
    }
}
