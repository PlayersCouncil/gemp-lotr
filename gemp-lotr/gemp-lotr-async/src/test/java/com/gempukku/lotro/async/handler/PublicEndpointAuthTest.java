package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.polling.LongPollingSystem;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Which server endpoints a visitor who is not logged in (no loggedUser cookie) may use.  Help and Server Info are
 * public (hall.html#format-..., #pc-errata, #info/stats links work before logging in), so the read-only data they show
 * must answer without a login; everything to do with playing stays behind it.
 */
public class PublicEndpointAuthTest {
    private static final String SERVER = "/gemp-lotr-server";

    private String previousTestProperty;
    private Map<Type, Object> context;
    private LotroFormatLibrary formatLibrary;
    private GameHistoryService gameHistoryService;

    @Before
    public void setUp() {
        // getResourceOwnerSafely treats the participantId as the login when -Dtest=true; these requests are anonymous
        previousTestProperty = System.clearProperty("test");

        formatLibrary = mock(LotroFormatLibrary.class);
        LotroFormat pcErrata = mock(LotroFormat.class);
        when(formatLibrary.getFormat("pc_errata")).thenReturn(pcErrata);

        gameHistoryService = mock(GameHistoryService.class);

        context = new HashMap<>();
        context.put(LotroFormatLibrary.class, formatLibrary);
        context.put(LotroCardBlueprintLibrary.class, mock(LotroCardBlueprintLibrary.class));
        context.put(GameHistoryService.class, gameHistoryService);
    }

    @After
    public void tearDown() {
        if (previousTestProperty != null)
            System.setProperty("test", previousTestProperty);
    }

    private static HttpRequest anonymous(HttpMethod method, String uri) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri);
    }

    private HallRequestHandler hall() {
        return new HallRequestHandler(context, mock(LongPollingSystem.class));
    }

    /** Runs the request; a 401 (thrown or written) fails the test. */
    private ResponseWriter assertPublic(UriRequestHandler handler, String path, String uri) throws Exception {
        ResponseWriter writer = mock(ResponseWriter.class);
        try {
            handler.handleRequest(path, anonymous(HttpMethod.GET, SERVER + uri), context, writer, "127.0.0.1");
        } catch (HttpProcessingException e) {
            fail(uri + " answered " + e.getStatus() + " to a visitor who is not logged in");
        }
        verify(writer, never()).writeError(401);
        verify(writer, never()).writeError(eq(401), anyMap());
        return writer;
    }

    /** Runs the request; it must answer 401 (thrown or written). */
    private void assertLoginRequired(UriRequestHandler handler, String path, String uri) throws Exception {
        ResponseWriter writer = mock(ResponseWriter.class);
        try {
            handler.handleRequest(path, anonymous(HttpMethod.GET, SERVER + uri), context, writer, "127.0.0.1");
        } catch (HttpProcessingException e) {
            assertEquals(uri + " without a login", 401, e.getStatus());
            return;
        }
        verify(writer).writeError(401);
    }

    // ---- public: Help › Format Definitions, Help › PC Errata, Server Info › Server Stats ----

    @Test
    public void formatDefinitionsArePublic() throws Exception {
        ResponseWriter writer = assertPublic(hall(), "/formats/json", "/hall/formats/json");
        verify(writer).writeJsonResponse(anyString());
    }

    @Test
    public void oldFormatDefinitionsPageIsPublic() throws Exception {
        ResponseWriter writer = assertPublic(hall(), "/formats/html", "/hall/formats/html");
        verify(writer).writeHtmlResponse(anyString());
    }

    @Test
    public void pcErrataArePublic() throws Exception {
        ResponseWriter writer = assertPublic(hall(), "/errata/json", "/hall/errata/json");
        verify(writer).writeJsonResponse(anyString());
    }

    @Test
    public void serverStatsArePublic() throws Exception {
        ResponseWriter writer = assertPublic(new ServerStatsRequestHandler(context), "",
                "/stats?startDay=2026-09-01&length=week");
        verify(writer).writeJsonResponse(anyString());
        verify(gameHistoryService).getActivePlayersCount(any(), any());
    }

    @Test
    public void serverStatsStillRejectBadInput() throws Exception {
        try {
            new ServerStatsRequestHandler(context).handleRequest("",
                    anonymous(HttpMethod.GET, SERVER + "/stats?startDay=2026-09-01&length=year"), context,
                    mock(ResponseWriter.class), "127.0.0.1");
            fail("an unknown period is a bad request");
        } catch (HttpProcessingException e) {
            assertEquals(400, e.getStatus());
        }
    }

    // ---- login required: the Game Hall and the player's own data ----

    @Test
    public void gameHallNeedsLogin() throws Exception {
        assertLoginRequired(hall(), "", "/hall");
    }

    @Test
    public void playerInfoNeedsLogin() throws Exception {
        assertLoginRequired(new PlayerInfoRequestHandler(context), "", "/player");
    }

    @Test
    public void ownGameHistoryNeedsLogin() throws Exception {
        assertLoginRequired(new GameHistoryRequestHandler(context), "", "/gameHistory?start=0&count=10");
    }
}
