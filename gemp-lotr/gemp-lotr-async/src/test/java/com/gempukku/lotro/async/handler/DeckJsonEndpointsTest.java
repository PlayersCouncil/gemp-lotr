package com.gempukku.lotro.async.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.db.DeckSerialization;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * /deck/statsJson and /deck/convertJson: the public JSON versions of /deck/stats and /deck/convert that Red Book
 * (the PC deck site) calls to validate decks and apply errata.  Neither needs a login.
 */
public class DeckJsonEndpointsTest {
    private static final String SERVER = "/gemp-lotr-server";
    private static final String DECK = "1_290|1_2|1_319,1_323|1_50,1_50,1_178|";

    private String previousTestProperty;
    private Map<Type, Object> context;
    private LotroFormat format;
    private DeckRequestHandler handler;

    @Before
    public void setUp() throws CardNotFoundException {
        // getResourceOwnerSafely treats the participantId as the login when -Dtest=true; these requests are anonymous
        previousTestProperty = System.clearProperty("test");

        format = mock(LotroFormat.class);
        when(format.getCode()).thenReturn("pc_fotr_block");
        when(format.getName()).thenReturn("Fellowship Block (PC)");
        when(format.getErrataCardMap()).thenReturn(Map.of("1_50", "51_50"));

        LotroFormatLibrary formatLibrary = mock(LotroFormatLibrary.class);
        when(formatLibrary.getFormat("pc_fotr_block")).thenReturn(format);

        // As LotroServer does: contents need 3 or 4 '|' separators, otherwise there's no deck
        LotroServer lotroServer = mock(LotroServer.class);
        when(lotroServer.createDeckWithValidate(anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(inv -> {
                    String contents = inv.getArgument(1);
                    long separators = contents.chars().filter(c -> c == '|').count();
                    return separators < 3 || separators > 4 ? null : DeckSerialization.buildDeckFromContents(
                            inv.getArgument(0), contents, inv.getArgument(2), inv.getArgument(3));
                });

        LotroCardBlueprint freePeoples = mock(LotroCardBlueprint.class);
        when(freePeoples.getSide()).thenReturn(Side.FREE_PEOPLE);
        LotroCardBlueprint shadow = mock(LotroCardBlueprint.class);
        when(shadow.getSide()).thenReturn(Side.SHADOW);
        LotroCardBlueprintLibrary library = mock(LotroCardBlueprintLibrary.class);
        when(library.getLotroCardBlueprint("1_50")).thenReturn(freePeoples);
        when(library.getLotroCardBlueprint("1_178")).thenReturn(shadow);
        when(library.getLotroCardBlueprint("9_99")).thenThrow(new CardNotFoundException("9_99"));

        context = new HashMap<>();
        context.put(LotroFormatLibrary.class, formatLibrary);
        context.put(LotroServer.class, lotroServer);
        context.put(LotroCardBlueprintLibrary.class, library);
        handler = new DeckRequestHandler(context);
    }

    @After
    public void tearDown() {
        if (previousTestProperty != null)
            System.setProperty("test", previousTestProperty);
    }

    private static HttpRequest post(String uri, Map<String, String> form) {
        StringBuilder body = new StringBuilder();
        for (var entry : form.entrySet()) {
            if (!body.isEmpty())
                body.append('&');
            body.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        var request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, SERVER + uri,
                Unpooled.copiedBuffer(body.toString(), StandardCharsets.UTF_8));
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        return request;
    }

    /** Sends the request anonymously and returns the JSON written with a 200. */
    private JSONObject ok(String path, Map<String, String> form) throws Exception {
        ResponseWriter writer = mock(ResponseWriter.class);
        handler.handleRequest(path, post("/deck" + path, form), context, writer, "127.0.0.1");
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(writer).writeJsonResponse(json.capture());
        verify(writer, never()).writeError(401);
        return JSON.parseObject(json.getValue());
    }

    /** Sends the request anonymously and returns the JSON written with the given error status. */
    private JSONObject error(String path, Map<String, String> form, int status) throws Exception {
        ResponseWriter writer = mock(ResponseWriter.class);
        handler.handleRequest(path, post("/deck" + path, form), context, writer, "127.0.0.1");
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(writer).writeJsonResponse(eq(status), json.capture());
        return JSON.parseObject(json.getValue());
    }

    @Test
    public void statsJsonListsEveryProblemWithoutALogin() throws Exception {
        when(format.validateDeck(any(LotroDeck.class), isNull()))
                .thenReturn(List.of("Deck has fewer than 60 cards", "Too many copies of Legolas, Greenleaf"));
        when(format.applyErrata(any(LotroDeck.class))).thenAnswer(inv -> inv.getArgument(0));

        JSONObject result = ok("/statsJson", Map.of("targetFormat", "pc_fotr_block", "deckContents", DECK));

        assertEquals("pc_fotr_block", result.getString("formatCode"));
        assertEquals("Fellowship Block (PC)", result.getString("formatName"));
        assertEquals(2, result.getIntValue("freePeoplesCount"));
        assertEquals(1, result.getIntValue("shadowCount"));
        assertFalse(result.getBooleanValue("valid"));
        assertEquals(List.of("Deck has fewer than 60 cards", "Too many copies of Legolas, Greenleaf"),
                result.getJSONArray("problems").toJavaList(String.class));
        assertFalse(result.getBooleanValue("validWithErrata"));
        assertEquals(2, result.getJSONArray("problemsWithErrata").size());
    }

    @Test
    public void statsJsonReportsAValidDeck() throws Exception {
        when(format.validateDeck(any(LotroDeck.class), isNull())).thenReturn(List.of());
        when(format.applyErrata(any(LotroDeck.class))).thenAnswer(inv -> inv.getArgument(0));

        JSONObject result = ok("/statsJson", Map.of("targetFormat", "pc_fotr_block", "deckContents", DECK));

        assertTrue(result.getBooleanValue("valid"));
        assertTrue(result.getJSONArray("problems").isEmpty());
    }

    @Test
    public void statsJsonLeavesErrataFieldsNullWhenTheFormatHasNoErrata() throws Exception {
        when(format.getErrataCardMap()).thenReturn(Map.of());
        when(format.validateDeck(any(LotroDeck.class), isNull())).thenReturn(List.of());

        JSONObject result = ok("/statsJson", Map.of("targetFormat", "pc_fotr_block", "deckContents", DECK));

        assertTrue(result.containsKey("validWithErrata"));
        assertNull(result.get("validWithErrata"));
        assertNull(result.get("problemsWithErrata"));
    }

    @Test
    public void statsJsonRejectsMalformedDeckContents() throws Exception {
        JSONObject result = error("/statsJson", Map.of("targetFormat", "pc_fotr_block", "deckContents", "1_50"), 400);
        assertEquals("Invalid deck contents", result.getString("error"));
    }

    @Test
    public void statsJsonRejectsUnknownCards() throws Exception {
        JSONObject result = error("/statsJson",
                Map.of("targetFormat", "pc_fotr_block", "deckContents", "1_290|1_2||9_99|"), 400);
        assertEquals("Unknown card: 9_99", result.getString("error"));
    }

    @Test
    public void statsJsonRequiresAFormat() throws Exception {
        JSONObject result = error("/statsJson", Map.of("deckContents", DECK), 400);
        assertTrue(result.getString("error").startsWith("Invalid format"));
    }

    @Test
    public void convertJsonAppliesTheFormatsErrataWithoutALogin() throws Exception {
        LotroDeck converted = DeckSerialization.buildDeckFromContents("", "1_290|1_2|1_319,1_323|51_50,51_50,1_178|",
                "Fellowship Block (PC)", "");
        when(format.applyErrata(any(LotroDeck.class))).thenReturn(converted);

        JSONObject result = ok("/convertJson", Map.of("targetFormat", "pc_fotr_block", "deckContents", DECK));

        assertEquals("pc_fotr_block", result.getString("formatCode"));
        assertEquals("1_290", result.getString("ringBearer"));
        assertEquals("1_2", result.getString("ring"));
        assertEquals(List.of("1_319", "1_323"), result.getJSONArray("sites").toJavaList(String.class));
        assertEquals(List.of("51_50", "51_50", "1_178"), result.getJSONArray("drawDeck").toJavaList(String.class));
        assertEquals(DeckSerialization.buildContentsFromDeck(converted), result.getString("deckContents"));
    }

    @Test
    public void convertJsonRequiresBothParameters() throws Exception {
        JSONObject result = error("/convertJson", Map.of("targetFormat", "pc_fotr_block"), 400);
        assertEquals("targetFormat and deckContents are required", result.getString("error"));
    }
}
