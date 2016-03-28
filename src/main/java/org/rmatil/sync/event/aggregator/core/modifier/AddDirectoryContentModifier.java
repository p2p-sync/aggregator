package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.event.aggregator.config.Config;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.DeleteEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.api.IObjectStore;
import org.rmatil.sync.version.core.model.PathObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AddDirectoryContentModifier implements IModifier {

    final static Logger logger = LoggerFactory.getLogger(AddDirectoryContentModifier.class);

    protected IObjectStore objectStore;

    protected Path rootDir;

    public AddDirectoryContentModifier(Path rootDir, IObjectStore objectStore) {
        this.objectStore = objectStore;
        this.rootDir = rootDir;
    }

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        Collections.sort(events);
        List<IEvent> modifiedEvents = new ArrayList<>();

        for (IEvent event : events) {
            if (event instanceof DeleteEvent) {
                modifiedEvents.add(event);
                try {
                    // create for each child of the path a delete event
                    List<PathObject> deletedChildren = this.objectStore.getObjectManager().getChildren(event.getPath().toString());
                    modifiedEvents.addAll(
                            deletedChildren
                                    .stream()
                                    .map(entry -> new DeleteEvent(Paths.get(entry.getAbsolutePath()), entry.getName(), null, event.getTimestamp() - Math.abs(this.getAdditionalMilliseconds(event.getPath().toString(), entry.getAbsolutePath()))))
                                    .collect(Collectors.toList()));
                } catch (InputOutputException e) {
                    logger.error("Failed to get deleted objects from directory " + event.getPath().toString() + ". Message: " + e.getMessage());
                }
            } else if (event instanceof CreateEvent) {
                modifiedEvents.add(event);
                // create createEvent for each file contained in the dir
                if (this.rootDir.resolve(event.getPath()).toFile().isDirectory()) {
                    modifiedEvents.addAll(createCreateEventForChildren(events, this.rootDir.resolve(event.getPath()).toFile(), event.getTimestamp()));
                }
            } else {
                modifiedEvents.add(event);
            }
        }

        Collections.sort(modifiedEvents);
        return modifiedEvents;
    }

    protected List<IEvent> createCreateEventForChildren(final List<IEvent> origEvents, File parentDirectory, long timestamp) {
        List<IEvent> events = new ArrayList<>();
        if (null == parentDirectory) {
            return events;
        }

        // traverse each file
        for (File file : parentDirectory.listFiles()) {

            boolean hasCreateEvent = false;
            for (IEvent entry : origEvents) {
                if (entry instanceof CreateEvent && entry.getPath().toString().equals(this.rootDir.relativize(file.toPath()).toString())) {
                    hasCreateEvent = true;
                }
            }

            // only build a create event if none exists yet
            if (! hasCreateEvent) {
                try {
                    logger.trace("Create createEvent for subfile " + file.toPath().toString() + " in parentDir " + parentDirectory.toString());
                    // add additional n milliseconds such that the child contents are processed later than the parent ones
                    events.add(new CreateEvent(this.rootDir.relativize(file.toPath()), file.getName(), Hash.hash(Config.DEFAULT.getHashingAlgorithm(), file), timestamp + Math.abs(this.getAdditionalMilliseconds(parentDirectory.toString(), file.toString()))));
                } catch (IOException e) {
                    logger.error("Could not hash contents of file " + file.toPath().toString());
                }
            }

            if (file.isDirectory()) {
                // add additional n milliseconds such that the child contents are processed later than the parent ones
                events.addAll(this.createCreateEventForChildren(origEvents, file, timestamp + Math.abs(this.getAdditionalMilliseconds(parentDirectory.toString(), file.toString()))));
            }
        }

        return events;
    }

    /**
     * Returns the number of additional milliseconds to add for a child path relative to the event path
     * so that while traversing directories, their child entries are processed later than the parent.
     * All elements on the same level get the same number of additional milliseconds
     *
     * @param eventPath The path of the event
     * @param childPath The path to the child
     *
     * @return The number of additional milliseconds to add
     */
    protected int getAdditionalMilliseconds(String eventPath, String childPath) {
        int nrOfSlashes = eventPath.length() - eventPath.replace("/", "").length();
        int nrOfSlashesInChild = childPath.length() - childPath.replace("/", "").length();

        // we add for each level one millisecond
        return Math.abs(nrOfSlashesInChild - nrOfSlashes);
    }
}
