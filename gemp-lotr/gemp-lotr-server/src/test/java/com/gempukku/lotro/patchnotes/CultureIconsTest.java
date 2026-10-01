package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.Culture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** :isengard: / [isengard] culture icons: the renderer, the page's summary, share previews and the announcement. */
public class CultureIconsTest {
    private static final File WEB = new File("../gemp-lotr-async/src/main/web");
    private final PatchNoteRenderer _renderer = new PatchNoteRenderer(
            reference -> reference.equals("Isengard Warrior") ? CardLookup.Result.found("1_9", "Isengard Warrior")
                    : CardLookup.Result.failed("no card is named '" + reference + "'"));

    private Document render(String markdown) {
        return Jsoup.parseBodyFragment(_renderer.render(markdown));
    }

    private static List<String> icons(Document doc) {
        return doc.select("img.patchnote-culture").stream().map(img -> img.attr("alt")).collect(Collectors.toList());
    }

    @Test
    public void bothSyntaxesAnyCaseAndAliases() {
        Document doc = render("Adds a [dunland] token, :Isengard: and :URUK-HAI: minions, [ringwraith] and :elf: and [Dwarves].");
        assertEquals(List.of("Dunland", "Isengard", "Uruk-hai", "Ringwraith", "Elven", "Dwarven"), icons(doc));
        assertEquals("Adds a token, and minions, and and .", doc.select("p").text());   // (jsoup joins the spaces)
        assertTrue(doc.html(), doc.select("p").html().startsWith("Adds a <img class=\"patchnote-culture\" src=\"images/cultures/dunland.png\""));
    }

    @Test
    public void theIconIsThePcErrataOneWithItsNameAsAltAndTitle() {
        Element img = render("An :uruk: minion").selectFirst("img");
        assertEquals("images/cultures/uruk_hai.png", img.attr("src"));
        assertEquals("Uruk-hai", img.attr("alt"));
        assertEquals("Uruk-hai", img.attr("title"));
        assertEquals("patchnote-culture", img.className());
        // exactly this markup, next to the text around it
        assertEquals("<p>An <img class=\"patchnote-culture\" src=\"images/cultures/uruk_hai.png\" alt=\"Uruk-hai\" title=\"Uruk-hai\"> minion</p>",
                _renderer.render("An :uruk: minion").trim());
    }

    @Test
    public void everyCultureWithAnIconIsCoveredAndEveryIconExists() {
        Set<String> codes = new HashSet<>();
        for (CultureIcons.Icon icon : CultureIcons.ALIASES.values())
            codes.add(icon.code());
        for (Culture culture : Culture.values()) {
            if (culture == Culture.FALLEN_REALMS)
                continue;      // no icon
            assertTrue(culture.name(), codes.contains(culture.name().toLowerCase()));
            assertNotNull(culture.name(), CultureIcons.icon(culture.getHumanReadable()));
        }
        if (WEB.isDirectory()) {
            for (String code : codes)
                assertTrue(code, new File(WEB, "images/cultures/" + code + ".png").isFile());
        }
        // every alias is lower case and names one icon
        for (Map.Entry<String, CultureIcons.Icon> alias : CultureIcons.ALIASES.entrySet())
            assertEquals(alias.getKey().toLowerCase(), alias.getKey());
    }

    @Test
    public void unknownNamesStayAsWritten() {
        Document doc = render("Time 10:30: see :hobbit: and [note] and :fallen realms: [x]");
        assertTrue(icons(doc).isEmpty());
        assertEquals("Time 10:30: see :hobbit: and [note] and :fallen realms: [x]", doc.select("p").text());
    }

    @Test
    public void notInCodeCardLinksLinkTextUrlsOrWords() {
        String markdown = """
                `:isengard:` and `[gondor]`

                ```
                [rohan] :shire:
                ```

                [[Isengard Warrior]] and [[:isengard:]] and [:moria:](#pc-errata) and [[sauron]](#x) and [elven][ref]

                [dwarven][nope] and ![men](img/men.png) and https://example.org/:isengard:/x and <https://x.org/[gondor]>

                word:men: and a[orc] and x [raider]: text

                [ref]: #events
                """;
        Document doc = render(markdown);
        assertTrue(doc.html(), icons(doc).isEmpty());
        assertEquals(":isengard:", doc.select("code").first().text());
        assertTrue(doc.select("pre").text().contains("[rohan] :shire:"));
        assertEquals("Isengard Warrior", doc.select("span.patchnote-card").text());
        assertEquals(":moria:", doc.select("a[href=#pc-errata]").text());
        assertTrue(doc.text(), doc.text().contains("[dwarven][nope]"));
    }

