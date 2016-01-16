package org.rmatil.sync.event.aggregator.core.aggregator;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.commons.list.Lists;
import org.rmatil.sync.event.aggregator.config.Config;
import org.rmatil.sync.event.aggregator.core.events.*;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.api.IObjectManager;
import org.rmatil.sync.version.core.model.PathObject;
import org.rmatil.sync.version.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

/**
 * Aggregates a delete and add event of the same hash to a MoveEvent.
 * <p>
 * Since the filesystem notifies at the moment of the deletion, no
 * hash of the deleted file can be created anymore. Therefore, this
 * aggregator contacts the object manager for the hash of the last
 * stored version of the delete file, and if they match, creates the move event.
 *
 * @see IObjectManager The object manager
 */
public class HistoryMoveAggregator implements IAggregator {

	/**
	 * The tolerance between two events up to which they
	 * are aggregated
	 */
	public static final int EVENT_TIMESTAMP_TOLERANCE = 10;

    final static Logger logger = LoggerFactory.getLogger(HistoryMoveAggregator.class);

    /**
     * An object manager to access stored versions
     * of particular files
     */
    protected IObjectManager objectManager;

    public HistoryMoveAggregator(IObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    /**
     * Aggregates events based on the hash of a certain
     * path element.
     * <p>
     * <i>Note</i>: Filenames are not considered, since it is possible,
     * that two files are renamed to the name of the other in the event list.
     * This would loose track of the correct renaming (i.e. moving) events.
     * Therefore, we only consider events which are applied to
     * files with the same hash
     *
     * @param events The events to aggregate
     *
     * @return The list of aggregated events
     */
    public List<IEvent> aggregate(List<IEvent> events) {
        Collections.sort(events);

        Map<String, List<IEvent>> sameHashFileEvents = new HashMap<>();

        // add all events with the same file hash to the same place
        for (IEvent event : events) {
            // enrich delete event with last stored hash of history to force a move event
            // when an add event with the same hash occurs
            if (event instanceof DeleteEvent && null == event.getHash()) {
                logger.trace("Looking for versions of deleted path " + event.getPath().toString());
                try {
                    PathObject object = this.objectManager.getObject(Hash.hash(Config.DEFAULT.getHashingAlgorithm(), event.getPath().toString()));
                    if (null != object && object.getVersions().size() > 0) {
                        Version lastVersion = object.getVersions().get(object.getVersions().size() - 1);
                        logger.trace("Updating delete event with hash for path " + event.getPath().toString() + ". Hash is " + lastVersion.getHash());
                        event = new DeleteEvent(
                                event.getPath(),
                                event.getName(),
                                lastVersion.getHash(),
                                event.getTimestamp()
                        );
                    }
                } catch (InputOutputException e) {
                    logger.error("Failed to get versions of deleted path. Message: " + e.getMessage());
                }
            }

            if (null == event.getHash()) {
                if (null == sameHashFileEvents.get("__empty_key")) {
                    sameHashFileEvents.put("__empty_key", new ArrayList<>());
                }

                sameHashFileEvents.get("__empty_key").add(event);
            } else {
                if (null == sameHashFileEvents.get(event.getHash())) {
                    sameHashFileEvents.put(event.getHash(), new ArrayList<>());
                }

                sameHashFileEvents.get(event.getHash()).add(event);
            }
        }

        // the final aggregated events which we will return
        List<IEvent> aggregatedEvents = new ArrayList<>();

        for (Map.Entry<String, List<IEvent>> entry : sameHashFileEvents.entrySet()) {
            if (entry.getValue().size() < 2) {
                // only one event for the same hash
                // -> no event aggregation
                aggregatedEvents.add(entry.getValue().get(0));
            } else {
                // add all events which we do not handle in this aggregator
                // These events should not occur in between the deletion & creation event
                aggregatedEvents.addAll(Lists.getInstances(entry.getValue(), ModifyEvent.class));
                aggregatedEvents.addAll(Lists.getInstances(entry.getValue(), MoveEvent.class));

                // -> delete & add => move
                List<IEvent> deleteHits = Lists.getInstances(entry.getValue(), DeleteEvent.class);
                List<IEvent> createHits = Lists.getInstances(entry.getValue(), CreateEvent.class);

                if (! deleteHits.isEmpty() && ! createHits.isEmpty()) {
                    // if the same file is multiple times deleted, we try
                    // to assign a move event for the same filename

                    if (deleteHits.size() > 1 && deleteHits.size() == createHits.size()) {
                        logger.info("Trying to arbitrary move event");

                        for (IEvent deleteEvent : deleteHits) {
                            Path fileName = deleteEvent.getPath().getFileName();

                            // look in the create events for the corresponding file name
                            for (Iterator<IEvent> iterator = createHits.iterator(); iterator.hasNext(); ) {
                                IEvent createEvent = iterator.next();
                                if (createEvent.getPath().getFileName().equals(fileName) && deleteEvent.getTimestamp() <= createEvent.getTimestamp()) {
                                    // we found a hit with the same filename
                                    MoveEvent moveEvent = new MoveEvent(deleteEvent.getPath(), createEvent.getPath(), createEvent.getName(), createEvent.getHash(), createEvent.getTimestamp());
                                    aggregatedEvents.add(moveEvent);
                                    logger.trace("Creating moveEvent from " + deleteEvent.getPath() + " to " + createEvent.getPath());

                                    // finally remove the used event to avoid creating a move to the same file again
                                    iterator.remove();

                                    break;
                                }
                            }
                        }

                        continue;
                    }

                    if (deleteHits.size() > 1) {
                        logger.info("Delete hits for file with the same hash is bigger than one. Skipping these events...");
                        aggregatedEvents.addAll(deleteHits);
                        aggregatedEvents.addAll(createHits); // add these too, otherwise they get lost
                        continue;
                    }

                    // if multiple files with the same hash are created, we can not
                    // assign the correct delete event to it
                    if (createHits.size() > 1) {
                        logger.info("Create hits for file with the same hash is bigger than one. Skipping these events...");
                        aggregatedEvents.addAll(createHits);
                        aggregatedEvents.addAll(deleteHits); // add these too, otherwise they get lost
                        continue;
                    }

                    IEvent deleteHit = deleteHits.get(0);
                    IEvent createHit = createHits.get(0);

                    // check timestamps: which was first?
                    if (deleteHit.getTimestamp() <= createHit.getTimestamp() || Math.abs(deleteHit.getTimestamp() - createHit.getTimestamp()) <= EVENT_TIMESTAMP_TOLERANCE) {
                        MoveEvent moveEvent = new MoveEvent(deleteHit.getPath(), createHit.getPath(), createHit.getName(), createHit.getHash(), createHit.getTimestamp());
                        aggregatedEvents.add(moveEvent);
                        logger.trace("Creating moveEvent from " + deleteHit.getPath() + " to " + createHit.getPath());
                    } else {
                        // we just add both events unchanged to the results
                        aggregatedEvents.add(deleteHit);
                        aggregatedEvents.add(createHit);
                    }

                } else {
                    // finally add all events if no match could have been found
                    aggregatedEvents.addAll(deleteHits);
                    aggregatedEvents.addAll(createHits);
                }
            }
        }

        Collections.sort(aggregatedEvents);

        return aggregatedEvents;
    }

}
