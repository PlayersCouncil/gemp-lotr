package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.patchnotes.PatchNotesLibrary;
import com.gempukku.lotro.share.CardImages;
import com.gempukku.lotro.share.SharePages;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** /gemp-lotr/share/...: routing, headers, the origin in the tags, 404s. */
public class ShareRequestHandlerTest {
    private Path _notes;
    private ShareRequestHandler _handler;

    @Before
    public void setUp() throws IOException {
        _notes = Files.createTempDirectory("share-handler");
        Files.writeString(_notes.resolve("2026-09-27-hall-overhaul.md"), "---\ntitle: A friendlier Game Hall\n---\nHi.\n",
                StandardCharsets.UTF_8);
        LotroFormatLibrary formats = mock(LotroFormatLibrary.class);
        LotroFormat movie = mock(LotroFormat.class);
        when(movie.getName()).thenReturn("Movie Block (PC)");
        when(formats.getFormat("pc_movie")).thenReturn(movie);
        SharePages pages = new SharePages(new PatchNotesLibrary(_notes.toFile(), 0, System::currentTimeMillis), null,
                formats, new CardImages(null), (owner, name) -> null);
        _handler = new ShareRequestHandler(pages);
    }

    @After
    public void tearDown() throws IOException {
        try (Stream<Path> paths = Files.walk(_notes)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private record Answer(String html, Map<String, String> headers) {
    }

    @SuppressWarnings("unchecked")
    private Answer get(String uri, String host, String proto) throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        if (host != null)
            request.headers().set("Host", host);
        if (proto != null)
            request.headers().set("X-Forwarded-Proto", proto);
        ResponseWriter writer = mock(ResponseWriter.class);
        String path = uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;   // as GempukkuHttpRequestHandler passes it
        assertTrue(ShareRequestHandler.handles(path));
        _handler.handleRequest(path, request, Map.of(), writer, "127.0.0.1");
        ArgumentCaptor<byte[]> body = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<Map> headers = ArgumentCaptor.forClass(Map.class);
        verify(writer).writeByteResponse(body.capture(), headers.capture());
        verify(writer, never()).writeError(anyInt());
        return new Answer(new String(body.getValue(), StandardCharsets.UTF_8), headers.getValue());
    }

    private int status(String uri) throws Exception {
        try {
            get(uri, "play.lotrtcgpc.net", "https");
            return 200;
        } catch (HttpProcessingException exp) {
            return exp.getStatus();
        }
    }

    @Test
    public void aPatchNoteLinkIsACacheablePreviewThatRedirects() throws Exception {
        Answer answer = get("/gemp-lotr/share/patch-notes/2026-09-27-hall-overhaul", "play.lotrtcgpc.net", "https");
        assertEquals("text/html; charset=UTF-8", answer.headers().get("content-type"));
        assertEquals("public, max-age=300", answer.headers().get("cache-control"));
        assertEquals("nosniff", answer.headers().get("X-Content-Type-Options"));
        assertTrue(answer.html(), answer.html().contains("<meta property=\"og:title\" content=\"A friendlier Game Hall\">"));
        assertTrue(answer.html().contains("<meta property=\"og:url\" content=\"https://play.lotrtcgpc.net/gemp-lotr/share/patch-notes/2026-09-27-hall-overhaul\">"));
        assertTrue(answer.html().contains("<meta property=\"og:image\" content=\"https://play.lotrtcgpc.net/gemp-lotr/images/splash.jpg\">"));
        assertTrue(answer.html().contains("<meta http-equiv=\"refresh\" content=\"0; url=/gemp-lotr/hall.html#patch-notes/2026-09-27-hall-overhaul\">"));
        assertTrue(answer.html().contains("window.location.replace(\"/gemp-lotr/hall.html#patch-notes/2026-09-27-hall-overhaul\")"));
    }

    @Test
    public void everyKindRoutes() throws Exception {
        assertTrue(get("/gemp-lotr/share/patch-notes", "h", null).html().contains("url=/gemp-lotr/hall.html#patch-notes\""));
        assertTrue(get("/gemp-lotr/share/patch-notes/", "h", null).html().contains("url=/gemp-lotr/hall.html#patch-notes\""));
        assertTrue(get("/gemp-lotr/share/Patch-Notes/2026-09-27-HALL-overhaul", "h", null).html()
                .contains("url=/gemp-lotr/hall.html#patch-notes/2026-09-27-hall-overhaul\""));
        assertTrue(get("/gemp-lotr/share/errata/1_5", "h", null).html().contains("url=/gemp-lotr/hall.html#errata-1_5\""));
        assertTrue(get("/gemp-lotr/share/format/pc_movie", "h", null).html().contains("Movie Block (PC) — Format Definitions"));
        assertTrue(get("/gemp-lotr/share/card/1_5", "h", null).html().contains("url=/gemp-lotr/hall.html#card-1_5\""));
    }

    @Test
    public void deckLinksInBothForms() throws Exception {
        String code = Base64.getEncoder().encodeToString("ketura|My Deck/+?".getBytes(StandardCharsets.UTF_8));
        String encoded = java.net.URLEncoder.encode(code, StandardCharsets.UTF_8);
        String target = "url=/gemp-lotr-server/deck/html?id=" + encoded + "\"";
        assertTrue(get("/gemp-lotr/share/deck/" + encoded, "h", null).html().contains(target));
        assertTrue(get("/gemp-lotr/share/deck?id=" + encoded, "h", null).html().contains(target));
        // today's links, if the proxy forwards /share/ here
        Answer old = get("/share/deck?id=" + encoded, "play.lotrtcgpc.net", null);
        assertTrue(old.html().contains(target));
        assertTrue(old.html(), old.html().contains("og:url\" content=\"https://play.lotrtcgpc.net/gemp-lotr/share/deck?id=" + encoded + "\""));
    }

    @Test
    public void unknownKindsAndMalformedIdsAre404() throws Exception {
        assertEquals(404, status("/gemp-lotr/share/"));
        assertEquals(404, status("/gemp-lotr/share/nope/1_5"));
        assertEquals(404, status("/gemp-lotr/share/card/Cleaving%20Blow"));
        assertEquals(404, status("/gemp-lotr/share/errata/1_5%3Cscript%3E"));
        assertEquals(404, status("/gemp-lotr/share/patch-notes/..%2F..%2Fetc"));
        assertEquals(404, status("/gemp-lotr/share/deck?id=%2A%2A"));
        assertEquals(404, status("/gemp-lotr/share/deck"));
        assertEquals(200, status("/gemp-lotr/share/patch-notes/2001-01-01"));   // well-formed: the hall says it is gone
    }

    @Test
    public void onlyGetAndHead() throws Exception {
        HttpRequest post = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/gemp-lotr/share/card/1_5");
        try {
            _handler.handleRequest("/gemp-lotr/share/card/1_5", post, Map.of(), mock(ResponseWriter.class), "1.2.3.4");
            fail();
        } catch (HttpProcessingException exp) {
            assertEquals(405, exp.getStatus());
        }
    }

    @Test
    public void theOriginComesFromTheHostAndTheProxy() throws Exception {
        assertTrue(get("/gemp-lotr/share/card/1_5", "test.lotrtcgpc.net", "https").html()
                .contains("og:url\" content=\"https://test.lotrtcgpc.net/gemp-lotr/share/card/1_5\""));
        assertTrue(get("/gemp-lotr/share/card/1_5", "localhost:8080", null).html()
                .contains("og:url\" content=\"http://localhost:8080/gemp-lotr/share/card/1_5\""));
        assertTrue(get("/gemp-lotr/share/card/1_5", "gemp.example", null).html()
                .contains("og:url\" content=\"https://gemp.example/gemp-lotr/share/card/1_5\""));
        // a Host header that is not a plain host name is not echoed into the page
        String evil = get("/gemp-lotr/share/card/1_5", "evil.example\"><script>x</script>", "javascript").html();
        assertFalse(evil, evil.contains("evil.example"));
        assertTrue(evil.contains("og:url\" content=\"" + ShareRequestHandler.DEFAULT_ORIGIN + "/gemp-lotr/share/card/1_5\""));
        assertTrue(get("/gemp-lotr/share/card/1_5", null, null).html().contains(ShareRequestHandler.DEFAULT_ORIGIN));
    }

    @Test
    public void theRouterSendsOnlyShareLinksHere() {
        assertTrue(ShareRequestHandler.handles("/gemp-lotr/share/card/1_5"));
        assertTrue(ShareRequestHandler.handles("/share/deck"));
        assertFalse(ShareRequestHandler.handles("/gemp-lotr/hall.html"));
        assertFalse(ShareRequestHandler.handles("/gemp-lotr/sharethis.html"));
        assertFalse(ShareRequestHandler.handles("/gemp-lotr-server/share"));
    }
}