    @Test
    public void worksInHeadingsListsAndEmphasis() {
        Document doc = render("### :gondor: fixes\n- **[rohan]** and *:shire:*\n\n> :sauron: quote");
        assertEquals(1, doc.select("h3 img.patchnote-culture").size());
        assertEquals(1, doc.select("li strong img.patchnote-culture").size());
        assertEquals(1, doc.select("li em img.patchnote-culture").size());
        assertEquals(1, doc.select("blockquote img.patchnote-culture").size());
    }

    @Test
    public void adjacentTokensAndPunctuation() {
        assertEquals(List.of("Gondor", "Rohan", "Shire", "Elven"), icons(render(":gondor::rohan: ([shire]), :elven:.")));
    }

    @Test
    public void htmlInTheNoteCannotForgeAnIcon() {
        // a hand-written marker span is not a token: only the renderer's own become icons, and only known names
        Document doc = render("<span data-culture=\"x\"></span> <span data-culture=\"isengard\">text</span>"
                + " <span data-culture=\"isengard:0\">more</span>");
        assertTrue(doc.html(), doc.select("img").isEmpty());
        assertEquals("text more", doc.text());
    }

    @Test
    public void theSummaryGetsIconsEscaped() {
        PatchNote note = new PatchNote("2026-09-27", LocalDate.of(2026, 9, 27), "T",
                "<b>Bold?</b> & a [dunland] token", 0, "x");
        Map<String, Object> json = note.toJson(_renderer);
        assertEquals("&lt;b&gt;Bold?&lt;/b&gt; &amp; a <img class=\"patchnote-culture\" src=\"images/cultures/dunland.png\" alt=\"Dunland\" title=\"Dunland\"> token",
                json.get("summaryHtml"));
        PatchNote plain = new PatchNote("2026-09-28", LocalDate.of(2026, 9, 28), "T", "No tokens [[1_5]]", 0, "x");
        assertNull(plain.toJson(_renderer).get("summaryHtml"));
        assertEquals("a Dunland token", CultureIcons.plainText("a [dunland] token"));
    }

    @Test
    public void announcementsInTheFeedGetIconsToo() {
        String html = _renderer.render("Maintenance for :isengard: players\nsecond line", p -> {
        }, true);
        assertTrue(html, html.contains("alt=\"Isengard\"") && html.contains("<br>"));
    }

    @Test
    public void theShippedPreWcNoteShowsTheDunlandToken() {
        File folder = new File(WEB, "patchnotes");
        org.junit.Assume.assumeTrue(new File(folder, "2026-09-27-pre-wc-errata.md").isFile());
        PatchNotesLibrary library = new PatchNotesLibrary(folder, 0, System::currentTimeMillis);
        Document doc = Jsoup.parseBodyFragment(library.get("2026-09-27-pre-wc-errata").getHtml(library.getRenderer()));
        Element icon = doc.selectFirst("li img.patchnote-culture");
        assertNotNull(doc.html(), icon);
        assertEquals("Dunland", icon.attr("alt"));
        assertTrue(icon.parent().text(), icon.parent().text().contains("adding a token needs 25 twilight tokens"));
    }

    // ---- twilight icons ----

    @Test
    public void twilightCostsInBothSyntaxes() {
        Document doc = render("Remove (1) to add (X); :3twilight:, :0TWILIGHT: and :xTwilight: (9).");
        assertEquals(List.of("(1) twilight", "(X) twilight", "(3) twilight", "(0) twilight", "(X) twilight", "(9) twilight"),
                icons(doc));
        assertEquals("<p>Remove <img class=\"patchnote-culture\" src=\"images/cultures/twilight_1.svg\" alt=\"(1) twilight\""
                        + " title=\"(1) twilight\"> to play</p>",
                _renderer.render("Remove (1) to play").trim());
        assertEquals("images/cultures/twilight_x.svg", render("(X)").selectFirst("img").attr("src"));
    }

    @Test
    public void twilightLookalikesStayText() {
        String text = "(x) lower case, (10), (2023), (25) twilight, f(1), (1)st, (Y), :10twilight: and :ytwilight: and :1twilights:";
        Document doc = render(text);
        assertTrue(doc.html(), icons(doc).isEmpty());
        assertEquals(text, doc.select("p").text());
    }

    @Test
    public void twilightNotInCodeCardLinksLinkTextOrUrls() {
        String markdown = """
                `(1)` and `:2twilight:`

                [[Isengard Warrior|costs (1)]] and [(3)](#pc-errata) and [x](4) and ![(5)](img/a.png)

                https://example.org/(6)/x and <https://x.org/(7)>
                """;
        Document doc = render(markdown);
        assertTrue(doc.html(), icons(doc).isEmpty());
        assertEquals("(1)", doc.select("code").first().text());
        assertEquals("costs (1)", doc.select("span.patchnote-card").text());
        assertEquals("(3)", doc.select("a[href=#pc-errata]").text());
    }

