package org.rmatil.sync.event.aggregator.test.core.modifier;

import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.event.aggregator.core.events.CreateEvent;
import org.rmatil.sync.event.aggregator.core.events.IEvent;
import org.rmatil.sync.event.aggregator.core.events.ModifyEvent;
import org.rmatil.sync.event.aggregator.core.modifier.FixFileSystemCreationModifier;
import org.rmatil.sync.event.aggregator.core.modifier.IModifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FixFileSystemCreationModifierTest {

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
                HASH,
                123L
        );

        ModifyEvent modifyEvent = new ModifyEvent(
                PATH,
                FILENAME,
                Hash.EMPTY_SHA256_HASH,
                1234L
        );

        ModifyEvent modifyEventCorrect = new ModifyEvent(
                PATH,
                FILENAME,
                HASH,
                12345L
        );

        events.add(createEvent);
        events.add(modifyEvent);
        events.add(modifyEventCorrect);

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
        IModifier modifier = new FixFileSystemCreationModifier();

        List<IEvent> modifiedEvents = modifier.modify(events);

        assertEquals("Size should be 2", 2, modifiedEvents.size());
        assertThat("First event should be CreateEvent", modifiedEvents.get(0), is(instanceOf(CreateEvent.class)));
        assertThat("2nd event should be ModifyEvent", modifiedEvents.get(1), is(instanceOf(ModifyEvent.class)));
    }

    @Test
    public void test2() {
        IModifier modifier = new FixFileSystemCreationModifier();

        List<IEvent> modifiedEvents2 = modifier.modify(events2);

        assertEquals("Size should be 1", 1, modifiedEvents2.size());
        assertThat("Event should be modifyEvent", modifiedEvents2.get(0), is(CoreMatchers.instanceOf(ModifyEvent.class)));
    }

    @Test
    public void test3() {
        IModifier modifier = new FixFileSystemCreationModifier();

        List<IEvent> modifiedEvents3 = modifier.modify(events3);

        assertEquals("Size should be 2", 2, modifiedEvents3.size());
        assertThat("Event should be modifyEvent", modifiedEvents3.get(0), is(instanceOf(ModifyEvent.class)));
        assertEquals("Event should have empty hash", HASH, modifiedEvents3.get(0).getHash());
        assertThat("Event should be modifyEvent", modifiedEvents3.get(1), is(instanceOf(ModifyEvent.class)));
        assertEquals("Event should have empty hash", Hash.EMPTY_SHA256_HASH, modifiedEvents3.get(1).getHash());
    }
}
