package com.gempukku.lotro.async;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;

public class ConnectionFileCacheTest {
    private static final long EARLIER = 1_000_000_000L;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final ConnectionFileCache cache = new ConnectionFileCache();

    private File file(String contents, long lastModified) throws IOException {
        File file = new File(folder.getRoot(), "index.html");
        Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
        file.setLastModified(lastModified);
        return file;
    }

    private static byte[] bytes(String contents) {
        return contents.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void readsTheFile() throws IOException {
        assertArrayEquals(bytes("old"), cache.read(file("old", EARLIER)));
    }

    @Test
    public void keepsAnUnchangedFile() throws IOException {
        File file = file("old", EARLIER);
        assertSame(cache.read(file), cache.read(file));
    }

    @Test
    public void readsAFileReplacedOnDiskAgain() throws IOException {
        cache.read(file("old", EARLIER));
        assertArrayEquals(bytes("new"), cache.read(file("new", EARLIER + 5000)));
    }

    @Test
    public void readsAFileOfAnotherLengthAgainEvenAtTheSameTime() throws IOException {
        cache.read(file("old", EARLIER));
        assertArrayEquals(bytes("longer"), cache.read(file("longer", EARLIER)));
    }
}
