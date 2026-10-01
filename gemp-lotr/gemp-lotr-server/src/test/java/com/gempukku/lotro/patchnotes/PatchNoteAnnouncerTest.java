package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.common.DateUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class PatchNoteAnnouncerTest {
    private Path _folder;
    private final AtomicReference<ZonedDateTime> _now = new AtomicReference<>(DateUtils.DateOf(2026, 9, 27).plusHours(18));
    private FakeAnnouncements _dao;

    /** The announcements table as TransferDAO.addServerAnnouncementIfAbsent sees it; every other method fails. */
    static class FakeAnnouncements {
        record Row(int id, String title, String content, ZonedDateTime start, ZonedDateTime until) {
        }

        final List<Row> rows = new ArrayList<>();
        int calls = 0;

        synchronized int addIfAbsent(String marker, String title, String content, ZonedDateTime start, ZonedDateTime until) {
            calls++;
            for (Row row : rows) {
                if (row.content().contains(marker))
                    return -1;
            }
            Row row = new Row(rows.size() + 1, title, content, start, until);
            rows.add(row);
            return row.id();
        }

        synchronized Row bySlug(String slug) {
            for (Row row : rows) {
                if (slug.equals(PatchNoteAnnouncer.markedSlug(row.content())))
                    return row;
            }
            return null;
        }

        /** a TransferDAO backed by this table (a proxy, so methods added to the interface later don't break it) */
        TransferDAO dao() {
            return (TransferDAO) Proxy.newProxyInstance(TransferDAO.class.getClassLoader(), new Class<?>[]{TransferDAO.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("addServerAnnouncementIfAbsent"))
                            return addIfAbsent((String) args[0], (String) args[1], (String) args[2],
                                    (ZonedDateTime) args[3], (ZonedDateTime) args[4]);
                        if (method.getName().equals("toString"))
                            return "FakeAnnouncements";
                        throw new UnsupportedOperationException(method.getName());
                    });
        }
    }

    /** 1_5 is Cleaving Blow; 51_5 its errata; anything else is unknown */
    private static final CardLookup CARDS = reference -> switch (reference) {
        case "1_5", "Cleaving Blow" -> CardLookup.Result.found("1_5", "Cleaving Blow");
        case "51_5" -> CardLookup.Result.found("51_5", "Cleaving Blow");
        case "1_89" -> CardLookup.Result.found("1_89", "Aragorn, Ranger of the North");
        default -> CardLookup.Result.failed("no card " + reference);
    };

    @Before
    public void setUp() throws IOException {
        _folder = Files.createTempDirectory("patchnotes");
        _dao = new FakeAnnouncements();
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
        return new PatchNotesLibrary(_folder.toFile(), 0, System::currentTimeMillis);
    }

    private PatchNoteAnnouncer announcer(PatchNotesLibrary library) {
        return new PatchNoteAnnouncer(library, _dao.dao(), CARDS, _now::get);
    }

    private PatchNoteAnnouncer announcer() {
        return announcer(library());
    }

    private static PatchNote note(String slug, String title, String body) {
        return new PatchNote(slug, LocalDate.parse(slug.substring(0, 10)), title, null, 0, body);
    }

    private static PatchNote note(String slug, String title, String summary, String body) {
        return new PatchNote(slug, LocalDate.parse(slug.substring(0, 10)), title, summary, 0, body);
    }

    private static List<String> lines(String text) {
        return List.of(text.split("\n", -1));
    }

    // ---- the content ----

    @Test
    public void summaryIsTheBody() {
        String body = "### Playing a game\n- A bullet.\n\n![Shot](img/a.png)\n";
        String content = announcer().content(note("2026-09-27-hall-overhaul", "A friendlier Game Hall",
                "New players can start a game straight from the Deck Library.", body));
        assertEquals(List.of(
                "<!-- gemp-patchnote:2026-09-27-hall-overhaul -->",
                "# A friendlier Game Hall",
                "",
                "New players can start a game straight from the Deck Library.",
                "",
                "![Shot](/gemp-lotr/patchnotes/img/a.png)",
                "",
                "**[Read the full patch notes here](#patch-notes/2026-09-27-hall-overhaul)**",
                ""), lines(content));
    }

    @Test
    public void aBlankSummaryFallsBackToTheFirstParagraph() {
        String content = announcer().content(note("2026-09-27", "T", "   ", "First block.\n\nSecond."));
        assertEquals("First block.", lines(content).get(3));
    }

    @Test
    public void summaryCardLinksBecomeNames() {
        assertEquals("Fixed Cleaving Blow, the errata and Aragorn, Ranger of the North.",
                announcer().summaryForAnnouncement("Fixed [[1_5]], [[51_5|the errata]] and [[1_89]]."));
    }

    @Test
    public void summaryIsPlainText() {
        PatchNoteAnnouncer announcer = announcer();
        assertEquals("2 \\* 3 \\<b\\> \\[x\\](y) a\\|b \\~\\~c\\~\\~ \\`code\\` \\\\ snake_case",
                announcer.summaryForAnnouncement("2 * 3 <b> [x](y) a|b ~~c~~ `code` \\ snake_case"));
        assertEquals("\\# Not a heading", announcer.summaryForAnnouncement("# Not a heading"));
        assertEquals("\\- not a list\n1\\. nor this", announcer.summaryForAnnouncement("- not a list\n1. nor this"));
        assertEquals("<p>- not a list<br />1. nor this</p><br/>",
                new MarkdownParser().renderMarkdown(announcer.summaryForAnnouncement("- not a list\n1. nor this"), false));

        // rendered as the popup renders it, it reads exactly as written
        String html = new MarkdownParser().renderMarkdown(
                announcer.summaryForAnnouncement("- 2 * 3 <b>bold?</b> [[1_5]] ~~no~~ snake_case [x](y)"), false);
        assertEquals("<p>- 2 * 3 &lt;b&gt;bold?&lt;/b&gt; Cleaving Blow ~~no~~ snake_case [x](y)</p><br/>", html);
    }

    @Test
    public void contentIsMarkerFirstParagraphFirstImageAndLink() {
        String body = """
                ### Playing a game
                - **Deck Library for everyone.** Pick a deck.
                - Your last-used deck is remembered.

                ![The table-type screen](img/2026-09-27-play-selector.png) ![Casual](img/2026-09-27-play-casual.png)

                ### The Game Hall
                - More things.
                """;
        String content = announcer().content(note("2026-09-27-hall-overhaul", "A friendlier Game Hall", body));
        assertEquals(List.of(
                "<!-- gemp-patchnote:2026-09-27-hall-overhaul -->",
                "# A friendlier Game Hall",
                "",
                "### Playing a game",
                "- **Deck Library for everyone.** Pick a deck.",
                "- Your last-used deck is remembered.",
                "",
                "![The table-type screen](/gemp-lotr/patchnotes/img/2026-09-27-play-selector.png)",
                "",
                "**[Read the full patch notes here](#patch-notes/2026-09-27-hall-overhaul)**",
                ""), lines(content));
    }

    @Test
    public void firstParagraphIsTheFirstBlockUpToABlankLine() {
        assertEquals("### New\n- one\n- two", PatchNoteAnnouncer.firstParagraph("### New\n- one\n- two\n\n### Fixes\n- three"));
        assertEquals("Just one line.", PatchNoteAnnouncer.firstParagraph("Just one line."));
        // Windows line ends, whitespace-only lines count as blank
        assertEquals("a\nb", PatchNoteAnnouncer.firstParagraph("a\r\nb\r\n   \r\nc"));
        assertEquals("", PatchNoteAnnouncer.firstParagraph(""));
        assertEquals("", PatchNoteAnnouncer.firstParagraph(null));
    }

    @Test
    public void leadingBlankImageAndCommentLinesAreSkipped() {
        String body = "\n\n![Banner](img/banner.png)\n<img src=\"img/b2.png\" width=\"500\">\n<!-- draft -->\n\nThe real first paragraph.\nSecond line.\n\nMore.";
        assertEquals("The real first paragraph.\nSecond line.", PatchNoteAnnouncer.firstParagraph(body));

        String content = announcer().content(note("2026-09-27", null, body));
        List<String> lines = lines(content);
        assertEquals("# Patch notes 2026-09-27", lines.get(1));
        assertEquals("The real first paragraph.", lines.get(3));
        // the skipped banner is still the note's first image
        assertTrue(content, content.contains("![Banner](/gemp-lotr/patchnotes/img/banner.png)"));
    }

    @Test
    public void firstImageComesFromAnywhereInTheNote() {
        String body = "Intro without pictures.\n\n### Later\n\n- a\n- b\n\n<p>Look: <img alt=\"Deck builder\" src=\"img/deck.png\"></p>\n\n![Second](img/second.png)";
        String content = announcer().content(note("2026-09-27", "T", body));
        assertTrue(content, content.contains("\n\n![Deck builder](/gemp-lotr/patchnotes/img/deck.png)\n\n"));
        assertFalse(content, content.contains("second.png"));
    }

    @Test
    public void imagesInCodeAreNotImages() {
        String body = "Text.\n\n```\n![not an image](img/code.png)\n```\n\n![Real](https://example.com/real.png)";
        String content = announcer().content(note("2026-09-27", "T", body));
        assertFalse(content, content.contains("code.png"));
        assertTrue(content, content.contains("![Real](https://example.com/real.png)"));
    }

    @Test
    public void noImageMeansNoImageLine() {
        String content = announcer().content(note("2026-09-27", "T", "- Fixed a card."));
        assertEquals(List.of("<!-- gemp-patchnote:2026-09-27 -->", "# T", "", "- Fixed a card.", "",
                "**[Read the full patch notes here](#patch-notes/2026-09-27)**", ""), lines(content));
    }

    @Test
    public void anImageInsideTheParagraphIsNotRepeated() {
        String content = announcer().content(note("2026-09-27", "T", "Look ![shot](img/a.png) here.\n\nMore ![b](img/b.png)"));
        assertTrue(content, content.contains("Look ![shot](/gemp-lotr/patchnotes/img/a.png) here."));
        assertEquals(content, 1, content.split("img/a\\.png", -1).length - 1);
        assertFalse(content, content.contains("img/b.png"));
    }

    @Test
    public void cardLinksBecomePlainNames() {
        String paragraph = announcer().paragraphForAnnouncement(
                "Fixed [[1_5]], [[Cleaving Blow]], [[51_5|the errata]], [[1_89]] and [[1_999]]; kept `code`.");
        assertEquals("Fixed Cleaving Blow, Cleaving Blow, the errata, Aragorn, Ranger of the North and 1_999; kept `code`.",
                paragraph);
    }

    @Test
    public void cardLinksWithoutALibraryKeepTheirText() {
        PatchNoteAnnouncer noCards = new PatchNoteAnnouncer(library(), _dao.dao(), null, _now::get);
        assertEquals("Fixed 1_5 and Cleaving Blow and the errata.",
                noCards.paragraphForAnnouncement("Fixed [[1_5]] and [[Cleaving Blow]] and [[51_5|the errata]]."));
    }

    @Test
    public void htmlIsDroppedAndRelativeLinksMadeAbsolute() {
        String paragraph = announcer().paragraphForAnnouncement(
                "<span style=\"color:red\">New</span> text<br>here, see [the list](img/list.png), [PC-Movie](#format-pc_movie)"
                        + " and [site](https://lotrtcgpc.net/x).");
        assertEquals("New text here, see [the list](/gemp-lotr/patchnotes/img/list.png), [PC-Movie](#format-pc_movie)"
                + " and [site](https://lotrtcgpc.net/x).", paragraph);
    }

    @Test
    public void titleFallsBackToTheDateAndIsCut() {
        PatchNoteAnnouncer announcer = announcer();
        assertEquals("Patch notes 2026-09-27", announcer.announcementFor(note("2026-09-27", null, "x")).title());
        String longTitle = "x".repeat(300);
        String title = announcer.announcementFor(note("2026-09-27", longTitle, "x")).title();
        assertEquals(255, title.length());
        assertTrue(title.endsWith("..."));
    }

    @Test
    public void emojiAreDroppedForTheUtf8Table() {
        PatchNoteAnnouncer.Announcement a = announcer().announcementFor(note("2026-09-27", "Party \uD83C\uDF89 time", "Cake \uD83C\uDF82 and \u24D8 info."));
        assertEquals("Party  time", a.title());
        assertTrue(a.content(), a.content().contains("Cake  and \u24D8 info."));
    }

    @Test
    public void windowIsTheNotesDateAtMidnightUtcForFourteenDays() {
        PatchNoteAnnouncer.Announcement a = announcer().announcementFor(note("2026-09-20-events-calendar", "T", "x"));
        assertEquals(DateUtils.DateOf(2026, 9, 20), a.start());
        assertEquals(DateUtils.DateOf(2026, 10, 4), a.until());
        assertEquals("<!-- gemp-patchnote:2026-09-20-events-calendar -->", a.marker());
    }

    // ---- the marker ----

    @Test
    public void markerIsFoundAndStripped() {
        String content = "<!-- gemp-patchnote:2026-09-27-hall-overhaul -->\nText.\n";
        assertEquals("2026-09-27-hall-overhaul", PatchNoteAnnouncer.markedSlug(content));
        assertEquals("Text.\n", PatchNoteAnnouncer.stripMarker(content));
        assertEquals("Text.", PatchNoteAnnouncer.stripMarker("<!-- gemp-patchnote:2026-09-27 -->\r\nText."));
        assertNull(PatchNoteAnnouncer.markedSlug("An admin announcement.\n<!-- gemp-patchnote:2026-09-27 -->"));
        assertEquals("An admin announcement.", PatchNoteAnnouncer.stripMarker("An admin announcement."));
        assertNull(PatchNoteAnnouncer.stripMarker(null));
    }

    /** What the popup shows: the announcement rendered as every announcement is (CachedTransferDAO). */
    @Test
    public void renderedAnnouncementShowsNoMarkerAndLinksToTheNote() {
        String body = "### Fixes\n- Fixed [[1_5]] and [[51_5|its errata]].\n- A_card_with_underscores.\n\n![Shot](img/2026-09-27_shot.png)";
        String content = announcer().content(note("2026-09-27-fixes", "Fixes", body));
        String html = new MarkdownParser().renderMarkdown(PatchNoteAnnouncer.stripMarker(content), false);
        assertFalse(html, html.contains("gemp-patchnote"));
        assertFalse(html, html.contains("&lt;!--"));
        assertFalse(html, html.contains("[["));
        assertTrue(html, html.contains("Fixed Cleaving Blow and its errata."));
        assertTrue(html, html.contains("<img src=\"/gemp-lotr/patchnotes/img/2026-09-27_shot.png\" alt=\"Shot\" />"));
        assertTrue(html, html.matches("(?s).*<strong><a [^>]*href=\"#patch-notes/2026-09-27-fixes\"[^>]*>Read the full patch notes here</a></strong>.*"));
        assertTrue(html, html.contains("<h3>Fixes</h3>"));
    }

    @Test
    public void theTitleIsRepeatedAsAHeadingAboveTheSummary() {
        String content = announcer().content(note("2026-09-27-x", "Pre-World Championship errata", "Eight cards change.", "x"));
        List<String> lines = lines(content);
        assertEquals("<!-- gemp-patchnote:2026-09-27-x -->", lines.get(0));
        assertEquals("# Pre-World Championship errata", lines.get(1));
        assertEquals("", lines.get(2));
        assertEquals("Eight cards change.", lines.get(3));
        String html = new MarkdownParser().renderMarkdown(PatchNoteAnnouncer.stripMarker(content), false);
        assertTrue(html, html.startsWith("<h1>Pre-World Championship errata</h1>"));
        assertFalse(html, html.contains("gemp-patchnote"));
    }

    @Test
    public void theHeadingIsEscapedSoTheTitleReadsAsWritten() {
        String title = "Fixes *and* [more](x) #1 & <b>bold</b> `code` snake_case ~~no~~ | !img #";
        String heading = PatchNoteAnnouncer.headingText(title);
        String html = new MarkdownParser().renderMarkdown("# " + heading, false);
        assertEquals("<h1>Fixes *and* [more](x) #1 &amp; &lt;b&gt;bold&lt;/b&gt; `code` snake_case ~~no~~ | !img #</h1><br/>", html);
        // one line, whatever the title holds
        assertEquals("a b", PatchNoteAnnouncer.headingText(" a\n b "));
        // with no title, the date
        String content = announcer().content(note("2026-09-27", null, "x"));
        assertEquals("# Patch notes 2026-09-27", lines(content).get(1));
    }

    @Test
    public void thePopupShowsTheHeadingAndACultureIcon() {
        String content = announcer().content(note("2026-09-27-errata", "Pre-WC errata",
                "Adding a [dunland] token now needs 25 twilight.", "x"));
        String html = new MarkdownParser().renderMarkdown(PatchNoteAnnouncer.stripMarker(content), false);
        assertTrue(html, html.startsWith("<h1>Pre-WC errata</h1>"));
        assertTrue(html, html.contains("Adding a <img src=\"/gemp-lotr/images/cultures/dunland.png\" alt=\"Dunland\" title=\"Dunland\" /> token now needs 25 twilight."));
    }

    // ---- which notes are announced ----

    @Test
    public void announcesOnlyNotesWhoseWindowHasNotEnded() throws IOException {
        write("2026-09-13.md", "---\ntitle: Fourteen days ago\n---\nOld.");          // ended exactly at 09-27 00:00
        write("2026-09-14.md", "---\ntitle: Thirteen days ago\n---\nStill running.");
        write("2026-09-27-today.md", "---\ntitle: Today\n---\nNew.");
        write("2026-10-01-soon.md", "---\ntitle: Soon\n---\nLater.");
        write("2023-12-09.md", "Ancient.");

        assertEquals(3, announcer().check());
        assertNull(_dao.bySlug("2026-09-13"));
        assertNull(_dao.bySlug("2023-12-09"));
        assertEquals("Thirteen days ago", _dao.bySlug("2026-09-14").title());
        assertEquals(DateUtils.DateOf(2026, 9, 28), _dao.bySlug("2026-09-14").until());
        assertEquals("Today", _dao.bySlug("2026-09-27-today").title());
        // a note dated ahead is announced from its date on
        assertEquals(DateUtils.DateOf(2026, 10, 1), _dao.bySlug("2026-10-01-soon").start());
    }

    @Test
    public void ofTwoNotesOnTheSameDayTheOneListedFirstGetsTheHigherId() throws IOException {
        // the hall shows the running announcement with the latest start, ties going to the higher id
        write("2026-09-27-a.md", "---\ntitle: Listed second\n---\nA.");
        write("2026-09-27-b.md", "---\ntitle: Listed first\norder: 1\n---\nB.");
        write("2026-09-20.md", "---\ntitle: Last week\n---\nC.");

        assertEquals(3, announcer().check());
        assertTrue(_dao.bySlug("2026-09-27-b").id() > _dao.bySlug("2026-09-27-a").id());
        assertTrue(_dao.bySlug("2026-09-27-a").id() > _dao.bySlug("2026-09-20").id());
    }

    @Test
    public void checkingAgainCreatesNothingNew() throws IOException {
        write("2026-09-27.md", "---\ntitle: Today\n---\nNew.");
        PatchNotesLibrary library = library();
        PatchNoteAnnouncer announcer = announcer(library);
        assertEquals(1, announcer.check());
        assertEquals(0, announcer.check());
        assertEquals(1, _dao.calls);             // this process remembers it is done

        // another server process (or a restart) on the same database: the marker says it exists
        assertEquals(0, announcer(library).check());
        assertEquals(1, _dao.rows.size());
    }

    @Test
    public void aNoteAddedLaterIsAnnouncedOnTheNextCheck() throws IOException {
        PatchNotesLibrary library = library();
        PatchNoteAnnouncer announcer = announcer(library);
        assertEquals(0, announcer.check());
        write("2026-09-27-hotfix.md", "---\ntitle: Hotfix\n---\n- Fixed a crash.");
        assertEquals(1, announcer.check());
        assertEquals("Hotfix", _dao.bySlug("2026-09-27-hotfix").title());
    }

    @Test
    public void aDeletedAnnouncementComesBackOnlyForANewProcess() throws IOException {
        write("2026-09-27.md", "---\ntitle: Today\n---\nNew.");
        PatchNotesLibrary library = library();
        PatchNoteAnnouncer announcer = announcer(library);
        announcer.check();
        _dao.rows.clear();                        // an admin deletes it
        assertEquals(0, announcer.check());       // this process does not ask again
        assertEquals(1, announcer(library).check());   // a restart recreates it while it is inside its window
    }

    @Test
    public void aFailedInsertIsRetriedNextTime() throws IOException {
        write("2026-09-27.md", "---\ntitle: Today\n---\nNew.");
        boolean[] fail = {true};
        TransferDAO flaky = (TransferDAO) Proxy.newProxyInstance(TransferDAO.class.getClassLoader(), new Class<?>[]{TransferDAO.class},
                (proxy, method, args) -> {
                    if (fail[0])
                        throw new RuntimeException("database down");
                    return _dao.dao().addServerAnnouncementIfAbsent((String) args[0], (String) args[1], (String) args[2],
                            (ZonedDateTime) args[3], (ZonedDateTime) args[4]);
                });
        PatchNoteAnnouncer announcer = new PatchNoteAnnouncer(library(), flaky, CARDS, _now::get);
        assertEquals(0, announcer.check());
        fail[0] = false;
        assertEquals(1, announcer.check());
    }

    @Test
    public void twoProcessesCheckingAtOnceCreateOneAnnouncement() throws Exception {
        write("2026-09-27.md", "---\ntitle: Today\n---\nNew.");
        write("2026-09-20.md", "---\ntitle: Last week\n---\nOlder.");
        int processes = 8;
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < processes; i++) {
            PatchNoteAnnouncer announcer = announcer(library());
            Thread thread = new Thread(() -> {
                try {
                    go.await();
                } catch (InterruptedException ignored) {
                }
                announcer.check();
            });
            thread.start();
            threads.add(thread);
        }
        go.countDown();
        for (Thread thread : threads)
            thread.join();
        assertEquals(2, _dao.rows.size());
    }

    // ---- the real notes ----

    @Test
    public void theShippedNotesAnnounceSensibly() {
        File folder = new File("../gemp-lotr-async/src/main/web/patchnotes");
        Assume.assumeTrue(new File(folder, "2026-09-27-hall-overhaul.md").isFile());
        PatchNotesLibrary library = new PatchNotesLibrary(folder, 0, System::currentTimeMillis);
        PatchNoteAnnouncer announcer = announcer(library);

        PatchNote hall = library.get("2026-09-27-hall-overhaul");
        String content = announcer.content(hall);
        List<String> lines = lines(content);
        assertEquals("<!-- gemp-patchnote:2026-09-27-hall-overhaul -->", lines.get(0));
        assertEquals("# " + PatchNoteAnnouncer.headingText(PatchNoteAnnouncer.title(hall)), lines.get(1));
        assertEquals(hall.getSummary(), lines.get(3));   // nothing in it needs escaping
        // (the notes' wording and pictures get edited, so only the shape is pinned here)
        assertFalse(content, content.contains("\n### "));
        assertTrue(content, content.contains("](/gemp-lotr/patchnotes/img/2026-09-27-"));
        assertTrue(content, content.endsWith("**[Read the full patch notes here](#patch-notes/2026-09-27-hall-overhaul)**\n"));

        String calendar = announcer.content(library.get("2026-09-20-events-calendar"));
        assertTrue(calendar, calendar.contains("\n" + library.get("2026-09-20-events-calendar").getSummary() + "\n"));
        assertFalse(calendar, calendar.contains("\n### "));
        assertTrue(calendar, calendar.contains("](/gemp-lotr/patchnotes/img/2026-09-20-"));

        // on 2026-09-27 the September updates are inside their window and nothing older is (later-dated notes may
        // be added to the folder, so this doesn't pin an exact count)
        int created = announcer.check();
        assertEquals(created, _dao.rows.size());
        List<String> contents = _dao.rows.stream().map(r -> r.content()).toList();
        assertTrue(contents.stream().anyMatch(c -> c.startsWith("<!-- gemp-patchnote:2026-09-27-hall-overhaul -->")));
        assertTrue(contents.stream().anyMatch(c -> c.startsWith("<!-- gemp-patchnote:2026-09-20-events-calendar -->")));
        assertTrue(contents.toString(), contents.stream().noneMatch(c -> c.matches("(?s)<!-- gemp-patchnote:(20[01]\\d|202[0-5]|2026-0[1-8]).*")));
    }
}
