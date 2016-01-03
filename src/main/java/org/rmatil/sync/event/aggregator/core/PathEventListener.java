package org.rmatil.sync.event.aggregator.core;

import name.mitterdorfer.perlock.PathChangeListener;
import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.event.aggregator.api.IEventListener;
import org.rmatil.sync.event.aggregator.config.Config;
import org.rmatil.sync.event.aggregator.core.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * Listens for path element changes.
 * Incoming events are being added and hold until
 * one fetches and clears them.
 */
public class PathEventListener implements PathChangeListener, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PathEventListener.class);

    /**
     * The list of events
     */
    protected List<IEvent> eventBag;

    /**
     * List of event listeners which are notified on changes
     */
    protected List<IEventListener> eventListeners;

    public PathEventListener() {
        this.eventBag = new ArrayList<>();
        this.eventListeners = new ArrayList<>();
    }

    public void onPathCreated(Path path) {
        logger.trace("Got notified about the creation of '" + path + "'");

        String hash = null;
        try {
            if (path.toFile().isFile()) {
                hash = Hash.hash(Config.getDefaultConfiguration().getHashingAlgorithm(), path.toFile());
            }
        } catch (IOException e) {
            logger.error("Could not hash on path creation: " + e.getMessage());
        }

        this.eventBag.add(new CreateEvent(path, path.toFile().getName(), hash, System.currentTimeMillis()));
    }

    public void onPathModified(Path path) {
        logger.trace("Got notified about the modifying of '" + path + "'");

        String hash = null;
        try {
            if (path.toFile().isFile()) {
                hash = Hash.hash(Config.getDefaultConfiguration().getHashingAlgorithm(), path.toFile());
            }
        } catch (IOException e) {
            logger.error("Could not hash on path modification: " + e.getMessage());
        }

        this.eventBag.add(new ModifyEvent(path, path.toFile().getName(), hash, System.currentTimeMillis()));
    }

    public void onPathDeleted(Path path) {
        logger.trace("Got notified about the deletion of '" + path + "'");

        this.eventBag.add(new DeleteEvent(path, path.toFile().getName(), null, System.currentTimeMillis()));
    }

    @Override
    public void run() {
        try {
            if (this.eventBag.size() < 1) {
                return;
            }

            List<IEvent> aggregatedEvents = new ArrayList<>();
            for (IEvent event : this.eventBag) {
                switch (event.getEventName()) {
                    case CreateEvent.EVENT_NAME:
                        aggregatedEvents.add(new CreateEvent((CreateEvent) event));
                        break;
                    case ModifyEvent.EVENT_NAME:
                        aggregatedEvents.add(new ModifyEvent((ModifyEvent) event));
                        break;
                    case DeleteEvent.EVENT_NAME:
                        aggregatedEvents.add(new DeleteEvent((DeleteEvent) event));
                        break;
                    case MoveEvent.EVENT_NAME:
                        aggregatedEvents.add(new MoveEvent((MoveEvent) event));
                }
            }

            this.eventBag.clear();

            Collections.sort(aggregatedEvents);

            // notify all listeners about our changes
            for (IEventListener listener : this.eventListeners) {
                listener.onChange(aggregatedEvents);
            }
        } catch (Exception e) {
            logger.error("Thread error. Message: " + e.getMessage(), e);
        }
    }

    /**
     * Adds the given event listener
     *
     * @param eventListener The event listener which should be notified about changes
     */
    public void addListener(IEventListener eventListener) {
        this.eventListeners.add(eventListener);
    }

    /**
     * Removes the given event listener
     *
     * @param eventListener The listener to remove
     */
    public void removeListener(IEventListener eventListener) {
        this.eventListeners.remove(eventListener);
    }

    /**
     * Returns the list of currently registered event listeners
     *
     * @return The list of event listeners
     */
    public List<IEventListener> getListener() {
        return this.eventListeners;
    }
}