    @Test
    public void twilightAndCultureTokensTogether() {
        assertEquals(List.of("Orc", "(2) twilight", "Isengard"),
                icons(render("Each :orc: minion costs (2) more, [isengard] too.")));
    }

    @Test
    public void everyTwilightIconExistsAndHandWrittenMarkersAreNotIcons() {
        assertEquals(11, CultureIcons.TWILIGHT.size());
        if (WEB.isDirectory()) {
            for (CultureIcons.Icon icon : CultureIcons.TWILIGHT.values())
                assertTrue(icon.src(), new File(WEB, icon.src()).isFile());
        }
        assertEquals("(4) twilight", CultureIcons.byCode("twilight_4").name());
        assertNull(CultureIcons.byCode("twilight_X"));
        assertNull(CultureIcons.byCode("twilight_10"));
        assertEquals("Isengard", CultureIcons.byCode("isengard").name());
        // no :name: or [name] spelling reaches a twilight icon
        assertNull(CultureIcons.icon("twilight_1"));
        assertTrue(icons(render(":twilight_x: [twilight_x] <span data-culture=\"twilight_1\">t</span>")).isEmpty());
    }

    @Test
    public void theSummaryAndSharePreviewGetTwilightToo() {
        PatchNote note = new PatchNote("2026-09-27", LocalDate.of(2026, 9, 27), "T", "Remove (1) & draw", 0, "x");
        assertEquals("Remove <img class=\"patchnote-culture\" src=\"images/cultures/twilight_1.svg\" alt=\"(1) twilight\""
                + " title=\"(1) twilight\"> &amp; draw", note.toJson(_renderer).get("summaryHtml"));
        assertEquals("Remove (1) twilight & draw", CultureIcons.plainText("Remove (1) & draw"));
    }

    @Test
    public void thePopupShowsTwilightIcons() {
        String summary = _announcer.summaryForAnnouncement("Remove (1) and add :xtwilight:, not (10)");
        assertEquals("Remove ![(1) twilight](/gemp-lotr/images/cultures/twilight_1.svg \"(1) twilight\") and add "
                + "![(X) twilight](/gemp-lotr/images/cultures/twilight_x.svg \"(X) twilight\"), not (10)", summary);
        String html = new MarkdownParser().renderMarkdown(summary, false);
        assertTrue(html, html.contains("<img src=\"/gemp-lotr/images/cultures/twilight_1.svg\" alt=\"(1) twilight\" title=\"(1) twilight\" />"));
        assertTrue(html, html.contains("<img src=\"/gemp-lotr/images/cultures/twilight_x.svg\" alt=\"(X) twilight\" title=\"(X) twilight\" />"));

        String paragraph = _announcer.paragraphForAnnouncement(
                "- Now removes (1), not `(2)`, [[Isengard Warrior|(3) guy]], [(4)](#x) or https://x.org/(5)");
        assertEquals("- Now removes ![(1) twilight](/gemp-lotr/images/cultures/twilight_1.svg \"(1) twilight\"), not `(2)`, "
                + "(3) guy, [(4)](#x) or https://x.org/(5)", paragraph);
    }

    // ---- the popup ----

    private final PatchNoteAnnouncer _announcer = new PatchNoteAnnouncer(null, null, null);

    @Test
    public void thePopupShowsIconsInlineAsMarkdownImages() {
        String summary = _announcer.summaryForAnnouncement("Adds a [dunland] token and :uruk-hai: minions *not bold*");
        assertEquals("Adds a ![Dunland](/gemp-lotr/images/cultures/dunland.png \"Dunland\") token and "
                + "![Uruk-hai](/gemp-lotr/images/cultures/uruk_hai.png \"Uruk-hai\") minions \\*not bold\\*", summary);
        String html = new MarkdownParser().renderMarkdown(summary, false);
        assertTrue(html, html.contains("<img src=\"/gemp-lotr/images/cultures/dunland.png\" alt=\"Dunland\" title=\"Dunland\" />"));
        // the renderer escapes underscores itself; the path must survive that
        assertTrue(html, html.contains("<img src=\"/gemp-lotr/images/cultures/uruk_hai.png\" alt=\"Uruk-hai\" title=\"Uruk-hai\" />"));
        assertTrue(html, html.contains("minions *not bold*"));
    }

    @Test
    public void theCopiedParagraphConvertsTokensOutsideCodeAndLinks() {
        String paragraph = _announcer.paragraphForAnnouncement(
                "- Fixed :isengard: cards, not `:gondor:`, [[Isengard Warrior|:rohan: guy]], [:moria:](#x) or https://x.org/:men:");
        assertEquals("- Fixed ![Isengard](/gemp-lotr/images/cultures/isengard.png \"Isengard\") cards, not `:gondor:`, "
                + ":rohan: guy, [:moria:](#x) or https://x.org/:men:", paragraph);
    }
}
