package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.DeckRenderer;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.tournament.TournamentService;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.AsciiString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.FindException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public class ReplayRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final GameRecorder _gameRecorder;
    private final DeckRenderer _deckRenderer;

    private static final Logger _log = LogManager.getLogger(ReplayRequestHandler.class);

    public ReplayRequestHandler(Map<Type, Object> context) {
        super(context);

        _gameRecorder = extractObject(context, GameRecorder.class);
        _deckRenderer = new DeckRenderer(extractObject(context, LotroCardBlueprintLibrary.class),
                extractObject(context, LotroFormatLibrary.class), new SortAndFilterCards());
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith("/summarydecks")) {
            int length = "/summarydecks/".length();
            ServeSummaryDecks(uri.substring(length, uri.length()), request, responseWriter);
        }
        else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            ServeReplayXML(uri.substring(1), request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void ServeSummaryDecks(String replayId, HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        try {
            var summaryFile = _gameRecorder.getRecordedSummary(replayId);
            String json = new String(summaryFile.readAllBytes());
            var summary = JsonUtils.Convert(json, ReplayMetadata.class);

            var decks = _deckRenderer.ExtractDecksFromReplaySummary(summary);

            var fragments = new ArrayList<String>();
            for(var deck : decks) {
                fragments.add(_deckRenderer.convertDeckToForumFragment(deck, deck.getNotes()));
            }

            responseWriter.writeHtmlResponse(_deckRenderer.AddDeckReadoutHeaderAndFooter(fragments));
        }
        catch(IOException ex) {
            throw new HttpProcessingException(404);
        } catch (CardNotFoundException e) {
            throw new HttpProcessingException(500);
        }


    }

    private void ServeReplayXML(String replayId, HttpRequest request, ResponseWriter responseWriter) throws Exception {
        if (!replayId.contains("$"))
            throw new HttpProcessingException(404);
        if (replayId.contains("."))
            throw new HttpProcessingException(404);

        final String[] split = replayId.split("\\$");
        if (split.length != 2)
            throw new HttpProcessingException(404);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final InputStream recordedGame = _gameRecorder.getRecordedGame(split[0], split[1]);
        try (recordedGame) {
            if (recordedGame == null)
                throw new HttpProcessingException(404);
            byte[] bytes = new byte[1024];
            int count;
            while ((count = recordedGame.read(bytes)) != -1)
                baos.write(bytes, 0, count);
        } catch (IOException exp) {
            _log.error("Error 404 response for " + request.uri(), exp);
            throw new HttpProcessingException(404);
        }

        Map<AsciiString, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, "application/html; charset=UTF-8");

        responseWriter.writeByteResponse(baos.toByteArray(), headers);
    }
}
