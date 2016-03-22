package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.api.IObjectManager;
import org.rmatil.sync.version.core.model.PathObject;
import org.rmatil.sync.version.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This modifier removes modify events which
 * have the same hash as the last in the object store.
 */
public class IgnoreSameHashModifier implements IModifier {

    private static final Logger logger = LoggerFactory.getLogger(IgnoreSameHashModifier.class);

    protected IObjectManager objectManager;

    public IgnoreSameHashModifier(IObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        Collections.sort(events);
        List<IEvent> modifiedEvents = new ArrayList<>();

        List<IEvent> clone = events.stream().collect(Collectors.toList());
        Iterator<IEvent> cloneItr = clone.iterator();

        for (IEvent event : events) {
            if (event instanceof ModifyEvent) {
                // check whether the last hash in the object store
                // is the same as the hash of the event

                boolean ignoredDueToSameCreateHash = false;
                // look for a create event for the same file
                for (IEvent potentialCreateEvent : events) {
                    if (potentialCreateEvent instanceof CreateEvent) {
                        if (potentialCreateEvent.getPath().toString().equals(event.getPath().toString()) &&
                                potentialCreateEvent.getHash().equals(event.getHash())) {
                            // we found the create event with the same hash
                            logger.info("Ignoring modify event for " + event.getPath() + " since its change (" + event.getHash() + ") is equal to the CreateEvent-Hash");
                            ignoredDueToSameCreateHash = true;
                            break;
                        }
                    }
                }

                if (! ignoredDueToSameCreateHash) {
                    while (cloneItr.hasNext()) {
                        IEvent potentialSameHashModifyEvent = cloneItr.next();

                        if (potentialSameHashModifyEvent instanceof ModifyEvent) {
                            // do not compare the own event
                            if (event == potentialSameHashModifyEvent) {
                                continue;
                            }

                            if (potentialSameHashModifyEvent.getPath().toString().equals(event.getPath().toString()) &&
                                    potentialSameHashModifyEvent.getHash().equals(event.getHash())) {
                                logger.info("Ignoring modify event for " + event.getPath() + " since its change (" + event.getHash() + ") is equal to another ModifyEvent-Hash");
                                // remove element from list to avoid wrong
                                // ignores of happening
                                cloneItr.remove();
                                ignoredDueToSameCreateHash = true;
                                // but do not break here to remove all other matching events too
                            }
                        }
                    }

                    // reset iterator
                    cloneItr = clone.iterator();
                }

                if (! ignoredDueToSameCreateHash) {
                    // try to get information about the element from the ObjectStore
                    try {
                        PathObject pathObject = this.objectManager.getObjectForPath(event.getPath().toString());

                        Version lastVersion = (! pathObject.getVersions().isEmpty()) ? pathObject.getVersions().get(Math.max(0, pathObject.getVersions().size() - 1)) : null;

                        if (null != lastVersion && lastVersion.getHash().equals(event.getHash())) {
                            logger.info("Ignoring modify event for " + event.getPath() + " since its change (" + event.getHash() + ") is already stored in the ObjectStore");
                        } else {
                            // versions are not equal
                            modifiedEvents.add(event);
                        }

                    } catch (InputOutputException e) {
                        logger.error("Failed to check whether the last hash is equal to the hash of the modify event for element " + event.getPath() + ". Message: " + e.getMessage() + ". Not ignoring this event...");
                        modifiedEvents.add(event);
                    }
                }
            } else {
                modifiedEvents.add(event);
            }

        }

        return modifiedEvents;
    }
}
