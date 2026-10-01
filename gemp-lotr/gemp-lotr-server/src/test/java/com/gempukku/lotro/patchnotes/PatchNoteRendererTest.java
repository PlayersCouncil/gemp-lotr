package com.gempukku.lotro.patchnotes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PatchNoteRendererTest {
    private final PatchNoteRenderer _renderer = new PatchNoteRenderer();

    private Document render(String markdown) {
        return Jsoup.parseBodyFragment(_renderer.render(markdown));
    }

    @Test
    public void rendersCommonMarkWithTheProjectExtensions() {
        Document doc = render("### New\n\n- **Bold** and *italic*\n- ~~gone~~\n\nSee https://lotrtcgpc.net for more.\n\n1. first\n2. second");
        assertEquals("New", doc.select("h3").text());
        assertEquals(2, doc.select("ul > li").size());
        assertEquals("Bold", doc.select("ul strong").text());
        assertEquals("italic", doc.select("ul em").text());
        assertEquals("gone", doc.select("del").text());
        assertEquals("https://lotrtcgpc.net", doc.select("p a").attr("href"));
        assertEquals(2, doc.select("ol > li").size());
    }

    @Test
    public void blankBodyRendersEmpty() {
        assertEquals("", _renderer.render(null));
        assertEquals("", _renderer.render("  \n "));
    }

    @Test
    public void relativeImagesResolveFromThePatchnotesFolder() {
        Document doc = render("![Deck builder](img/2026-09-26-deck.png) ![b](./img/b.png) ![c](patchnotes/img/c.png)"
                + " ![d](https://i.lotrtcgpc.net/x.jpg) <img src=\"img/e.jpg\" width=\"500\">");
        var images = doc.select("img");
        assertEquals(5, images.size());
        assertEquals("patchnotes/img/2026-09-26-deck.png", images.get(0).attr("src"));
        assertEquals("Deck builder", images.get(0).attr("alt"));
        assertEquals("patchnotes/img/b.png", images.get(1).attr("src"));
        assertEquals("patchnotes/img/c.png", images.get(2).attr("src"));
        assertEquals("https://i.lotrtcgpc.net/x.jpg", images.get(3).attr("src"));
        assertEquals("patchnotes/img/e.jpg", images.get(4).attr("src"));
        assertEquals("500", images.get(4).attr("width"));
        for (var img : images)
            assertEquals("lazy", img.attr("loading"));
    }

    @Test
    public void hallLinksStayInTheHallAndOtherSitesOpenInANewTab() {
        Document doc = render("[PC-Movie](#format-pc_movie), [earlier](#patch-notes/2023-12-09), "
                + "[blog](https://blog.lotrtcgpc.net/x), <a href=\"https://x.org\" target=\"_self\">raw</a>, [file](img/notes.pdf)");
        var links = doc.select("a");
        assertEquals(5, links.size());
        assertEquals("#format-pc_movie", links.get(0).attr("href"));
        assertFalse(links.get(0).hasAttr("target"));
        assertEquals("#patch-notes/2023-12-09", links.get(1).attr("href"));
        assertEquals("_blank", links.get(2).attr("target"));
        assertEquals("noopener noreferrer", links.get(2).attr("rel"));
        assertEquals("_blank", links.get(3).attr("target"));
        assertEquals("patchnotes/img/notes.pdf", links.get(4).attr("href"));
        assertFalse(links.get(4).hasAttr("target"));
    }

    @Test
    public void legacyInlineHtmlIsKept() {
        String html = _renderer.render("- NERFED: Mountain-troll (15R112)\\\n  &emsp;&emsp;Twilight: 10 -> <span style=\"color:green\">8</span>"
                + " <b>fierce</b> <span style=\"color:red\"><br>Shadow: Remove (2)</span>");
        Document doc = Jsoup.parseBodyFragment(html);
        assertEquals("color:green", doc.select("span").get(0).attr("style"));
        assertEquals("8", doc.select("span").get(0).text());
        assertEquals("fierce", doc.select("b").text());
        assertEquals(2, doc.select("br").size());
        assertTrue(html, html.contains("  Twilight: 10 -&gt; "));
    }

    @Test
    public void scriptsHandlersAndJavascriptLinksAreRemoved() {
        String html = _renderer.render("Hi <script>window.x=1</script> there\n\n"
                + "<script>\nalert(1)\n</script>\n\n"
                + "<img src=\"img/a.png\" onerror=\"alert(2)\"> <a href=\"javascript:alert(3)\">a</a> [b](javascript:alert(4))\n\n"
                + "<iframe src=\"https://evil.example\"></iframe><form action=\"x\"><input name=\"p\"></form>"
                + "<div onclick=\"alert(5)\" style=\"position:fixed;top:0;color:red\">d</div>\n\n"
                + "<span style=\"background:url(javascript:alert(6))\">s</span> <style>body{display:none}</style>");
        String lower = html.toLowerCase();
        for (String bad : new String[]{"<script", "alert(", "onerror", "onclick", "javascript:", "<iframe", "<form", "<input",
                "position", "url(", "<style", "display:none"})
            assertFalse(bad + " in " + html, lower.contains(bad));
        Document doc = Jsoup.parseBodyFragment(html);
        assertEquals("patchnotes/img/a.png", doc.select("img").attr("src"));
        assertEquals("color:red", doc.select("div").attr("style"));
        assertFalse(doc.select("span").hasAttr("style"));
    }

    @Test
    public void styleFilterKeepsOnlyColoursAndFontStyles() {
        assertEquals("color:red;font-weight:bold", PatchNoteRenderer.filterStyle("color: red; position: absolute; font-weight: bold"));
        assertEquals("color:#ff8800", PatchNoteRenderer.filterStyle("COLOR:#ff8800;"));
        assertEquals("background-color:rgb(1, 2, 3)", PatchNoteRenderer.filterStyle("background-color: rgb(1, 2, 3)"));
        assertEquals("", PatchNoteRenderer.filterStyle("color: expression(alert(1))"));
        assertEquals("", PatchNoteRenderer.filterStyle("background-color: url(x.png)"));
        assertEquals("", PatchNoteRenderer.filterStyle("color: red !important; x"));
    }

    @Test
    public void rebaseOnlyTouchesPathsRelativeToTheFolder() {
        assertEquals("patchnotes/img/a.png", PatchNoteRenderer.rebase("img/a.png"));
        assertEquals("patchnotes/img/a.png", PatchNoteRenderer.rebase("./img/a.png"));
        assertEquals("patchnotes/img/a.png", PatchNoteRenderer.rebase("patchnotes/img/a.png"));
        assertEquals("/gemp-lotr/images/x.png", PatchNoteRenderer.rebase("/gemp-lotr/images/x.png"));
        assertEquals("#pc-errata", PatchNoteRenderer.rebase("#pc-errata"));
        assertEquals("https://x.org/a.png", PatchNoteRenderer.rebase("https://x.org/a.png"));
        assertEquals("mailto:a@b.c", PatchNoteRenderer.rebase("mailto:a@b.c"));
    }

    // ---- [[card links]] ----

    /** Cleaving Blow is 1_5 (also by 1C5); "Aragorn" is ambiguous; anything else is unknown. */
    private static final CardLookup CARDS = reference -> {
        Map<String, CardLookup.Result> known = Map.of(
                "Cleaving Blow", CardLookup.Result.found("1_5", "Cleaving Blow"),
                "1C5", CardLookup.Result.found("1_5", "Cleaving Blow"),
                "1_5", CardLookup.Result.found("1_5", "Cleaving Blow"),
                "51_5", CardLookup.Result.found("51_5", "Cleaving Blow"),
                "Bill the Pony", CardLookup.Result.found("3_106", "Bill \"the\" <Pony> & co"),
                "Aragorn", CardLookup.Result.failed("'Aragorn' matches 9 different cards"));
        CardLookup.Result result = known.get(reference);
        return result != null ? result : CardLookup.Result.failed("no card is named or numbered '" + reference + "'");
    };

    private final PatchNoteRenderer _cardRenderer = new PatchNoteRenderer(CARDS);

    private Document renderCards(String markdown, List<String> problems) {
        return Jsoup.parseBodyFragment(_cardRenderer.render(markdown, problems::add));
    }

    @Test
    public void cardLinksByNameIdAndCollectorInfoBecomeCardHints() {
        List<String> problems = new ArrayList<>();
        Document doc = renderCards("Fixed [[Cleaving Blow]], [[1C5]], [[1_5]] and [[51_5|the errata]].", problems);
        var cards = doc.select("span.patchnote-card");
        assertEquals(4, cards.size());
        for (var card : cards) {
            assertTrue(card.hasClass("cardHint"));
            assertEquals("Cleaving Blow", card.attr("title"));
            assertFalse(card.hasAttr("data-card"));
        }
        assertEquals("1_5", cards.get(0).attr("value"));
        assertEquals("Cleaving Blow", cards.get(0).text());   // a name shows as written
        assertEquals("1C5", cards.get(1).text());             // so does a collector's info
        assertEquals("Cleaving Blow", cards.get(2).text());   // an id shows the card's name
        assertEquals("51_5", cards.get(3).attr("value"));
        assertEquals("the errata", cards.get(3).text());      // custom text wins
        assertEquals("Fixed Cleaving Blow, 1C5, Cleaving Blow and the errata.", doc.select("p").text());
        assertEquals(List.of(), problems);
    }

    @Test
    public void unknownAndAmbiguousCardsAreLeftAsTextAndReported() {
        List<String> problems = new ArrayList<>();
        Document doc = renderCards("- [[Aragorn]] and [[Nobody|someone]] and [[99_999]]\n- ok [[Cleaving Blow]]", problems);
        assertEquals(1, doc.select("span.patchnote-card").size());
        assertEquals("Aragorn and someone and 99_999", doc.select("li").get(0).text());
        assertEquals(3, problems.size());
        assertTrue(problems.get(0), problems.get(0).startsWith("[[Aragorn]]: 'Aragorn' matches 9"));
        assertTrue(problems.get(1), problems.get(1).startsWith("[[Nobody|someone]]: no card"));
        assertTrue(problems.get(2), problems.get(2).startsWith("[[99_999]]: no card"));
    }

    @Test
    public void cardLinksWorkInHeadingsListsAndEmphasisButNotInCodeOrLinks() {
        List<String> problems = new ArrayList<>();
        Document doc = renderCards("### [[Cleaving Blow]] fixed\n\n- **[[1_5]]** and *[[1C5|italic]]*\n\n"
                + "Write `[[Cleaving Blow]]` for a card link.\n\n```\n[[1_5]]\n```\n\n"
                + "[see [[1_5]]](#errata-1_5) ![[[1_5]]](img/x.png)", problems);
        assertEquals(1, doc.select("h3 span.patchnote-card").size());
        assertEquals(1, doc.select("strong > span.patchnote-card").size());
        assertEquals("italic", doc.select("em > span.patchnote-card").text());
        assertEquals("[[Cleaving Blow]]", doc.select("p code").text());
        assertEquals("[[1_5]]", doc.select("pre code").text().trim());
        assertEquals("see [[1_5]]", doc.select("a[href=#errata-1_5]").text());
        assertEquals("[[1_5]]", doc.select("img").attr("alt"));
        assertEquals(3, doc.select("span.patchnote-card").size());
        assertEquals(List.of(), problems);
    }

    @Test
    public void cardLinkTextAndTitlesAreEscaped() {
        List<String> problems = new ArrayList<>();
        // (a literal <tag> between the brackets is inline HTML to Markdown, so it is no card link: entities are text)
        String html = _cardRenderer.render("[[Bill the Pony]] [[1_5|&lt;b onclick=x&gt;bold&lt;/b&gt; &amp; \"q\"]] "
                + "[[1_5|<img src=x onerror=alert(1)>]]", problems::add);
        Document doc = Jsoup.parseBodyFragment(html);
        var cards = doc.select("span.patchnote-card");
        assertEquals(2, cards.size());
        assertEquals("Bill \"the\" <Pony> & co", cards.get(0).attr("title"));
        assertEquals("Bill the Pony", cards.get(0).text());
        assertEquals("<b onclick=x>bold</b> & \"q\"", cards.get(1).text());
        assertEquals(0, doc.select("b").size());
        assertFalse(html, html.contains("onerror"));
        assertEquals(0, doc.select("[onclick], [onerror]").size());
        assertEquals(List.of(), problems);
    }

    @Test
    public void hallSpansCannotSmuggleAnyCardValue() {
        // the class and value only ever come from a resolved card link or a data-card that is a blueprint id
        Document doc = Jsoup.parseBodyFragment(_cardRenderer.render("<span data-card=\"1_5\" class=\"x\">a</span> "
                + "<span data-card=\"javascript:alert(1)\">b</span> <span class=\"cardHint\" value=\"1_5\">c</span>"));
        var spans = doc.select("span");
        assertEquals(3, spans.size());
        assertEquals("cardHint patchnote-card", spans.get(0).attr("class"));
        assertEquals("1_5", spans.get(0).attr("value"));
        assertFalse(spans.get(1).hasAttr("class"));
        assertFalse(spans.get(1).hasAttr("value"));
        assertFalse(spans.get(1).hasAttr("data-card"));
        assertFalse(spans.get(2).hasAttr("class"));
        assertFalse(spans.get(2).hasAttr("value"));
    }

    @Test
    public void withoutACardLibraryCardLinksAreText() {
        List<String> problems = new ArrayList<>();
        assertEquals("<p>Fixed Cleaving Blow and the errata.</p>",
                _renderer.render("Fixed [[Cleaving Blow]] and [[51_5|the errata]].", problems::add).trim());
        assertEquals(2, problems.size());
        assertEquals("<p>[[ ]] and [[|x]] stay</p>", _renderer.render("[[ ]] and [[|x]] stay").trim());
    }
}
