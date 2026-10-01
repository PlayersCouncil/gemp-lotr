package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.common.DBDefs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * The Patch Notes feed: tags and the tag filter, the month index, and the past announcements shown among the notes
 * (with a fake announcement source, no database).
 */
public class PatchNotesFeedTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 27, 12, 0);

    private Path _folder;
    private final AtomicLong _clock = new AtomicLong(1_000_000);
    private final List<DBDefs.Announcement> _announcements = new ArrayList<>();
    private final AtomicInteger _reads = new AtomicInteger();

    @Before
    public void setUp() throws IOException {
        _folder = Files.createTempDirectory("patchnotes-feed");
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
        return new PatchNotesLibrary(_folder.toFile(), 5000, _clock::get);
    }

    private PatchNotesLibrary libraryWithAnnouncements() {
        PatchNotesLibrary library = library();
        library.setAnnouncements(() -> {
            _reads.incrementAndGet();
            return new ArrayList<>(_announcements);
        }, 60_000, () -> NOW);
        return library;
    }

    private static DBDefs.Announcement announcement(int id, String title, String content, LocalDateTime start) {
        DBDefs.Announcement a = new DBDefs.Announcement();
        a.id = id;
        a.title = title;
        a.content = content;
        a.start = start;
        a.until = start.plusDays(14);
        return a;
    }

    private static List<String> slugs(List<PatchNote> notes) {
        return notes.stream().map(PatchNote::getSlug).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static List<String> pageSlugs(Map<String, Object> page) {
        return ((List<Map<String, Object>>) page.get("notes")).stream().map(n -> (String) n.get("slug"))
                .collect(Collectors.toList());
    }

    // ---- tags in front matter ----

    @Test
    public void tagsAreACommaListABracketListOrAYamlList() {
        PatchNote comma = PatchNotesLibrary.parse("2026-09-01.md", "---\ntags: Card Fixes, user interface\n---\nx");
        assertEquals(List.of("Card Fixes", "User Interface"), comma.getTags());

        PatchNote brackets = PatchNotesLibrary.parse("2026-09-02.md",
                "---\ntags: [\"PC Updates\", 'card-fixes']\n---\nx");
        assertEquals(List.of("PC Updates", "Card Fixes"), brackets.getTags());

        PatchNote yaml = PatchNotesLibrary.parse("2026-09-03.md",
                "---\ntitle: T\ntags:\n  - Card Fixes\n  - \"User Interface\"\n  - card fixes\nsummary: S\n---\nx");
        assertEquals(List.of("Card Fixes", "User Interface"), yaml.getTags());
        assertEquals("T", yaml.getTitle());
        assertEquals("S", yaml.getSummary());

        PatchNote none = PatchNotesLibrary.parse("2026-09-04.md", "---\ntitle: T\n---\nx");
        assertEquals(List.of(), none.getTags());
        assertEquals(List.of(), PatchNotesLibrary.parse("2026-09-05.md", "---\ntags:\n---\nx").getTags());
    }

    @Test
    public void anUnknownTagIsKeptAsWritten() {
        PatchNote note = PatchNotesLibrary.parse("2026-09-01.md", "---\ntags: Card Fixes, Balance\n---\nx");
        assertEquals(List.of("Card Fixes", "Balance"), note.getTags());
        assertTrue(note.hasTag("card fixes"));
        assertTrue(note.hasTag("Balance"));
        assertFalse(PatchNote.isKnownTag("Balance"));
    }

    @Test
    public void aListItemWithoutAKeyIsStillAnError() {
        try {
            PatchNotesLibrary.parse("2026-09-01.md", "---\ntitle: T\n- Card Fixes\n---\nx");
            fail();
        } catch (IllegalArgumentException expected) {
            // a "- item" line only belongs to a key with no value on its own line
        }
    }

    @Test
    public void canonicalTagsIgnoreCaseSpacesAndDashes() {
        assertEquals("Card Fixes", PatchNote.canonicalTag("card-fixes"));
        assertEquals("Card Fixes", PatchNote.canonicalTag("  CARD   FIXES "));
        assertEquals("PC Updates", PatchNote.canonicalTag("pc_updates"));
        assertEquals("User Interface", PatchNote.canonicalTag("UserInterface"));
        assertEquals("Announcement", PatchNote.canonicalTag("announcement"));
        assertNull(PatchNote.canonicalTag(" "));
        assertEquals(List.of("Card Fixes", "PC Updates", "User Interface", "Announcement"), PatchNote.TAGS);
    }

    // ---- the tag filter ----

    private void writeTaggedNotes() throws IOException {
        // newest first: 04-02 (UI), 04-01 (cards), 03-20 (cards + PC), 03-10 (none), 02-05 (cards), 01-15 (PC)
        write("2026-04-02.md", "---\ntags: User Interface\n---\na");
        write("2026-04-01.md", "---\ntags: Card Fixes\n---\nb");
        write("2026-03-20.md", "---\ntags: Card Fixes, PC Updates\n---\nc");
        write("2026-03-10.md", "d");
        write("2026-02-05.md", "---\ntags: [card fixes]\n---\ne");
        write("2026-01-15.md", "---\ntags:\n  - PC Updates\n---\nf");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void pagesOfATagHoldOnlyItsNotes() throws IOException {
        writeTaggedNotes();
        PatchNotesLibrary library = library();

        Map<String, Object> cards = library.pageJson(0, 2, null, null, "card-fixes");
        assertEquals("Card Fixes", cards.get("tag"));
        assertEquals(3, cards.get("total"));
        assertEquals(6, cards.get("all"));
        assertEquals(List.of("2026-04-01", "2026-03-20"), pageSlugs(cards));
        assertEquals(List.of("2026-02-05"), pageSlugs(library.pageJson(2, 2, null, null, "Card Fixes")));

        List<Map<String, Object>> filters = (List<Map<String, Object>>) cards.get("filters");
        assertEquals(List.of("Card Fixes", "PC Updates", "User Interface", "Announcement"),
                filters.stream().map(f -> f.get("tag")).collect(Collectors.toList()));
        assertEquals(List.of(3, 2, 1, 0), filters.stream().map(f -> f.get("count")).collect(Collectors.toList()));

        // no tag (or a blank one) is everything
        assertEquals(6, library.pageJson(0, 5, null, null, null).get("total"));
        assertEquals(6, library.pageJson(0, 5, null, null, " ").get("total"));
        assertNull(library.pageJson(0, 5, null, null, "").get("tag"));
        // a tag nothing has is an empty feed, not an error
        assertEquals(0, library.pageJson(0, 5, null, null, "Announcement").get("total"));
    }

    @Test
    public void aDeepLinkWithATagStartsInTheTagsFeedOrIsNotThere() throws IOException {
        writeTaggedNotes();
        PatchNotesLibrary library = library();

        Map<String, Object> page = library.pageJson(0, 5, "2026-03-20", null, "PC Updates");
        assertEquals(0, page.get("start"));
        assertEquals(List.of("2026-03-20", "2026-01-15"), pageSlugs(page));
        assertEquals(1, library.pageJson(0, 5, "2026-03-20", null, "Card Fixes").get("start"));
        // the note is not tagged so: the page falls back to All (patchNotesUi.js)
        assertNull(library.pageJson(0, 5, "2026-03-10", null, "Card Fixes"));
        assertEquals(3, library.pageJson(0, 5, "2026-03-10", null, null).get("start"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void noteJsonNeighboursAreThoseOfTheTag() throws IOException {
        writeTaggedNotes();
        PatchNotesLibrary library = library();
        Map<String, Object> json = library.noteJson("2026-03-20", "Card Fixes");
        assertEquals(1, json.get("index"));
        assertEquals(3, json.get("total"));
        assertEquals("2026-04-01", ((Map<String, Object>) json.get("newer")).get("slug"));
        assertEquals("2026-02-05", ((Map<String, Object>) json.get("older")).get("slug"));
        assertEquals(List.of("Card Fixes", "PC Updates"), ((Map<String, Object>) json.get("note")).get("tags"));
        assertNull(library.noteJson("2026-03-10", "Card Fixes"));
        assertEquals(3, library.noteJson("2026-03-10", null).get("index"));
    }

    // ---- months ----

    @Test
    @SuppressWarnings("unchecked")
    public void theMonthIndexNamesEachMonthsNewestNote() throws IOException {
        writeTaggedNotes();
        PatchNotesLibrary library = library();

        Map<String, Object> all = library.monthsJson(null);
        assertNull(all.get("tag"));
        List<Map<String, Object>> months = (List<Map<String, Object>>) all.get("months");
        assertEquals(List.of("2026-04", "2026-03", "2026-02", "2026-01"),
                months.stream().map(m -> m.get("month")).collect(Collectors.toList()));
        assertEquals(List.of("2026-04-02", "2026-03-20", "2026-02-05", "2026-01-15"),
                months.stream().map(m -> m.get("first")).collect(Collectors.toList()));
        assertEquals(List.of(2, 2, 1, 1), months.stream().map(m -> m.get("count")).collect(Collectors.toList()));

        List<Map<String, Object>> ui = (List<Map<String, Object>>) library.monthsJson("user interface").get("months");
        assertEquals(1, ui.size());
        assertEquals("2026-04-02", ui.get(0).get("first"));
        List<Map<String, Object>> pc = (List<Map<String, Object>>) library.monthsJson("PC Updates").get("months");
        assertEquals(List.of("2026-03", "2026-01"), pc.stream().map(m -> m.get("month")).collect(Collectors.toList()));
        assertEquals(List.of(), library.monthsJson("Announcement").get("months"));
    }

    @Test
    public void aPageCanStartAtAMonth() throws IOException {
        writeTaggedNotes();
        PatchNotesLibrary library = library();

        Map<String, Object> march = library.pageJson(0, 5, null, "2026-03", null);
        assertEquals(2, march.get("start"));
        assertEquals(List.of("2026-03-20", "2026-03-10", "2026-02-05", "2026-01-15"), pageSlugs(march));
        // a month without notes starts at the newest older one
        assertEquals(0, library.pageJson(0, 5, null, "2026-12", null).get("start"));
        assertEquals(1, library.pageJson(0, 5, null, "2026-02", "PC Updates").get("start"));
        assertEquals(List.of("2026-01-15"), pageSlugs(library.pageJson(0, 5, null, "2026-02", "PC Updates")));
        // nothing at or before it, or not a month
        assertNull(library.pageJson(0, 5, null, "2025-12", null));
        assertNull(library.pageJson(0, 5, null, "2026-13", null));
        assertNull(library.pageJson(0, 5, null, "March", null));
        // from wins over month
        assertEquals(0, library.pageJson(0, 5, "2026-04-02", "2026-01", null).get("start"));
    }

    // ---- announcements ----

    @Test
    @SuppressWarnings("unchecked")
    public void pastAnnouncementsInterleaveByStartDate() throws IOException {
        writeTaggedNotes();
        _announcements.add(announcement(7, "Maintenance", "Down **tonight**.", LocalDateTime.of(2026, 3, 15, 18, 0)));
        _announcements.add(announcement(9, "Same day", "After the note.", LocalDateTime.of(2026, 4, 1, 9, 30)));
        PatchNotesLibrary library = libraryWithAnnouncements();

        assertEquals(List.of("2026-04-02", "2026-04-01", "announcement-9", "2026-03-20", "announcement-7",
                "2026-03-10", "2026-02-05", "2026-01-15"), slugs(library.getFeed(null)));
        // the notes on their own are unchanged
        assertEquals(6, library.getNotes().size());
        assertEquals(6, library.getCount());
        assertEquals(2, library.getAnnouncementCount());

        Map<String, Object> page = library.pageJson(0, 5, "announcement-7", null, null);
        Map<String, Object> entry = ((List<Map<String, Object>>) page.get("notes")).get(0);
        assertEquals("announcement-7", entry.get("slug"));
        assertEquals("announcement", entry.get("kind"));
        assertEquals("2026-03-15", entry.get("date"));
        assertEquals("Maintenance", entry.get("title"));
        assertNull(entry.get("summary"));
        assertEquals(List.of("Announcement"), entry.get("tags"));
        assertTrue((String) entry.get("html"), ((String) entry.get("html")).contains("<strong>tonight</strong>"));
        assertEquals(8, page.get("all"));

        // the Announcement filter, its months, and a link to one
        assertEquals(List.of("announcement-9", "announcement-7"), slugs(library.getFeed("Announcement")));
        List<Map<String, Object>> filters = (List<Map<String, Object>>) page.get("filters");
        assertEquals(2, filters.get(3).get("count"));
        List<Map<String, Object>> months = (List<Map<String, Object>>) library.monthsJson("Announcement").get("months");
        assertEquals(List.of("announcement-9", "announcement-7"),
                months.stream().map(m -> m.get("first")).collect(Collectors.toList()));
        assertEquals("Same day", library.get("announcement-9").getTitle());
        assertEquals("2026-03-20", ((Map<String, Object>) library.noteJson("announcement-7").get("newer")).get("slug"));
        assertEquals("announcement-9",
                ((Map<String, Object>) library.noteJson("announcement-7", "Announcement").get("newer")).get("slug"));
        assertEquals(0, library.pageJson(0, 5, "ANNOUNCEMENT-9", null, "Announcement").get("start"));
    }

    @Test
    public void announcementsOfPatchNotesAndFutureOnesAreLeftOut() throws IOException {
        write("2026-09-27-hall.md", "---\ntitle: Hall\n---\nx");
        _announcements.add(announcement(1, "A new patch", "<!-- gemp-patchnote:2026-09-27-hall -->\nThe hall...",
                LocalDateTime.of(2026, 9, 27, 0, 0)));
        _announcements.add(announcement(2, "Soon", "Not yet.", NOW.plusMinutes(1)));
        _announcements.add(announcement(3, "Just now", "Started.", NOW));
        _announcements.add(null);
        DBDefs.Announcement noStart = announcement(4, "x", "y", NOW);
        noStart.start = null;
        _announcements.add(noStart);
        PatchNotesLibrary library = libraryWithAnnouncements();

        assertEquals(List.of("2026-09-27-hall", "announcement-3"), slugs(library.getFeed(null)));
        assertNull(library.get("announcement-1"));
        assertNull(library.pageJson(0, 5, "announcement-2", null, null));
    }

    @Test
    public void anAnnouncementWithoutATitleIsHeadedByItsDateAndKeepsItsLineBreaks() {
        _announcements.add(announcement(5, " ", "Line one\nLine two\n\nNew paragraph", NOW.minusDays(1)));
        PatchNotesLibrary library = libraryWithAnnouncements();
        PatchNote entry = library.get("announcement-5");
        assertNull(entry.getTitle());
        String html = entry.getHtml(library.getRenderer());
        assertEquals("<p>Line one<br>Line two</p>\n<p>New paragraph</p>", html.trim());
    }

    @Test
    public void announcementsAreReadAgainAfterTheirTtlOrWhenToldTheyChanged() {
        _announcements.add(announcement(1, "One", "1", NOW.minusDays(3)));
        PatchNotesLibrary library = libraryWithAnnouncements();
        assertEquals(List.of("announcement-1"), slugs(library.getFeed(null)));
        assertEquals(1, _reads.get());
        PatchNote first = library.get("announcement-1");

        _announcements.add(announcement(2, "Two", "2", NOW.minusDays(1)));
        _clock.addAndGet(59_000);
        assertEquals(List.of("announcement-1"), slugs(library.getFeed("Announcement")));
        assertEquals(1, _reads.get());

        _clock.addAndGet(2_000);
        assertEquals(List.of("announcement-2", "announcement-1"), slugs(library.getFeed(null)));
        assertEquals(2, _reads.get());
        // an unchanged announcement keeps its entry (and rendered HTML)
        assertSame(first, library.get("announcement-1"));

        // an edit shows once read again
        _announcements.get(0).content = "1, edited";
        library.announcementsChanged();
        assertEquals("1, edited", library.get("announcement-1").getMarkdown());
        assertEquals(3, _reads.get());

        // Clear Server Cache reads them again too
        _announcements.remove(1);
        library.clearCache();
        assertEquals(List.of("announcement-1"), slugs(library.getFeed(null)));
        assertEquals(4, _reads.get());
    }

    @Test
    public void aFailingSourceLeavesTheNotesAndTriesAgainLater() throws IOException {
        write("2026-09-01.md", "x");
        AtomicReference<RuntimeException> failure = new AtomicReference<>(new RuntimeException("db down"));
        PatchNotesLibrary library = library();
        library.setAnnouncements(() -> {
            _reads.incrementAndGet();
            if (failure.get() != null)
                throw failure.get();
            return List.of(announcement(1, "Back", "up", NOW.minusDays(1)));
        }, 60_000, () -> NOW);

        assertEquals(List.of("2026-09-01"), slugs(library.getFeed(null)));
        failure.set(null);
        _clock.addAndGet(30_000);
        assertEquals(List.of("2026-09-01"), slugs(library.getFeed(null)));
        assertEquals(1, _reads.get());
        _clock.addAndGet(31_000);
        assertEquals(List.of("announcement-1", "2026-09-01"), slugs(library.getFeed(null)));
    }

    @Test
    public void announcementSlugsAreValidLinks() {
        assertTrue(PatchNotesLibrary.SLUG.matcher("announcement-12").matches());
        assertTrue(PatchNotesLibrary.SLUG.matcher("2026-09-27-hall-overhaul").matches());
        assertFalse(PatchNotesLibrary.SLUG.matcher("announcement-").matches());
        assertFalse(PatchNotesLibrary.SLUG.matcher("announcement-x").matches());
        // a file can never be named like an announcement
        assertFalse(PatchNotesLibrary.FILE_NAME.matcher("announcement-1.md").matches());
    }

    @Test
    public void withoutASourceTheFeedIsTheNotes() throws IOException {
        writeTaggedNotes();
        PatchNotesLibrary library = library();
        assertEquals(slugs(library.getNotes()), slugs(library.getFeed(null)));
        assertEquals(0, library.getAnnouncementCount());
    }
}
