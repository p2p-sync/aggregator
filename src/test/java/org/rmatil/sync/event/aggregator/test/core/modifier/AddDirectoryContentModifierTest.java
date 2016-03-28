package org.rmatil.sync.event.aggregator.test.core.modifier;

import org.junit.*;
import org.rmatil.sync.event.aggregator.core.events.*;
import org.rmatil.sync.event.aggregator.core.modifier.AddDirectoryContentModifier;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.core.APathTest;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;
import org.rmatil.sync.persistence.core.tree.local.LocalStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.core.ObjectStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AddDirectoryContentModifierTest {

    protected static AddDirectoryContentModifier addDirectoryContentModifier;

    protected static Path dir            = Config.DEFAULT.getRootTestDir().resolve("testDir1");
    protected static Path fileInDir      = dir.resolve("file1.txt");
    protected static Path file2InDir     = dir.resolve("file2.txt");
    protected static Path dirInDir       = dir.resolve("nestedDir");
    protected static Path fileInDirInDir = dirInDir.resolve("file3.txt");

    protected static ObjectStore objectStore;

    @BeforeClass
    public static void setUp()
            throws InputOutputException {
        APathTest.setUp();

        objectStore = new ObjectStore(
                new LocalStorageAdapter(Config.DEFAULT.getRootTestDir()),
                "index.json",
                "object",
                new LocalStorageAdapter(Config.DEFAULT.getRootTestDir())
        );

        addDirectoryContentModifier = new AddDirectoryContentModifier(Config.DEFAULT.getRootTestDir(), objectStore);
    }

    @AfterClass
    public static void tearDown() {
        APathTest.tearDown();
    }

    @Before
    public void before()
            throws IOException, InputOutputException {
        FileUtil.createTestDir(Config.DEFAULT.getRootTestDir());

        if (! Files.exists(dir)) {
            Files.createDirectory(dir);
            objectStore.onCreateFile(Config.DEFAULT.getRootTestDir().relativize(dir).toString(), null);
        }

        if (! Files.exists(fileInDir)) {
            Files.createFile(fileInDir);
            objectStore.onCreateFile(Config.DEFAULT.getRootTestDir().relativize(fileInDir).toString(), "fileInDir");
        }

        if (! Files.exists(file2InDir)) {
            Files.createFile(file2InDir);
            objectStore.onCreateFile(Config.DEFAULT.getRootTestDir().relativize(file2InDir).toString(), "file2InDir");
        }

        if (! Files.exists(dirInDir)) {
            Files.createDirectory(dirInDir);
            objectStore.onCreateFile(Config.DEFAULT.getRootTestDir().relativize(dirInDir).toString(), null);
        }

        if (! Files.exists(fileInDirInDir)) {
            Files.createFile(fileInDirInDir);
            objectStore.onCreateFile(Config.DEFAULT.getRootTestDir().relativize(fileInDirInDir).toString(), "fileInDirInDir");
        }
    }

    @After
    public void after() {
        FileUtil.deleteTestDir(Config.DEFAULT.getRootTestDir());
    }

    @Test
    public void testModifyDelete()
            throws IOException {

        Path relativePath = Config.DEFAULT.getRootTestDir().relativize(dir);
        DeleteEvent deleteEvent = new DeleteEvent(
                relativePath,
                relativePath.getFileName().toString(),
                null,
                System.currentTimeMillis()
        );

        List<IEvent> events = new ArrayList<>();
        events.add(deleteEvent);

        List<IEvent> results = addDirectoryContentModifier.modify(events);

        // expected events
        // 1: deleteEvent itself for dir
        // 2: fileInDir
        // 3: file2InDir
        // 4: dirInDir
        // 5: fileInDirInDir
        assertEquals("Not expected number of results modified", 5, results.size());
        assertThat("No create event should be inside results", events, not(hasItem(isA(CreateEvent.class))));
        assertThat("No modify event should be inside results", events, not(hasItem(isA(ModifyEvent.class))));
        assertThat("No move event should be inside results", events, not(hasItem(isA(MoveEvent.class))));
    }

    @Test
    public void testModifyCreate() {
        Path relativePath = Config.DEFAULT.getRootTestDir().relativize(dir);
        CreateEvent createEvent = new CreateEvent(
                relativePath,
                relativePath.getFileName().toString(),
                null,
                System.currentTimeMillis()
        );

        List<IEvent> events = new ArrayList<>();
        events.add(createEvent);

        List<IEvent> results = addDirectoryContentModifier.modify(events);

        // expected events
        // 1: create event itself
        // 2: fileInDir
        // 3: file2InDir
        // 4: dirInDir
        // 5: fileInDirInDir
        assertEquals("Not expected number of results modified", 5, results.size());
        assertThat("No delete event should be inside results", events, not(hasItem(isA(DeleteEvent.class))));
        assertThat("No modify event should be inside results", events, not(hasItem(isA(ModifyEvent.class))));
        assertThat("No move event should be inside results", events, not(hasItem(isA(MoveEvent.class))));
    }

    @Test
    public void testModifyCreateWithCreateEvent() {
        Path relativePath = Config.DEFAULT.getRootTestDir().relativize(dir);
        CreateEvent createEvent = new CreateEvent(
                relativePath,
                relativePath.getFileName().toString(),
                null,
                System.currentTimeMillis()
        );

        // create an additional create event which should force the
        // relative path modifier to avoid
        // creating a new create event again
        Path relativePathOfFileInDir = Config.DEFAULT.getRootTestDir().relativize(fileInDir);
        CreateEvent additionalCreateEvent = new CreateEvent(
                relativePathOfFileInDir,
                relativePathOfFileInDir.getFileName().toString(),
                null,
                System.currentTimeMillis()
        );

        List<IEvent> events = new ArrayList<>();
        events.add(createEvent);
        events.add(additionalCreateEvent);

        List<IEvent> results = addDirectoryContentModifier.modify(events);

        // expected events
        // 1: create event itself
        // 2: fileInDir
        // 3: file2InDir
        // 4: dirInDir
        // 5: fileInDirInDir
        assertEquals("Not expected number of results modified", 5, results.size());
        assertThat("No delete event should be inside results", events, not(hasItem(isA(DeleteEvent.class))));
        assertThat("No modify event should be inside results", events, not(hasItem(isA(ModifyEvent.class))));
        assertThat("No move event should be inside results", events, not(hasItem(isA(MoveEvent.class))));
    }

    @Test
    public void testModify() {
        Path relativePath = Config.DEFAULT.getRootTestDir().relativize(dir);
        ModifyEvent modifyEvent = new ModifyEvent(
                relativePath,
                relativePath.getFileName().toString(),
                null,
                System.currentTimeMillis()
        );

        List<IEvent> events = new ArrayList<>();
        events.add(modifyEvent);

        List<IEvent> results = addDirectoryContentModifier.modify(events);

        // expected events
        // 1: only modify event
        assertEquals("Not expected number of results modified", 1, results.size());
        assertThat("No modify event should be inside results", events, hasItem(isA(ModifyEvent.class)));

    }
}
