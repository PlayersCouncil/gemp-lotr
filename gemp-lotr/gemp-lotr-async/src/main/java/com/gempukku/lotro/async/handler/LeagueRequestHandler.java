package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.DateUtils;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueData;
import com.gempukku.lotro.league.LeagueSerieData;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.packs.ProductLibrary;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LeagueRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LeagueService _leagueService;
    private final LotroFormatLibrary _formatLibrary;
    private final LotroCardBlueprintLibrary _library;
    private final ProductLibrary _productLibrary;

    private static final Logger _log = LogManager.getLogger(LeagueRequestHandler.class);

    public LeagueRequestHandler(Map<Type, Object> context) {
        super(context);

        _library = extractObject(context, LotroCardBlueprintLibrary.class);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _leagueService = extractObject(context, LeagueService.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            getNonExpiredLeagues(request, responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getLeagueInformation(request, uri.substring(1), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            joinLeague(request, uri.substring(1), responseWriter, remoteIp);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void joinLeague(HttpRequest request, String leagueType, ResponseWriter responseWriter, String remoteIp) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        League league = _leagueService.getLeagueByType(leagueType);
        if (league == null)
            throw new HttpProcessingException(404);

        if (!_leagueService.playerJoinsLeague(league, resourceOwner, remoteIp))
            throw new HttpProcessingException(409);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getLeagueInformation(HttpRequest request, String leagueType, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        Document doc = documentBuilder.newDocument();

        League league = getLeagueByType(leagueType);

        if (league == null)
            throw new HttpProcessingException(404);

        final LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
        final List<LeagueSerieData> series = leagueData.getSeries();

        int end = series.get(series.size() - 1).getEnd();
        int start = series.get(0).getStart();
        int currentDate = DateUtils.getCurrentDate();

        Element leagueElem = doc.createElement("league");
        boolean inLeague = _leagueService.isPlayerInLeague(league, resourceOwner);

        leagueElem.setAttribute("member", String.valueOf(inLeague));
        leagueElem.setAttribute("joinable", String.valueOf(!inLeague && end >= currentDate));
        leagueElem.setAttribute("draftable", String.valueOf(inLeague && leagueData.isSoloDraftLeague() && start <= currentDate));
        leagueElem.setAttribute("type", league.getType());
        leagueElem.setAttribute("name", league.getName());
        leagueElem.setAttribute("cost", String.valueOf(league.getCost()));
        leagueElem.setAttribute("start", String.valueOf(series.get(0).getStart()));
        leagueElem.setAttribute("end", String.valueOf(end));

        for (LeagueSerieData serie : series) {
            Element serieElem = doc.createElement("serie");
            serieElem.setAttribute("type", serie.getName());
            serieElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
            serieElem.setAttribute("start", String.valueOf(serie.getStart()));
            serieElem.setAttribute("end", String.valueOf(serie.getEnd()));
            serieElem.setAttribute("formatType", serie.getFormat().getCode());
            serieElem.setAttribute("format", serie.getFormat().getName());
            serieElem.setAttribute("collection", serie.getCollectionType().getFullName());
            serieElem.setAttribute("limited", String.valueOf(serie.isLimited()));

            Element matchesElem = doc.createElement("matches");
            Collection<LeagueMatchResult> playerMatches = _leagueService.getPlayerMatchesInSerie(league, serie, resourceOwner.getName());
            for (LeagueMatchResult playerMatch : playerMatches) {
                Element matchElem = doc.createElement("match");
                matchElem.setAttribute("winner", playerMatch.getWinner());
                matchElem.setAttribute("loser", playerMatch.getLoser());
                matchesElem.appendChild(matchElem);
            }
            serieElem.appendChild(matchesElem);

            final List<PlayerStanding> standings = _leagueService.getLeagueSerieStandings(league, serie);
            for (PlayerStanding standing : standings) {
                Element standingElem = doc.createElement("standing");
                setStandingAttributes(standing, standingElem);
                serieElem.appendChild(standingElem);
            }

            leagueElem.appendChild(serieElem);
        }

        List<PlayerStanding> leagueStandings = _leagueService.getLeagueStandings(league);
        for (PlayerStanding standing : leagueStandings) {
            Element standingElem = doc.createElement("leagueStanding");
            setStandingAttributes(standing, standingElem);
            leagueElem.appendChild(standingElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponse(doc);
    }

    private void getNonExpiredLeagues(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element leagues = doc.createElement("leagues");

        for (League league : _leagueService.getActiveLeagues()) {
            final LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
            final List<LeagueSerieData> series = leagueData.getSeries();

            int end = series.get(series.size() - 1).getEnd();

            Element leagueElem = doc.createElement("league");

            leagueElem.setAttribute("type", league.getType());
            leagueElem.setAttribute("name", league.getName());
            leagueElem.setAttribute("start", String.valueOf(series.get(0).getStart()));
            leagueElem.setAttribute("end", String.valueOf(end));

            leagues.appendChild(leagueElem);
        }

        doc.appendChild(leagues);

        responseWriter.writeXmlResponse(doc);
    }

    public League getLeagueByType(String type) {
        for (League league : _leagueService.getActiveLeagues()) {
            if (league.getType().equals(type))
                return league;
        }
        return null;
    }

    private void setStandingAttributes(PlayerStanding standing, Element standingElem) {
        standingElem.setAttribute("player", standing.playerName());
        standingElem.setAttribute("standing", String.valueOf(standing.standing()));
        standingElem.setAttribute("points", String.valueOf(standing.points()));
        standingElem.setAttribute("gamesPlayed", String.valueOf(standing.gamesPlayed()));
        DecimalFormat format = new DecimalFormat("##0.00%");
        standingElem.setAttribute("opponentWin", format.format(standing.opponentScore()));
    }

}
