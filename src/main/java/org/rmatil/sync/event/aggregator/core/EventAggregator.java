package org.rmatil.sync.event.aggregator.core;

import name.mitterdorfer.perlock.PathWatcher;
import org.rmatil.sync.event.aggregator.api.IEventAggregator;
import org.rmatil.sync.event.aggregator.api.IEventListener;
import org.rmatil.sync.event.aggregator.core.aggregator.IAggregator;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.modifier.IModifier;
import org.rmatil.sync.event.aggregator.core.pathwatcher.IPathWatcherFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The event aggregator which aggregates multiple
 * file system events to one
 */
public class EventAggregator implements Runnable, IEventAggregator {

    protected final static int     NUMBER_OF_PATHS_TO_WATCH     = 1;
    protected final static boolean CREATE_RECURSIVE_WATCHER     = true;
    protected final static long    DEFAULT_AGGREGATION_INTERVAL = 5000L;
    protected final static long    TIME_GAP_LIFE_CYCLE          = 100L;

    /**
     * The thread executor for the path watcher service.
     * The path watcher does live in another thread.
     */
    protected ExecutorService pathWatcherExecutorService;

    /**
     * The scheduled thread executor for this aggregator.
     * Used to propagate all events in the specified time interval
     */
    protected ScheduledExecutorService aggregationExecutorService;

    /**
     * The path watcher instance used to watch path changes
     */
    protected PathWatcher pathWatcher;

    /**
     * The root path which is watched
     */
    protected Path rootPath;

    /**
     * An event listener listening for path element changes
     */
    protected PathEventListener pathEventListener;

    /**
     * A list of event listener we notify
     * with aggregated path element changes
     */
    protected List<IEventListener> eventListener;

    /**
     * A list of modifiers which are able to modify the list
     * of events which are propagated further
     */
    protected List<IModifier> modifiers;

    /**
     * A list of aggregators which each is responsible
     * to aggregate a particular bunch of certain events
     */
    protected List<IAggregator> aggregators;

    /**
     * The interval in which the events are aggregated
     */
    protected long aggregationInterval;

    /**
     * The root path element which is being watched
     *
     * @param rootPath The root path to watch
     */
    public EventAggregator(Path rootPath, IPathWatcherFactory pathWatcherFactory) {
        this.rootPath = rootPath;
        this.aggregationInterval = DEFAULT_AGGREGATION_INTERVAL;
        this.pathEventListener = new PathEventListener();
        this.eventListener = new ArrayList<>();
        this.aggregators = new ArrayList<>();
        this.modifiers = new ArrayList<>();

        this.pathWatcherExecutorService = Executors.newFixedThreadPool(EventAggregator.NUMBER_OF_PATHS_TO_WATCH);

        if (CREATE_RECURSIVE_WATCHER) {
            this.pathWatcher = pathWatcherFactory.createRecursiveWatcher(pathWatcherExecutorService, this.rootPath, this.pathEventListener);
        } else {
            this.pathWatcher = pathWatcherFactory.createNonRecursiveWatcher(pathWatcherExecutorService, this.rootPath, this.pathEventListener);
        }

        this.aggregationExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void addListener(IEventListener eventListener) {
        this.eventListener.add(eventListener);
    }

    public void removeListener(IEventListener eventListener) {
        this.eventListener.remove(eventListener);
    }

    public List<IEventListener> getListeners() {
        return this.eventListener;
    }

    public void addAggregator(IAggregator aggregator) {
        this.aggregators.add(aggregator);
    }

    public void removeAggregator(IAggregator aggregator) {
        this.aggregators.remove(aggregator);
    }

    public List<IAggregator> getAggregators() {
        return this.aggregators;
    }

    public void addModifier(IModifier modifier) {
        this.modifiers.add(modifier);
    }

    public void removeModifier(IModifier modifier) {
        this.modifiers.remove(modifier);
    }

    public List<IModifier> getModifiers() {
        return this.modifiers;
    }

    public void setAggregationInterval(long milliSeconds) {
        this.aggregationInterval = milliSeconds;
    }

    public long getAggregationInterval() {
        return this.aggregationInterval;
    }

    public void start()
            throws IOException {
        try {
            this.pathWatcher.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // as starting/stop happens in the background (in a new thread) we might miss events. Wait a bit...
            Thread.sleep(TIME_GAP_LIFE_CYCLE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.aggregationExecutorService.scheduleAtFixedRate(this, 0, this.aggregationInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (this.pathWatcher.isRunning()) {
            this.pathWatcher.stop();
        }

        this.pathWatcherExecutorService.shutdown();
        this.aggregationExecutorService.shutdown();
    }

    public void run() {
        // we need to copy these events, otherwise we clear them right after the
        // reference is copied
        List<IEvent> aggregatedEvents = new ArrayList<IEvent>();
        aggregatedEvents.addAll(this.pathEventListener.getEventBag());
        this.pathEventListener.clearEvents();

        // sort events according to their timestamp
        Collections.sort(aggregatedEvents);

        for (IModifier modifier : modifiers) {
            aggregatedEvents = modifier.modify(aggregatedEvents);
        }

        for (IAggregator aggregator : aggregators) {
            aggregatedEvents = aggregator.aggregate(aggregatedEvents);
        }

        // do not notify about empty events
        if (aggregatedEvents.isEmpty()) {
            return;
        }

        // notify all event listeners for the made changes
        for (IEventListener listener : this.eventListener) {
            listener.onChange(aggregatedEvents);
        }
    }
}