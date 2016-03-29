package org.rmatil.sync.event.aggregator.test.core.aggregator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.event.aggregator.core.aggregator.EmptyHashAggregator;
import org.rmatil.sync.event.aggregator.core.aggregator.IAggregator;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EmptyHashAggregatorTest {

    private static final Path   PATH     = Paths.get("testFile.txt");
    private static final String FILENAME = PATH.getFileName().toString();
    private static final String HASH     = "someHash";


    private static List<IEvent> events  = new ArrayList<>();
    private static List<IEvent> events2 = new ArrayList<>();
    private static List<IEvent> events3 = new ArrayList<>();

    @BeforeClass
    public static void setUp() {
        CreateEvent createEvent = new CreateEvent(
                PATH,
                FILENAME,
                Hash.EMPTY_SHA256_HASH,
                123L
        );

        ModifyEvent modifyEvent = new ModifyEvent(
                PATH,
                FILENAME,
                HASH,
                1234L
        );

        events.add(createEvent);
        events.add(modifyEvent);

        CreateEvent createEvent2 = new CreateEvent(
                PATH,
                FILENAME,
                HASH,
                123L
        );

        events2.add(createEvent2);
        events2.add(modifyEvent);

        ModifyEvent modifyEvent2 = new ModifyEvent(
                PATH,
                FILENAME,
                HASH,
                1234L
        );

        ModifyEvent modifyEvent3 = new ModifyEvent(
                PATH,
                FILENAME,
                Hash.EMPTY_SHA256_HASH,
                12345L
        );

        events3.add(modifyEvent2);
        events3.add(modifyEvent3);
    }

    @Test
    public void test() {
        IAggregator aggregator = new EmptyHashAggregator();

        List<IEvent> aggregates = aggregator.aggregate(events);

        assertEquals("Size should be 1", 1, aggregates.size());
        assertThat("Event should be CreateEvent", aggregates.get(0), is(instanceOf(CreateEvent.class)));
        assertEquals("EventHash should not be the empty hash", events.get(1).getHash(), aggregates.get(0).getHash());
    }

    @Test
    public void test2() {
        IAggregator aggregator = new EmptyHashAggregator();

        List<IEvent> aggregates = aggregator.aggregate(events2);

        assertEquals("Size should be 2", 2, aggregates.size());
        assertThat("Event should be CreateEvent", aggregates.get(0), is(instanceOf(CreateEvent.class)));
        assertEquals("Hash should be equal", events2.get(0).getHash(), aggregates.get(0).getHash());
        assertThat("Event should be ModifyEvent", aggregates.get(1), is(instanceOf(ModifyEvent.class)));
        assertEquals("Hash should be equal", events2.get(1).getHash(), aggregates.get(1).getHash());
    }

    @Test
    public void test3() {
        IAggregator aggregator = new EmptyHashAggregator();

        List<IEvent> aggregates = aggregator.aggregate(events3);

        assertEquals("Size should be 2", 2, aggregates.size());
        assertThat("Event should be ModifyEvent", aggregates.get(0), is(instanceOf(ModifyEvent.class)));
        assertThat("Event should be ModifyEvent", aggregates.get(1), is(instanceOf(ModifyEvent.class)));
    }
}
