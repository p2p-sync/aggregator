package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.event.aggregator.core.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Removes all events from the given list
 * which are in the specified list of ignored paths
 */
public class IgnorePathsModifier implements IModifier {

    final static Logger logger = LoggerFactory.getLogger(IgnorePathsModifier.class);

    protected List<Path> ignoredPaths;

    /**
     * @param ignoredPaths A list of paths (relative to the root of the sync folder) which are ignored
     */
    public IgnorePathsModifier(List<Path> ignoredPaths) {
        this.ignoredPaths = ignoredPaths;
    }

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        List<IEvent> modifiedEvents = new ArrayList<>();

        for (IEvent event : events) {
            boolean isChildOfIgnored = false;

            for (Path path : ignoredPaths) {
                if (event instanceof CreateEvent ||
                        event instanceof ModifyEvent ||
                        event instanceof DeleteEvent) {
                    if (event.getPath().startsWith(path)) {
                        isChildOfIgnored = true;
                        logger.trace("Ignoring file " + event.getPath().toString() + " since it is stored in the ignored path " + path.toString());
                        break;
                    }
                } else if (event instanceof MoveEvent) {
                    if (event.getPath().startsWith(path) ||
                            ((MoveEvent) event).getNewPath().startsWith(path)) {
                        isChildOfIgnored = true;
                        logger.trace("Ignoring the move of file " + event.getPath().toString() + " to " + ((MoveEvent) event).getNewPath() + " since it is stored in the ignored path " + path.toString());
                        break;
                    }
                }
            }

            if (! isChildOfIgnored) {
                modifiedEvents.add(event);
            }
        }

        return modifiedEvents;
    }
}
