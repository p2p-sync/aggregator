package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.event.aggregator.core.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Removes all events from the given list
 * which are in the specified list of ignored paths
 */
public class IgnorePathsModifier implements IModifier {

    final static Logger logger = LoggerFactory.getLogger(IgnorePathsModifier.class);

    protected List<Path>   ignoredPaths;
    protected List<String> ignoredPatterns;

    /**
     * @param ignoredPaths A list of paths (relative to the root of the sync folder) which are ignored
     */
    public IgnorePathsModifier(List<Path> ignoredPaths) {
        this(ignoredPaths, new ArrayList<>());
    }

    /**
     * @param ignoredPaths    A list of paths (relative to the root of the sync folder) which are ignored
     * @param ignoredPatterns A list of regex patterns
     */
    public IgnorePathsModifier(List<Path> ignoredPaths, List<String> ignoredPatterns) {
        this.ignoredPaths = ignoredPaths;
        this.ignoredPatterns = ignoredPatterns;
    }

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        Collections.sort(events);
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
                // check if the path is matching any ignored pattern
                for (String pattern : this.ignoredPatterns) {
                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
                    // resolve paths relative to the root dir
                    if (event instanceof CreateEvent ||
                            event instanceof ModifyEvent ||
                            event instanceof DeleteEvent) {
                        if (matcher.matches(event.getPath())) {
                            isChildOfIgnored = true;
                            logger.trace("Ignoring file " + event.getPath().toString() + " since it matches the registered glob pattern " + pattern);
                            break;
                        }
                    } else if (event instanceof MoveEvent) {
                        if (matcher.matches(event.getPath()) ||
                                matcher.matches(((MoveEvent) event).getNewPath())) {
                            isChildOfIgnored = true;
                            logger.trace("Ignoring the move of file " + event.getPath().toString() + " to " + ((MoveEvent) event).getNewPath() + " since it matches the registered glob pattern " + pattern);
                            break;
                        }
                    }
                }
            }

            if (! isChildOfIgnored) {
                modifiedEvents.add(event);
            }
        }

        Collections.sort(modifiedEvents);
        return modifiedEvents;
    }
}
