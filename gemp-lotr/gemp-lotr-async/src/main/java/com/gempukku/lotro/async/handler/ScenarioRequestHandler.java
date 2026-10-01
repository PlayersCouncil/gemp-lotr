package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.scenario.ScenarioDefinition;
import com.gempukku.lotro.scenario.ScenarioLibrary;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Scenarios: tutorials, puzzles and sandboxes (see {@link ScenarioLibrary}).  Everything here needs a login.
 * <p>
 * GET /scenarios - what the player can start:
 * {@code {scenarios:[{id, kind, title, summary, author, tags, version, canonical, verified}], current:{gameId, id}|null}}.
 * {@code verified} is false for a scenario that does not play through headless; only its owner and the admins are
 * offered one of those.  {@code current} is the scenario game the player is in the middle of, if any.<br>
 * POST /scenarios/start (form: id) - starts a game of the scenario against its scripted opponent and answers
 * {@code {gameId}}; the client then opens game.html?gameId=...  A scenario the player was already in the middle of is
 * given up.  An unknown id, or one the player is not offered, is a 404 with an {@code {error}} body.<br>
 * GET /scenarios/errors - admins only: every scenario file, with what is wrong with it and the report of its
 * headless run: {@code {files:[{path, owner, id, playable, verified, errors, warnings, report}]}}.
 */
public class ScenarioRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private static final Logger _log = LogManager.getLogger(ScenarioRequestHandler.class);

    private final ScenarioLibrary _scenarios;
    private final LotroServer _lotroServer;

    public ScenarioRequestHandler(Map<Type, Object> context) {
        super(context);
        _scenarios = extractObject(context, ScenarioLibrary.class);
        _lotroServer = extractObject(context, LotroServer.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if ((uri.isEmpty() || uri.equals("/")) && request.method() == HttpMethod.GET) {
            list(request, responseWriter);
        } else if (uri.equals("/start") && request.method() == HttpMethod.POST) {
            start(request, responseWriter);
        } else if (uri.equals("/errors") && request.method() == HttpMethod.GET) {
            errors(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void list(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Player player = getResourceOwnerSafely(request, getQueryParameterSafely(queryDecoder, "participantId"));

        List<Map<String, Object>> scenarios = new ArrayList<>();
        for (ScenarioLibrary.Entry entry : _scenarios.getAvailableTo(player.getName(), player.hasType(Player.Type.ADMIN))) {
            ScenarioDefinition def = entry.def();
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", entry.id());
            json.put("kind", def.kind.name().toLowerCase());
            json.put("title", def.title);
            json.put("summary", def.summary);
            json.put("author", def.author);
            json.put("tags", def.tags);
            json.put("version", def.version);
            json.put("canonical", entry.canonical());
            json.put("verified", entry.verified());
            scenarios.add(json);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenarios", scenarios);
        LotroGameMediator current = _lotroServer.getScenarioGame(player.getName());
        if (current == null) {
            result.put("current", null);
        } else {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("gameId", current.getGameId());
            json.put("id", current.getScenario().Definition().id);
            result.put("current", json);
        }
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    private void start(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            Player player = getResourceOwnerSafely(request, getFormParameterSafely(postDecoder, "participantId"));
            String id = getFormParameterSafely(postDecoder, "id");

            ScenarioLibrary.Entry entry = _scenarios.get(id);
            if (entry == null || !entry.availableTo(player.getName(), player.hasType(Player.Type.ADMIN))) {
                writeError(responseWriter, 404, "There is no scenario called '" + id + "'.");
                return;
            }

            LotroGameMediator mediator;
            try {
                mediator = _lotroServer.createScenarioGame(player, entry.def(), entry.format(), _scenarios.textFor(entry));
            } catch (RuntimeException exp) {
                _log.error("Scenario " + id + " could not be started for " + player.getName(), exp);
                writeError(responseWriter, 500, "The scenario could not be started.");
                return;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("gameId", mediator.getGameId());
            responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
        } finally {
            postDecoder.destroy();
        }
    }

    private void errors(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        //Not on behalf of anybody else: the caller has to be an admin themselves
        Player player = getResourceOwnerSafely(request, null);
        if (!player.hasType(Player.Type.ADMIN))
            throw new HttpProcessingException(403);

        List<Map<String, Object>> files = new ArrayList<>();
        for (ScenarioLibrary.Entry entry : _scenarios.getAll()) {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("path", entry.path());
            json.put("owner", entry.owner());
            json.put("id", entry.id());
            json.put("playable", entry.playable());
            json.put("verified", entry.verified());
            json.put("errors", entry.errors());
            json.put("warnings", entry.warnings());
            json.put("report", entry.report() == null ? null : entry.report().toString());
            files.add(json);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("files", files);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    private static void writeError(ResponseWriter responseWriter, int status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        responseWriter.writeJsonResponse(status, JsonUtils.SerializeWithNulls(error));
    }
}
