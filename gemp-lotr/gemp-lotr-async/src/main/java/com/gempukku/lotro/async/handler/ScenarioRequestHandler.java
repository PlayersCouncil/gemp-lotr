package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.scenario.ScenarioAuthoring;
import com.gempukku.lotro.scenario.ScenarioDefinition;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.scenario.ScenarioLibrary;
import com.gempukku.lotro.scenario.ScenarioRunLog;
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
 * GET /scenarios - what the player can start, and {@code unreadable: [{path, owner, mine, id, errors}]}: the files a
 * game cannot be started of, with why (all of them for an admin, their own for anybody else); and, for somebody who
 * may write scenarios, {@code mayAuthor: true}, {@code mayPublish} (whether they may put one among the game's own) and
 * {@code authoring: {gameId, id, title, over, test}|null}, the draft they have open):
 * {@code {scenarios:[{id, kind, title, summary, author, tags, version, canonical, verified, path, owner, mine,
 * campaign:{id, title, position, count, next:{id, title}|null}|null,
 * progress:{status, attempts, version, firstCompleted, lastPlayed}|null}], current:{gameId, id}|null}}.
 * {@code campaign} is the campaign the scenario is part of: a game of it ends by offering {@code next}.
 * {@code progress.status} is "started" or "complete".
 * {@code verified} is false for a scenario that does not play through headless; only its owner and the admins are
 * offered one of those.  {@code current} is the scenario game the player is in the middle of, if any.<br>
 * POST /scenarios/start (form: id, optionally from=checkpoint) - starts a game of the scenario against its scripted
 * opponent and answers {@code {gameId, resumedAt}}; the client then opens game.html?gameId=...  A scenario the player
 * was already in the middle of is given up.  With from=checkpoint, and if that scenario was this same one and had
 * reached a checkpoint, the new game picks up there and resumedAt is the checkpoint step's number; otherwise the game
 * starts at the beginning and resumedAt is null.  An unknown id, or one the player is not offered, is a 404 with an
 * {@code {error}} body.<br>
 * GET /scenarios/errors - admins only: every scenario file, with what is wrong with it and the report of its
 * headless run: {@code {files:[{path, owner, id, playable, verified, errors, warnings, report}]}}.
 * <p>
 * Under /scenarios/author/ is the authoring tool (see {@link ScenarioAuthoring}), for now open to admins only.  An
 * author has one draft open at a time, and a game it is being written in.  Every POST answers
 * {@code {ok, gameId, events, rebuilt, state}}: {@code rebuilt} means there is a fresh game to go to (gameId),
 * {@code events} that the game in hand has been sent something its client should fetch, and {@code state} is what
 * GET state gives.  Something that cannot be done is a 400 with an {@code {error}} body.<br>
 * GET state - the draft and where its game stands: {@code {session, gameId, revision, mode, dirty, path, draft,
 * statuses, position, waiting, pending, board, problems, ...}}; {@code {session:false}} with no draft open.<br>
 * GET source - {@code {source}}: the draft as the file it would be saved as.<br>
 * GET cards?q=&sites= - {@code {cards:[{id, name, type, culture, side, cost}]}}: cards by name.<br>
 * POST open (id | new=true, id, title); edit (patch, goto); goto (step, sub); answer (term, record | least=true);
 * do (op, setup); alias (from, to); here; retake; settings (recording, askOpponent, way);
 * save (path, canonical, as: copy|rename); left;
 * check; test (step);
 * resume; close.
 */
