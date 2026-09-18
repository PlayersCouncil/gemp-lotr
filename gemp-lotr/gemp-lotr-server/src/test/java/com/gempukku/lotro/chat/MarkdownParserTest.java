package com.gempukku.lotro.chat;

import org.junit.Test;

import static org.junit.Assert.*;

public class MarkdownParserTest {

    private final MarkdownParser _parser = new MarkdownParser();

    @Test
    public void descriptionRendersMarkdownWithLinks() {
        String html = _parser.renderDescription("Play **Fellowship Block** - rules on [the wiki](https://wiki.lotrtcgpc.net/x)");
        assertTrue(html, html.contains("<strong>Fellowship Block</strong>"));
        assertTrue(html, html.contains("href=\"https://wiki.lotrtcgpc.net/x\""));
        assertTrue(html, html.contains(">the wiki</a>"));
    }

    @Test
    public void descriptionEscapesStrayAngleBrackets() {
        String html = _parser.renderDescription("Twilight pool < 5 means *go*");
        assertTrue(html, html.contains("&lt; 5"));
        assertTrue(html, html.contains("<em>go</em>"));
    }

    @Test
    public void legacyHtmlDescriptionIsPassedThroughUntouched() {
        String legacy = "Two series.<br>See <a href=\"https://example.org\">here</a> for <b>rules</b>.";
        assertEquals(legacy, _parser.renderDescription(legacy));
    }

    @Test
    public void blankDescriptionRendersEmpty() {
        assertEquals("", _parser.renderDescription(null));
        assertEquals("", _parser.renderDescription("   "));
    }
}
