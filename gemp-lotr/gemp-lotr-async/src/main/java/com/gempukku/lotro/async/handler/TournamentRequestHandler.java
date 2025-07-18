package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.DeckRenderer;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.HallException;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.*;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TournamentRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    public static final Duration RecentTournamentDuration = Duration.ofDays(7);

    private final TournamentService _tournamentService;
    private final LotroFormatLibrary _formatLibrary;
    private final LotroCardBlueprintLibrary _library;
    private final SortAndFilterCards _sortAndFilterCards;
    private final DeckRenderer _deckRenderer;

    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final TableDraftDefinitions _tableDraftLibrary;
    private final ProductLibrary _productLibrary;

    private final HallServer _hallServer;

    private static final Logger _log = LogManager.getLogger(TournamentRequestHandler.class);

    public TournamentRequestHandler(Map<Type, Object> context) {
        super(context);

        _tournamentService = extractObject(context, TournamentService.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _library = extractObject(context, LotroCardBlueprintLibrary.class);
        _sortAndFilterCards = new SortAndFilterCards();

        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _tableDraftLibrary = extractObject(context, TableDraftDefinitions.class);
        _productLibrary = extractObject(context, ProductLibrary.class);

        _deckRenderer = new DeckRenderer(_library, _formatLibrary, _sortAndFilterCards);

        _hallServer = extractObject(context, HallServer.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            getCurrentTournaments(request, responseWriter);
        } else if (uri.equals("/history") && request.method() == HttpMethod.GET) {
            getTournamentHistory(request, responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/html") && uri.contains("/deck/") && request.method() == HttpMethod.GET) {
            getTournamentDeck(request, uri.substring(1, uri.indexOf("/deck/")), uri.substring(uri.indexOf("/deck/") + 6, uri.lastIndexOf("/html")), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/html") && uri.contains("/report/") && request.method() == HttpMethod.GET) {
            getTournamentReport(request, uri.substring(1, uri.indexOf("/report/")), responseWriter);
        } else if (uri.equals("/create") && request.method() == HttpMethod.POST) {
            processPlayerMadeTournament(request, responseWriter);
        } else if (uri.equals("/tournamentFormats") && request.method() == HttpMethod.GET) {
            getTournamentFormats(request, responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getTournamentInfo(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getTournamentFormats(HttpRequest request, ResponseWriter responseWriter) {
        JSONDefs.PlayerMadeTournamentAvailableFormats data = new JSONDefs.PlayerMadeTournamentAvailableFormats();

        Map<String, String> availableSoloDraftFormats = new HashMap<>();
        availableSoloDraftFormats.put("fotr_draft", "Fellowship Block");
        availableSoloDraftFormats.put("ttt_draft", "Towers Block");
        availableSoloDraftFormats.put("hobbit_random_draft", "Hobbit");
        List<String> orderedSoloDrafts = List.of("fotr_draft", "ttt_draft", "hobbit_random_draft");

        data.constructed = _formatLibrary.getHallFormats().values().stream()
                .map(constructedFormat -> new JSONDefs.ItemStub(constructedFormat.getCode(), constructedFormat.getName()))
                .collect(Collectors.toList());
        data.sealed = _formatLibrary.getAllHallSealedTemplates().values().stream()
                .map(sealed -> new JSONDefs.ItemStub(sealed.GetID(), sealed.GetName().substring(3)))
                .collect(Collectors.toList());
        data.sealed.sort(Comparator.comparing(o -> _formatLibrary.GetSealedTemplate(o.code).GetName())); // Sort by sealed format number
        data.soloDrafts = orderedSoloDrafts.stream()
                .filter(code -> _soloDraftDefinitions.getAllSoloDrafts().values().stream().anyMatch(soloDraft -> code.equals(soloDraft.getCode()))).map(code -> new JSONDefs.ItemStub(code, availableSoloDraftFormats.get(code)))
                .collect(Collectors.toList());
        data.tableDrafts = _tableDraftLibrary.getAllTableDrafts().stream()
                .map(tableDraftDefinition -> new JSONDefs.LiveDraftInfo(tableDraftDefinition.getCode(), tableDraftDefinition.getName(), tableDraftDefinition.getMaxPlayers(), tableDraftDefinition.getRecommendedTimer().name()))
                .collect(Collectors.toList());
        data.draftTimerTypes = DraftTimer.getAllTypes();

        responseWriter.writeJsonResponse(JsonUtils.Serialize(data));
    }

    private void processPlayerMadeTournament(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        var postDecoder = new HttpPostRequestDecoder(request);

        String participantId = getFormParameterSafely(postDecoder, "participantId");
        Player resourceOwner = getResourceOwnerSafely(request, participantId);
        String deckName = getFormParameterSafely(postDecoder, "deckName");

        String typeStr = getFormParameterSafely(postDecoder, "type");
        String deckbuildingDurationStr = getFormParameterSafely(postDecoder, "deckbuildingDuration");
        String playoff = getFormParameterSafely(postDecoder, "playoff");
        String maxPlayersStr = getFormParameterSafely(postDecoder, "maxPlayers");


        String competitiveStr = getFormParameterSafely(postDecoder, "competitive");
        boolean competitive = Throw400IfNullOrNonBoolean("competitive", competitiveStr);

        String startableStr = getFormParameterSafely(postDecoder, "startable");
        boolean startable = Throw400IfNullOrNonBoolean("startable", startableStr);

        String readyCheckStr = getFormParameterSafely(postDecoder, "readyCheck");
        int readyCheck = Throw400IfNullOrNonInteger("readyCheck", readyCheckStr);

        String constructedFormatCodeStr = getFormParameterSafely(postDecoder, "constructedFormatCode");

        String sealedFormatCodeStr = getFormParameterSafely(postDecoder, "sealedFormatCode");

        String soloDraftFormatCodeStr = getFormParameterSafely(postDecoder, "soloDraftFormatCode");

        String soloTableDraftFormatCodeStr = getFormParameterSafely(postDecoder, "soloTableDraftFormatCode");

        String tableDraftFormatCodeStr = getFormParameterSafely(postDecoder, "tableDraftFormatCode");
        String tableDraftTimer = getFormParameterSafely(postDecoder, "tableDraftTimer");


        Throw400IfStringNull("type", typeStr);
        var type = Tournament.TournamentType.parse(typeStr);
        Throw400IfValidationFails("type", typeStr, type != null);

        Throw400IfValidationFails("playoff", playoff,Tournament.getPairingMechanism(playoff) != null);

        int maxPlayers = Throw400IfNullOrNonInteger("maxPlayers", maxPlayersStr);

        var params = new TournamentParams();

        String casualPrefix = "Casual ";
        String competitivePrefix = "Competitive ";
        String prefix = competitive ? competitivePrefix : casualPrefix;

        if (type == Tournament.TournamentType.CONSTRUCTED) {
            params.type = Tournament.TournamentType.CONSTRUCTED;
            var format = _formatLibrary.getFormat(constructedFormatCodeStr);
            Throw400IfValidationFails("constructedFormatCodeStr", constructedFormatCodeStr,format != null);
            params.format = constructedFormatCodeStr;
            params.name = prefix + format.getName();
            params.requiresDeck = true;
        } else if(type == Tournament.TournamentType.SEALED) {
            var sealedParams = new SealedTournamentParams();
            sealedParams.type = Tournament.TournamentType.SEALED;

            sealedParams.deckbuildingDuration = Throw400IfNullOrNonInteger("deckbuildingDuration", deckbuildingDurationStr);
            sealedParams.turnInDuration = 0;

            Throw400IfStringNull("sealedFormatCode", sealedFormatCodeStr);
            var sealedFormat = _formatLibrary.GetSealedTemplate(sealedFormatCodeStr);
            Throw400IfValidationFails("sealedFormatCode", sealedFormatCodeStr,sealedFormat != null);
            sealedParams.sealedFormatCode = sealedFormatCodeStr;
            sealedParams.format = sealedFormat.GetFormat().getCode();
            sealedParams.name = prefix + sealedFormat.GetName().substring(3); // Strip the ordering number for sealed formats
            sealedParams.requiresDeck = false;
            params = sealedParams;
        }
        else if (type == Tournament.TournamentType.SOLODRAFT) {
            var soloDraftParams = new SoloDraftTournamentParams();
            soloDraftParams.type = Tournament.TournamentType.SOLODRAFT;

            soloDraftParams.deckbuildingDuration = Throw400IfNullOrNonInteger("soloDraftDeckbuildingDuration", deckbuildingDurationStr);
            soloDraftParams.turnInDuration = 0;

            Throw400IfStringNull("soloDraftFormatCode", soloDraftFormatCodeStr);
            var soloDraftFormat = _soloDraftDefinitions.getSoloDraft(soloDraftFormatCodeStr);
            Throw400IfValidationFails("soloDraftFormatCode", soloDraftFormatCodeStr,soloDraftFormat != null);
            soloDraftParams.soloDraftFormatCode = soloDraftFormatCodeStr;
            soloDraftParams.format = soloDraftFormat.getFormat();
            switch (soloDraftFormatCodeStr) {
                case "fotr_draft" -> soloDraftParams.name = prefix + "FotR Solo Draft";
                case "ttt_draft" -> soloDraftParams.name = prefix + "TTT Solo Draft";
                case "hobbit_random_draft" -> soloDraftParams.name = prefix + "Hobbit Solo Draft";
                default -> soloDraftParams.name = prefix + soloDraftFormatCodeStr;
            }
            soloDraftParams.requiresDeck = false;
            params = soloDraftParams;
        }
        else if (type == Tournament.TournamentType.TABLE_SOLODRAFT) {
            var soloTableDraftParams = new SoloTableDraftTournamentParams();
            soloTableDraftParams.type = Tournament.TournamentType.TABLE_SOLODRAFT;

            soloTableDraftParams.deckbuildingDuration = Throw400IfNullOrNonInteger("soloTableDraftDeckbuildingDuration", deckbuildingDurationStr);
            soloTableDraftParams.turnInDuration = 0;

            Throw400IfStringNull("soloTableDraftFormatCode", soloTableDraftFormatCodeStr);
            var tableDraftDefinition = _tableDraftLibrary.getTableDraftDefinition(soloTableDraftFormatCodeStr);
            Throw400IfValidationFails("soloTableDraftFormatCode", soloTableDraftFormatCodeStr,tableDraftDefinition != null);
            soloTableDraftParams.soloTableDraftFormatCode = soloTableDraftFormatCodeStr;
            soloTableDraftParams.format = tableDraftDefinition.getFormat();
            soloTableDraftParams.name = prefix + tableDraftDefinition.getName();
            soloTableDraftParams.requiresDeck = false;
            params = soloTableDraftParams;
        }
        else if (type == Tournament.TournamentType.TABLE_DRAFT) {
            var tableDraftParams = new TableDraftTournamentParams();
            tableDraftParams.type = Tournament.TournamentType.TABLE_DRAFT;

            tableDraftParams.deckbuildingDuration = Throw400IfNullOrNonInteger("tableDraftDeckbuildingDuration", deckbuildingDurationStr);
            tableDraftParams.turnInDuration = 0;

            Throw400IfStringNull("tableDraftFormatCode", tableDraftFormatCodeStr);
            var tableDraftDefinition = _tableDraftLibrary.getTableDraftDefinition(tableDraftFormatCodeStr);
            Throw400IfValidationFails("tableDraftFormatCode", tableDraftFormatCodeStr,tableDraftDefinition != null);
            //Check if all players can get to one table
            Throw400IfValidationFails("maxPlayers", maxPlayersStr, tableDraftDefinition.getMaxPlayers() >= maxPlayers);
            tableDraftParams.tableDraftFormatCode = tableDraftFormatCodeStr;
            tableDraftParams.format = tableDraftDefinition.getFormat();
            tableDraftParams.draftTimerType = DraftTimer.getTypeFromString(tableDraftTimer);
            tableDraftParams.name = prefix + tableDraftDefinition.getName();
            tableDraftParams.requiresDeck = false;
            params = tableDraftParams;
        }
        else {
            Throw400IfValidationFails("type", typeStr, false, "Unknown game type");
            return;
        }

        params.tournamentId =  params.format + System.currentTimeMillis();
        params.cost = 0; // Gold is not being used, they can be free to enter
        params.playoff = Tournament.PairingType.parse(playoff);
        params.tiebreaker = "owr";
        params.prizes = Tournament.PrizeType.WIN_GAME_FOR_AWARD; // At 4+ players, get one Event Award for each win or bye
        params.maximumPlayers = maxPlayers;
        params.manualKickoff = false;

        TournamentInfo info;
        if (type == Tournament.TournamentType.CONSTRUCTED) {
            info = new TournamentInfo(_tournamentService, _productLibrary, _formatLibrary, DateUtils.Today(), params);
        } else if (type == Tournament.TournamentType.SEALED) {
            info = new SealedTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, DateUtils.Today(), (SealedTournamentParams)params);
        }
        else if (type == Tournament.TournamentType.SOLODRAFT) {
            info = new SoloDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, DateUtils.Today(), ((SoloDraftTournamentParams) params), _soloDraftDefinitions);
        }
        else if (type == Tournament.TournamentType.TABLE_SOLODRAFT) {
            info = new SoloTableDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, DateUtils.Today(), ((SoloTableDraftTournamentParams) params), _tableDraftLibrary);
        }
        else if (type == Tournament.TournamentType.TABLE_DRAFT) {
            info = new TableDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, DateUtils.Today(), ((TableDraftTournamentParams) params), _tableDraftLibrary);
        }
        else {
            Throw400IfValidationFails("type", typeStr, false, "Unknown game type");
            return;
        }
        try {
            if (_hallServer.addPlayerMadeQueue(info, resourceOwner, deckName, startable, readyCheck)) {
                responseWriter.sendJsonOK();
            } else {
                Throw400IfValidationFails("Error", "Error", false, "Error while creating queue or joining");
            }
        } catch (HallException badDeck) {
            Throw400IfValidationFails("deckName", deckName, false, "Select valid deck for the requested format");
        }
    }

    private void getTournamentInfo(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(404);

        Element tournamentElem = doc.createElement("tournament");

        tournamentElem.setAttribute("id", tournament.getTournamentId());
        tournamentElem.setAttribute("name", tournament.getTournamentName());
        tournamentElem.setAttribute("format", _formatLibrary.getFormat(tournament.getFormatCode()).getName());
        tournamentElem.setAttribute("collection", tournament.getCollectionType().getFullName());
        tournamentElem.setAttribute("round", String.valueOf(tournament.getCurrentRound()));
        tournamentElem.setAttribute("stage", tournament.getTournamentStage().getHumanReadable());

        List<PlayerStanding> leagueStandings = tournament.getCurrentStandings();
        for (PlayerStanding standing : leagueStandings) {
            Element standingElem = doc.createElement("tournamentStanding");
            setStandingAttributes(standing, standingElem);
            tournamentElem.appendChild(standingElem);
        }

        doc.appendChild(tournamentElem);

        responseWriter.writeXmlResponse(doc);
    }

    private void setStandingAttributes(PlayerStanding standing, Element standingElem) {
        standingElem.setAttribute("player", standing.playerName);
        standingElem.setAttribute("standing", String.valueOf(standing.standing));
        standingElem.setAttribute("points", String.valueOf(standing.points));
        standingElem.setAttribute("gamesPlayed", String.valueOf(standing.gamesPlayed));
        DecimalFormat format = new DecimalFormat("##0.00%");
        standingElem.setAttribute("opponentWin", format.format(standing.opponentWinRate));
        standingElem.setAttribute("medianScore", String.valueOf(standing.medianScore));
        standingElem.setAttribute("cumulativeScore", String.valueOf(standing.cumulativeScore));
    }

    private void getTournamentDeck(HttpRequest request, String tournamentId, String playerName, ResponseWriter responseWriter) throws Exception {
        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(404);

        if (tournament.getTournamentStage() != Tournament.Stage.FINISHED)
            throw new HttpProcessingException(403);

        LotroDeck deck = _tournamentService.retrievePlayerDeck(tournamentId, playerName, tournament.getFormatCode());
        if (deck == null)
            throw new HttpProcessingException(404);

        String fragment = _deckRenderer.convertDeckToHTMLFragment(deck, playerName);

        responseWriter.writeHtmlResponse(_deckRenderer.AddDeckReadoutHeaderAndFooter(fragment));
    }

    private void getTournamentReport(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(404);

        if (tournament.getTournamentStage() != Tournament.Stage.FINISHED)
            throw new HttpProcessingException(403);

        var result = tournament.produceReport(_deckRenderer);

        responseWriter.writeHtmlResponse(result);
    }

    private void getTournamentHistory(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element tournaments = doc.createElement("tournaments");

        for (Tournament tournament : _tournamentService.getOldTournaments(ZonedDateTime.now().minus(RecentTournamentDuration)).reversed()) {
            Element tournamentElem = doc.createElement("tournament");

            tournamentElem.setAttribute("id", tournament.getTournamentId());
            tournamentElem.setAttribute("name", tournament.getTournamentName());
            tournamentElem.setAttribute("format", _formatLibrary.getFormat(tournament.getFormatCode()).getName());
            tournamentElem.setAttribute("collection", tournament.getCollectionType().getFullName());
            tournamentElem.setAttribute("round", String.valueOf(tournament.getCurrentRound()));
            tournamentElem.setAttribute("stage", tournament.getTournamentStage().getHumanReadable());

            if (tournament.getTournamentStage().equals(Tournament.Stage.FINISHED) && tournament.getCurrentRound() == 0) {
                // do NOT include past tournaments with 0 games played (for example draft of one player against bots)
                continue;
            }
            tournaments.appendChild(tournamentElem);
        }

        doc.appendChild(tournaments);

        responseWriter.writeXmlResponse(doc);
    }

    private void getCurrentTournaments(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element tournaments = doc.createElement("tournaments");

        for (Tournament tournament : _tournamentService.getLiveTournaments()) {
            Element tournamentElem = doc.createElement("tournament");

            tournamentElem.setAttribute("id", tournament.getTournamentId());
            tournamentElem.setAttribute("name", tournament.getTournamentName());
            tournamentElem.setAttribute("format", _formatLibrary.getFormat(tournament.getFormatCode()).getName());
            tournamentElem.setAttribute("collection", tournament.getCollectionType().getFullName());
            tournamentElem.setAttribute("round", String.valueOf(tournament.getCurrentRound()));
            tournamentElem.setAttribute("stage", tournament.getTournamentStage().getHumanReadable());

            tournaments.appendChild(tournamentElem);
        }

        doc.appendChild(tournaments);

        responseWriter.writeXmlResponse(doc);
    }
}
