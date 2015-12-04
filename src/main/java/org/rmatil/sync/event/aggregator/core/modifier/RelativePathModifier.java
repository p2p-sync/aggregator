package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.event.aggregator.core.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modifies each path in a way that local filesystem
 * paths are resolved relative to the root of the
 * synchronized folder
 */
public class RelativePathModifier implements IModifier {

    final static Logger logger = LoggerFactory.getLogger(RelativePathModifier.class);

    protected Path rootDir;

    /**
     * @param rootDir The root dir to which the paths should be resolved (i.e. the root of the synchronized folder)
     */
    public RelativePathModifier(Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        Collections.sort(events);
        List<IEvent> modifiedEvents = new ArrayList<>();

        for (IEvent event : events) {
            Path relativePath = rootDir.relativize(event.getPath());

            logger.trace("Relativizing path from " + event.getPath().toString() + " to " + relativePath.toString());

            IEvent e = null;
            switch (event.getEventName()) {
                case CreateEvent.EVENT_NAME:
                    e = new CreateEvent(relativePath, event.getName(), event.getHash(), event.getTimestamp());
                    break;
                case ModifyEvent.EVENT_NAME:
                    e = new ModifyEvent(relativePath, event.getName(), event.getHash(), event.getTimestamp());
                    break;
                case DeleteEvent.EVENT_NAME:
                    e = new DeleteEvent(relativePath, event.getName(), event.getHash(), event.getTimestamp());
                    break;
                case MoveEvent.EVENT_NAME:
                    MoveEvent moveEvent = (MoveEvent) event;
                    Path newRelativePath = rootDir.relativize(moveEvent.getNewPath());
                    e = new MoveEvent(relativePath, newRelativePath, moveEvent.getName(), moveEvent.getHash(), moveEvent.getTimestamp());
            }

            modifiedEvents.add(e);
        }

        Collections.sort(modifiedEvents);
        return modifiedEvents;
    }
}
