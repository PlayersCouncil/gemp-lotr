package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.share.CardImages;
import com.gempukku.lotro.share.SharePages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/** Notes in subfolders of patchnotes/: found at any depth, named by their file, changes noticed, paths resolved. */
public class PatchNotesFoldersTest {
    private Path _root;
    private final AtomicLong _now = new AtomicLong(1_000_000);

    @Before
    public void setUp() throws IOException {
        _root = Files.createTempDirectory("patchnotes-folders");
    }

    @After
    public void tearDown() throws IOException {
        try (Stream<Path> paths = Files.walk(_root)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private Path write(String path, String text) throws IOException {
        Path file = _root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text, StandardCharsets.UTF_8);
        return file;
    }

    private PatchNotesLibrary library() {
        return new PatchNotesLibrary(_root.toFile(), 5000, _now::get);
    }

    private static List<String> slugs(List<PatchNote> notes) {
        return notes.stream().map(PatchNote::getSlug).collect(Collectors.toList());
    }

    @Test
    public void notesAreReadFromEverySubfolder() throws IOException {
        write("2026-09-27.md", "root");
        write("2011/2011-09-21.md", "one");
        write("old/2012/2012-03-05-fixes.md", "two");
        write("a/b/c/d/e/f/g/2013-01-01.md", "seven deep");
        write("a/b/c/d/e/f/g/h/i/2013-01-02.md", "too deep");
        write("2011/README.md", "not a note");
        write("README.md", "not a note");
        write("2011/img/2011-09-22.md", "in an img folder");
        write("img/2011-09-23.md", "in the img folder");
        write(".git/2011-09-24.md", "hidden");
        write("2011/notes.txt", "not a note");
        PatchNotesLibrary library = library();

        assertEquals(List.of("2026-09-27", "2013-01-01", "2012-03-05-fixes", "2011-09-21"), slugs(library.getNotes()));
        assertEquals("", library.get("2026-09-27").getFolder());
        assertEquals("2011", library.get("2011-09-21").getFolder());
        assertEquals("old/2012", library.get("2012-03-05-fixes").getFolder());
        // the link is the file name, whatever the folder
        assertNotNull(library.pageJson(0, 5, "2012-03-05-fixes"));
    }

    @Test
    public void aDuplicateNameKeepsTheShallowestThenTheFirstByPath() throws IOException {
        write("b/2020-01-01.md", "---\ntitle: in b\n---\nx");
        write("2020-01-01.md", "---\ntitle: at the top\n---\nx");
        write("z/2020-01-02.md", "---\ntitle: in z\n---\nx");
        write("a/2020-01-02.md", "---\ntitle: in a\n---\nx");
        write("a/deeper/2020-01-02.md", "---\ntitle: deeper\n---\nx");
        PatchNotesLibrary library = library();
        assertEquals(List.of("2020-01-02", "2020-01-01"), slugs(library.getNotes()));
        assertEquals("at the top", library.get("2020-01-01").getTitle());
        assertEquals("in a", library.get("2020-01-02").getTitle());
        // and the same one every time the folder is read
        library.clearCache();
        assertEquals("in a", library.get("2020-01-02").getTitle());
    }

    @Test
    public void changesInSubfoldersAreNoticed() throws IOException {
        Path note = write("2011/2011-09-21.md", "---\ntitle: First\n---\nx");
        PatchNotesLibrary library = library();
        assertEquals("First", library.get("2011-09-21").getTitle());

        // edited
        Files.writeString(note, "---\ntitle: Edited, and longer\n---\nx", StandardCharsets.UTF_8);
        _now.addAndGet(6000);
        assertEquals("Edited, and longer", library.get("2011-09-21").getTitle());

        // added, in a new folder
        write("2012/deep/2012-01-01.md", "new");
        _now.addAndGet(6000);
        assertEquals(List.of("2012-01-01", "2011-09-21"), slugs(library.getNotes()));

        // moved to another folder: same name, same link, its new folder
        Path moved = _root.resolve("old/2011/2011-09-21.md");
        Files.createDirectories(moved.getParent());
        Files.move(note, moved);
        _now.addAndGet(6000);
        assertEquals(List.of("2012-01-01", "2011-09-21"), slugs(library.getNotes()));
        assertEquals("old/2011", library.get("2011-09-21").getFolder());

        // removed
        Files.delete(moved);
        _now.addAndGet(6000);
        assertEquals(List.of("2012-01-01"), slugs(library.getNotes()));
    }

    @Test
    public void relativePathsResolveNextToTheNoteThenFromTheRoot() throws IOException {
        write("img/root-only.png", "png");
        write("img/both.png", "png");
        write("2011/img/both.png", "png");
        write("2011/img/local only.png", "png");
        write("2011/other.pdf", "pdf");
        write("2011/2011-09-21.md", """
                ![a](img/both.png) ![b](img/root-only.png) ![c](img/local%20only.png) ![d](./img/both.png)
                ![e](../img/both.png) ![f](img/missing.png) ![g](patchnotes/img/both.png) ![h](/gemp-lotr/x.png)
                [pdf](other.pdf#page=2) [up](../../../etc/passwd) [hall](#events) [site](https://x.org/img/a.png)
                <img src="img/both.png" width="100">
                """);
        write("2026-09-27.md", "![r](img/both.png)");
        PatchNotesLibrary library = library();

        String html = library.get("2011-09-21").getHtml(library.getRenderer());
        assertTrue(html, html.contains("src=\"patchnotes/img/root-only.png\""));
        assertTrue(html, html.contains("src=\"patchnotes/2011/img/local%20only.png\""));
        assertEquals(3, count(html, "src=\"patchnotes/2011/img/both.png\""));   // a, d and the <img>
        assertTrue(html, html.contains("src=\"patchnotes/img/both.png\""));      // e (../) and g (already rooted)
        assertTrue(html, html.contains("src=\"patchnotes/img/missing.png\""));   // nowhere: as before, from the root
        assertTrue(html, html.contains("src=\"/gemp-lotr/x.png\""));
        assertTrue(html, html.contains("href=\"patchnotes/2011/other.pdf#page=2\""));
        // climbing out of the patchnotes folder: left as a note at the top would have it
        assertTrue(html, html.contains("href=\"patchnotes/../../../etc/passwd\""));
        assertTrue(html, html.contains("href=\"#events\""));
        assertTrue(html, html.contains("href=\"https://x.org/img/a.png\""));

        // a note at the top: as always
        assertTrue(library.get("2026-09-27").getHtml(library.getRenderer()).contains("src=\"patchnotes/img/both.png\""));
    }

    @Test
    public void theAnnouncementAndTheSharePreviewUseTheSameResolution() throws IOException {
        write("2011/img/2011-shot.png", "png");
        write("img/root-shot.png", "png");
        write("2011/2011-09-21-sub.md", "---\ntitle: Sub\n---\nThe :isengard: fixes.\n\n![Local](img/2011-shot.png) ![Root](img/root-shot.png)");
        write("2011/2011-09-20-root.md", "---\ntitle: Root pic\n---\nText.\n\n![Root](img/root-shot.png)");
        PatchNotesLibrary library = library();

        PatchNoteAnnouncer announcer = new PatchNoteAnnouncer(library, null, null);
        String content = announcer.content(library.get("2011-09-21-sub"));
        assertTrue(content, content.contains("![Local](/gemp-lotr/patchnotes/2011/img/2011-shot.png)"));
        assertTrue(content, content.contains("![Isengard](/gemp-lotr/images/cultures/isengard.png \"Isengard\")"));
        assertTrue(announcer.content(library.get("2011-09-20-root")).contains("![Root](/gemp-lotr/patchnotes/img/root-shot.png)"));
        assertEquals("/gemp-lotr/patchnotes/2011/img/2011-shot.png",
                PatchNoteAnnouncer.absolute("img/2011-shot.png", library.get("2011-09-21-sub").getPaths()));

        SharePages pages = new SharePages(library, null, null, new CardImages(null), (owner, name) -> null);
        SharePages.Page page = pages.page("patch-notes", "2011-09-21-sub");
        assertEquals("patchnotes/2011/img/2011-shot.png", page.image());
        assertTrue(page.description(), page.description().contains("The Isengard fixes."));
        assertEquals("patchnotes/img/root-shot.png", pages.page("patch-notes", "2011-09-20-root").image());
    }

    @Test
    public void normalizeKeepsInsideTheFolder() {
        assertEquals("img/a.png", PatchNotesLibrary.normalize("2011/../img/a.png"));
        assertEquals("2011/img/a.png", PatchNotesLibrary.normalize("2011/./img//a.png"));
        assertNull(PatchNotesLibrary.normalize("2011/../../a.png"));
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1))
            n++;
        return n;
    }
}
