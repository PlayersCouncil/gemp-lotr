package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.cache.CacheManager;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.LeagueParticipationDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.lotro.league.*;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.prizes.PrizeDefinitionException;
import com.gempukku.lotro.prizes.PrizeItem;
import com.gempukku.lotro.prizes.PrizeService;
import com.gempukku.lotro.prizes.PrizeTier;
import com.gempukku.lotro.service.AdminLookupService;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.*;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
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
import java.time.LocalDate;
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
    private final LeagueParticipationDAO _leagueParticipationDao;
    private final CollectionsManager _collectionManager;
    private final PlayerDAO _playerDAO;
    private final AdminService _adminService;
    private final ChatServer _chatServer;
    private final TableDraftDefinitions _tableDraftLibrary;
    private final LeagueFactory _leagueFactory;
    private final LeagueScheduleService _leagueScheduleService;
    private final MarkdownParser _markdownParser;
    private final PrizeService _prizeService;
    // t4-add-items: card / product / player lookups for the add-items form
    private final AdminLookupService _lookupService;

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
        _leagueParticipationDao = extractObject(context, LeagueParticipationDAO.class);
        _playerDAO = extractObject(context, PlayerDAO.class);
        _collectionManager = extractObject(context, CollectionsManager.class);
        _adminService = extractObject(context, AdminService.class);
        _cardLibrary = extractObject(context, LotroCardBlueprintLibrary.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
        _chatServer = extractObject(context, ChatServer.class);
        _tableDraftLibrary = extractObject(context, TableDraftDefinitions.class);
        _leagueFactory = extractObject(context, LeagueFactory.class);
        _leagueScheduleService = extractObject(context, LeagueScheduleService.class);
        _markdownParser = extractObject(context, MarkdownParser.class);
        _prizeService = extractObject(context, PrizeService.class);
        _lookupService = new AdminLookupService(_cardLibrary, _productLibrary, _playerDAO);
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
        } else if (uri.equals("/previewRTMDLeague") && request.method() == HttpMethod.POST) {
            processRTMDLeague(request, responseWriter, true);
        } else if (uri.equals("/addRTMDLeague") && request.method() == HttpMethod.POST) {
            processRTMDLeague(request, responseWriter, false);
        } else if (uri.equals("/league") && request.method() == HttpMethod.GET) {
            getLeague(request, responseWriter);
        } else if (uri.equals("/updateLeague") && request.method() == HttpMethod.POST) {
            updateLeague(request, responseWriter);
        } else if (uri.equals("/markdownPreview") && request.method() == HttpMethod.POST) {
            markdownPreview(request, responseWriter);
        } else if (uri.equals("/leagueSchedules") && request.method() == HttpMethod.GET) {
            getLeagueSchedules(request, responseWriter);
        } else if (uri.equals("/saveLeagueSchedule") && request.method() == HttpMethod.POST) {
            saveLeagueSchedule(request, responseWriter, false);
        } else if (uri.equals("/previewLeagueSchedule") && request.method() == HttpMethod.POST) {
            saveLeagueSchedule(request, responseWriter, true);
        } else if (uri.equals("/deleteLeagueSchedule") && request.method() == HttpMethod.POST) {
            deleteLeagueSchedule(request, responseWriter);
        } else if (uri.equals("/runLeagueSchedule") && request.method() == HttpMethod.POST) {
            runLeagueSchedule(request, responseWriter);
        } else if (uri.equals("/rtmdModifiers") && request.method() == HttpMethod.GET) {
            getRTMDModifiers(request, responseWriter);
        } else if (uri.equals("/rtmdRandomPath") && request.method() == HttpMethod.GET) {
            getRTMDRandomPath(request, responseWriter);
        } else if (uri.equals("/prizes") && request.method() == HttpMethod.GET) {
            getPrizes(request, responseWriter);
        } else if (uri.equals("/prizeHolders") && request.method() == HttpMethod.GET) {
            getPrizeHolders(request, responseWriter);
        } else if (uri.equals("/resolvePrize") && request.method() == HttpMethod.POST) {
            resolvePrize(request, responseWriter);
        } else if (uri.equals("/addPromise") && request.method() == HttpMethod.POST) {
            addPromise(request, responseWriter);
        } else if (uri.equals("/cardName") && request.method() == HttpMethod.GET) {
            getCardName(request, responseWriter);
        } else if (uri.equals("/processScheduledTournament") && request.method() == HttpMethod.POST) {
            processScheduledTournament(request, responseWriter, false);
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
        // ---- t4-add-items: lookups for the add-items form on the Prizes tab ----
        } else if (uri.equals("/searchPlayers") && request.method() == HttpMethod.GET) {
            searchPlayers(request, responseWriter);
        } else if (uri.equals("/resolvePlayers") && request.method() == HttpMethod.POST) {
            resolvePlayers(request, responseWriter);
        } else if (uri.equals("/searchItems") && request.method() == HttpMethod.GET) {
            searchItems(request, responseWriter);
        } else if (uri.equals("/addItemsCollections") && request.method() == HttpMethod.GET) {
            getAddItemsCollections(request, responseWriter);
        // ---- end t4-add-items ----
        // ---- t4-admin-load-from-event: "load from event" on the add-items form ----
        } else if (uri.equals("/addItemsEvents") && request.method() == HttpMethod.GET) {
            getAddItemsEvents(request, responseWriter);
        } else if (uri.equals("/addItemsEventParticipants") && request.method() == HttpMethod.GET) {
            getAddItemsEventParticipants(request, responseWriter);
        // ---- end t4-admin-load-from-event ----
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
        } else if (uri.equals("/toggleSealedHallStatus") && request.method() == HttpMethod.POST) {
            toggleSealedHallStatus(request, responseWriter);
        // ---- t3-tournament-admin: unified tournament form ----
        } else if (uri.equals("/tournament") && request.method() == HttpMethod.GET) {
            getTournamentForAdmin(request, responseWriter);
        } else if (uri.equals("/tournaments") && request.method() == HttpMethod.GET) {
            getTournamentsForAdmin(request, responseWriter);
        } else if (uri.equals("/tournamentPlayers") && request.method() == HttpMethod.GET) {
            getTournamentPlayersForAdmin(request, responseWriter);
        } else if (uri.equals("/updateScheduledTournament") && request.method() == HttpMethod.POST) {
            processScheduledTournament(request, responseWriter, true);
        // ---- end t3-tournament-admin ----
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void toggleSealedHallStatus(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);
        var postDecoder = new HttpPostRequestDecoder(request);

        String sealedFormatCodeStr = getFormParameterSafely(postDecoder, "sealedFormatCode");


        Throw400IfStringNull("sealedFormatCode", sealedFormatCodeStr);
        var sealedFormat = _formatLibrary.GetSealedTemplate(sealedFormatCodeStr);
        Throw400IfValidationFails("sealedFormatCode", sealedFormatCodeStr,sealedFormat != null);

        if (_formatLibrary.toggleSealedInHall(sealedFormatCodeStr)) {
            responseWriter.writeHtmlResponse("Added format");
        } else {
            responseWriter.writeHtmlResponse("Removed format");
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
            String durationParam = getFormParameterSafely(postDecoder, "duration");
            int duration;
            try {
                duration = Integer.parseInt(durationParam == null ? "" : durationParam.trim());
            } catch (NumberFormatException exp) {
                throw new HttpProcessingException(400);
            }

            if (login == null || duration <= 0)
                throw new HttpProcessingException(400);

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

            // t4-add-items: the Prizes tab form asks for everything to be checked first and for a per-player report
            if ("true".equals(getFormParameterSafely(postDecoder, "detailed"))) {
                addItemsDetailed(players, product, collectionType, getFormParameterSafely(postDecoder, "reason"), responseWriter);
                return;
            }

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

    // ---- t4-add-items ----------------------------------------------------------------------------------------------

    private static final String ADD_ITEMS_DEFAULT_REASON = "Administrator action";

    /**
     * POST /addItems with detailed=true: the same award as plain /addItems, but everything is checked before
     * anything is handed out (unknown players, items or collection -> 400 and nothing awarded), the collection must be
     * permanent, trophy or an active sealed/draft league's collection, an optional reason is recorded on the
     * transfer, and the answer is JSON:
     * {collectionType, collectionName, reason, items:[{item, kind, name}], results:[{player, status: ok|skipped|error, message}], awarded}
     */
    private void addItemsDetailed(String players, String product, String collectionCode, String reason,
                                  ResponseWriter responseWriter) throws HttpProcessingException {
        Throw400IfStringNull("collectionType", collectionCode);
        Throw400IfStringNull("product", product);
        Throw400IfStringNull("players", players);

        collectionCode = collectionCode.trim();
        CollectionType collectionType = null;
        boolean leagueCollection = false;
        if (collectionCode.equals(CollectionType.MY_CARDS.getCode()))
            collectionType = CollectionType.MY_CARDS;
        else if (collectionCode.equals(CollectionType.TROPHY.getCode()))
            collectionType = CollectionType.TROPHY;
        else if (CollectionType.parseCollectionCode(collectionCode) == null) {
            collectionType = _leagueService.getCollectionTypeByCode(collectionCode);
            leagueCollection = collectionType != null;
        }
        if (collectionType == null)
            throw new HttpProcessingException(400, "Items can only be added to My cards, Trophies or the collection of an active sealed or draft league; '"
                    + collectionCode + "' is none of those.");

        // items: every line must parse and name a real card / product
        var itemDescriptions = new ArrayList<Map<String, Object>>();
        var productItems = new ArrayList<CardCollection.Item>();
        var problems = new ArrayList<String>();
        for (String line : product.split("\n")) {
            if (line.isBlank())
                continue;
            var check = _lookupService.checkItemLine(line);
            if (check.problem() != null) {
                problems.add(check.problem());
                continue;
            }
            productItems.add(CardCollection.Item.createItem(check.line()));
            var d = new LinkedHashMap<String, Object>();
            d.put("item", check.line());
            d.put("kind", check.kind().label);
            d.put("name", check.name());
            itemDescriptions.add(d);
        }
        if (!problems.isEmpty())
            throw new HttpProcessingException(400, String.join(" ", problems) + " Nothing was awarded.");
        if (productItems.isEmpty())
            throw new HttpProcessingException(400, "No items to award.");

        // players: every name must be a real player
        var recipients = new ArrayList<Player>();
        var unknown = new ArrayList<String>();
        var seen = new HashSet<String>();
        for (String name : getPlayerNames(players)) {
            Player player = _playerDAO.getPlayer(name);
            if (player == null)
                unknown.add(name);
            else if (seen.add(player.getName()))
                recipients.add(player);
        }
        if (!unknown.isEmpty())
            throw new HttpProcessingException(400, "Unknown player" + (unknown.size() == 1 ? "" : "s") + ": "
                    + String.join(", ", unknown) + ". Nothing was awarded.");
        if (recipients.isEmpty())
            throw new HttpProcessingException(400, "No players to award.");

        String transferReason = (reason == null || reason.isBlank()) ? ADD_ITEMS_DEFAULT_REASON : reason.trim();
        if (transferReason.length() > 255)
            throw new HttpProcessingException(400, "The reason must be 255 characters or less.");

        var results = new ArrayList<Map<String, Object>>();
        int awarded = 0;
        for (Player player : recipients) {
            var r = new LinkedHashMap<String, Object>();
            r.put("player", player.getName());
            try {
                // a league collection only exists once the player has joined; addItemsToPlayerCollection would
                // silently do nothing, so say so instead
                if (leagueCollection && _collectionManager.getPlayerCollection(player, collectionType.getCode()) == null) {
                    r.put("status", "skipped");
                    r.put("message", "has not joined that league, so has no such collection");
                } else {
                    _collectionManager.addItemsToPlayerCollection(true, transferReason, player, collectionType, productItems);
                    r.put("status", "ok");
                    r.put("message", null);
                    awarded++;
                }
            } catch (RuntimeException exp) {
                _log.error("Unable to add items to " + player.getName() + "'s " + collectionType.getCode() + " collection", exp);
                r.put("status", "error");
                r.put("message", "server error: " + exp.getMessage());
            }
            results.add(r);
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("collectionType", collectionType.getCode());
        result.put("collectionName", collectionType.getFullName());
        result.put("reason", transferReason);
        result.put("items", itemDescriptions);
        result.put("results", results);
        result.put("awarded", awarded);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    private int parseLimit(String value) {
        if (value == null || value.isBlank())
            return AdminLookupService.DEFAULT_LIMIT;
        try {
            return AdminLookupService.clampLimit(Integer.parseInt(value.trim()));
        } catch (NumberFormatException exp) {
            return AdminLookupService.DEFAULT_LIMIT;
        }
    }

    /**
     * GET /searchPlayers?q=...&limit=... : {players:[name...]}, names containing q (ignoring case), best first.
     */
    private void searchPlayers(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        var queryDecoder = new QueryStringDecoder(request.uri());
        String query = getQueryParameterSafely(queryDecoder, "q");
        int limit = parseLimit(getQueryParameterSafely(queryDecoder, "limit"));

        var result = new LinkedHashMap<String, Object>();
        result.put("query", query == null ? "" : query);
        result.put("players", _lookupService.searchPlayers(query, limit));
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * POST /resolvePlayers {players: names separated by newlines, commas, semicolons or spaces} :
     * {players:[{input, name (null when unknown), suggestions:[...]}]}
     */
    private void resolvePlayers(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        try {
            String players = getFormParameterSafely(postDecoder, "players");
            var list = new ArrayList<Map<String, Object>>();
            for (var resolution : _lookupService.resolvePlayers(AdminLookupService.splitPlayerList(players))) {
                var p = new LinkedHashMap<String, Object>();
                p.put("input", resolution.input());
                p.put("name", resolution.name());
                p.put("suggestions", resolution.suggestions());
                list.add(p);
            }
            var result = new LinkedHashMap<String, Object>();
            result.put("players", list);
            responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
        } finally {
            postDecoder.destroy();
        }
    }

    /**
     * GET /searchItems?q=...&limit=... : {items:[{value, kind: card|pack|selection|award, title, subtitle, detail}]},
     * cards (by title, accents ignored, or by blueprint id) and products (by name), best first.  value is what goes
     * after the "Nx" in the addItems product list.
     */
    private void searchItems(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        var queryDecoder = new QueryStringDecoder(request.uri());
        String query = getQueryParameterSafely(queryDecoder, "q");
        int limit = parseLimit(getQueryParameterSafely(queryDecoder, "limit"));

        var items = new ArrayList<Map<String, Object>>();
        for (var entry : _lookupService.searchItems(query, limit)) {
            var i = new LinkedHashMap<String, Object>();
            i.put("value", entry.value());
            i.put("kind", entry.kind().label);
            i.put("title", entry.title());
            i.put("subtitle", entry.subtitle());
            i.put("detail", entry.detail());
            items.add(i);
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("query", query == null ? "" : query);
        result.put("items", items);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    /**
     * GET /addItemsCollections : {collections:[{value, label, kind: permanent|trophy|league, leagueName, leagueType,
     * start, end, running}]}: My cards, Trophies, then the collection of every active sealed / draft league.
     */
    private void getAddItemsCollections(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        var collections = new ArrayList<Map<String, Object>>();
        collections.add(describeFixedCollection(CollectionType.MY_CARDS, "Permanent (My cards)", "permanent"));
        collections.add(describeFixedCollection(CollectionType.TROPHY, "Trophy", "trophy"));

        var now = DateUtils.Now();
        for (var lc : _leagueService.getActiveLeagueCollections()) {
            var c = new LinkedHashMap<String, Object>();
            c.put("value", lc.code());
            c.put("label", lc.leagueName());
            c.put("kind", "league");
            c.put("leagueName", lc.leagueName());
            c.put("collectionName", lc.collectionName());
            c.put("leagueType", lc.type() == null ? null : lc.type().toString());
            c.put("start", lc.start() == null ? null : lc.start().toLocalDate().toString());
            c.put("end", lc.end() == null ? null : lc.end().toLocalDate().toString());
            c.put("running", lc.start() == null || !lc.start().isAfter(now));
            collections.add(c);
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("collections", collections);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    private static Map<String, Object> describeFixedCollection(CollectionType type, String label, String kind) {
        var c = new LinkedHashMap<String, Object>();
        c.put("value", type.getCode());
        c.put("label", label);
        c.put("kind", kind);
        c.put("leagueName", null);
        c.put("collectionName", type.getFullName());
        c.put("leagueType", null);
        c.put("start", null);
        c.put("end", null);
        c.put("running", true);
        return c;
    }

    // ---- end t4-add-items ------------------------------------------------------------------------------------------

    // ---- t4-admin-load-from-event: "load from event" on the add-items form -------------------------------------------

    // "recent window" per the maintainer's request; plus anything still running, regardless of how long ago it started
    private static final int LOAD_FROM_EVENT_RECENT_DAYS = 120;

    // how far ahead a scheduled tournament may start and still be listed (only once players have signed up for it;
    // sign-up opens when its queue is loaded, a few days before the start)
    private static final int LOAD_FROM_EVENT_UPCOMING_DAYS = 60;

    /**
     * GET /addItemsEvents : {events:[{kind: league|tournament, id, name, start, end, status, running, playerCount}]}
     * Leagues and tournaments running now, finished within the last {@value #LOAD_FROM_EVENT_RECENT_DAYS} days, or
     * not started yet but with at least one player signed up; running events first, then newest start first (see
     * {@link AdminLookupService#EVENT_ORDER}: events yet to start sort before finished ones).  playerCount is the
     * number of sign-ups for an event that has not started, null for the rest: counting everyone would cost queries
     * for every listed event, and the count is reported once an event is loaded ({@link #getAddItemsEventParticipants}).
     */
    private void getAddItemsEvents(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);
        var now = DateUtils.Now();

        var events = new ArrayList<AdminLookupService.EventEntry>();
        events.addAll(collectLeagueEvents(now));
        events.addAll(collectTournamentEvents(now));
        events.sort(AdminLookupService.EVENT_ORDER);

        var list = new ArrayList<Map<String, Object>>();
        for (var event : events) {
            var o = new LinkedHashMap<String, Object>();
            o.put("kind", event.kind() == AdminLookupService.EventKind.LEAGUE ? "league" : "tournament");
            o.put("id", event.id());
            o.put("name", event.name());
            o.put("start", event.start());
            o.put("end", event.end());
            o.put("status", event.status());
            o.put("running", event.running());
            o.put("playerCount", event.playerCount());
            list.add(o);
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("events", list);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    /**
     * Every active league ({@code getActiveLeagues} is every league whose {@code end_date} has not passed), then
     * every league that ended in the last {@link #LOAD_FROM_EVENT_RECENT_DAYS} days that is not already listed. An
     * active league that has not started yet (the scheduler creates them ahead of their start) is listed, as
     * "upcoming" with its sign-up count, only when someone has signed up.  Dates come from the league's series (its
     * own record carries none); a league whose stored definition no longer parses is still listed, with no dates.
     */
    private List<AdminLookupService.EventEntry> collectLeagueEvents(ZonedDateTime now) {
        var result = new ArrayList<AdminLookupService.EventEntry>();
        var seen = new HashSet<String>();

        for (League league : _leagueService.getActiveLeagues()) {
            String code = league.getCodeStr();
            ZonedDateTime start = null;
            ZonedDateTime end = null;
            try {
                for (var serie : league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).getSeries()) {
                    if (serie.getStart() != null && (start == null || serie.getStart().isBefore(start)))
                        start = serie.getStart();
                    if (serie.getEnd() != null && (end == null || serie.getEnd().isAfter(end)))
                        end = serie.getEnd();
                }
            } catch (RuntimeException ignored) {
                // parameters that no longer parse: still offer the league, just without dates
            }
            if (!seen.add(code))
                continue;
            String timing = AdminLookupService.eventTiming(start, end, now);
            Integer signedUp = null;
            if (AdminLookupService.EVENT_UPCOMING.equals(timing)) {
                var participants = _leagueParticipationDao.getUsersParticipating(code);
                signedUp = participants == null ? 0 : participants.size();
                if (signedUp == 0)
                    continue;   // nothing to load
            }
            result.add(new AdminLookupService.EventEntry(AdminLookupService.EventKind.LEAGUE, code, league.getName(),
                    start == null ? null : start.format(DateUtils.APIDateTimeFormat),
                    end == null ? null : end.format(DateUtils.APIDateTimeFormat),
                    timing, AdminLookupService.EVENT_RUNNING.equals(timing), signedUp));
        }

        LocalDate from = now.toLocalDate().minusDays(LOAD_FROM_EVENT_RECENT_DAYS);
        LocalDate to = now.toLocalDate().plusDays(1); // half-open: today's own end_date must still be included
        var recentlyEnded = _leagueDao.loadLeaguesEndingBetween(from, to);
        if (recentlyEnded != null) {
            for (var row : recentlyEnded) {
                String code = String.valueOf(row.code);
                if (!seen.add(code))
                    continue;
                result.add(new AdminLookupService.EventEntry(AdminLookupService.EventKind.LEAGUE, code, row.name,
                        row.start_date == null ? null : row.start_date.toString(),
                        row.end_date == null ? null : row.end_date.toString(),
                        AdminLookupService.EVENT_FINISHED, false, null));
            }
        }
        return result;
    }

    /**
     * Live tournaments (including ones nobody has played a game in yet, such as World Championship bookkeeping
     * tournaments), scheduled ones starting within {@link #LOAD_FROM_EVENT_UPCOMING_DAYS} days that players have
     * signed up for (with the sign-up count), and those finished within {@link #LOAD_FROM_EVENT_RECENT_DAYS} days.
     */
    private List<AdminLookupService.EventEntry> collectTournamentEvents(ZonedDateTime now) {
        var result = new ArrayList<AdminLookupService.EventEntry>();
        for (var summary : _tournamentService.getAdminTournamentList(now, LOAD_FROM_EVENT_RECENT_DAYS, LOAD_FROM_EVENT_UPCOMING_DAYS)) {
            boolean running = TournamentService.ADMIN_STATUS_LIVE.equals(summary.status());
            Integer signedUp = null;
            if (TournamentService.ADMIN_STATUS_SCHEDULED.equals(summary.status())) {
                var view = _tournamentService.getAdminTournamentView(summary.tournamentId(), now);
                signedUp = view == null ? 0 : view.signedUp();
                if (signedUp == 0)
                    continue;   // queue not open yet, or nobody in it: nothing to load
            }
            result.add(new AdminLookupService.EventEntry(AdminLookupService.EventKind.TOURNAMENT, summary.tournamentId(),
                    summary.name(), summary.start() == null ? null : summary.start().format(DateUtils.APIDateTimeFormat),
                    null, // a tournament has no recorded finish time; see EventHistoryService's class comment
                    summary.status(), running, signedUp));
        }
        return result;
    }

    /**
     * Every signed-up player of the league, in the order of the standings its event page shows
     * ({@link LeagueService#getLeagueStandings}); see {@link AdminLookupService#participantsInStandingsOrder}.
     */
    private List<AdminLookupService.EventParticipant> leagueParticipants(League league) {
        var participants = _leagueParticipationDao.getUsersParticipating(league.getCodeStr());
        List<com.gempukku.lotro.competitive.PlayerStanding> standings = null;
        try {
            standings = _leagueService.getLeagueStandings(league);
        } catch (RuntimeException exp) {
            // standings that cannot be produced (a recorded match for someone who is not signed up) cost the order only
            _log.warn("Could not produce standings for league " + league.getCodeStr() + "; loading its players by name", exp);
        }
        return AdminLookupService.participantsInStandingsOrder(participants, standings, null);
    }

    /**
     * Every player of the tournament: a started one's registered players, those who dropped included (its standings
     * keep them), in the order of the standings its event page shows ({@link Tournament#getCurrentStandings}); a
     * scheduled one that has not started, the players waiting in its queue, by name.
     */
    private List<AdminLookupService.EventParticipant> tournamentParticipants(String tournamentId,
                                                                             TournamentService.AdminTournamentView view) {
        if (view.live() == null && view.record() == null) {
            // not started: its sign-ups are only in the queue (not getPlayerList(): a "competitive" queue hides it)
            return AdminLookupService.participantsInStandingsOrder(
                    view.queue() instanceof com.gempukku.lotro.tournament.AbstractTournamentQueue queue
                            ? queue.getSignedUpPlayers() : null, null, null);
        }
        var registered = _tournamentService.retrieveTournamentPlayers(tournamentId);
        var dropped = _tournamentService.retrieveAbandonedPlayers(tournamentId);
        List<com.gempukku.lotro.competitive.PlayerStanding> standings = null;
        try {
            Tournament tournament = view.live() != null ? view.live() : _tournamentService.getTournamentById(tournamentId);
            if (tournament != null)
                standings = tournament.getCurrentStandings();
        } catch (RuntimeException exp) {
            _log.warn("Could not produce standings for tournament " + tournamentId + "; loading its players by name", exp);
        }
        return AdminLookupService.participantsInStandingsOrder(registered, standings, dropped);
    }

    /**
     * GET /addItemsEventParticipants?kind=league|tournament&amp;id=... :
     * {kind, id, name, order, players:[{name, standing, gamesPlayed, dropped}]}
     * <p>
     * Every participant of the event, whether they have played or not, in the order of the event's standings (0-game
     * players last; order "standings"), or by name when nobody has played a game yet (order "name").  A league: every
     * signed-up player.  A tournament: every registered player, those who dropped included (flagged); one that has
     * not started: the players signed up in its queue.
     * <p>
     * 404 when there is no league with that code / no tournament with that id.
     */
    private void getAddItemsEventParticipants(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);
        var queryDecoder = new QueryStringDecoder(request.uri());
        String kind = getQueryParameterSafely(queryDecoder, "kind");
        String id = getQueryParameterSafely(queryDecoder, "id");
        Throw400IfStringNull("kind", kind);
        Throw400IfStringNull("id", id);
        kind = kind.trim().toLowerCase();
        id = id.trim();

        String name;
        List<AdminLookupService.EventParticipant> players;
        if ("league".equals(kind)) {
            League league = findLeagueByCodeStr(id);
            if (league == null)
                throw new HttpProcessingException(404, "There is no league '" + id + "'.");
            name = league.getName();
            players = leagueParticipants(league);
        } else if ("tournament".equals(kind)) {
            var view = _tournamentService.getAdminTournamentView(id, DateUtils.Now());
            if (view == null)
                throw new HttpProcessingException(404, "There is no tournament '" + id + "'.");
            name = view.live() != null ? view.live().getTournamentName()
                    : view.record() != null && view.record().name != null ? view.record().name
                    : view.scheduled() != null && view.scheduled().name != null ? view.scheduled().name
                    : view.queue() != null ? view.queue().getTournamentQueueName() : id;
            players = tournamentParticipants(id, view);
        } else {
            throw new HttpProcessingException(400, "'kind' must be 'league' or 'tournament'.");
        }

        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(
                AdminLookupService.participantsResponse(kind, id, name, players)));
    }

    private League findLeagueByCodeStr(String code) {
        try {
            return _leagueService.getLeagueByCode(Long.parseLong(code));
        } catch (NumberFormatException exp) {
            return null;
        }
    }

    // ---- end t4-admin-load-from-event ----------------------------------------------------------------------------

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
     * Turns the YYYYMMDD form value into the league start date.
     */
    private java.time.LocalDateTime parseLeagueStart(String startStr) throws HttpProcessingException {
        if (startStr == null || startStr.length() != 8)
            throw new HttpProcessingException(400, "Parameter 'start' must be exactly 8 digits long: YYYYMMDD");
        int start = Throw400IfNullOrNonInteger("start", startStr);
        try {
            return DateUtils.ParseDate(start).toLocalDateTime();
        } catch (DateTimeParseException exp) {
            throw new HttpProcessingException(400, "Parameter 'start' is not a valid date: " + startStr);
        }
    }

    /**
     * Reads the fields every league form shares (name, description, cost, start, repeat matches, invite-only) into
     * a fresh LeagueParams.  Type-specific fields are filled in by the caller.
     */
    private LeagueParams parseCommonLeagueFields(HttpPostRequestDecoder postDecoder) throws Exception {
        String name = getFormParameterSafely(postDecoder, "name");
        String description = getFormParameterSafely(postDecoder, "description");
        String costStr = getFormParameterSafely(postDecoder, "cost");
        String startStr = getFormParameterSafely(postDecoder, "start");
        String maxRepeatMatchesStr = getFormParameterSafely(postDecoder, "maxRepeatMatches");
        String inviteOnlyStr = getFormParameterSafely(postDecoder, "inviteOnly");

        Throw400IfStringNull("name", name);

        var params = new LeagueParams();
        params.name = name;
        params.description = description;
        params.cost = Throw400IfNullOrNonInteger("cost", costStr);
        params.start = parseLeagueStart(startStr);
        params.maxRepeatMatches = Throw400IfNullOrNonInteger("maxRepeatMatches", maxRepeatMatchesStr);
        params.inviteOnly = inviteOnlyStr != null && inviteOnlyStr.equalsIgnoreCase("true");

        String campaign = getFormParameterSafely(postDecoder, "campaign");
        params.campaign = (campaign == null || campaign.isBlank()) ? null : campaign.trim();
        params.prizeTiers = parsePrizeTiers(getFormParameterSafely(postDecoder, "prizeTiers"));
        return params;
    }

    /**
     * Reads the {@code prizeTiers} form value: a JSON array of {@link PrizeTier} objects, as the prize tier editor
     * posts it.  Missing or empty means no extra prizes.  Only the syntax is checked here; the contents are
     * validated by the LeagueFactory (through PrizeService.validateTiers).
     */
    private ArrayList<PrizeTier> parsePrizeTiers(String json) throws HttpProcessingException {
        var tiers = new ArrayList<PrizeTier>();
        if (json == null || json.isBlank())
            return tiers;
        try {
            List<PrizeTier> parsed = com.alibaba.fastjson2.JSON.parseArray(json.trim(), PrizeTier.class);
            if (parsed != null)
                tiers.addAll(parsed);
        } catch (RuntimeException exp) {
            throw new HttpProcessingException(400, "Parameter 'prizeTiers' is not a valid prize tier list: " + exp.getMessage());
        }
        return tiers;
    }

    /**
     * Reads the parallel format[] / serieDuration[] / maxMatches[] lists submitted by the multi-serie forms.
     */
    private ArrayList<LeagueParams.SerieData> parseSerieList(HttpPostRequestDecoder postDecoder) throws Exception {
        List<String> formats = getFormMultipleParametersSafely(postDecoder, "format[]");
        List<String> serieDurationsStr = getFormMultipleParametersSafely(postDecoder, "serieDuration[]");
        List<String> maxMatchesStr = getFormMultipleParametersSafely(postDecoder, "maxMatches[]");

        Throw400IfAnyStringNull("formats", formats);
        List<Integer> serieDurations = Throw400IfAnyNullOrNonInteger("serieDurations", serieDurationsStr);
        List<Integer> maxMatches = Throw400IfAnyNullOrNonInteger("maxMatches", maxMatchesStr);

        if (formats.size() != serieDurations.size() || formats.size() != maxMatches.size())
            throw new HttpProcessingException(400, "Size mismatch between provided formats, serieDurations, and maxMatches");

        var series = new ArrayList<LeagueParams.SerieData>();
        for (int i = 0; i < formats.size(); i++) {
            series.add(new LeagueParams.SerieData(formats.get(i), serieDurations.get(i), maxMatches.get(i)));
        }
        return series;
    }

    /**
     * Reads the single format / serieDuration / maxMatches trio submitted by the sealed and solo draft forms.
     */
    private LeagueParams.SerieData parseSingleSerie(HttpPostRequestDecoder postDecoder) throws Exception {
        String format = getFormParameterSafely(postDecoder, "format");
        String serieDurationStr = getFormParameterSafely(postDecoder, "serieDuration");
        String maxMatchesStr = getFormParameterSafely(postDecoder, "maxMatches");

        Throw400IfStringNull("format", format);
        int serieDuration = Throw400IfNullOrNonInteger("serieDuration", serieDurationStr);
        int maxMatches = Throw400IfNullOrNonInteger("maxMatches", maxMatchesStr);
        return new LeagueParams.SerieData(format, serieDuration, maxMatches);
    }

    /**
     * Hands an assembled definition to the LeagueFactory: validation failures become HTTP 400; otherwise the league
     * is either created for real or rendered as the JSON preview the admin panel displays.
     * @param preview If true, no league will be created and the client will have an XML payload returned representing
     *                what the league would be upon creation.  If false, the league will be created for real.
     */
    private void processLeagueDefinition(League.LeagueType type, LeagueParams params, boolean preview, ResponseWriter responseWriter) throws Exception {
        LeagueFactory.PreparedLeague prepared;
        try {
            prepared = _leagueFactory.prepare(type, params);
        } catch (LeagueDefinitionException exp) {
            throw new HttpProcessingException(400, exp.getMessage());
        }

        if (!preview) {
            _leagueFactory.create(prepared);
            responseWriter.sendJsonOK();
            return;
        }

        responseWriter.writeJsonResponse(JsonUtils.Serialize(buildLeaguePreview(prepared)));
    }

    /**
     * Renders a prepared (but not created) league as the JSON object the admin panel's preview dialog consumes.
     * Dates are ISO local dates (yyyy-MM-dd); prizes are returned as data for the client to render.
     */
    private Map<String, Object> buildLeaguePreview(LeagueFactory.PreparedLeague prepared) {
        var params = prepared.params();

        var league = new LinkedHashMap<String, Object>();
        league.put("name", params.name);
        league.put("type", prepared.type().toString());
        league.put("code", String.valueOf(params.code));
        league.put("cost", params.cost);
        league.put("start", formatPreviewDate(prepared.start()));
        league.put("end", formatPreviewDate(prepared.displayEnd()));
        league.put("collection", params.collectionName);
        league.put("inviteOnly", params.inviteOnly);
        league.put("maxRepeatMatches", params.maxRepeatMatches);
        league.put("description", params.description == null ? "" : params.description);
        league.put("descriptionHtml", _markdownParser.renderDescription(params.description));

        league.put("campaign", params.campaign);
        league.put("prizeTiers", describePrizeTiers(params.prizeTiers));

        Map<String, Object> race = null;
        if (prepared.data() instanceof RTMDLeague) {
            race = new LinkedHashMap<>();
            race.put("pathLength", params.racePath.size());
            race.put("cumulative", params.raceCumulative);
            race.put("advancementMode", params.raceAdvancementMode.toString());
            race.put("advanceFactor", params.raceAdvanceFactor);
            race.put("intensityFloor", params.raceIntensityFloor);
            race.put("intensityCeiling", params.raceIntensityCeiling);

            var metaSites = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < params.racePath.size(); i++) {
                var site = new LinkedHashMap<String, Object>();
                site.put("position", i + 1);
                site.put("blueprintId", params.racePath.get(i));
                site.put("visualBlueprintId", i < params.raceVisualPath.size() ? params.raceVisualPath.get(i) : null);
                String siteName = null;
                try {
                    var bp = _cardLibrary.getLotroCardBlueprint(params.racePath.get(i));
                    if (bp != null)
                        siteName = bp.getFullName();
                } catch (Exception ignored) {}
                site.put("name", siteName);
                metaSites.add(site);
            }
            race.put("metaSites", metaSites);
        }
        league.put("race", race);

        var series = new ArrayList<Map<String, Object>>();
        for (LeagueSerieInfo serie : prepared.series()) {
            var serieMap = new LinkedHashMap<String, Object>();
            serieMap.put("name", serie.getName());
            serieMap.put("maxMatches", serie.getMaxMatches());
            serieMap.put("start", formatPreviewDate(serie.getStart()));
            serieMap.put("end", formatPreviewDate(serie.getEnd()));
            serieMap.put("format", serie.getFormat().getName());
            serieMap.put("formatCode", serie.getFormat().getCode());
            serieMap.put("collection", serie.getCollectionType().getFullName());
            serieMap.put("limited", serie.isLimited());
            series.add(serieMap);
        }
        league.put("series", series);

        return league;
    }

    private static String formatPreviewDate(ZonedDateTime date) {
        return date.toLocalDate().toString();
    }

    // ------------------------------------------------------------------------------------------------
    // Prize tiers and promised prizes
    // ------------------------------------------------------------------------------------------------

    /**
     * What a blueprint id stands for, as the admin pages show it:
     * {@code {blueprintId, kind: card|pack|placeholder|unknown, name}}.  A placeholder ({@code 404_N}) reports the
     * promise it stands for as its name; a pack is named after its id.
     */
    private Map<String, Object> describeProduct(String blueprintId) {
        var result = new LinkedHashMap<String, Object>();
        String id = blueprintId == null ? "" : blueprintId.trim();
        result.put("blueprintId", id);
        String kind = "unknown";
        String name = null;
        if (!id.isEmpty()) {
            if (PrizeService.isPlaceholder(id)) {
                kind = "placeholder";
                try {
                    var bp = _cardLibrary.getLotroCardBlueprint(id);
                    if (bp != null)
                        name = bp.getFullName();
                } catch (Exception ignored) {}
            } else if (id.contains("_")) {
                try {
                    var bp = _cardLibrary.getLotroCardBlueprint(id);
                    if (bp != null) {
                        kind = "card";
                        name = bp.getFullName();
                    }
                } catch (Exception ignored) {}
            } else if (_productLibrary.GetProduct(id) != null) {
                kind = "pack";
                name = id;
            }
        }
        result.put("kind", kind);
        result.put("name", name);
        return result;
    }

    /**
     * Renders prize tiers as the data the preview dialog displays: the tier fields plus a human description and,
     * per item, what the id resolves to.
     */
    private List<Map<String, Object>> describePrizeTiers(List<PrizeTier> tiers) {
        var result = new ArrayList<Map<String, Object>>();
        if (tiers == null)
            return result;
        for (PrizeTier tier : tiers) {
            if (tier == null)
                continue;
            var t = new LinkedHashMap<String, Object>();
            t.put("kind", tier.kind == null ? null : tier.kind.toString());
            t.put("from", tier.from);
            t.put("to", tier.to);
            t.put("games", tier.games);
            t.put("scope", tier.scope == null ? null : tier.scope.toString());
            t.put("label", tier.label);
            t.put("description", _prizeService.describeTier(tier));
            var items = new ArrayList<Map<String, Object>>();
            if (tier.items != null) {
                for (PrizeItem item : tier.items) {
                    if (item == null)
                        continue;
                    var i = new LinkedHashMap<String, Object>();
                    i.put("blueprintId", item.blueprintId);
                    i.put("promise", item.promise);
                    i.put("count", item.count);
                    if (item.hasPromise()) {
                        i.put("kind", "promise");
                        i.put("name", item.promise.trim());
                    } else {
                        var product = describeProduct(item.blueprintId);
                        String kind = (String) product.get("kind");
                        i.put("kind", "placeholder".equals(kind) ? "unknown" : kind);
                        i.put("name", product.get("name"));
                    }
                    items.add(i);
                }
            }
            t.put("items", items);
            result.add(t);
        }
        return result;
    }

    private Map<String, Object> describePlaceholder(DBDefs.PrizePlaceholder row) {
        var p = new LinkedHashMap<String, Object>();
        p.put("id", row.id);
        p.put("blueprintId", PrizeService.placeholderBlueprint(row.id));
        p.put("label", row.label);
        p.put("count", row.count);
        p.put("eventKind", row.event_kind);
        p.put("eventId", row.event_id);
        p.put("eventName", row.event_name);
        p.put("tierIndex", row.tier_index);
        p.put("created", row.created == null ? null : row.created.toString());
        p.put("createdBy", row.created_by);
        p.put("holders", _prizeService.holderCount(row.id));
        p.put("resolvedBlueprint", row.resolved_blueprint);
        String resolvedName = null;
        if (row.resolved_blueprint != null)
            resolvedName = (String) describeProduct(row.resolved_blueprint).get("name");
        p.put("resolvedCardName", resolvedName);
        p.put("resolvedOn", row.resolved_on == null ? null : row.resolved_on.toString());
        p.put("resolvedBy", row.resolved_by);
        return p;
    }

    private static final int RESOLVED_PRIZES_LIMIT = 50;

    /**
     * GET /prizes : every unresolved promise and the last few resolved ones, for the Prizes admin tab.
     */
    private void getPrizes(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var unresolved = new ArrayList<Map<String, Object>>();
        for (var row : _prizeService.getUnresolved())
            unresolved.add(describePlaceholder(row));
        var resolved = new ArrayList<Map<String, Object>>();
        for (var row : _prizeService.getResolved(RESOLVED_PRIZES_LIMIT))
            resolved.add(describePlaceholder(row));

        var result = new LinkedHashMap<String, Object>();
        result.put("unresolved", unresolved);
        result.put("resolved", resolved);
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * GET /prizeHolders?id=... : who holds a placeholder, one entry per (player, collection).
     */
    private void getPrizeHolders(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var queryDecoder = new QueryStringDecoder(request.uri());
        int id = Throw400IfNullOrNonInteger("id", getQueryParameterSafely(queryDecoder, "id"));
        var row = _prizeService.getPlaceholder(id);
        if (row == null)
            throw new HttpProcessingException(404, "There is no prize placeholder with id " + id + ".");

        var holders = new ArrayList<Map<String, Object>>();
        for (var holder : _prizeService.getHolders(id)) {
            var h = new LinkedHashMap<String, Object>();
            h.put("player", holder.player_name);
            h.put("collectionType", holder.collection_type);
            h.put("quantity", holder.quantity);
            holders.add(h);
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("id", row.id);
        result.put("label", row.label);
        result.put("holders", holders);
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * POST /resolvePrize {id, blueprintId} : swaps a placeholder for the real card in every holder's collection.
     */
    private void resolvePrize(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);
        Player admin = getResourceOwnerSafely(request, null);

        var postDecoder = new HttpPostRequestDecoder(request);
        try {
            int id = Throw400IfNullOrNonInteger("id", getFormParameterSafely(postDecoder, "id"));
            String blueprintId = getFormParameterSafely(postDecoder, "blueprintId");
            Throw400IfStringNull("blueprintId", blueprintId);

            PrizeService.ResolutionResult resolution;
            try {
                resolution = _prizeService.resolve(id, blueprintId, admin.getName());
            } catch (PrizeDefinitionException exp) {
                throw new HttpProcessingException(400, exp.getMessage());
            }

            var result = new LinkedHashMap<String, Object>();
            result.put("placeholderId", resolution.placeholderId());
            result.put("label", resolution.label());
            result.put("blueprintId", resolution.blueprintId());
            result.put("cardName", describeProduct(resolution.blueprintId()).get("name"));
            result.put("players", resolution.players());
            result.put("cardsSwapped", resolution.cardsSwapped());
            responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
        } finally {
            postDecoder.destroy();
        }
    }

    /**
     * POST /addPromise {label, count, players (one per line)} : creates a promise by hand and hands its placeholder
     * to the named players.
     */
    private void addPromise(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);
        Player admin = getResourceOwnerSafely(request, null);

        var postDecoder = new HttpPostRequestDecoder(request);
        try {
            String label = getFormParameterSafely(postDecoder, "label");
            String countStr = getFormParameterSafely(postDecoder, "count");
            String players = getFormParameterSafely(postDecoder, "players");
            Throw400IfStringNull("label", label);
            int count = (countStr == null || countStr.isBlank()) ? 1 : Throw400IfNullOrNonInteger("count", countStr);
            Throw400IfStringNull("players", players);

            DBDefs.PrizePlaceholder row;
            try {
                row = _prizeService.createManualPromise(label, count, getPlayerNames(players), admin.getName());
            } catch (PrizeDefinitionException exp) {
                throw new HttpProcessingException(400, exp.getMessage());
            }
            responseWriter.writeJsonResponse(JsonUtils.Serialize(describePlaceholder(row)));
        } finally {
            postDecoder.destroy();
        }
    }

    /**
     * GET /cardName?blueprintId=... : what an id stands for, for the editors' live lookups.
     */
    private void getCardName(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var queryDecoder = new QueryStringDecoder(request.uri());
        String blueprintId = getQueryParameterSafely(queryDecoder, "blueprintId");
        responseWriter.writeJsonResponse(JsonUtils.Serialize(describeProduct(blueprintId)));
    }

    /**
     * Reads the form fields the add/preview form for {@code type} posts into a LeagueParams.  Shared by league
     * creation and league editing so that the two cannot drift apart.
     */
    private LeagueParams parseLeagueParams(League.LeagueType type, HttpPostRequestDecoder postDecoder) throws Exception {
        return switch (type) {
            case SEALED -> parseSealedLeagueParams(postDecoder);
            case SOLODRAFT -> parseSoloDraftLeagueParams(postDecoder);
            case CONSTRUCTED -> parseConstructedLeagueParams(postDecoder);
            case RTMD -> parseRTMDLeagueParams(postDecoder);
        };
    }

    /**
     * Constructed league: common fields, a chosen collection, prizes and any number of series.
     */
    private LeagueParams parseConstructedLeagueParams(HttpPostRequestDecoder postDecoder) throws Exception {
        var params = parseCommonLeagueFields(postDecoder);
        String collectionType = getFormParameterSafely(postDecoder, "collectionType");
        Throw400IfStringNull("collectionType", collectionType);
        params.collectionName = collectionType;
        params.series = parseSerieList(postDecoder);
        return params;
    }

    private void processConstructedLeague(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        var params = parseConstructedLeagueParams(postDecoder);

        processLeagueDefinition(League.LeagueType.CONSTRUCTED, params, preview, responseWriter);
    }

    /**
     * Race to Mount Doom league: a constructed league plus the race path and advancement settings.
     */
    private LeagueParams parseRTMDLeagueParams(HttpPostRequestDecoder postDecoder) throws Exception {
        var params = parseCommonLeagueFields(postDecoder);
        params.collectionName = "default";
        params.series = parseSerieList(postDecoder);

        List<String> racePath = getFormMultipleParametersSafely(postDecoder, "racePath[]");
        List<String> raceVisualPath = getFormMultipleParametersSafely(postDecoder, "raceVisualPath[]");
        String raceCumulativeStr = getFormParameterSafely(postDecoder, "raceCumulative");
        String raceIntensityFloorStr = getFormParameterSafely(postDecoder, "raceIntensityFloor");
        String raceIntensityCeilingStr = getFormParameterSafely(postDecoder, "raceIntensityCeiling");
        String raceAdvancementModeStr = getFormParameterSafely(postDecoder, "raceAdvancementMode");
        String raceAdvanceFactorStr = getFormParameterSafely(postDecoder, "raceAdvanceFactor");
        String racePathLengthStr = getFormParameterSafely(postDecoder, "racePathLength");
        String raceRandomizeStr = getFormParameterSafely(postDecoder, "raceRandomizeEachInstance");

        params.racePath = racePath == null ? new ArrayList<>() : new ArrayList<>(racePath);
        params.raceVisualPath = raceVisualPath == null ? new ArrayList<>() : new ArrayList<>(raceVisualPath);
        params.raceCumulative = raceCumulativeStr != null && raceCumulativeStr.equalsIgnoreCase("true");

        if (raceIntensityFloorStr != null && !raceIntensityFloorStr.isBlank())
            params.raceIntensityFloor = Throw400IfNullOrNonInteger("raceIntensityFloor", raceIntensityFloorStr);
        if (raceIntensityCeilingStr != null && !raceIntensityCeilingStr.isBlank())
            params.raceIntensityCeiling = Throw400IfNullOrNonInteger("raceIntensityCeiling", raceIntensityCeilingStr);

        if (raceAdvancementModeStr != null && !raceAdvancementModeStr.isBlank()) {
            try {
                params.raceAdvancementMode = RTMDLeague.AdvanceType.valueOf(raceAdvancementModeStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new HttpProcessingException(400, "Invalid advancement mode: " + raceAdvancementModeStr
                        + ". Must be WIN or SCORE.");
            }
        }
        if (raceAdvanceFactorStr != null && !raceAdvanceFactorStr.isBlank())
            params.raceAdvanceFactor = Throw400IfNullOrNonInteger("raceAdvanceFactor", raceAdvanceFactorStr);
        if (racePathLengthStr != null && !racePathLengthStr.isBlank())
            params.racePathLength = Throw400IfNullOrNonInteger("racePathLength", racePathLengthStr);
        // only a schedule template ever sets this; a hand-made league keeps the path the admin rolled
        params.raceRandomizeEachInstance = raceRandomizeStr != null && raceRandomizeStr.equalsIgnoreCase("true");

        return params;
    }

    private void processRTMDLeague(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        var params = parseRTMDLeagueParams(postDecoder);

        processLeagueDefinition(League.LeagueType.RTMD, params, preview, responseWriter);
    }

    /**
     * Solo draft league: common fields plus a single draft type / duration / match limit.
     */
    private LeagueParams parseSoloDraftLeagueParams(HttpPostRequestDecoder postDecoder) throws Exception {
        var params = parseCommonLeagueFields(postDecoder);
        params.collectionName = params.name;
        params.series.add(parseSingleSerie(postDecoder));
        return params;
    }

    private void processSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        var params = parseSoloDraftLeagueParams(postDecoder);

        processLeagueDefinition(League.LeagueType.SOLODRAFT, params, preview, responseWriter);
    }

    /**
     * Sealed league: common fields plus a single sealed template / serie duration / match limit.
     */
    private LeagueParams parseSealedLeagueParams(HttpPostRequestDecoder postDecoder) throws Exception {
        var params = parseCommonLeagueFields(postDecoder);
        params.collectionName = params.name;
        params.series.add(parseSingleSerie(postDecoder));
        return params;
    }

    private void processSealedLeague(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        var params = parseSealedLeagueParams(postDecoder);

        processLeagueDefinition(League.LeagueType.SEALED, params, preview, responseWriter);
    }

    // ------------------------------------------------------------------------------------------------
    // Editing existing leagues
    // ------------------------------------------------------------------------------------------------

    /**
     * Parses a league code form/query value; anything that is not a long is a 400.
     */
    private long parseLeagueCode(String codeStr) throws HttpProcessingException {
        Throw400IfStringNull("code", codeStr);
        try {
            return Long.parseLong(codeStr.trim());
        } catch (NumberFormatException exp) {
            throw new HttpProcessingException(400, "Parameter 'code' must be a league code (a whole number): " + codeStr);
        }
    }

    /**
     * Finds a league by code: the active-league cache first, then the database, so that leagues that have already
     * ended can still be looked up.
     * @throws HttpProcessingException 404 if there is no league with that code
     */
    private League findLeagueByCode(long code) throws HttpProcessingException {
        League league = _leagueService.getLeagueByType(String.valueOf(code));
        if (league == null)
            league = _leagueService.getLeagueByCode(code);
        if (league == null)
            throw new HttpProcessingException(404, "League '" + code + "' does not exist.");
        return league;
    }

    /**
     * GET /league?code=... : the stored definition of one league, for the admin edit form.
     */
    private void getLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var queryDecoder = new QueryStringDecoder(request.uri());
        long code = parseLeagueCode(getQueryParameterSafely(queryDecoder, "code"));
        League league = findLeagueByCode(code);

        LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
        List<LeagueSerieInfo> series = leagueData.getSeries();
        ZonedDateTime start = series.getFirst().getStart();
        ZonedDateTime end = series.getLast().getEnd();

        var result = new LinkedHashMap<String, Object>();
        result.put("code", league.getCodeStr());
        result.put("type", league.getType().toString());
        result.put("name", league.getName());
        result.put("start", formatPreviewDate(start));
        result.put("end", formatPreviewDate(end));
        result.put("participants", _leagueParticipationDao.getUsersParticipating(league.getCodeStr()).size());
        result.put("started", !start.isAfter(DateUtils.Today()));
        result.put("scheduleId", league.getScheduleId());
        result.put("params", leagueData.getParameters());
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * POST /markdownPreview : renders a description the way players will see it (admin-level markdown, links
     * allowed), for the live preview beside the description box.  Takes {@code text}; returns {@code {html}}.
     */
    private void markdownPreview(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String text = getFormParameterSafely(postDecoder, "text");
            var result = new LinkedHashMap<String, Object>();
            result.put("html", _markdownParser.renderDescription(text));
            responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
        } finally {
            postDecoder.destroy();
        }
    }

    /**
     * POST /updateLeague : replaces the definition of an existing league.  Takes the league code plus exactly the
     * fields the add form for the league's (stored, unchangeable) type posts.
     */
    private void updateLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        long code = parseLeagueCode(getFormParameterSafely(postDecoder, "code"));
        League existing = findLeagueByCode(code);

        var params = parseLeagueParams(existing.getType(), postDecoder);
        try {
            _leagueFactory.update(existing, params);
        } catch (LeagueDefinitionException exp) {
            throw new HttpProcessingException(400, exp.getMessage());
        }

        responseWriter.sendJsonOK();
    }

    // ------------------------------------------------------------------------------------------------
    // League schedules
    // ------------------------------------------------------------------------------------------------

    private static final int SCHEDULE_UPCOMING_COUNT = 6;

    private Map<String, Object> describeSchedule(LeagueSchedule schedule) {
        var obj = new LinkedHashMap<String, Object>();
        obj.put("id", schedule.getId());
        obj.put("name", schedule.getName());
        obj.put("leagueType", schedule.getType() == null ? null : schedule.getType().toString());
        obj.put("template", schedule.getTemplate());
        var events = new ArrayList<Map<String, Object>>();
        for (var event : schedule.getEvents()) {
            var e = new LinkedHashMap<String, Object>();
            e.put("name", event.name());
            e.put("params", event.overrides());
            events.add(e);
        }
        obj.put("events", events);
        obj.put("namePattern", schedule.getNamePattern());
        obj.put("nextEventDate", schedule.getNextEventDate() == null ? null : schedule.getNextEventDate().toString());
        obj.put("nextEventIndex", schedule.getNextEventIndex());
        obj.put("intervalMonths", schedule.getIntervalMonths());
        obj.put("leadDays", schedule.getLeadDays());
        obj.put("active", schedule.isActive());
        obj.put("lastCreatedLeagueId", schedule.getLastCreatedLeagueId());
        obj.put("lastRun", schedule.getLastRun() == null ? null : schedule.getLastRun().toString());
        obj.put("lastError", schedule.getLastError());
        obj.put("upcoming", describeProjection(_leagueScheduleService.projectNext(schedule, SCHEDULE_UPCOMING_COUNT)));
        return obj;
    }

    static List<Map<String, Object>> describeProjection(List<LeagueScheduleService.ProjectedEvent> projected) {
        var result = new ArrayList<Map<String, Object>>();
        for (var event : projected) {
            var e = new LinkedHashMap<String, Object>();
            e.put("scheduleId", event.scheduleId());
            e.put("scheduleName", event.scheduleName());
            e.put("eventIndex", event.eventIndex());
            e.put("eventName", event.eventName());
            e.put("leagueName", event.leagueName());
            e.put("start", event.start().toString());
            e.put("end", event.end().toString());
            e.put("createdOn", event.createdOn().toString());
            e.put("error", event.error());
            result.add(e);
        }
        return result;
    }

    private void getLeagueSchedules(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var schedules = new ArrayList<Map<String, Object>>();
        for (var schedule : _leagueScheduleService.getSchedules())
            schedules.add(describeSchedule(schedule));

        var result = new LinkedHashMap<String, Object>();
        result.put("schedules", schedules);
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * Reads a schedule definition from the form.  Template and events arrive as JSON strings (the admin page edits
     * them as such), which the service validates.
     */
    private DBDefs.LeagueSchedule parseScheduleDefinition(HttpPostRequestDecoder postDecoder) throws Exception {
        var row = new DBDefs.LeagueSchedule();

        String idStr = getFormParameterSafely(postDecoder, "id");
        row.id = (idStr == null || idStr.isBlank()) ? 0 : Throw400IfNullOrNonInteger("id", idStr);
        row.name = getFormParameterSafely(postDecoder, "name");
        row.league_type = getFormParameterSafely(postDecoder, "leagueType");
        row.template = getFormParameterSafely(postDecoder, "template");
        row.events = getFormParameterSafely(postDecoder, "events");
        row.name_pattern = getFormParameterSafely(postDecoder, "namePattern");

        String nextEventDateStr = getFormParameterSafely(postDecoder, "nextEventDate");
        Throw400IfStringNull("nextEventDate", nextEventDateStr);
        try {
            row.next_event_date = java.time.LocalDate.parse(nextEventDateStr.trim());
        } catch (DateTimeParseException exp) {
            throw new HttpProcessingException(400, "Parameter 'nextEventDate' must be a date in yyyy-MM-dd form.");
        }

        String nextEventIndexStr = getFormParameterSafely(postDecoder, "nextEventIndex");
        row.next_event_index = (nextEventIndexStr == null || nextEventIndexStr.isBlank()) ? 0 : Throw400IfNullOrNonInteger("nextEventIndex", nextEventIndexStr);

        String intervalStr = getFormParameterSafely(postDecoder, "intervalMonths");
        row.interval_months = Throw400IfNullOrNonFloat("intervalMonths", intervalStr);

        String leadDaysStr = getFormParameterSafely(postDecoder, "leadDays");
        row.lead_days = (leadDaysStr == null || leadDaysStr.isBlank()) ? 7 : Throw400IfNullOrNonInteger("leadDays", leadDaysStr);

        String activeStr = getFormParameterSafely(postDecoder, "active");
        row.active = activeStr == null || activeStr.isBlank() || activeStr.equalsIgnoreCase("true");
        return row;
    }

    /**
     * Validates a schedule definition and either stores it or returns what it would produce.
     * @param preview If true nothing is stored; the response carries the upcoming events and a full preview of the
     *                next league.  If false the schedule is saved and returned.
     */
    private void saveLeagueSchedule(HttpRequest request, ResponseWriter responseWriter, boolean preview) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        var row = parseScheduleDefinition(postDecoder);

        try {
            if (preview) {
                _leagueScheduleService.validateDefinition(row);
                var schedule = new LeagueSchedule(row);
                var upcoming = _leagueScheduleService.projectNext(schedule, SCHEDULE_UPCOMING_COUNT);

                var result = new LinkedHashMap<String, Object>();
                result.put("upcoming", describeProjection(upcoming));
                var nextParams = LeagueScheduleService.buildEventParams(schedule, schedule.getNextEventIndex(), schedule.getNextEventDate());
                result.put("nextLeague", buildLeaguePreview(_leagueFactory.prepare(schedule.getType(), nextParams)));
                responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
                return;
            }

            var saved = _leagueScheduleService.saveSchedule(row);
            responseWriter.writeJsonResponse(JsonUtils.Serialize(describeSchedule(saved)));
        } catch (LeagueDefinitionException exp) {
            throw new HttpProcessingException(400, exp.getMessage());
        }
    }

    private void deleteLeagueSchedule(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        int id = Throw400IfNullOrNonInteger("id", getFormParameterSafely(postDecoder, "id"));
        if (_leagueScheduleService.getSchedule(id) == null)
            throw new HttpProcessingException(404, "No league schedule with id " + id);

        _leagueScheduleService.deleteSchedule(id);
        responseWriter.sendJsonOK();
    }

    /**
     * Creates the schedule's next league immediately, ignoring the lead time.
     */
    private void runLeagueSchedule(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);
        int id = Throw400IfNullOrNonInteger("id", getFormParameterSafely(postDecoder, "id"));
        if (_leagueScheduleService.getSchedule(id) == null)
            throw new HttpProcessingException(404, "No league schedule with id " + id);

        var created = _leagueScheduleService.runNow(id);
        var schedule = _leagueScheduleService.getSchedule(id);
        if (created == null)
            throw new HttpProcessingException(409, schedule.getLastError() == null ? "The league could not be created." : schedule.getLastError());

        var result = new LinkedHashMap<String, Object>();
        result.put("league", buildLeaguePreview(created));
        result.put("schedule", describeSchedule(schedule));
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * Processes the passed parameters for a theoretical scheduled tournament.  Based on the preview parameter, this will
     * either create the tournament for real, or just return the parsed values to the client so the admin can preview
     * the input.
     * @param request the request
     * @param responseWriter the response writer
     * @throws Exception
     */
    private void getRTMDModifiers(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var modifiers = new ArrayList<Map<String, Object>>();
        var visualCards = new ArrayList<Map<String, Object>>();

        // Visual cards: set 90. Modifiers: sets 91-94.  The server-side randomiser reads the same sets.
        var visualSets = RTMDPathGenerator.VISUAL_SETS;
        var modifierSets = RTMDPathGenerator.MODIFIER_SETS;

        // Iterate all loaded blueprints rather than rarity files (which may not exist for these sets)
        for (var mapEntry : _cardLibrary.getBaseCards().entrySet()) {
            String blueprintId = mapEntry.getKey();
            int setId;
            try {
                setId = Integer.parseInt(blueprintId.split("_")[0]);
            } catch (NumberFormatException e) {
                continue;
            }

            var bp = mapEntry.getValue();
            if (visualSets.contains(setId)) {
                var entry = new LinkedHashMap<String, Object>();
                entry.put("blueprintId", blueprintId);
                entry.put("title", bp.getFullName());
                entry.put("intensity", bp.getIntensity());
                visualCards.add(entry);
            } else if (modifierSets.contains(setId)) {
                var entry = new LinkedHashMap<String, Object>();
                entry.put("blueprintId", blueprintId);
                entry.put("title", bp.getFullName());
                entry.put("intensity", bp.getIntensity());
                entry.put("gameText", bp.getGameText());
                modifiers.add(entry);
            }
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("modifiers", modifiers);
        result.put("visualCards", visualCards);

        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * GET /rtmdRandomPath?pathLength=&amp;floor=&amp;ceiling=&amp;locked=id,,id,, : one freshly rolled race path,
     * from the same {@link RTMDPathGenerator} that scheduled leagues use, so the admin page's Randomize button and
     * the scheduler cannot drift apart.
     * <p>
     * {@code locked} is optional and positional: one comma-separated entry per slot, a blueprint ID to keep that
     * slot as it is or an empty entry to re-roll it.
     * Returns {@code {racePath: [...], raceVisualPath: [...]}}, the two lists parallel and the same length.
     */
    private void getRTMDRandomPath(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);

        var queryDecoder = new QueryStringDecoder(request.uri());
        String pathLengthStr = getQueryParameterSafely(queryDecoder, "pathLength");
        String floorStr = getQueryParameterSafely(queryDecoder, "floor");
        String ceilingStr = getQueryParameterSafely(queryDecoder, "ceiling");
        String lockedStr = getQueryParameterSafely(queryDecoder, "locked");

        int pathLength = (pathLengthStr == null || pathLengthStr.isBlank())
                ? RTMDPathGenerator.DEFAULT_PATH_LENGTH : Throw400IfNullOrNonInteger("pathLength", pathLengthStr);
        int floor = (floorStr == null || floorStr.isBlank()) ? -10 : Throw400IfNullOrNonInteger("floor", floorStr);
        int ceiling = (ceilingStr == null || ceilingStr.isBlank()) ? 10 : Throw400IfNullOrNonInteger("ceiling", ceilingStr);
        if (floor > ceiling)
            throw new HttpProcessingException(400, "Intensity floor must be less than or equal to the ceiling.");

        List<String> locked = null;
        if (lockedStr != null && !lockedStr.isBlank())
            locked = Arrays.asList(lockedStr.split(",", -1));

        var path = RTMDPathGenerator.generate(RTMDPathGenerator.loadPool(_cardLibrary), pathLength, floor, ceiling,
                locked, new Random());

        var result = new LinkedHashMap<String, Object>();
        result.put("racePath", path.racePath());
        result.put("raceVisualPath", path.raceVisualPath());
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * POST /admin/processScheduledTournament (update false) creates a scheduled tournament; POST
     * /admin/updateScheduledTournament (update true) takes the same parameters and rewrites the scheduled tournament
     * {@code tournamentId} in place (it must exist, must not have started and must have nobody signed up; the id is
     * never changed, so the World Championship prefix is not applied again).  With preview=true neither saves
     * anything and the JSON preview is returned instead.
     */
    private void processScheduledTournament(HttpRequest request, ResponseWriter responseWriter, boolean update) throws Exception {
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
        String minPlayersStr = getFormParameterSafely(postDecoder, "minPlayers");
        String manualKickoffStr = getFormParameterSafely(postDecoder, "manualKickoff");
        // optional JSON array of PrizeTier, as the prize tier editor posts it
        ArrayList<PrizeTier> prizeTiers = parsePrizeTiers(getFormParameterSafely(postDecoder, "prizeTiers"));

        Throw400IfStringNull("type", typeStr);
        var type = Tournament.TournamentType.parse(typeStr);
        Throw400IfValidationFails("type", typeStr, type != null);
        Throw400IfStringNull("name", name);
        Throw400IfValidationFails("name", name, name.length() <= 45, "Tournament name must be 45 characters or less.");
        boolean wc = ParseBoolean("wc", wcStr, false);
        Throw400IfStringNull("tournamentId", tournamentId);
        Throw400IfValidationFails("tournamentId", tournamentId, !tournamentId.contains(" "), "Tournament id must not contain spaces.");
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
        // The automatic prize structure is always DAILY; extra prizes are the configurable tiers.
        try {
            PrizeService.validateTiers(prizeTiers, _cardLibrary, _productLibrary);
        } catch (PrizeDefinitionException exp) {
            throw new HttpProcessingException(400, exp.getMessage());
        }
        int minPlayers = Throw400IfNullOrNonInteger("minPlayers", minPlayersStr);
        boolean manualKickoff = ParseBoolean("manualKickoff", manualKickoffStr, false);

        if (update) {
            // t3-tournament-admin: an edit keeps its id (already prefixed if it was created as a WC event)
            String blocker = _tournamentService.getScheduledTournamentEditBlocker(tournamentId);
            if (blocker != null)
                throw new HttpProcessingException(400, blocker);
        } else {
            if (wc) {
                tournamentId = DateUtils.Now().getYear() + "-wc-" + tournamentId;
            }

            Throw400IfValidationFails("tournamentId", tournamentId, _tournamentService.getTournamentById(tournamentId) == null, "Tournament with that Id already exists.");
            Throw400IfValidationFails("tournamentId", tournamentId, _tournamentService.getScheduledTournamentById(tournamentId) == null, "Scheduled Tournament with that Id already exists.");
        }

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
            tableDraftParams.draftTimerType = DraftTimer.getTypeFromString(tableDraftTimer);
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
        params.prizeTiers = prizeTiers;
        params.minimumPlayers = minPlayers;
        params.manualKickoff = manualKickoff;

        if (wc) {
            params.wc = true;
        }

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
            if (update) {
                try {
                    _tournamentService.updateScheduledTournament(info, DateUtils.Now());
                } catch (IllegalStateException exp) {
                    throw new HttpProcessingException(400, exp.getMessage());
                }
            } else {
                _tournamentService.addScheduledTournament(info);
            }
            // promises become placeholders now, so they can be resolved before the tournament even runs
            _prizeService.registerPromises(new PrizeService.EventRef(PrizeService.KIND_TOURNAMENT, params.tournamentId,
                    params.name, null), params.prizeTiers);
            responseWriter.sendJsonOK();
            return;
        }

        //We aren't creating the tournament for real, so instead we will return the tournament in JSON format for the
        // admin panel preview: the parameters as they would be stored, with the prize tiers described the way the
        // league preview describes them (each tier gains a "description" and every item its kind and name).
        Map<String, Object> previewMap = com.alibaba.fastjson2.JSON.parseObject(JsonUtils.Serialize(params));
        previewMap.put("prizeTiers", describePrizeTiers(params.prizeTiers));
        responseWriter.writeJsonResponse(JsonUtils.Serialize(previewMap));
    }

    // ------------------------------------------------------------------------------------------------
    // t3-tournament-admin: the unified tournament form (calendar click -> load, deck-source list, player list)
    // ------------------------------------------------------------------------------------------------

    /** The deck-source list reaches this far back for finished tournaments... */
    private static final int ADMIN_TOURNAMENT_FINISHED_DAYS = 60;
    /** ...and this far ahead for scheduled ones. */
    private static final int ADMIN_TOURNAMENT_SCHEDULED_DAYS = 366;

    private String formatNameOrCode(String formatCode) {
        return formatCode == null ? null : com.gempukku.lotro.game.formats.FormatNames.nameOrCode(_formatLibrary, formatCode);
    }

    /** The stored parameters, or null when they cannot be read (the form then shows what it can without them). */
    private static TournamentParams parseParamsOrNull(String type, String parameters) {
        try {
            return Tournament.parseInfo(type, parameters);
        } catch (RuntimeException exp) {
            _log.warn("Unreadable tournament parameters: " + exp.getMessage());
            return null;
        }
    }

    private static String stageName(Tournament.Stage stage) {
        return stage == null ? null : stage.getHumanReadable();
    }

    /**
     * GET /admin/tournament?tournamentId=... - one tournament, queue or scheduled tournament for the unified form:
     * {tournamentId, status: scheduled|expired|live|finished, name, type, format, formatName,
     *  start ("yyyy-MM-ddTHH:mm", UTC), scheduled (has a scheduled-tournament row), editable, editBlocker,
     *  signedUp (players waiting in its queue), stage (enum name), stageName, round, playerCount,
     *  params: {the stored TournamentParams, type-specific fields and prizeTiers included}}.
     * Fields that do not apply are null.  404 when nothing has that id.
     */
    private void getTournamentForAdmin(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);
        var queryDecoder = new QueryStringDecoder(request.uri());
        String tournamentId = getQueryParameterSafely(queryDecoder, "tournamentId");
        Throw400IfStringNull("tournamentId", tournamentId);

        var view = _tournamentService.getAdminTournamentView(tournamentId.trim(), DateUtils.Now());
        if (view == null)
            throw new HttpProcessingException(404, "There is no tournament '" + tournamentId + "'.");

        TournamentParams params = null;
        String type = null;
        java.time.LocalDateTime start = null;
        String name = null;
        if (view.record() != null) {
            type = view.record().type;
            params = parseParamsOrNull(type, view.record().parameters);
            start = view.record().start_date;
            name = view.record().name;
        } else if (view.scheduled() != null) {
            type = view.scheduled().type;
            params = parseParamsOrNull(type, view.scheduled().parameters);
            start = view.scheduled().start_date;
            name = view.scheduled().name;
        } else if (view.queue() != null && view.queue().getInfo() != null) {
            params = view.queue().getInfo().Parameters();
            type = params.type == null ? null : params.type.name();
            start = params.startTime;
            name = view.queue().getTournamentQueueName();
        }

        Tournament.Stage stage = null;
        Integer round = null;
        if (view.live() != null) {
            stage = view.live().getTournamentStage();
            round = view.live().getCurrentRound();
        } else if (view.record() != null) {
            stage = view.record().stage == null ? null : Tournament.Stage.parseStage(view.record().stage);
            round = view.record().round;
        }
        boolean started = view.record() != null || view.live() != null;

        var result = new LinkedHashMap<String, Object>();
        result.put("tournamentId", view.tournamentId());
        result.put("status", view.status());
        result.put("name", name);
        result.put("type", type);
        String format = params == null ? null : params.format;
        result.put("format", format);
        result.put("formatName", formatNameOrCode(format));
        result.put("start", start == null ? null : start.format(DateUtils.APIDateTimeFormat));
        result.put("scheduled", view.scheduled() != null);
        result.put("editable", view.editBlocker() == null);
        result.put("editBlocker", view.editBlocker());
        result.put("signedUp", view.queue() == null ? null : view.signedUp());
        result.put("stage", stage == null ? null : stage.name());
        result.put("stageName", stageName(stage));
        result.put("round", round);
        result.put("playerCount", started ? _tournamentService.retrieveTournamentPlayers(view.tournamentId()).size() : null);
        result.put("params", params == null ? null : com.alibaba.fastjson2.JSON.parseObject(JsonUtils.Serialize(params)));
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    /**
     * GET /admin/tournaments - {tournaments: [{tournamentId, name, type, format, formatName, status, stage, stageName,
     * round, start}]}: live tournaments (newest first), scheduled ones that have not started (soonest first), then
     * those finished in the last {@value #ADMIN_TOURNAMENT_FINISHED_DAYS} days (newest first).
     */
    private void getTournamentsForAdmin(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);
        var list = new ArrayList<Map<String, Object>>();
        for (var summary : _tournamentService.getAdminTournamentList(DateUtils.Now(),
                ADMIN_TOURNAMENT_FINISHED_DAYS, ADMIN_TOURNAMENT_SCHEDULED_DAYS)) {
            var obj = new LinkedHashMap<String, Object>();
            obj.put("tournamentId", summary.tournamentId());
            obj.put("name", summary.name());
            obj.put("type", summary.type());
            obj.put("format", summary.format());
            obj.put("formatName", formatNameOrCode(summary.format()));
            obj.put("status", summary.status());
            obj.put("stage", summary.stage());
            obj.put("stageName", summary.stage() == null ? null : stageName(Tournament.Stage.parseStage(summary.stage())));
            obj.put("round", summary.round());
            obj.put("start", summary.start() == null ? null : summary.start().format(DateUtils.APIDateTimeFormat));
            list.add(obj);
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("tournaments", list);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    /**
     * GET /admin/tournamentPlayers?tournamentId=... - the players recorded in a started tournament, for the manual
     * table form: {tournamentId, name, format, formatName, started, players: [{name, dropped, hasDeck}]}.  A
     * tournament that has not started yet has no recorded players (started false, players empty).  404 when
     * nothing has that id.
     */
    private void getTournamentPlayersForAdmin(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateEventAdmin(request);
        var queryDecoder = new QueryStringDecoder(request.uri());
        String tournamentId = getQueryParameterSafely(queryDecoder, "tournamentId");
        Throw400IfStringNull("tournamentId", tournamentId);
        tournamentId = tournamentId.trim();

        var view = _tournamentService.getAdminTournamentView(tournamentId, DateUtils.Now());
        if (view == null)
            throw new HttpProcessingException(404, "There is no tournament '" + tournamentId + "'.");

        var result = new LinkedHashMap<String, Object>();
        result.put("tournamentId", tournamentId);
        var players = new ArrayList<Map<String, Object>>();
        if (view.record() != null) {
            TournamentParams params = parseParamsOrNull(view.record().type, view.record().parameters);
            String format = params == null ? null : params.format;
            result.put("name", view.record().name);
            result.put("format", format);
            result.put("formatName", formatNameOrCode(format));
            result.put("started", true);
            for (var player : _tournamentService.getAdminTournamentPlayers(tournamentId, formatNameOrCode(format))) {
                var obj = new LinkedHashMap<String, Object>();
                obj.put("name", player.name());
                obj.put("dropped", player.dropped());
                obj.put("hasDeck", player.hasDeck());
                players.add(obj);
            }
        } else {
            result.put("name", view.scheduled() != null ? view.scheduled().name : null);
            result.put("format", view.scheduled() != null ? view.scheduled().format : null);
            result.put("formatName", formatNameOrCode(view.scheduled() != null ? view.scheduled().format : null));
            result.put("started", false);
        }
        result.put("players", players);
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    // ---- end t3-tournament-admin ----

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

        try {
            _cardLibrary.reloadAllDefinitions();

            _productLibrary.ReloadPacks();

            _formatLibrary.ReloadFormats();
            _formatLibrary.ReloadSealedTemplates();

            _soloDraftDefinitions.ReloadDraftsFromFile();

            _tableDraftLibrary.reloadDraftsFromFile(_cardLibrary, _formatLibrary);
            _tournamentService.reloadQueues();

            responseWriter.writeHtmlResponse("OK");
        }
        catch(RuntimeException exception) {
            responseWriter.writeHtmlResponse(exception.toString());
        }
        finally {
            _chatServer.sendSystemMessageToAllChatRooms("@everyone Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page and please let the mod team know in the game hall ASAP if the problem doesn't go away.");
        }
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
        _cacheManager.clearCaches();
        _hallServer.cleanup(true);
    }
}
