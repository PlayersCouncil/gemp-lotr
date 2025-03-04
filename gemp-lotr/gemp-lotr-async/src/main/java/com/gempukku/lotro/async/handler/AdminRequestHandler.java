package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.cache.CacheManager;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.DraftTimerProducer;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.lotro.league.*;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.*;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Stream;

public class AdminRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final LotroCardBlueprintLibrary _cardLibrary;
    private final ProductLibrary _productLibrary;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final CacheManager _cacheManager;
    private final HallServer _hallServer;
    private final LotroFormatLibrary _formatLibrary;
    private final LeagueDAO _leagueDao;
    private final CollectionsManager _collectionManager;
    private final PlayerDAO _playerDAO;
    private final AdminService _adminService;
    private final ChatServer _chatServer;
    private final TableDraftDefinitions _tableDraftLibrary;

    private static final Logger _log = LogManager.getLogger(AdminRequestHandler.class);

    public AdminRequestHandler(Map<Type, Object> context) {
        super(context);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _leagueService = extractObject(context, LeagueService.class);
        _tournamentService = extractObject(context, TournamentService.class);
        _cacheManager = extractObject(context, CacheManager.class);
        _hallServer = extractObject(context, HallServer.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _leagueDao = extractObject(context, LeagueDAO.class);
        _playerDAO = extractObject(context, PlayerDAO.class);
        _collectionManager = extractObject(context, CollectionsManager.class);
        _adminService = extractObject(context, AdminService.class);
        _cardLibrary = extractObject(context, LotroCardBlueprintLibrary.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
        _chatServer = extractObject(context, ChatServer.class);
        _tableDraftLibrary = extractObject(context, TableDraftDefinitions.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/clearCache") && request.method() == HttpMethod.POST) {
            clearCache(request, responseWriter);
        } else if (uri.equals("/shutdown") && request.method() == HttpMethod.POST) {
            shutdown(request, responseWriter);
        } else if (uri.equals("/reloadCards") && request.method() == HttpMethod.POST) {
            reloadCards(request, responseWriter);
        } else if (uri.equals("/getMOTD") && request.method() == HttpMethod.GET) {
            getMotd(request, responseWriter);
        }else if (uri.equals("/setMOTD") && request.method() == HttpMethod.POST) {
            setMotd(request, responseWriter);
        }else if (uri.equals("/previewSealedLeague") && request.method() == HttpMethod.POST) {
            processSealedLeague(request, responseWriter, true);
        } else if (uri.equals("/addSealedLeague") && request.method() == HttpMethod.POST) {
            processSealedLeague(request, responseWriter, false);
        } else if (uri.equals("/previewConstructedLeague") && request.method() == HttpMethod.POST) {
            processConstructedLeague(request, responseWriter, true);
        } else if (uri.equals("/addConstructedLeague") && request.method() == HttpMethod.POST) {
            processConstructedLeague(request, responseWriter, false);
        } else if (uri.equals("/processScheduledTournament") && request.method() == HttpMethod.POST) {
            processScheduledTournament(request, responseWriter);
        } else if (uri.equals("/setTournamentStage") && request.method() == HttpMethod.POST) {
            setTournamentStage(request, responseWriter);
        } else if (uri.equals("/addTables") && request.method() == HttpMethod.POST) {
            addTables(request, responseWriter);
        } else if (uri.equals("/previewSoloDraftLeague") && request.method() == HttpMethod.POST) {
            processSoloDraftLeague(request, responseWriter, true);
        } else if (uri.equals("/addSoloDraftLeague") && request.method() == HttpMethod.POST) {
            processSoloDraftLeague(request, responseWriter, false);
        } else if (uri.equals("/addLeaguePlayers") && request.method() == HttpMethod.POST) {
            addPlayersToLeague(request, responseWriter, remoteIp);
        } else if (uri.equals("/addItems") && request.method() == HttpMethod.POST) {
            addItems(request, responseWriter);
        } else if (uri.equals("/addItemsToCollection") && request.method() == HttpMethod.POST) {
            addItemsToCollection(request, responseWriter);
        } else if (uri.equals("/banUser") && request.method() == HttpMethod.POST) {
            banUser(request, responseWriter);
        } else if (uri.equals("/resetUserPassword") && request.method() == HttpMethod.POST) {
            resetUserPassword(request, responseWriter);
        } else if (uri.equals("/banMultiple") && request.method() == HttpMethod.POST) {
            banMultiple(request, responseWriter);
        } else if (uri.equals("/banUserTemp") && request.method() == HttpMethod.POST) {
            banUserTemp(request, responseWriter);
        } else if (uri.equals("/unBanUser") && request.method() == HttpMethod.POST) {
            unBanUser(request, responseWriter);
        } else if (uri.equals("/findMultipleAccounts") && request.method() == HttpMethod.POST) {
            findMultipleAccounts(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void setTournamentStage(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);
        var postDecoder = new HttpPostRequestDecoder(request);

        String tournamentId = getFormParameterSafely(postDecoder, "tournamentId");
        String stageStr = getFormParameterSafely(postDecoder, "stage");

        Throw400IfStringNull("tournamentId", tournamentId);
        Throw400IfStringNull("stage", stageStr);

        var stage = Tournament.Stage.parseStage(stageStr);
        Throw400IfValidationFails("stage", stageStr, stage != null);

        _tournamentService.recordTournamentStage(tournamentId, stage);

        clearCacheInternal();
        responseWriter.sendOK();
    }

    private void addPlayersToLeague(HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        validateEventAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);

        String codeStr = getFormParameterSafely(postDecoder, "code");
        List<String> players = getFormMultipleParametersSafely(postDecoder, "players[]");

        League league = _leagueService.getLeagueByType(codeStr);
        if (league == null) {
            throw new HttpProcessingException(400, "League '" + codeStr + "' does not exist.");
        }

        for(String playerName : players) {
            var player = _playerDAO.getPlayer(playerName);
            if(player == null)
                throw new HttpProcessingException(400, "Player '" + playerName + "' does not exist.");

            if (!_leagueService.isPlayerInLeague(league, player)) {
                if (!_leagueService.playerJoinsLeague(league, player, remoteIp, true, true)) {
                    throw new HttpProcessingException(500, "Failed to add player '" + player + "' to the league.  Aborting.");
                }
            }
        }

        responseWriter.sendOK();
    }

    private void findMultipleAccounts(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login").trim();

            List<Player> similarPlayers = _playerDAO.findSimilarAccounts(login);
            if (similarPlayers == null)
                throw new HttpProcessingException(400);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();
            Element players = doc.createElement("players");

            for (Player similarPlayer : similarPlayers) {
                Element playerElem = doc.createElement("player");
                playerElem.setAttribute("id", String.valueOf(similarPlayer.getId()));
                playerElem.setAttribute("name", similarPlayer.getName());
                playerElem.setAttribute("password", similarPlayer.getPassword());
                playerElem.setAttribute("status", getStatus(similarPlayer));
                playerElem.setAttribute("createIp", similarPlayer.getCreateIp());
                playerElem.setAttribute("loginIp", similarPlayer.getLastIp());
                players.appendChild(playerElem);
            }

            doc.appendChild(players);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private String getStatus(Player similarPlayer) {
        if (similarPlayer.getType().equals(""))
            return "Banned permanently";
        if (similarPlayer.getBannedUntil() != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return "Banned until " + format.format(similarPlayer.getBannedUntil());
        }
        if (similarPlayer.hasType(Player.Type.UNBANNED))
            return "Unbanned";
        return "OK";
    }

    private void resetUserPassword(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login==null)
                throw new HttpProcessingException(400);

            if (!_adminService.resetUserPassword(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login==null)
                throw new HttpProcessingException(400);

            if (!_adminService.banUser(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banMultiple(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            List<String> logins = getFormParametersSafely(postDecoder, "login[]");
            if (logins == null)
                throw new HttpProcessingException(400);

            for (String login : logins) {
                if (!_adminService.banUser(login))
                    throw new HttpProcessingException(404);
            }

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUserTemp(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");
            int duration = Integer.parseInt(getFormParameterSafely(postDecoder, "duration"));

            if (!_adminService.banUserTemp(login, duration))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void unBanUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (!_adminService.unBanUser(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItemsToCollection(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String reason = getFormParameterSafely(postDecoder, "reason");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            var productItems = CardCollection.Item.createItems(product);

            Map<Player, CardCollection> playersCollection = _collectionManager.getPlayersCollection(collectionType);

            for (Map.Entry<Player, CardCollection> playerCollection : playersCollection.entrySet())
                _collectionManager.addItemsToPlayerCollection(true, reason, playerCollection.getKey(), createCollectionType(collectionType), productItems);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItems(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, IOException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String players = getFormParameterSafely(postDecoder, "players");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            var productItems = CardCollection.Item.createItems(product);

            List<String> playerNames = getPlayerNames(players);

            for (String playerName : playerNames) {
                Player player = _playerDao.getPlayer(playerName);

                _collectionManager.addItemsToPlayerCollection(true, "Administrator action", player, createCollectionType(collectionType), productItems);
            }

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private List<String> getPlayerNames(String values) {
        List<String> result = new LinkedList<>();
        for (String pack : values.split("\n")) {
            String blueprint = pack.trim();
            if (!blueprint.isEmpty())
                result.add(blueprint);
        }
        return result;
    }

    private CollectionType createCollectionType(String collectionType) {
        final CollectionType result = CollectionType.parseCollectionCode(collectionType);
        if (result != null)
            return result;

        return _leagueService.getCollectionTypeByCode(collectionType);
    }

    private void addTables(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String name = getFormParameterSafely(postDecoder, "name");
            String tournamentID = getFormParameterSafely(postDecoder, "tournament");
            String formatCode = getFormParameterSafely(postDecoder, "format");
            String timerCode = getFormParameterSafely(postDecoder, "timer");
            List<String> playerones = getFormMultipleParametersSafely(postDecoder, "playerones[]");
            List<String> playertwos = getFormMultipleParametersSafely(postDecoder, "playertwos[]");

            var tournament = _tournamentService.getTournamentById(tournamentID);

            if(tournament == null) {
                throw new HttpProcessingException(400, "Tournament '" + tournamentID + "' not found.");
            }

            var formats = _formatLibrary.getAllFormats();
            if(!formats.containsKey(formatCode)) {
                throw new HttpProcessingException(400, "Format code '" + formatCode + "' not found.");
            }

            var format = formats.get(formatCode);

            var timer = GameTimer.ResolveTimer(timerCode);

            var submittedPlayers = Stream.concat(playerones.stream(), playertwos.stream()).toList();
            var decks = new HashMap<String, LotroDeck>();

            for(String playerName : submittedPlayers) {
                var player = _playerDAO.getPlayer(playerName);
                if(player == null)
                    throw new HttpProcessingException(400, "Player '" + playerName + "' does not exist.");

                var deck = _tournamentService.retrievePlayerDeck(tournament.getTournamentId(), player.getName(), format.getName());
                if(deck == null)
                    throw new HttpProcessingException(400, "Player '" + playerName + "' has no deck registered for '" + tournamentID + "'.");

                if(decks.containsKey(player.getName())) {
                    throw new HttpProcessingException(400, "Player '" + playerName + "' was listed twice.");
                }
                decks.put(player.getName(), deck);
            }

            var spawner = _hallServer.createManualGameSpawner(tournament, format, timer, name);
            for(int i = 0; i < playerones.size(); i++) {
                String p1 = playerones.get(i);
                String p2 = playertwos.get(i);
                spawner.createGame(p1, decks.get(p1), p2, decks.get(p2));
            }

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    /**
     * Processes the passed parameters for a theoretical Constructed League.  Based on the preview parameter, this will
     * either create the league for real, or just return the parsed values to the client so the admin can preview
     * the input.
     * @param request the request
     * @param responseWriter the response writer
     * @param preview If true, no league will be created and the client will have an XML payload returned representing
     *                what the league would be upon creation.  If false, the league will be created for real.
     * @throws Exception
     */
    private void processConstructedLeague(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);

        String name = getFormParameterSafely(postDecoder, "name");
        String description = getFormParameterSafely(postDecoder, "description");
        String costStr = getFormParameterSafely(postDecoder, "cost");
        String startStr = getFormParameterSafely(postDecoder, "start");
        String collectionType = getFormParameterSafely(postDecoder, "collectionType");

        String maxRepeatMatchesStr = getFormParameterSafely(postDecoder, "maxRepeatMatches");
        String inviteOnlyStr = getFormParameterSafely(postDecoder, "inviteOnly");
        String topPrizeStr = getFormParameterSafely(postDecoder, "topPrize");
        String topCutoffStr = getFormParameterSafely(postDecoder, "topCutoff");
        String participationPrizeStr = getFormParameterSafely(postDecoder, "participationPrize");
        String participationGamesStr = getFormParameterSafely(postDecoder, "participationGames");
        //Individual serie definitions
        List<String> formats = getFormMultipleParametersSafely(postDecoder, "format[]");
        List<String> serieDurationsStr = getFormMultipleParametersSafely(postDecoder, "serieDuration[]");
        List<String> maxMatchesStr = getFormMultipleParametersSafely(postDecoder, "maxMatches[]");

        Throw400IfStringNull("name", name);
        int cost = Throw400IfNullOrNonInteger("cost", costStr);
        if(startStr.length() != 8)
            throw new HttpProcessingException(400, "Parameter 'start' must be exactly 8 digits long: YYYYMMDD");
        int start = Throw400IfNullOrNonInteger("start", startStr);
        Throw400IfStringNull("collectionType", collectionType);
        int maxRepeatMatches = Throw400IfNullOrNonInteger("maxRepeatMatches", maxRepeatMatchesStr);
        boolean inviteOnly = inviteOnlyStr.equalsIgnoreCase("true");
        int topCutoff = Throw400IfNullOrNonInteger("topCutoff", topCutoffStr);
        int participationGames = Throw400IfNullOrNonInteger("participationGames", participationGamesStr);
        Throw400IfAnyStringNull("formats", formats);
        List<Integer> serieDurations = Throw400IfAnyNullOrNonInteger("serieDurations", serieDurationsStr);
        List<Integer> maxMatches = Throw400IfAnyNullOrNonInteger("maxMatches", maxMatchesStr);

        if(formats.size() != serieDurations.size() || formats.size() != maxMatches.size())
            throw new HttpProcessingException(400, "Size mismatch between provided formats, serieDurations, and maxMatches");

        var params = new LeagueParams();
        params.name = name;
        params.code = System.currentTimeMillis();
        params.start = DateUtils.ParseDate(start).toLocalDateTime();
        params.cost = cost;
        params.collectionName = collectionType;
        params.inviteOnly = inviteOnly;
        params.maxRepeatMatches = maxRepeatMatches;
        params.description = description;
        params.series = new ArrayList<>();
        for (int i = 0; i < formats.size(); i++) {
            params.series.add(new LeagueParams.SerieData(formats.get(i), serieDurations.get(i), maxMatches.get(i)));
        }
        params.extraPrizes = new LeagueParams.PrizeData(topPrizeStr, topCutoff, participationPrizeStr, participationGames);

        var leagueData = new ConstructedLeague(_productLibrary, _formatLibrary, params);
        List<LeagueSerieInfo> series = leagueData.getSeries();

        var leagueStart = series.getFirst().getStart();
        var displayEnd = series.getLast().getEnd().plusDays(2);

        if(!preview) {
            _leagueDao.addLeague(name, params.code, League.LeagueType.CONSTRUCTED, params, leagueStart, displayEnd, cost);

            _leagueService.clearCache();

            responseWriter.sendXmlOK();
            return;
        }

        //We aren't creating the league for real, so instead we will return the league in XML format for the
        // admin panel preview.

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element leagueElem = doc.createElement("league");

        leagueElem.setAttribute("name", name);
        leagueElem.setAttribute("code", String.valueOf(params.code));
        leagueElem.setAttribute("cost", String.valueOf(cost));
        leagueElem.setAttribute("start", String.valueOf(series.getFirst().getStart()));
        leagueElem.setAttribute("end", String.valueOf(displayEnd));
        leagueElem.setAttribute("collection", collectionType);
        leagueElem.setAttribute("inviteOnly", String.valueOf(inviteOnly));
        leagueElem.setAttribute("maxRepeatMatches", String.valueOf(maxRepeatMatches));
        leagueElem.setAttribute("description", description);
        var topPrize = CardCollection.Item.createItem(topPrizeStr);
        var partPrize = CardCollection.Item.createItem(participationPrizeStr);

        String topID = topPrize.getBlueprintId();
        String top = "";
        try {
            if(!StringUtils.isBlank(topID)) {
                if (_cardLibrary.getLotroCardBlueprint(topID) != null) {
                    top = topPrize.getCount() + "x " + GameUtils.getDeluxeCardLink(topID, _cardLibrary.getLotroCardBlueprint(topID));
                } else if (_productLibrary.GetProduct(topID) != null) {
                    top = topPrize.getCount() + "x " + GameUtils.getProductLink(topID);
                }
            }
        }
        catch (Exception ex){
            top = topPrize.getCount() + "x " + "[UNKNOWN: " + topID + "]";
        }

        String partID = partPrize.getBlueprintId();
        String part = "";
        try {
            if(!StringUtils.isBlank(partID)) {
                if (_cardLibrary.getLotroCardBlueprint(partID) != null) {
                    part = partPrize.getCount() + "x " + GameUtils.getDeluxeCardLink(partID, _cardLibrary.getLotroCardBlueprint(partID));
                } else if (_productLibrary.GetProduct(partID) != null) {
                    part = partPrize.getCount() + "x " + GameUtils.getProductLink(partID);
                }
            }
        }
        catch (Exception ex){
            top = partPrize.getCount() + "x " + "[UNKNOWN: " + topID + "]";
        }

        leagueElem.setAttribute("topPrize", top);
        leagueElem.setAttribute("topCutoff", String.valueOf(topCutoff));
        leagueElem.setAttribute("participationPrize", part);
        leagueElem.setAttribute("participationGames", String.valueOf(participationGames));

        for (LeagueSerieInfo serie : series) {
            Element serieElem = doc.createElement("serie");
            serieElem.setAttribute("type", serie.getName());
            serieElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
            serieElem.setAttribute("start", String.valueOf(serie.getStart()));
            serieElem.setAttribute("end", String.valueOf(serie.getEnd()));
            serieElem.setAttribute("format", serie.getFormat().getName());

            serieElem.setAttribute("limited", String.valueOf(serie.isLimited()));

            leagueElem.appendChild(serieElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponse(doc);
    }

    /**
     * Processes the passed parameters for a theoretical Solo Draft League.  Based on the preview parameter, this will
     * either create the league for real, or just return the parsed values to the client so the admin can preview
     * the input.
     * @param request the request
     * @param responseWriter the response writer
     * @param preview If true, no league will be created and the client will have an XML payload returned representing
     *                what the league would be upon creation.  If false, the league will be created for real.
     * @throws Exception
     */
    private void processSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);

        String name = getFormParameterSafely(postDecoder, "name");
        String description = getFormParameterSafely(postDecoder, "description");
        String costStr = getFormParameterSafely(postDecoder, "cost");
        String startStr = getFormParameterSafely(postDecoder, "start");

        String maxRepeatMatchesStr = getFormParameterSafely(postDecoder, "maxRepeatMatches");
        String inviteOnlyStr = getFormParameterSafely(postDecoder, "inviteOnly");
//        String topPrizeStr = getFormParameterSafely(postDecoder, "topPrize");
//        String topCutoffStr = getFormParameterSafely(postDecoder, "topCutoff");
//        String participationPrizeStr = getFormParameterSafely(postDecoder, "participationPrize");
//        String participationGamesStr = getFormParameterSafely(postDecoder, "participationGames");
        //Individual serie definitions

        String format = getFormParameterSafely(postDecoder, "format");
        String serieDurationStr = getFormParameterSafely(postDecoder, "serieDuration");
        String maxMatchesStr = getFormParameterSafely(postDecoder, "maxMatches");


        Throw400IfStringNull("name", name);
        int cost = Throw400IfNullOrNonInteger("cost", costStr);
        if(startStr.length() != 8)
            throw new HttpProcessingException(400, "Parameter 'start' must be exactly 8 digits long: YYYYMMDD");
        int start = Throw400IfNullOrNonInteger("start", startStr);
        int maxRepeatMatches = Throw400IfNullOrNonInteger("maxRepeatMatches", maxRepeatMatchesStr);
        boolean inviteOnly = inviteOnlyStr.equalsIgnoreCase("true");

//        int topCutoff = Throw400IfNullOrNonInteger("topCutoff", topCutoffStr);
//        int participationGames = Throw400IfNullOrNonInteger("participationGames", participationGamesStr);
        Throw400IfStringNull("format", format);
        int serieDuration = Throw400IfNullOrNonInteger("serieDurationStr", serieDurationStr);
        int maxMatches = Throw400IfNullOrNonInteger("maxMatchesStr", maxMatchesStr);


        var params = new LeagueParams();
        params.name = name;
        params.code = System.currentTimeMillis();
        params.start = DateUtils.ParseDate(start).toLocalDateTime();
        params.cost = cost;
        params.collectionName = name;
        params.inviteOnly = inviteOnly;
        params.maxRepeatMatches = maxRepeatMatches;
        params.description = description;
        params.series = new ArrayList<>();
        params.series.add(new LeagueParams.SerieData(format, serieDuration, maxMatches));
        //params.extraPrizes = new LeagueParams.PrizeData(topPrizeStr, topCutoff, participationPrizeStr, participationGames);

        var leagueData = new SoloDraftLeague(_productLibrary, _formatLibrary, _soloDraftDefinitions, params);
        List<LeagueSerieInfo> series = leagueData.getSeries();

        var leagueStart = series.getFirst().getStart();
        var displayEnd = series.getLast().getEnd().plusDays(2);

        if(!preview) {
            _leagueDao.addLeague(name, params.code, League.LeagueType.SOLODRAFT, params, leagueStart, displayEnd, cost);

            _leagueService.clearCache();

            responseWriter.sendXmlOK();
            return;
        }

        //We aren't creating the league for real, so instead we will return the league in XML format for the
        // admin panel preview.

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element leagueElem = doc.createElement("league");

        leagueElem.setAttribute("name", name);
        leagueElem.setAttribute("code", String.valueOf(params.code));
        leagueElem.setAttribute("cost", String.valueOf(cost));
        leagueElem.setAttribute("start", String.valueOf(series.getFirst().getStart()));
        leagueElem.setAttribute("end", String.valueOf(displayEnd));
        leagueElem.setAttribute("inviteOnly", String.valueOf(inviteOnly));
        leagueElem.setAttribute("maxRepeatMatches", String.valueOf(maxRepeatMatches));
        leagueElem.setAttribute("description", description);
//        var topPrize = CardCollection.Item.createItem(topPrizeStr);
//        var partPrize = CardCollection.Item.createItem(participationPrizeStr);

//        String topID = topPrize.getBlueprintId();
//        String top = "";
//        try {
//            if(!StringUtils.isBlank(topID)) {
//                if (_cardLibrary.getLotroCardBlueprint(topID) != null) {
//                    top = topPrize.getCount() + "x " + GameUtils.getDeluxeCardLink(topID, _cardLibrary.getLotroCardBlueprint(topID));
//                } else if (_productLibrary.GetProduct(topID) != null) {
//                    top = topPrize.getCount() + "x " + GameUtils.getProductLink(topID);
//                }
//            }
//        }
//        catch (Exception ex){
//            top = topPrize.getCount() + "x " + "[UNKNOWN: " + topID + "]";
//        }
//
//        String partID = partPrize.getBlueprintId();
//        String part = "";
//        try {
//            if(!StringUtils.isBlank(partID)) {
//                if (_cardLibrary.getLotroCardBlueprint(partID) != null) {
//                    part = partPrize.getCount() + "x " + GameUtils.getDeluxeCardLink(partID, _cardLibrary.getLotroCardBlueprint(partID));
//                } else if (_productLibrary.GetProduct(partID) != null) {
//                    part = partPrize.getCount() + "x " + GameUtils.getProductLink(partID);
//                }
//            }
//        }
//        catch (Exception ex){
//            top = partPrize.getCount() + "x " + "[UNKNOWN: " + topID + "]";
//        }
//
//        leagueElem.setAttribute("topPrize", top);
//        leagueElem.setAttribute("topCutoff", String.valueOf(topCutoff));
//        leagueElem.setAttribute("participationPrize", part);
//        leagueElem.setAttribute("participationGames", String.valueOf(participationGames));

        for (LeagueSerieInfo serie : series) {
            Element serieElem = doc.createElement("serie");
            serieElem.setAttribute("type", serie.getName());
            serieElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
            serieElem.setAttribute("start", String.valueOf(serie.getStart()));
            serieElem.setAttribute("end", String.valueOf(serie.getEnd()));
            serieElem.setAttribute("format", serie.getFormat().getName());
            serieElem.setAttribute("collection", serie.getCollectionType().getFullName());
            serieElem.setAttribute("limited", String.valueOf(serie.isLimited()));

            leagueElem.appendChild(serieElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponse(doc);
    }

    /**
     * Processes the passed parameters for a theoretical Sealed League.  Based on the preview parameter, this will
     * either create the league for real, or just return the parsed values to the client so the admin can preview
     * the input.
     * @param request the request
     * @param responseWriter the response writer
     * @param preview If true, no league will be created and the client will have an XML payload returned representing
     *                what the league would be upon creation.  If false, the league will be created for real.
     * @throws Exception
     */
    private void processSealedLeague(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);

        String name = getFormParameterSafely(postDecoder, "name");
        String description = getFormParameterSafely(postDecoder, "description");
        String costStr = getFormParameterSafely(postDecoder, "cost");
        String startStr = getFormParameterSafely(postDecoder, "start");

        String maxRepeatMatchesStr = getFormParameterSafely(postDecoder, "maxRepeatMatches");
        String inviteOnlyStr = getFormParameterSafely(postDecoder, "inviteOnly");
//        String topPrizeStr = getFormParameterSafely(postDecoder, "topPrize");
//        String topCutoffStr = getFormParameterSafely(postDecoder, "topCutoff");
//        String participationPrizeStr = getFormParameterSafely(postDecoder, "participationPrize");
//        String participationGamesStr = getFormParameterSafely(postDecoder, "participationGames");
        //Individual serie definitions

        String format = getFormParameterSafely(postDecoder, "format");
        String serieDurationStr = getFormParameterSafely(postDecoder, "serieDuration");
        String maxMatchesStr = getFormParameterSafely(postDecoder, "maxMatches");


        Throw400IfStringNull("name", name);
        int cost = Throw400IfNullOrNonInteger("cost", costStr);
        if(startStr.length() != 8)
            throw new HttpProcessingException(400, "Parameter 'start' must be exactly 8 digits long: YYYYMMDD");
        int start = Throw400IfNullOrNonInteger("start", startStr);
        int maxRepeatMatches = Throw400IfNullOrNonInteger("maxRepeatMatches", maxRepeatMatchesStr);
        boolean inviteOnly = inviteOnlyStr.equalsIgnoreCase("true");

//        int topCutoff = Throw400IfNullOrNonInteger("topCutoff", topCutoffStr);
//        int participationGames = Throw400IfNullOrNonInteger("participationGames", participationGamesStr);
        Throw400IfStringNull("format", format);
        int serieDuration = Throw400IfNullOrNonInteger("serieDuration", serieDurationStr);
        int maxMatches = Throw400IfNullOrNonInteger("maxMatches", maxMatchesStr);


        var params = new LeagueParams();
        params.name = name;
        params.code = System.currentTimeMillis();
        params.start = DateUtils.ParseDate(start).toLocalDateTime();
        params.cost = cost;
        params.collectionName = name;
        params.inviteOnly = inviteOnly;
        params.maxRepeatMatches = maxRepeatMatches;
        params.description = description;
        params.series = new ArrayList<>();
        params.series.add(new LeagueParams.SerieData(format, serieDuration, maxMatches));
        //params.extraPrizes = new LeagueParams.PrizeData(topPrizeStr, topCutoff, participationPrizeStr, participationGames);

        var leagueData = new SealedLeague(_productLibrary, _formatLibrary, params);
        List<LeagueSerieInfo> series = leagueData.getSeries();

        var leagueStart = series.getFirst().getStart();
        var displayEnd = series.getLast().getEnd().plusDays(2);

        if(!preview) {
            _leagueDao.addLeague(name, params.code, League.LeagueType.SEALED, params, leagueStart, displayEnd, cost);

            _leagueService.clearCache();

            responseWriter.sendXmlOK();
            return;
        }

        //We aren't creating the league for real, so instead we will return the league in XML format for the
        // admin panel preview.

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element leagueElem = doc.createElement("league");

        leagueElem.setAttribute("name", name);
        leagueElem.setAttribute("code", String.valueOf(params.code));
        leagueElem.setAttribute("cost", String.valueOf(cost));
        leagueElem.setAttribute("start", String.valueOf(series.getFirst().getStart()));
        leagueElem.setAttribute("end", String.valueOf(displayEnd));
        leagueElem.setAttribute("inviteOnly", String.valueOf(inviteOnly));
        leagueElem.setAttribute("maxRepeatMatches", String.valueOf(maxRepeatMatches));
        leagueElem.setAttribute("description", description);
//        var topPrize = CardCollection.Item.createItem(topPrizeStr);
//        var partPrize = CardCollection.Item.createItem(participationPrizeStr);

//        String topID = topPrize.getBlueprintId();
//        String top = "";
//        try {
//            if(!StringUtils.isBlank(topID)) {
//                if (_cardLibrary.getLotroCardBlueprint(topID) != null) {
//                    top = topPrize.getCount() + "x " + GameUtils.getDeluxeCardLink(topID, _cardLibrary.getLotroCardBlueprint(topID));
//                } else if (_productLibrary.GetProduct(topID) != null) {
//                    top = topPrize.getCount() + "x " + GameUtils.getProductLink(topID);
//                }
//            }
//        }
//        catch (Exception ex){
//            top = topPrize.getCount() + "x " + "[UNKNOWN: " + topID + "]";
//        }
//
//        String partID = partPrize.getBlueprintId();
//        String part = "";
//        try {
//            if(!StringUtils.isBlank(partID)) {
//                if (_cardLibrary.getLotroCardBlueprint(partID) != null) {
//                    part = partPrize.getCount() + "x " + GameUtils.getDeluxeCardLink(partID, _cardLibrary.getLotroCardBlueprint(partID));
//                } else if (_productLibrary.GetProduct(partID) != null) {
//                    part = partPrize.getCount() + "x " + GameUtils.getProductLink(partID);
//                }
//            }
//        }
//        catch (Exception ex){
//            top = partPrize.getCount() + "x " + "[UNKNOWN: " + topID + "]";
//        }
//
//        leagueElem.setAttribute("topPrize", top);
//        leagueElem.setAttribute("topCutoff", String.valueOf(topCutoff));
//        leagueElem.setAttribute("participationPrize", part);
//        leagueElem.setAttribute("participationGames", String.valueOf(participationGames));

        for (LeagueSerieInfo serie : series) {
            Element serieElem = doc.createElement("serie");
            serieElem.setAttribute("type", serie.getName());
            serieElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
            serieElem.setAttribute("start", String.valueOf(serie.getStart()));
            serieElem.setAttribute("end", String.valueOf(serie.getEnd()));
            serieElem.setAttribute("format", serie.getFormat().getName());
            serieElem.setAttribute("collection", serie.getCollectionType().getFullName());
            serieElem.setAttribute("limited", String.valueOf(serie.isLimited()));

            leagueElem.appendChild(serieElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponse(doc);
    }

    /**
     * Processes the passed parameters for a theoretical scheduled tournament.  Based on the preview parameter, this will
     * either create the tournament for real, or just return the parsed values to the client so the admin can preview
     * the input.
     * @param request the request
     * @param responseWriter the response writer
     * @throws Exception
     */
    private void processScheduledTournament(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);

        String previewStr = getFormParameterSafely(postDecoder, "preview");
        boolean preview = Throw400IfNullOrNonBoolean("preview", previewStr);

        String name = getFormParameterSafely(postDecoder, "name");
        String typeStr = getFormParameterSafely(postDecoder, "type");

        String deckbuildingDurationStr = getFormParameterSafely(postDecoder, "deckbuildingDuration");
        String turnInDurationStr = getFormParameterSafely(postDecoder, "turnInDuration");
        String sealedFormatCodeStr = getFormParameterSafely(postDecoder, "sealedFormatCode");

        String soloDraftDeckbuildingDurationStr = getFormParameterSafely(postDecoder, "soloDraftDeckbuildingDuration");
        String soloDraftTurnInDurationStr = getFormParameterSafely(postDecoder, "soloDraftTurnInDuration");
        String soloDraftFormatCodeStr = getFormParameterSafely(postDecoder, "soloDraftFormatCode");

        String soloTableDraftDeckbuildingDurationStr = getFormParameterSafely(postDecoder, "soloTableDraftDeckbuildingDuration");
        String soloTableDraftTurnInDurationStr = getFormParameterSafely(postDecoder, "soloTableDraftTurnInDuration");
        String soloTableDraftFormatCodeStr = getFormParameterSafely(postDecoder, "soloTableDraftFormatCode");

        String tableDraftDeckbuildingDurationStr = getFormParameterSafely(postDecoder, "tableDraftDeckbuildingDuration");
        String tableDraftTurnInDurationStr = getFormParameterSafely(postDecoder, "tableDraftTurnInDuration");
        String tableDraftFormatCodeStr = getFormParameterSafely(postDecoder, "tableDraftFormatCode");
        String tableDraftTimer = getFormParameterSafely(postDecoder, "tableDraftTimer");

        String wcStr = getFormParameterSafely(postDecoder, "wc");
        String tournamentId = getFormParameterSafely(postDecoder, "tournamentId");
        String formatStr = getFormParameterSafely(postDecoder, "formatCode");
        String startStr = getFormParameterSafely(postDecoder, "start");
        String costStr = getFormParameterSafely(postDecoder, "cost");
        String playoff = getFormParameterSafely(postDecoder, "playoff");
        String tiebreaker = getFormParameterSafely(postDecoder, "tiebreaker");
        String prizeStructure = getFormParameterSafely(postDecoder, "prizeStructure");
        String minPlayersStr = getFormParameterSafely(postDecoder, "minPlayers");
        String manualKickoffStr = getFormParameterSafely(postDecoder, "manualKickoff");

        //String inviteOnlyStr = getFormParameterSafely(postDecoder, "inviteOnly");
        //String topPrizeStr = getFormParameterSafely(postDecoder, "topPrize");
        //String topCutoffStr = getFormParameterSafely(postDecoder, "topCutoff");
        //String participationPrizeStr = getFormParameterSafely(postDecoder, "participationPrize");
        //String participationGamesStr = getFormParameterSafely(postDecoder, "participationGames");

        Throw400IfStringNull("type", typeStr);
        var type = Tournament.TournamentType.parse(typeStr);
        Throw400IfValidationFails("type", typeStr, type != null);
        Throw400IfStringNull("name", name);
        boolean wc = ParseBoolean("wc", wcStr, false);
        Throw400IfStringNull("tournamentId", tournamentId);
        Throw400IfStringNull("format", formatStr);
        Throw400IfStringNull("start", startStr);

        ZonedDateTime start = null;
        try {
            start = DateUtils.ParseDate(startStr);
        }
        catch(DateTimeParseException ex) {
            Throw400IfValidationFails("start", startStr, false);
        }

        Throw400IfValidationFails("start", startStr, DateUtils.Now().isBefore(start), "Start date/time must be in the future.");

        int cost = Throw400IfNullOrNonInteger("cost", costStr);

        Throw400IfValidationFails("playoff", playoff,Tournament.getPairingMechanism(playoff) != null);
        //Turns out prizes are busted at the moment and are always Daily.
        //var prizes = Tournament.getTournamentPrizes(_productLibrary, prizeStructure);
        int minPlayers = Throw400IfNullOrNonInteger("minPlayers", minPlayersStr);
        boolean manualKickoff = ParseBoolean("manualKickoff", manualKickoffStr, false);

        if(wc) {
            tournamentId = DateUtils.Now().getYear() + "-wc-" + tournamentId;
        }

        Throw400IfValidationFails("tournamentId", tournamentId, _tournamentService.getTournamentById(tournamentId) == null, "Tournament with that Id already exists.");
        Throw400IfValidationFails("tournamentId", tournamentId, _tournamentService.getScheduledTournamentById(tournamentId) == null, "Scheduled Tournament with that Id already exists.");

        var params = new TournamentParams();

        if(type == Tournament.TournamentType.SEALED) {
            var sealedParams = new SealedTournamentParams();
            sealedParams.type = Tournament.TournamentType.SEALED;

            sealedParams.deckbuildingDuration = Throw400IfNullOrNonInteger("deckbuildingDuration", deckbuildingDurationStr);
            sealedParams.turnInDuration = Throw400IfNullOrNonInteger("turnInDuration", turnInDurationStr);

            Throw400IfStringNull("sealedFormatCode", sealedFormatCodeStr);
            var sealedFormat = _formatLibrary.GetSealedTemplate(sealedFormatCodeStr);
            Throw400IfValidationFails("sealedFormatCode", formatStr,sealedFormat != null);
            sealedParams.sealedFormatCode = sealedFormatCodeStr;
            sealedParams.format = sealedFormat.GetFormat().getCode();
            sealedParams.requiresDeck = false;
            params = sealedParams;
        }
        else if (type == Tournament.TournamentType.SOLODRAFT) {
            var soloDraftParams = new SoloDraftTournamentParams();
            soloDraftParams.type = Tournament.TournamentType.SOLODRAFT;

            soloDraftParams.deckbuildingDuration = Throw400IfNullOrNonInteger("soloDraftDeckbuildingDuration", soloDraftDeckbuildingDurationStr);
            soloDraftParams.turnInDuration = Throw400IfNullOrNonInteger("soloDraftTurnInDuration", soloDraftTurnInDurationStr);

            Throw400IfStringNull("soloDraftFormatCode", soloDraftFormatCodeStr);
            var soloDraftFormat = _soloDraftDefinitions.getSoloDraft(soloDraftFormatCodeStr);
            Throw400IfValidationFails("soloDraftFormatCode", soloDraftFormatCodeStr,soloDraftFormat != null);
            soloDraftParams.soloDraftFormatCode = soloDraftFormatCodeStr;
            soloDraftParams.format = soloDraftFormat.getFormat();
            soloDraftParams.requiresDeck = false;
            params = soloDraftParams;
        }
        else if (type == Tournament.TournamentType.TABLE_SOLODRAFT) {
            var soloTableDraftParams = new SoloTableDraftTournamentParams();
            soloTableDraftParams.type = Tournament.TournamentType.TABLE_SOLODRAFT;

            soloTableDraftParams.deckbuildingDuration = Throw400IfNullOrNonInteger("soloTableDraftDeckbuildingDuration", soloTableDraftDeckbuildingDurationStr);
            soloTableDraftParams.turnInDuration = Throw400IfNullOrNonInteger("soloTableDraftTurnInDuration", soloTableDraftTurnInDurationStr);

            Throw400IfStringNull("soloTableDraftFormatCode", soloTableDraftFormatCodeStr);
            var tableDraftDefinition = _tableDraftLibrary.getTableDraftDefinition(soloTableDraftFormatCodeStr);
            Throw400IfValidationFails("soloTableDraftFormatCode", soloTableDraftFormatCodeStr,tableDraftDefinition != null);
            soloTableDraftParams.soloTableDraftFormatCode = soloTableDraftFormatCodeStr;
            soloTableDraftParams.format = tableDraftDefinition.getFormat();
            soloTableDraftParams.requiresDeck = false;
            params = soloTableDraftParams;
        }
        else if (type == Tournament.TournamentType.TABLE_DRAFT) {
            var tableDraftParams = new TableDraftTournamentParams();
            tableDraftParams.type = Tournament.TournamentType.TABLE_DRAFT;

            tableDraftParams.deckbuildingDuration = Throw400IfNullOrNonInteger("tableDraftDeckbuildingDuration", tableDraftDeckbuildingDurationStr);
            tableDraftParams.turnInDuration = Throw400IfNullOrNonInteger("tableDraftTurnInDuration", tableDraftTurnInDurationStr);

            Throw400IfStringNull("tableDraftFormatCode", tableDraftFormatCodeStr);
            var tableDraftDefinition = _tableDraftLibrary.getTableDraftDefinition(tableDraftFormatCodeStr);
            Throw400IfValidationFails("tableDraftFormatCode", tableDraftFormatCodeStr,tableDraftDefinition != null);
            tableDraftParams.tableDraftFormatCode = tableDraftFormatCodeStr;
            tableDraftParams.format = tableDraftDefinition.getFormat();
            tableDraftParams.requiresDeck = false;
            tableDraftParams.draftTimerProducerType = DraftTimerProducer.getDraftTimerProducer(tableDraftTimer);
            tableDraftParams.maximumPlayers = tableDraftDefinition.getMaxPlayers();
            params = tableDraftParams;
        }
        else {
            params.type = Tournament.TournamentType.CONSTRUCTED;
            var format = _formatLibrary.getFormat(formatStr);
            Throw400IfValidationFails("format", formatStr,format != null);
            params.format = formatStr;
            params.requiresDeck = true;
        }

        params.name = name;
        params.tournamentId = tournamentId;
        params.startTime = start.toLocalDateTime();
        params.cost = cost;
        params.playoff = Tournament.PairingType.parse(playoff);
        params.tiebreaker = "owr";
        params.prizes = Tournament.PrizeType.DAILY;
        params.minimumPlayers = minPlayers;
        params.manualKickoff = manualKickoff;

        TournamentInfo info;

        if(type == Tournament.TournamentType.SEALED) {
            info = new SealedTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, start, (SealedTournamentParams)params);
        }
        else if (type == Tournament.TournamentType.SOLODRAFT) {
            info = new SoloDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, start, ((SoloDraftTournamentParams) params), _soloDraftDefinitions);
        }
        else if (type == Tournament.TournamentType.TABLE_SOLODRAFT) {
            info = new SoloTableDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, start, ((SoloTableDraftTournamentParams) params), _tableDraftLibrary);
        }
        else if (type == Tournament.TournamentType.TABLE_DRAFT) {
            info = new TableDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, start, ((TableDraftTournamentParams) params), _tableDraftLibrary);
        }
        else {
            info = new TournamentInfo(_tournamentService, _productLibrary, _formatLibrary, start, params);
        }

        if(!preview) {
            _tournamentService.addScheduledTournament(info);
            responseWriter.sendJsonOK();
            return;
        }

        //We aren't creating the tournament for real, so instead we will return the tournament in JSON format for the
        // admin panel preview.

        responseWriter.writeJsonResponse(JsonUtils.Serialize(params));
    }

    private void getMotd(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, IOException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String motd = _hallServer.getMOTD();

            if(motd != null) {
                responseWriter.writeJsonResponse(motd.replace("\n", "<br>"));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void setMotd(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, IOException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String motd = getFormParameterSafely(postDecoder, "motd");

            _hallServer.setMOTD(motd);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void shutdown(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            boolean shutdown = Boolean.parseBoolean(getFormParameterSafely(postDecoder, "shutdown"));

            _hallServer.setShutdown(shutdown);

            responseWriter.writeHtmlResponse("OK");
        } catch (Exception e) {
            _log.error("Error response for " + request.uri(), e);
            responseWriter.writeHtmlResponse("Error handling request");
        } finally {
            postDecoder.destroy();
        }
    }

    private void reloadCards(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, InterruptedException {
        validateAdmin(request);

        _chatServer.sendSystemMessageToAllChatRooms("@everyone Server is reloading card definitions.  This will impact game speed until it is complete.");

        _cardLibrary.reloadAllDefinitions();

        _productLibrary.ReloadPacks();

        _formatLibrary.ReloadFormats();
        _formatLibrary.ReloadSealedTemplates();

        _soloDraftDefinitions.ReloadDraftsFromFile();

        _chatServer.sendSystemMessageToAllChatRooms("@everyone Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page and please let the mod team know in the game hall ASAP if the problem doesn't go away.");

        responseWriter.writeHtmlResponse("OK");
    }

    private void clearCache(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, SQLException, IOException {
        validateAdmin(request);

        int before = _cacheManager.getTotalCount();
        clearCacheInternal();
        int after = _cacheManager.getTotalCount();

        responseWriter.writeHtmlResponse("Before: " + before + "<br><br>After: " + after);
    }

    private void clearCacheInternal() throws SQLException, IOException {
        _leagueService.clearCache();
        _tournamentService.clearCache();
        _cacheManager.clearCaches();
        _hallServer.cleanup(true);
    }
}
