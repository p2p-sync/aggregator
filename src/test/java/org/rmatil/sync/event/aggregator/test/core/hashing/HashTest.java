package org.rmatil.sync.event.aggregator.test.core.hashing;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.commons.hashing.HashingAlgorithm;
import org.rmatil.sync.event.aggregator.test.config.Config;
import org.rmatil.sync.event.aggregator.test.core.APathTest;
import org.rmatil.sync.event.aggregator.test.util.FileUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HashTest {

    protected static Path testFile;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUp() {
        APathTest.setUp();
    }

    @AfterClass
    public static void tearDown() {
        APathTest.tearDown();
    }

    @Before
    public void before() {
        testFile = FileUtil.createTestFile(Config.DEFAULT.getRootTestDir());
    }

    @After
    public void after() {
        testFile = FileUtil.deleteTestFile(Config.DEFAULT.getRootTestDir());

    }

    @Test
    public void testSha1() {
        String hash = null;
        try {
            hash = Hash.hash(HashingAlgorithm.SHA_1, testFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals("SHA1 hash is not equal", "da39a3ee5e6b4b0d3255bfef95601890afd80709", hash);

        testFile = FileUtil.modifyTestFile(Config.DEFAULT.getRootTestDir());

        try {
            hash = Hash.hash(HashingAlgorithm.SHA_1, testFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals("SHA1 hash is not equal after modification", "f9e7d601b1f22e2ead34c1afd7dfee195f31a711", hash);

        testFile = FileUtil.deleteTestFile(Config.DEFAULT.getRootTestDir());
    }

    @Test
    public void testSha256() {
        String hash = null;
        try {
            hash = Hash.hash(HashingAlgorithm.SHA_256, testFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals("SHA256 hash is not equal", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash);

        testFile = FileUtil.modifyTestFile(Config.DEFAULT.getRootTestDir());

        try {
            hash = Hash.hash(HashingAlgorithm.SHA_256, testFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals("SHA256 hash is not equal after modification", "2dddc2bb86f352ea34213f067371c3eea9edab97001bee9aa5047df583b739ba", hash);
    }

    @Test
    public void testSha512() {
        String hash = null;
        try {
            hash = Hash.hash(HashingAlgorithm.SHA_512, testFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals("SHA512 hash is not equal", "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e", hash);

        testFile = FileUtil.modifyTestFile(Config.DEFAULT.getRootTestDir());

        try {
            hash = Hash.hash(HashingAlgorithm.SHA_512, testFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals("SHA512 hash is not equal after modification", "e88c176ea596c01dc2ee01390e24b707792acbad34457ccb17f400a180c307b1b88ef60ee541d1a6f0f8ad5bb5702a03ebc59b4c4fbb5cb1f7c96aa9c39a31cc", hash);
    }

    @Test
    public void testFileNotExisting()
            throws IOException {
        thrown.expect(FileNotFoundException.class);

        FileUtil.deleteTestFile(Config.DEFAULT.getRootTestDir());

        String hash = Hash.hash(HashingAlgorithm.SHA_1, testFile.toFile());
    }

    @Test
    public void testFolderSha1()
            throws IOException {
        thrown.expect(FileNotFoundException.class);

        String hash = Hash.hash(HashingAlgorithm.SHA_1, Config.DEFAULT.getRootTestDir().toFile());
    }

}
