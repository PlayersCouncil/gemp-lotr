package com.gempukku.lotro.patchnotes;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class PatchNotesLibraryTest {
    private Path _folder;
    private final AtomicLong _now = new AtomicLong(1_000_000);

    @Before
    public void setUp() throws IOException {
        _folder = Files.createTempDirectory("patchnotes");
    }

    @After
    public void tearDown() throws IOException {
        try (Stream<Path> paths = Files.walk(_folder)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private void write(String name, String text) throws IOException {
        Files.writeString(_folder.resolve(name), text, StandardCharsets.UTF_8);
    }

    private PatchNotesLibrary library() {
        return new PatchNotesLibrary(_folder.toFile(), 5000, _now::get);
    }

    private static List<String> slugs(List<PatchNote> notes) {
        return notes.stream().map(PatchNote::getSlug).collect(Collectors.toList());
    }

    // ---- parsing ----

    @Test
    public void parsesFrontMatterAndBody() {
        PatchNote note = PatchNotesLibrary.parse("2026-09-26-overhaul.md",
                "---\ndate: 2026-09-26\ntitle: \"Patch notes \\\"overhaul\\\"\"\nsummary: 'It''s here.'\norder: 2\n# a comment\n---\n\n- one\n- two\n");
        assertEquals("2026-09-26-overhaul", note.getSlug());
        assertEquals(LocalDate.of(2026, 9, 26), note.getDate());
        assertEquals("Patch notes \"overhaul\"", note.getTitle());
        assertEquals("It's here.", note.getSummary());
        assertEquals(2, note.getOrder());
        assertEquals("- one\n- two", note.getMarkdown());
    }

    @Test
    public void everythingButTheFileNameIsOptional() {
        PatchNote note = PatchNotesLibrary.parse("2011-09-21.md", "- Adding clickable card names to game chat.\n");
        assertEquals("2011-09-21", note.getSlug());
        assertEquals(LocalDate.of(2011, 9, 21), note.getDate());
        assertNull(note.getTitle());
        assertNull(note.getSummary());
        assertEquals(0, note.getOrder());
        assertEquals("- Adding clickable card names to game chat.", note.getMarkdown());

        PatchNote blank = PatchNotesLibrary.parse("2011-09-22.md", "---\ntitle:   \nsummary:\n---\n");
        assertNull(blank.getTitle());
        assertNull(blank.getSummary());
        assertEquals("", blank.getMarkdown());
    }

    @Test
    public void blurbIsAnotherNameForSummaryAndWindowsFilesParse() {
        PatchNote note = PatchNotesLibrary.parse("2026-01-02.md", "﻿---\r\nblurb: Short.\r\n---\r\nBody\r\n");
        assertEquals("Short.", note.getSummary());
        assertEquals("Body", note.getMarkdown());
        assertEquals(LocalDate.of(2026, 1, 2), note.getDate());
    }

    @Test
    public void frontMatterDateWinsOverTheFileName() {
        assertEquals(LocalDate.of(2026, 9, 27), PatchNotesLibrary.parse("2026-09-26.md", "---\ndate: 2026-09-27\n---\n").getDate());
    }

    @Test
    public void malformedFilesAreRejected() {
        String[][] bad = {
                {"2026-9-26.md", "x"},
                {"notes.md", "x"},
                {"2026-09-26-Big_Update.md", "x"},
                {"2026-02-30.md", "x"},
                {"2026-09-26.md", "---\ntitle: never closed\n"},
                {"2026-09-26.md", "---\ndate: 26/09/2026\n---\n"},
                {"2026-09-26.md", "---\norder: first\n---\n"},
                {"2026-09-26.md", "---\njust text\n---\n"},
                {"2026-09-26.md", "---\ntitle: x\n----\n"},
        };
        for (String[] c : bad) {
            try {
                PatchNotesLibrary.parse(c[0], c[1]);
                fail("accepted " + c[0] + ": " + c[1]);
            } catch (IllegalArgumentException expected) {
                // expected
            }
        }
    }

    // ---- listing ----

    @Test
    public void listsNewestFirstByDateThenOrderThenName() throws IOException {
        write("2023-05-13-a.md", "---\norder: 1\n---\nA");
        write("2023-05-13-c.md", "---\norder: 3\n---\nC");
        write("2023-05-13-b.md", "---\norder: 2\n---\nB");
        write("2023-12-09.md", "Newest");
        write("2011-09-21.md", "Oldest");
        write("2022-11-04.md", "plain");
        write("2022-11-04-hobbit-fixes.md", "named");
        assertEquals(List.of("2023-12-09", "2023-05-13-c", "2023-05-13-b", "2023-05-13-a", "2022-11-04-hobbit-fixes",
                "2022-11-04", "2011-09-21"), slugs(library().getNotes()));
    }

    @Test
    public void onlyNoteFilesAreRead() throws IOException {
        write("README.md", "# Patch notes");
        write("2026-09-26.md", "Real");
        write("2026-09-26.txt", "not markdown");
        write("draft.md", "draft");
        Files.createDirectories(_folder.resolve("img"));
        Files.write(_folder.resolve("img").resolve("2026-09-26.md"), new byte[]{1});
        assertEquals(List.of("2026-09-26"), slugs(library().getNotes()));
    }

    @Test
    public void aBrokenFileIsSkippedNotFatal() throws IOException {
        write("2026-09-26.md", "---\ndate: someday\n---\nBroken");
        write("2026-09-25.md", "Fine");
        assertEquals(List.of("2026-09-25"), slugs(library().getNotes()));
    }

    @Test
    public void pagesAreClampedAndStopAtTheEnd() throws IOException {
        for (int day = 1; day <= 28; day++)
            write(String.format("2026-02-%02d.md", day), "Day " + day);
        PatchNotesLibrary library = library();
        assertEquals(28, library.getCount());
        assertEquals(List.of("2026-02-28", "2026-02-27", "2026-02-26", "2026-02-25", "2026-02-24"), slugs(library.getPage(0, 5)));
        assertEquals(List.of("2026-02-23", "2026-02-22"), slugs(library.getPage(5, 2)));
        assertEquals(List.of("2026-02-03", "2026-02-02", "2026-02-01"), slugs(library.getPage(25, 5)));
        assertTrue(library.getPage(28, 5).isEmpty());
        assertTrue(library.getPage(500, 5).isEmpty());
        assertEquals(PatchNotesLibrary.MAX_PAGE_SIZE, library.getPage(0, 1000).size());
        assertEquals(1, library.getPage(0, 0).size());
        assertEquals("2026-02-28", library.getPage(-3, 1).get(0).getSlug());
    }

    @Test
    public void notesAreFoundByNameIgnoringCase() throws IOException {
        write("2026-09-25.md", "Older");
        write("2026-09-26-overhaul.md", "Newer");
        PatchNotesLibrary library = library();
        assertEquals(0, library.indexOf("2026-09-26-overhaul"));
        assertEquals(0, library.indexOf("2026-09-26-OVERHAUL"));
        assertEquals(1, library.indexOf("2026-09-25"));
        assertEquals(-1, library.indexOf("2026-09-24"));
        assertEquals(-1, library.indexOf(null));
        assertEquals("Older", library.get("2026-09-25").getMarkdown());
        assertNull(library.get("../../etc/passwd"));
    }

    @Test
    public void jsonCarriesRenderedHtmlAndNulls() throws IOException {
        write("2026-09-26.md", "---\ntitle: T\n---\n**x** ![s](img/s.png)");
        PatchNotesLibrary library = library();
        var json = library.get("2026-09-26").toJson(library.getRenderer());
        assertEquals(List.of("slug", "kind", "date", "title", "summary", "tags", "summaryHtml", "html"), List.copyOf(json.keySet()));
        assertEquals("note", json.get("kind"));
        assertEquals(List.of(), json.get("tags"));
        assertEquals("2026-09-26", json.get("date"));
        assertEquals("T", json.get("title"));
        assertNull(json.get("summary"));
        assertTrue((String) json.get("html"), ((String) json.get("html")).contains("<strong>x</strong>"));
        assertTrue((String) json.get("html"), ((String) json.get("html")).contains("src=\"patchnotes/img/s.png\""));
        assertFalse(library.get("2026-09-26").toHeaderJson().containsKey("html"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void pageJsonPagesAndCanStartAtANamedNote() throws IOException {
        for (int day = 1; day <= 12; day++)
            write(String.format("2026-03-%02d.md", day), "Day " + day);
        PatchNotesLibrary library = library();

        Map<String, Object> first = library.pageJson(0, 5, null);
        assertEquals(List.of("total", "start", "count", "tag", "all", "filters", "notes"), List.copyOf(first.keySet()));
        assertNull(first.get("tag"));
        assertEquals(12, first.get("all"));
        assertEquals(12, first.get("total"));
        assertEquals(0, first.get("start"));
        assertEquals(5, first.get("count"));
        List<Map<String, Object>> notes = (List<Map<String, Object>>) first.get("notes");
        assertEquals(5, notes.size());
        assertEquals("2026-03-12", notes.get(0).get("slug"));
        assertTrue(((String) notes.get(0).get("html")).contains("Day 12"));

        Map<String, Object> last = library.pageJson(10, 5, null);
        assertEquals(10, last.get("start"));
        assertEquals(2, ((List<?>) last.get("notes")).size());

        Map<String, Object> from = library.pageJson(0, 3, "2026-03-07");
        assertEquals(5, from.get("start"));
        assertEquals(List.of("2026-03-07", "2026-03-06", "2026-03-05"),
                ((List<Map<String, Object>>) from.get("notes")).stream().map(n -> n.get("slug")).collect(Collectors.toList()));
        assertEquals(3, library.pageJson(0, 3, "").get("count"));
        assertEquals(0, library.pageJson(-4, 3, "  ").get("start"));

        assertNull(library.pageJson(0, 5, "2026-03-13"));
        assertNull(library.pageJson(0, 5, "../2026-03-01"));
        assertEquals(20, library.pageJson(0, 99, null).get("count"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void noteJsonNamesTheNeighbours() throws IOException {
        write("2026-03-01.md", "---\ntitle: First\n---\nOne");
        write("2026-03-02.md", "---\nsummary: Middle one.\n---\nTwo");
        write("2026-03-03.md", "Three");
        PatchNotesLibrary library = library();

        Map<String, Object> middle = library.noteJson("2026-03-02");
        assertEquals(List.of("index", "total", "note", "newer", "older"), List.copyOf(middle.keySet()));
        assertEquals(1, middle.get("index"));
        assertEquals(3, middle.get("total"));
        assertEquals("Middle one.", ((Map<String, Object>) middle.get("note")).get("summary"));
        assertTrue(((String) ((Map<String, Object>) middle.get("note")).get("html")).contains("Two"));
        assertEquals("2026-03-03", ((Map<String, Object>) middle.get("newer")).get("slug"));
        assertEquals("First", ((Map<String, Object>) middle.get("older")).get("title"));
        assertFalse(((Map<String, Object>) middle.get("older")).containsKey("html"));

        assertNull(library.noteJson("2026-03-03").get("newer"));
        assertTrue(library.noteJson("2026-03-03").containsKey("newer"));
        assertNull(library.noteJson("2026-03-01").get("older"));
        assertNull(library.noteJson("2026-03-04"));
        assertNull(library.noteJson("README"));
        assertNull(library.noteJson(null));
    }

    @Test
    public void aMissingFolderIsAnEmptyList() {
        PatchNotesLibrary library = new PatchNotesLibrary(_folder.resolve("nope").toFile(), 0, _now::get);
        assertTrue(library.getNotes().isEmpty());
        assertEquals(-1, library.indexOf("2026-09-26"));
    }

    // ---- refreshing ----

    @Test
    public void changesAppearAfterTheCheckInterval() throws IOException {
        write("2026-09-25.md", "Old");
        PatchNotesLibrary library = library();
        assertEquals(1, library.getCount());

        write("2026-09-26.md", "New");
        _now.addAndGet(1000);
        assertEquals("not re-read within the interval", 1, library.getCount());
        _now.addAndGet(5000);
        assertEquals(List.of("2026-09-26", "2026-09-25"), slugs(library.getNotes()));

        Files.delete(_folder.resolve("2026-09-25.md"));
        _now.addAndGet(5000);
        assertEquals(List.of("2026-09-26"), slugs(library.getNotes()));
    }

    @Test
    public void anEditedFileIsReRenderedAndOthersKeepTheirHtml() throws IOException {
        write("2026-09-25.md", "Old *one*");
        write("2026-09-26.md", "Old *two*");
        PatchNotesLibrary library = library();
        PatchNote untouched = library.get("2026-09-25");
        assertTrue(library.get("2026-09-26").getHtml(library.getRenderer()).contains("<em>two</em>"));

        File edited = _folder.resolve("2026-09-26.md").toFile();
        write("2026-09-26.md", "New **two**!");
        assertTrue(edited.setLastModified(edited.lastModified() + 10_000));
        _now.addAndGet(6000);
        assertTrue(library.get("2026-09-26").getHtml(library.getRenderer()).contains("<strong>two</strong>"));
        assertSame(untouched, library.get("2026-09-25"));
    }

    @Test
    public void clearCacheForcesARead() throws IOException {
        write("2026-09-25.md", "Old");
        PatchNotesLibrary library = library();
        assertEquals(1, library.getCount());
        assertEquals(1, library.getItemCount());
        write("2026-09-26.md", "New");
        library.clearCache();
        assertEquals(0, library.getItemCount());
        assertEquals(2, library.getCount());
    }

    // ---- card links, warming up ----

    @Test
    public void warmUpReadsTheFolderAndRendersOnlyTheNewestPage() throws IOException {
        for (int day = 1; day <= 7; day++)
            write("2026-01-0" + day + ".md", "Fixed [[Card " + day + "]].");
        List<String> looked = new java.util.ArrayList<>();
        CardLookup cards = reference -> {
            looked.add(reference);
            return CardLookup.Result.found("1_" + reference.substring(5), reference);
        };
        PatchNotesLibrary library = new PatchNotesLibrary(_folder.toFile(), 5000, _now::get, cards);
        assertEquals(0, library.getItemCount());
        library.warmUp(5);
        assertEquals(7, library.getItemCount());
        assertEquals(List.of("Card 7", "Card 6", "Card 5", "Card 4", "Card 3"), looked);

        // the first visitor's page is already rendered
        Map<String, Object> page = library.pageJson(0, 5, null);
        assertEquals(5, looked.size());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> notes = (List<Map<String, Object>>) page.get("notes");
        assertTrue((String) notes.get(0).get("html"), ((String) notes.get(0).get("html"))
                .contains("<span title=\"Card 7\" class=\"cardHint patchnote-card\" value=\"1_7\">Card 7</span>"));
    }

    @Test
    public void cardsChangedRendersTheNotesAgain() throws IOException {
        write("2026-01-01.md", "Fixed [[Cleaving Blow]].");
        java.util.concurrent.atomic.AtomicReference<String> id = new java.util.concurrent.atomic.AtomicReference<>("1_5");
        PatchNotesLibrary library = new PatchNotesLibrary(_folder.toFile(), 5000, _now::get,
                reference -> CardLookup.Result.found(id.get(), reference));
        assertTrue(library.get("2026-01-01").getHtml(library.getRenderer()).contains("value=\"1_5\""));
        id.set("51_5");
        assertTrue(library.get("2026-01-01").getHtml(library.getRenderer()).contains("value=\"1_5\""));   // kept
        library.cardsChanged();
        assertTrue(library.get("2026-01-01").getHtml(library.getRenderer()).contains("value=\"51_5\""));
    }

    @Test
    public void anUnresolvedCardLinkIsPlainTextAndTheNoteStillRenders() throws IOException {
        write("2026-01-01.md", "Fixed [[Nobody]] and [[Cleaving Blow]].");
        PatchNotesLibrary library = new PatchNotesLibrary(_folder.toFile(), 5000, _now::get,
                reference -> reference.equals("Nobody") ? CardLookup.Result.failed("no card is named 'Nobody'")
                        : CardLookup.Result.found("1_5", reference));
        String html = library.get("2026-01-01").getHtml(library.getRenderer());
        assertTrue(html, html.startsWith("<p>Fixed Nobody and <span"));
    }

    // ---- the notes shipped in the repo ----

    @Test
    public void everyShippedNoteParsesAndRenders() throws IOException {
        File shipped = new File("../gemp-lotr-async/src/main/web/patchnotes");
        Assume.assumeTrue("run from gemp-lotr-server", shipped.isDirectory());

        // a note whose name is mistyped would silently never show: every Markdown file but a README must be a note
        // (in any subfolder, outside the img/ folders), and no two may share a name
        Set<String> slugs = new HashSet<>();
        List<File> markdown;
        try (Stream<Path> paths = Files.walk(shipped.toPath())) {
            markdown = paths.filter(p -> p.toString().endsWith(".md") && !p.getFileName().toString().equals("README.md"))
                    .filter(p -> shipped.toPath().relativize(p).toString().replace('\\', '/').matches("(?!(.*/)?img/).*"))
                    .map(Path::toFile).toList();
        }
        for (File file : markdown) {
            assertTrue("not a patch note name: " + file.getName(), PatchNotesLibrary.FILE_NAME.matcher(file.getName()).matches());
            PatchNote note = PatchNotesLibrary.readNote(file);
            assertTrue(slugs.add(note.getSlug()));
        }

        PatchNotesLibrary library = new PatchNotesLibrary(shipped, 0, System::currentTimeMillis);
        List<PatchNote> notes = library.getNotes();
        assertEquals(slugs.size(), notes.size());
        assertTrue("the migrated change log alone has 359 entries", notes.size() >= 359);
        assertEquals("2011-09-21", notes.get(notes.size() - 1).getSlug());
        for (int i = 1; i < notes.size(); i++)
            assertFalse(notes.get(i).getDate().isAfter(notes.get(i - 1).getDate()));
        for (PatchNote note : notes) {
            for (String tag : note.getTags())
                assertTrue(note.getSlug() + ": unknown tag " + tag, PatchNote.isKnownTag(tag));
            String html = note.getHtml(library.getRenderer());
            assertFalse(note.getSlug() + " renders empty", html.isBlank());
            assertFalse(note.getSlug(), html.toLowerCase().contains("<script"));
        }
    }
}
