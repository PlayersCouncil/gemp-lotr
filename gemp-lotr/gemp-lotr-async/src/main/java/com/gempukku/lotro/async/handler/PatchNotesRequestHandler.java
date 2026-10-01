package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.patchnotes.PatchNotesLibrary;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Server Info > Patch Notes (public: no login, read-only; the notes are the Markdown files in the web root's
 * patchnotes/ folder, see {@link PatchNotesLibrary}).
 * <p>
 * The feed is the notes plus the past server announcements ({@code announcement-<id>}, kind "announcement", tag
 * "Announcement"); see {@link PatchNotesLibrary}.  Every request takes {@code tag=<tag>} (e.g. {@code tag=Card Fixes}):
 * paging, the month index and a note's neighbours are then those of the entries with that tag.
 * <p>
 * GET /patchnotes?start=0&amp;count=5 - a page, newest first:
 * {@code {total, start, count, tag, all, filters:[{tag, count}], notes:[{slug, kind, date, title, summary, tags, html}]}};
 * count is clamped to 1..20 (default 5).  {@code from=<slug>} instead of start begins the page at that entry (the page's
 * links); {@code month=YYYY-MM} begins it at the newest entry of that month (or the newest older one).  An unknown
 * {@code from} (or one without the tag), or a month with nothing at or before it, is a 404 with a
 * {@code {"error": ...}} body; a malformed month is a 400.<br>
 * GET /patchnotes/months - the months with entries, newest first:
 * {@code {tag, months:[{month: "2026-09", first: <slug of its newest entry>, count}]}}.<br>
 * GET /patchnotes/&lt;slug&gt; - one entry and its place in the feed:
 * {@code {index, total, note:{...}, newer:{slug, kind, date, title, summary, tags}|null, older:...|null}};
 * an unknown slug is a 404 with a {@code {"error": ...}} body.
 * <p>
 * title and summary are null when an entry has none, tags is [] when it has none.  html is rendered and sanitized
 * server-side.
 */
public class PatchNotesRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    public static final int DEFAULT_PAGE_SIZE = 5;

    private final PatchNotesLibrary _patchNotes;

    public PatchNotesRequestHandler(Map<Type, Object> context) {
        super(context);
        _patchNotes = extractObject(context, PatchNotesLibrary.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (request.method() != HttpMethod.GET)
            throw new HttpProcessingException(404);
        if (uri.isEmpty() || uri.equals("/")) {
            getPage(request, responseWriter);
        } else if (uri.equals("/months") || uri.equals("/months/")) {
            getMonths(request, responseWriter);
        } else if (uri.startsWith("/") && uri.indexOf('/', 1) < 0) {
            String slug = QueryStringDecoder.decodeComponent(uri.substring(1));
            getNote(slug, getQueryParameterSafely(new QueryStringDecoder(request.uri()), "tag"), responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getPage(HttpRequest request, ResponseWriter responseWriter) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Integer start = parseInt(getQueryParameterSafely(queryDecoder, "start"), 0);
        Integer count = parseInt(getQueryParameterSafely(queryDecoder, "count"), DEFAULT_PAGE_SIZE);
        if (start == null || start < 0 || count == null) {
            writeError(responseWriter, 400, "Parameters 'start' and 'count' must be whole numbers, start 0 or more.");
            return;
        }
        String from = getQueryParameterSafely(queryDecoder, "from");
        String month = getQueryParameterSafely(queryDecoder, "month");
        String tag = getQueryParameterSafely(queryDecoder, "tag");
        boolean byMonth = (from == null || from.isBlank()) && month != null && !month.isBlank();
        if (byMonth && !PatchNotesLibrary.MONTH.matcher(month.trim()).matches()) {
            writeError(responseWriter, 400, "Parameter 'month' must be YYYY-MM.");
            return;
        }
        Map<String, Object> page = _patchNotes.pageJson(start, count, from, month, tag);
        if (page == null) {
            writeError(responseWriter, 404, byMonth
                    ? "There are no patch notes in or before " + month.trim() + withTag(tag) + "."
                    : "There is no patch note named '" + from + "'" + withTag(tag) + ".");
            return;
        }
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(page));
    }

    private void getMonths(HttpRequest request, ResponseWriter responseWriter) {
        String tag = getQueryParameterSafely(new QueryStringDecoder(request.uri()), "tag");
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(_patchNotes.monthsJson(tag)));
    }

    private static String withTag(String tag) {
        return tag == null || tag.isBlank() ? "" : " tagged '" + tag.trim() + "'";
    }

    private void getNote(String slug, String tag, ResponseWriter responseWriter) {
        Map<String, Object> note = _patchNotes.noteJson(slug, tag);
        if (note == null) {
            writeError(responseWriter, 404, "There is no patch note named '" + slug + "'" + withTag(tag) + ".");
            return;
        }
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(note));
    }

    /** @return the value, the default when absent, or null when present but not a whole number */
    private static Integer parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank())
            return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exp) {
            return null;
        }
    }

    private static void writeError(ResponseWriter responseWriter, int status, String message) {
        var error = new LinkedHashMap<String, Object>();
        error.put("error", message);
        responseWriter.writeJsonResponse(status, JsonUtils.SerializeWithNulls(error));
    }
}
