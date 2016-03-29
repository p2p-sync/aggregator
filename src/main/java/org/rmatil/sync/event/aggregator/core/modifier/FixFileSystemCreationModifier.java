package org.rmatil.sync.event.aggregator.core.modifier;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;

import java.util.*;

/**
 * Modifies the list of events in such a way, that if
 * a file is changed from the initial hash (CreateEvent) to an emptyFileHash
 * back again to the initial hash, only the hash of the create event
 * and the modify event passes:
 * <p>
 * <ul>
 * <li>CreateEvent with initial hash != empty Hash</li>
 * <li>ModifyEvent with empty hash</li>
 * <li>ModifyEvent with initial hash</li>
 * </ul>
 * <p>
 * whereas emptyHash = hash("")
 */
public class FixFileSystemCreationModifier implements IModifier {

    @Override
    public List<IEvent> modify(List<IEvent> events) {
        // get events ordered by their timestamp
        Collections.sort(events);

        Map<String, Set<IEvent>> samePathEvents = new HashMap<>();

        for (IEvent event : events) {
            String path = event.getPath().toString();
            if (null == samePathEvents.get(path)) {
                samePathEvents.put(path, new HashSet<>());
            }

            samePathEvents.get(path).add(event);
        }


        Set<IEvent> modifiedEvents = new HashSet<>();
        List<IEvent> eventsToIgnore = new ArrayList<>();

        for (Map.Entry<String, Set<IEvent>> entry : samePathEvents.entrySet()) {
            for (IEvent event : entry.getValue()) {
                if (event instanceof CreateEvent && ! event.getHash().equals(Hash.EMPTY_SHA256_HASH)) {
                    // 1st case
                    // - create event with correct hash
                    // - modify event with empty hash
                    // - modify event with correct hash
                    // -> create event with correct hash

                    modifiedEvents.add(event);

                    IEvent emptyHashModifyEvent = null;
                    IEvent correctHashModifyEvent = null;

                    // now lets find a modify event with an empty hash and a second one with the correct hash
                    for (IEvent modifyEvent : events) {
                        if (modifyEvent instanceof ModifyEvent && modifyEvent.getPath().toString().equals(event.getPath().toString())) {
                            if (modifyEvent.getHash().equals(Hash.EMPTY_SHA256_HASH)) {
                                // found the empty hash modify event
                                emptyHashModifyEvent = modifyEvent;
                            } else if (modifyEvent.getHash().equals(event.getHash())) {
                                // found the correct hash modify event
                                correctHashModifyEvent = modifyEvent;
                            }
                        }
                    }


                    if (null != emptyHashModifyEvent && null == correctHashModifyEvent) {
                        // only a modification to an empty file is detected -> add this event
                        modifiedEvents.add(emptyHashModifyEvent);
                    } else if (null != emptyHashModifyEvent && null != correctHashModifyEvent) {
                        // the empty hash modification is not required
                        modifiedEvents.add(correctHashModifyEvent);
                        eventsToIgnore.add(emptyHashModifyEvent);
                    }
                } else {
                    modifiedEvents.add(event);
                }
            }
        }

        // remove all events which should have been ignored
        modifiedEvents.removeAll(eventsToIgnore);

        List<IEvent> uniqueEvents = new ArrayList<>(modifiedEvents);
        Collections.sort(uniqueEvents);

        return uniqueEvents;
    }
}
