package com.gempukku.lotro.async.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.polling.LongPollingSystem;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * GET /hall/format/{code}/json: one format's definition (an entry of /hall/formats/json) for any format the library
 * knows, hall format or not, without a login; 404 for an unknown code.
 */
public class HallFormatJsonTest {
    private String previousTestProperty;
    private Map<Type, Object> context;

    @Before
    public void setUp() {
        // anonymous requests (getResourceOwnerSafely treats the participantId as the login when -Dtest=true)
        previousTestProperty = System.clearProperty("test");

        JSONDefs.Format facts = new JSONDefs.Format();
        facts.sites = "FELLOWSHIP";
        LotroFormat leagueOnly = mock(LotroFormat.class);
        when(leagueOnly.getCode()).thenReturn("league_only");
        when(leagueOnly.getName()).thenReturn("League Only");
        when(leagueOnly.hallVisible()).thenReturn(false);
        when(leagueOnly.Serialize()).thenReturn(facts);
        when(leagueOnly.getSiteBlock()).thenReturn(SitesBlock.FELLOWSHIP);

        LotroFormatLibrary formatLibrary = mock(LotroFormatLibrary.class);
        when(formatLibrary.getFormat("league_only")).thenReturn(leagueOnly);

        context = new HashMap<>();
        context.put(LotroFormatLibrary.class, formatLibrary);
        context.put(LotroCardBlueprintLibrary.class, mock(LotroCardBlueprintLibrary.class));
    }

    @After
    public void tearDown() {
        if (previousTestProperty != null)
            System.setProperty("test", previousTestProperty);
    }

    private ResponseWriter get(String path) throws Exception {
        ResponseWriter writer = mock(ResponseWriter.class);
        new HallRequestHandler(context, mock(LongPollingSystem.class)).handleRequest(path,
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/gemp-lotr-server/hall" + path),
                context, writer, "127.0.0.1");
        return writer;
    }

    @Test
    public void anyFormatTheLibraryKnows() throws Exception {
        ResponseWriter writer = get("/format/league_only/json");
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(writer).writeJsonResponse(json.capture());

        JSONObject entry = JSON.parseObject(json.getValue());
        assertEquals("league_only", entry.getString("code"));
        assertEquals("League Only", entry.getString("name"));
        assertEquals("FELLOWSHIP", entry.getString("sites"));
        assertEquals(SitesBlock.FELLOWSHIP.getHumanReadable(), entry.getString("sitesName"));
        assertNotNull("the same shape as an entry of formats/json", entry.getJSONObject("lists"));
    }

    @Test
    public void unknownFormatIsNotFound() throws Exception {
        try {
            get("/format/no_such_format/json");
            fail("expected a 404");
        } catch (HttpProcessingException e) {
            assertEquals(404, e.getStatus());
        }
    }

    @Test
    public void onlyTheJsonShapeIsTakenForIt() {
        assertTrue(HallRequestHandler.isFormatJsonUri("/format/movie/json"));
        assertFalse("the HTML of a format coded \"json\"", HallRequestHandler.isFormatJsonUri("/format/json"));
        assertFalse("the HTML of a code ending in json", HallRequestHandler.isFormatJsonUri("/format/moviejson"));
        assertFalse(HallRequestHandler.isFormatJsonUri("/format//json"));
        assertFalse(HallRequestHandler.isFormatJsonUri("/format/a/b/json"));
        assertFalse(HallRequestHandler.isFormatJsonUri("/formats/json"));
    }
}
