package com.gempukku.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Decoding the player-name segment of /tournament/{id}/deck/{player}/html.  The HTTP layer hands handlers the raw
 * request path, so before this a name the browser had to percent-encode (a space, any non-ASCII letter) never
 * matched its player.
 */
public class UrlPathsTest {

    @Test
    public void aNameNeedingNoEncodingIsUnchanged() {
        // Every name registration accepts today: [A-Za-z0-9_-]{2,30}.
        assertEquals("Frodo_Baggins-99", UrlPaths.decodeSegment("Frodo_Baggins-99"));
        assertNull(UrlPaths.decodeSegment(null));
        assertEquals("", UrlPaths.decodeSegment(""));
    }

    @Test
    public void percentEscapesAreDecoded() {
        // What a browser sends for a raw space in an href, and what encodeURIComponent produces for the rest.
        assertEquals("Frodo Baggins", UrlPaths.decodeSegment("Frodo%20Baggins"));
        assertEquals("a/b", UrlPaths.decodeSegment("a%2Fb"));
        assertEquals("a#b", UrlPaths.decodeSegment("a%23b"));
        assertEquals("a?b", UrlPaths.decodeSegment("a%3Fb"));
        assertEquals("100%", UrlPaths.decodeSegment("100%25"));
        assertEquals("Éowyn", UrlPaths.decodeSegment("%C3%89owyn"));
    }

    @Test
    public void aPlusIsNotASpaceInAPath() {
        assertEquals("a+b", UrlPaths.decodeSegment("a+b"));
        assertEquals("a+b c", UrlPaths.decodeSegment("a+b%20c"));
    }

    @Test
    public void aMalformedEscapeIsLeftAsSent() {
        // A literal '%' the client did not encode must still come through rather than fail the request.
        assertEquals("100%", UrlPaths.decodeSegment("100%"));
        assertEquals("a%zzb", UrlPaths.decodeSegment("a%zzb"));
        assertEquals("a%2", UrlPaths.decodeSegment("a%2"));
    }
}
