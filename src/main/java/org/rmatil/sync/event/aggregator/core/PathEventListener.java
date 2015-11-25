package org.rmatil.sync.event.aggregator.core;

import name.mitterdorfer.perlock.PathChangeListener;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.config.Config;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.DeleteEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.rmatil.sync.event.aggregator.core.hashing.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Listens for path element changes.
 * Incoming eventBag are being added and hold until
 * one fetches and clears them.
 */
public class PathEventListener implements PathChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(PathEventListener.class);

    /**
     * The list of events
     */
    protected List<IEvent> eventBag;

    public PathEventListener() {
        this.eventBag = new ArrayList<IEvent>();
    }

    public void onPathCreated(Path path) {
        logger.trace("Got notified about the creation of '" + path + "'");

        String hash = null;
        try {
            hash = Hash.hash(Config.getDefaultConfiguration().getHashingAlgorithm(), path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eventBag.add(new CreateEvent(path, path.toFile().getName(), hash));
    }

    public void onPathModified(Path path) {
        logger.debug("Got notified about the modifying of '" + path + "'");

        String hash = null;
        try {
            hash = Hash.hash(Config.getDefaultConfiguration().getHashingAlgorithm(), path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eventBag.add(new ModifyEvent(path, path.toFile().getName(), hash));
    }

    public void onPathDeleted(Path path) {
        logger.debug("Got notified about the deletion of '" + path + "'");

        String hash = null;
        try {
            hash = Hash.hash(Config.getDefaultConfiguration().getHashingAlgorithm(), path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eventBag.add(new DeleteEvent(path, path.toFile().getName(), hash));
    }

    /**
     * Returns the aggregated event
     *
     * @return The aggregated eventBag
     */
    public List<IEvent> getEventBag() {
        return eventBag;
    }

    /**
     * Clears the aggregated eventBag
     */
    public void clearEvents() {
        this.eventBag.clear();
    }
}
