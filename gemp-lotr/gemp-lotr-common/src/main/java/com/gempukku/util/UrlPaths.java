package com.gempukku.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * The HTTP layer hands request handlers the raw, still percent-encoded request path.  Handlers that take a free-text
 * value out of the path (a player name, say) decode that one segment with this, after splitting the path.
 */
public final class UrlPaths {
    private UrlPaths() {
    }

    /**
     * Percent-decodes one path segment as UTF-8.  Unlike form decoding, a {@code +} stays a {@code +}.  A segment
     * with a malformed escape (a lone {@code %}, say) is returned unchanged rather than rejected, so a value that was
     * never encoded in the first place still comes through as it was sent.
     */
    public static String decodeSegment(String segment) {
        if (segment == null || segment.indexOf('%') < 0)
            return segment;
        try {
            return URLDecoder.decode(segment.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exp) {
            return segment;
        }
    }
}
