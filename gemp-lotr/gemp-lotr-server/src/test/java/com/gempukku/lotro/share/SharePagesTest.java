package com.gempukku.lotro.share;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.patchnotes.LibraryCardLookup;
import com.gempukku.lotro.patchnotes.PatchNotesLibrary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SharePagesTest {
    private static final String ORIGIN = "https://play.lotrtcgpc.net";
    private static final File WEB = new File("../gemp-lotr-async/src/main/web");

    private static LotroCardBlueprintLibrary _cards;
    private static Path _notes;
    private static SharePages _pages;

    @BeforeClass
    public static void setUp() throws IOException {
        _cards = new LotroCardBlueprintLibrary();
        _notes = Files.createTempDirectory("share-notes");
        Files.writeString(_notes.resolve("2026-09-27-hall-overhaul.md"), """
                ---
                title: A friendlier Game Hall
                summary: New players can start a game in two clicks.
                ---
                ### New
                - Tables list [[Cleaving Blow]] first.

                ![The hall](img/2026-09-27-hall.png) ![Second](img/second.png)
                """, StandardCharsets.UTF_8);
        Files.writeString(_notes.resolve("2023-12-09.md"), "- Fixed <b>Asfaloth</b> & friends.\n- Another fix.\n",
                StandardCharsets.UTF_8);
        Files.writeString(_notes.resolve("2023-12-10-evil.md"), """
                ---
                title: "Evil \\"title\\" </title><script>alert(1)</script>"
                summary: <img src=x onerror=alert(2)> & more
                ---
                Body.
                """, StandardCharsets.UTF_8);
        PatchNotesLibrary patchNotes = new PatchNotesLibrary(_notes.toFile(), 0, System::currentTimeMillis,
                new LibraryCardLookup(_cards));
        // a past server announcement is in the feed as announcement-<id> (PatchNotesFeedTest)
        DBDefs.Announcement announcement = new DBDefs.Announcement();
        announcement.id = 4;
        announcement.title = "";
        announcement.content = "Servers restart **tonight**.\n\nSorry!";
        announcement.start = LocalDateTime.of(2025, 5, 1, 12, 0);
        announcement.until = announcement.start.plusDays(3);
        patchNotes.setAnnouncements(() -> List.of(announcement), 60_000, LocalDateTime::now);

        LotroFormatLibrary formats = mock(LotroFormatLibrary.class);
        LotroFormat movie = mock(LotroFormat.class);
        when(movie.getName()).thenReturn("Movie Block (PC)");
        when(movie.getCode()).thenReturn("pc_movie");
        when(formats.getFormat("pc_movie")).thenReturn(movie);
        when(formats.getFormatByName("Movie Block (PC)")).thenReturn(movie);

        LotroDeck deck = new LotroDeck("Elves & <Friends>");
        deck.setRingBearer("1_290");
        deck.setRing("1_2");
        deck.addCard("1_5");
        deck.addCard("1_5");
        deck.setTargetFormat("Movie Block (PC)");
        SharePages.DeckSource decks = (owner, name) -> owner.equals("ketura") && name.equals("Elves & <Friends>") ? deck : null;

        _pages = new SharePages(patchNotes, _cards, formats, new CardImages(WEB.isDirectory() ? WEB : null), decks);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        try (Stream<Path> paths = Files.walk(_notes)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private static Document html(SharePages.Page page, String shareUrl) {
        return Jsoup.parse(SharePages.html(page, ORIGIN, shareUrl));
    }

    private static String meta(Document doc, String key) {
        var element = doc.selectFirst("meta[property=" + key + "], meta[name=" + key + "]");
        return element == null ? null : element.attr("content");
    }

    // ---- patch notes ----

    @Test
    public void aTitledNoteWithScreenshots() {
        SharePages.Page page = _pages.page("patch-notes", "2026-09-27-hall-overhaul");
        assertEquals("A friendlier Game Hall", page.title());
        assertEquals("2026-09-27: New players can start a game in two clicks.", page.description());
        assertEquals("patchnotes/img/2026-09-27-hall.png", page.image());   // the first screenshot
        assertEquals("hall.html#patch-notes/2026-09-27-hall-overhaul", page.target());
        assertTrue(page.largeImage());

        Document doc = html(page, ORIGIN + "/gemp-lotr/share/patch-notes/2026-09-27-hall-overhaul");
        assertEquals("A friendlier Game Hall · GEMP", doc.title());
        assertEquals("A friendlier Game Hall", meta(doc, "og:title"));
        assertEquals(ORIGIN + "/gemp-lotr/patchnotes/img/2026-09-27-hall.png", meta(doc, "og:image"));
        assertEquals(ORIGIN + "/gemp-lotr/share/patch-notes/2026-09-27-hall-overhaul", meta(doc, "og:url"));
        assertEquals("summary_large_image", meta(doc, "twitter:card"));
        assertEquals("GEMP", meta(doc, "og:site_name"));
        assertEquals("0; url=/gemp-lotr/hall.html#patch-notes/2026-09-27-hall-overhaul",
                doc.selectFirst("meta[http-equiv=refresh]").attr("content"));
        assertEquals("window.location.replace(\"/gemp-lotr/hall.html#patch-notes/2026-09-27-hall-overhaul\");",
                doc.selectFirst("script").data());
        assertEquals("/gemp-lotr/hall.html#patch-notes/2026-09-27-hall-overhaul", doc.selectFirst("body a").attr("href"));
    }

    @Test
    public void anUntitledNoteUsesItsDateAndFirstLines() {
        SharePages.Page page = _pages.page("patch-notes", "2023-12-09");
        assertEquals("GEMP update of 2023-12-09", page.title());
        assertEquals("Fixed Asfaloth & friends. Another fix.", page.description());
        assertEquals(SharePages.DEFAULT_IMAGE, page.image());
        assertEquals(ORIGIN + "/gemp-lotr/images/splash.jpg", meta(html(page, "u"), "og:image"));
        // names are case-insensitive, like the page's own links
        assertEquals("hall.html#patch-notes/2023-12-09", _pages.page("patch-notes", "2023-12-09").target());
    }

    @Test
    public void anAnnouncementInTheFeed() {
        SharePages.Page page = _pages.page("patch-notes", "Announcement-4");
        assertEquals("GEMP announcement of 2025-05-01", page.title());
        assertEquals("Servers restart tonight. Sorry!", page.description());
        assertEquals("hall.html#patch-notes/announcement-4", page.target());
    }

    @Test
    public void theNewestNotesAndUnknownOnes() {
        assertEquals("hall.html#patch-notes", _pages.page("patch-notes", "").target());
        SharePages.Page gone = _pages.page("patch-notes", "2001-01-01");
        assertEquals("GEMP patch notes", gone.title());
        assertEquals("hall.html#patch-notes/2001-01-01", gone.target());   // the page says it is not there
        assertNull(_pages.page("patch-notes", "not a slug"));
        assertNull(_pages.page("patch-notes", "../../etc/passwd"));
    }

    @Test
    public void everythingIsEscaped() {
        SharePages.Page page = _pages.page("patch-notes", "2023-12-10-evil");
        String html = SharePages.html(page, ORIGIN, ORIGIN + "/gemp-lotr/share/patch-notes/2023-12-10-evil\"><script>");
        assertFalse(html, html.contains("<script>alert"));
        assertFalse(html, html.contains("<img"));
        assertFalse(html, html.contains("\"><script>"));
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("script").size());
        assertEquals("Evil \"title\" </title><script>alert(1)</script>", meta(doc, "og:title"));
        assertTrue(meta(doc, "og:description"), meta(doc, "og:description").contains("<img src=x onerror=alert(2)> & more"));
        assertEquals(1, doc.select("title").size());
    }

    @Test
    public void theRedirectScriptCannotBeBrokenOutOf() {
        assertEquals("\"/a\\u003c/script\\u003e\\u0022\\u005c\\u2028\"", SharePages.jsString("/a</script>\"\\ "));
    }

    // ---- errata, formats, cards ----

    @Test
    public void errataByOriginalOrErrataId() {
        SharePages.Page page = _pages.page("errata", "1_5");
        assertEquals("PC Errata: Cleaving Blow", page.title());
        assertTrue(page.description(), page.description().startsWith("Errata of Cleaving Blow (1C5), old and new side by side. Now: "));
        assertTrue(page.description(), page.description().length() <= SharePages.DESCRIPTION_LENGTH + 1);
        assertEquals("hall.html#errata-1_5", page.target());
        if (WEB.isDirectory())
            assertEquals("https://i.lotrtcgpc.net/errata/LOTR-EN01E005.1_card.jpg", page.image());   // PC_Cards.js
        assertFalse(page.largeImage());
        assertEquals("summary", meta(html(page, "u"), "twitter:card"));

        SharePages.Page errata = _pages.page("errata", "51_5");
        assertEquals("PC Errata: Cleaving Blow", errata.title());
        assertEquals("hall.html#errata-51_5", errata.target());
        assertEquals(page.image(), errata.image());

        assertEquals("PC Errata", _pages.page("errata", "99_999").title());
        assertNull(_pages.page("errata", "1_5<script>"));
    }

    @Test
    public void formats() {
        SharePages.Page page = _pages.page("format", "pc_movie");
        assertEquals("Movie Block (PC) — Format Definitions", page.title());
        assertEquals("hall.html#format-pc_movie", page.target());
        assertEquals("Format Definitions", _pages.page("format", "nope").title());
        assertEquals("hall.html#format-nope", _pages.page("format", "nope").target());
        assertNull(_pages.page("format", "a b"));
    }

    @Test
    public void cards() {
        SharePages.Page page = _pages.page("card", "1_5");
        assertEquals("Cleaving Blow", page.title());
        assertTrue(page.description(), page.description().startsWith("1C5. "));
        assertEquals("https://i.lotrtcgpc.net/decipher/LOTR01005.jpg", page.image());
        assertEquals("hall.html#card-1_5", page.target());
        assertEquals("Aragorn, Ranger of the North", _pages.page("card", "1_89").title());
        // a card with no picture anywhere: the site's
        assertEquals("A GEMP card", _pages.page("card", "99_999").title());
        assertNull(_pages.page("card", "Cleaving Blow"));
        assertNull(_pages.page("nope", "1_5"));
        assertNull(_pages.page(null, "1_5"));
    }

    // ---- decks ----

    private static String code(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void aSharedDeck() {
        String code = code("ketura|Elves & <Friends>");
        SharePages.Page page = _pages.page("deck", code);
        assertEquals("Elves & <Friends>", page.title());
        assertTrue(page.description(), page.description().startsWith("A deck by ketura for Movie Block (PC), with "));
        assertTrue(page.description(), page.description().endsWith(" as Ring-bearer. 2 cards in the draw deck."));
        assertEquals("/gemp-lotr-server/deck/html?id=" + java.net.URLEncoder.encode(code, StandardCharsets.UTF_8), page.target());
        assertTrue(page.image(), page.image().startsWith("https://i.lotrtcgpc.net/"));
        Document doc = html(page, "u");
        assertEquals("Elves & <Friends>", meta(doc, "og:title"));
        assertEquals(page.target(), doc.selectFirst("body a").attr("href"));
    }

    @Test
    public void unknownAndMalformedDecks() {
        assertEquals("A GEMP deck", _pages.page("deck", code("ketura|Nothing")).title());
        assertNull(_pages.page("deck", code("no bar here")));
        assertNull(_pages.page("deck", code("a|b|c")));
        assertNull(_pages.page("deck", "***"));
        assertNull(_pages.page("deck", ""));
    }

    @Test
    public void descriptionsAreShortenedAtAWord() {
        String text = "word ".repeat(100);
        String shortened = SharePages.shorten(text);
        assertTrue(shortened.length() <= SharePages.DESCRIPTION_LENGTH);
        assertTrue(shortened.endsWith("word…"));
        assertEquals("a b", SharePages.shorten("  a \n b "));
    }
}