public class ScenarioRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private static final Logger _log = LogManager.getLogger(ScenarioRequestHandler.class);

    private final ScenarioLibrary _scenarios;
    private final ScenarioRunLog _runLog;
    private final LotroServer _lotroServer;
    private final ScenarioAuthoring _authoring;

    public ScenarioRequestHandler(Map<Type, Object> context) {
        super(context);
        _authoring = extractObject(context, ScenarioAuthoring.class);
        _scenarios = extractObject(context, ScenarioLibrary.class);
        _runLog = extractObject(context, ScenarioRunLog.class);
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
        } else if (uri.startsWith("/author/")) {
            author(uri.substring("/author/".length()), request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void list(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Player player = getResourceOwnerSafely(request, getQueryParameterSafely(queryDecoder, "participantId"));

        Map<String, DBDefs.ScenarioProgress> progress = _runLog.progressOf(player);
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
            //Where the file is within its folder ("tutorials/basics/01-into-the-wild.hjson", "users/<name>/..."), and
            // whose it is: the picker shows scenarios in the folders they are kept in
            json.put("path", entry.path());
            json.put("owner", entry.owner());
            json.put("mine", entry.owner() != null && entry.owner().equals(player.getName()));
            //The campaign it is part of, if any: {id, title, position, count, next}
            json.put("campaign", _scenarios.campaignInfo(entry.id(), player.getName(), player.hasType(Player.Type.ADMIN)));
            DBDefs.ScenarioProgress played = progress.get(entry.id());
            if (played == null) {
                json.put("progress", null);
            } else {
                Map<String, Object> progressJson = new LinkedHashMap<>();
                progressJson.put("status", played.status);
                progressJson.put("attempts", played.attempts);
                //The version last played or, once completed, the version completed: older than the scenario's own
                // version means it has changed since
                progressJson.put("version", played.version);
                progressJson.put("firstCompleted", played.first_completed == null ? null : played.first_completed.toString());
                progressJson.put("lastPlayed", played.last_played == null ? null : played.last_played.toString());
                json.put("progress", progressJson);
            }
            scenarios.add(json);
        }

        //The files that are not in that list because a game of them cannot be started at all (a file that is not
        // HJSON, a card that was never declared, a format that does not exist), each with why.  An admin is told of
        // every one; anybody else, of their own.  Without this a scenario that has been edited into a mistake just
        // disappears from the list, and nothing says why.  'openable' is whether the authoring tool can open it all
        // the same, with what is mistaken left out (see ScenarioSalvage).
        boolean admin = player.hasType(Player.Type.ADMIN);
        List<Map<String, Object>> unreadable = new ArrayList<>();
        for (ScenarioLibrary.Entry entry : _scenarios.getAll()) {
            boolean mine = entry.owner() != null && entry.owner().equals(player.getName());
            if (entry.playable() || !(admin || mine))
                continue;
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("path", (entry.owner() == null ? "" : "users/" + entry.owner() + "/") + entry.path());
            json.put("owner", entry.owner());
            json.put("mine", mine);
            json.put("id", entry.id());
            json.put("title", entry.def() == null ? null : entry.def().title);
            json.put("errors", entry.errors());
            json.put("openable", entry.openable() && mayAuthor(player));
            unreadable.add(json);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenarios", scenarios);
        result.put("unreadable", unreadable);
        //Whether the player may write scenarios, and the draft they have open (if they have one): {gameId, id, title,
        // over, test}
        result.put("mayAuthor", mayAuthor(player));
        result.put("authoring", mayAuthor(player) ? _authoring.currentForHall(player) : null);
        //(whether a new scenario can be kept somewhere other than among the game's own)
        result.put("ownFolder", _scenarios.hasUserFolder());
        //(and whether this player may put one among the game's own: the admins)
        result.put("mayPublish", admin);
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
                mediator = _lotroServer.createScenarioGame(player, entry.def(), entry.format(), _scenarios.textFor(entry),
                        "checkpoint".equals(getFormParameterSafely(postDecoder, "from")),
                        _scenarios.campaignInfo(entry.id(), player.getName(), player.hasType(Player.Type.ADMIN)));
            } catch (RuntimeException exp) {
                _log.error("Scenario " + id + " could not be started for " + player.getName(), exp);
                writeError(responseWriter, 500, "The scenario could not be started.");
                return;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("gameId", mediator.getGameId());
            //Where the game picks up: the number of the checkpoint step, or null for the beginning
            result.put("resumedAt", mediator.getScenario().ResumedAt());
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
            json.put("openable", entry.openable());
            json.put("errors", entry.errors());
            json.put("warnings", entry.warnings());
            json.put("report", entry.report() == null ? null : entry.report().toString());
            files.add(json);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("files", files);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    /** Who may write scenarios.  For now, the admins. */
    private static boolean mayAuthor(Player player) {
        return player.hasType(Player.Type.ADMIN);
    }

    private void author(String what, HttpRequest request, ResponseWriter responseWriter) throws Exception {
        //Not on behalf of anybody else: a draft is its author's own
        Player player = getResourceOwnerSafely(request, null);
        if (!mayAuthor(player))
            throw new HttpProcessingException(403);
        boolean admin = player.hasType(Player.Type.ADMIN);

        try {
            if (request.method() == HttpMethod.GET) {
                QueryStringDecoder query = new QueryStringDecoder(request.uri());
                Map<String, Object> result = new LinkedHashMap<>();
                switch (what) {
                    case "state" -> result = _authoring.state(player);
                    case "source" -> result.put("source", _authoring.source(player));
                    case "cards" -> result.put("cards", _authoring.findCards(getQueryParameterSafely(query, "q"),
                            "true".equals(getQueryParameterSafely(query, "sites"))));
                    case "images" -> result.put("images", _authoring.images());
                    default -> throw new HttpProcessingException(404);
                }
                responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
                return;
            }
            if (request.method() != HttpMethod.POST)
                throw new HttpProcessingException(404);

            HttpPostRequestDecoder form = new HttpPostRequestDecoder(request);
            try {
                Map<String, Object> result;
                switch (what) {
                    case "open" -> result = "true".equals(getFormParameterSafely(form, "new"))
                            ? _authoring.create(player, getFormParameterSafely(form, "id"), getFormParameterSafely(form, "title"),
                                    "true".equals(getFormParameterSafely(form, "canonical")), admin)
                            : _authoring.open(player, getFormParameterSafely(form, "id"), admin);
                    case "edit" -> result = _authoring.edit(player, getFormParameterSafely(form, "patch"), number(form, "goto"));
                    case "goto" -> {
                        Integer step = number(form, "step");
                        Integer sub = number(form, "sub");
                        result = _authoring.goTo(player, step == null ? 1 : step, sub == null ? 0 : sub);
                    }
                    case "answer" -> result = "true".equals(getFormParameterSafely(form, "least"))
                            ? _authoring.answer(player, null, "true".equals(getFormParameterSafely(form, "record")))
                            : _authoring.answer(player, getFormParameterSafely(form, "term"), !"false".equals(getFormParameterSafely(form, "record")));
                    case "do" -> result = _authoring.apply(player, getFormParameterSafely(form, "op"), "true".equals(getFormParameterSafely(form, "setup")));
                    case "alias" -> result = _authoring.renameCard(player, getFormParameterSafely(form, "from"), getFormParameterSafely(form, "to"));
                    case "here" -> result = _authoring.stepHere(player);
                    case "retake" -> result = _authoring.retake(player);
                    case "settings" -> {
                        String way = getFormParameterSafely(form, "way");
                        List<String> names = null;
                        if (way != null) {
                            names = new ArrayList<>();
                            for (String name : way.split(","))
                                if (!name.isBlank())
                                    names.add(name.trim());
                        }
                        result = _authoring.settings(player, flag(form, "recording"), flag(form, "askOpponent"), names,
                                "true".equals(getFormParameterSafely(form, "seenLeftOut")));
                    }
                    case "save" -> {
                        String path = getFormParameterSafely(form, "path");
                        boolean canonical = "true".equals(getFormParameterSafely(form, "canonical"));
                        String as = getFormParameterSafely(form, "as");
                        result = "copy".equals(as) ? _authoring.saveAs(player, path, canonical, admin)
                                : "rename".equals(as) ? _authoring.rename(player, path, canonical, admin)
                                : _authoring.save(player, path, canonical, admin);
                    }
                    case "left" -> {
                        _authoring.left(player);
                        result = new LinkedHashMap<>();
                        result.put("ok", true);
                    }
                    case "check" -> result = _authoring.check(player);
                    case "test" -> {
                        Integer step = number(form, "step");
                        result = _authoring.test(player, step == null ? 1 : step);
                    }
                    case "resume" -> result = _authoring.resume(player);
                    case "close" -> {
                        _authoring.close(player);
                        result = new LinkedHashMap<>();
                        result.put("ok", true);
                    }
                    default -> throw new HttpProcessingException(404);
                }
                responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
            } finally {
                form.destroy();
            }
        } catch (ScenarioAuthoring.AuthoringException exp) {
            writeError(responseWriter, 400, exp.getMessage());
        } catch (HttpProcessingException exp) {
            throw exp;
        } catch (RuntimeException exp) {
            _log.error("The authoring tool could not " + what + " for " + player.getName(), exp);
            writeError(responseWriter, 500, "That went wrong on the server: " + exp);
        }
    }

    private Integer number(HttpPostRequestDecoder form, String name) throws Exception {
        String value = getFormParameterSafely(form, name);
        if (value == null || value.isBlank())
            return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exp) {
            return null;
        }
    }

    private Boolean flag(HttpPostRequestDecoder form, String name) throws Exception {
        String value = getFormParameterSafely(form, name);
        return value == null || value.isBlank() ? null : Boolean.valueOf(value.trim());
    }

    private static void writeError(ResponseWriter responseWriter, int status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        responseWriter.writeJsonResponse(status, JsonUtils.SerializeWithNulls(error));
    }
}
