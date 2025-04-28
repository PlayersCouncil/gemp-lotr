package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.DeckRenderer;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
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
import java.util.List;
import java.util.Map;

public class TournamentRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    public static final Duration RecentTournamentDuration = Duration.ofDays(7);

    private final TournamentService _tournamentService;
    private final LotroFormatLibrary _formatLibrary;
    private final LotroCardBlueprintLibrary _library;
    private final SortAndFilterCards _sortAndFilterCards;
    private final DeckRenderer _deckRenderer;

    private static final Logger _log = LogManager.getLogger(TournamentRequestHandler.class);

    public TournamentRequestHandler(Map<Type, Object> context) {
        super(context);

        _tournamentService = extractObject(context, TournamentService.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _library = extractObject(context, LotroCardBlueprintLibrary.class);
        _sortAndFilterCards = new SortAndFilterCards();

        _deckRenderer = new DeckRenderer(_library, _formatLibrary, _sortAndFilterCards);
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
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getTournamentInfo(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(404);
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
