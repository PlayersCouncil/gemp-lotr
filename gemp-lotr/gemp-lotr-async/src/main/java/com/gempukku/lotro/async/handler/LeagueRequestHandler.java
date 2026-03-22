package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueData;
import com.gempukku.lotro.league.LeagueSerieInfo;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.league.RTMDLeague;
import com.gempukku.lotro.packs.ProductLibrary;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
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
        final List<LeagueSerieInfo> series = leagueData.getSeries();

        var end = series.getLast().getEnd();
        var start = series.getFirst().getStart();
        var currentDate = DateUtils.Today();

        Element leagueElem = doc.createElement("league");
        boolean inLeague = _leagueService.isPlayerInLeague(league, resourceOwner);

        leagueElem.setAttribute("member", String.valueOf(inLeague));
        leagueElem.setAttribute("joinable", String.valueOf(!inLeague && !league.inviteOnly() && currentDate.isBefore(end)));
        leagueElem.setAttribute("draftable", String.valueOf(inLeague && leagueData.isSoloDraftLeague() && DateUtils.IsAfterStart(currentDate, start)));
        leagueElem.setAttribute("code", league.getCodeStr());
        leagueElem.setAttribute("name", league.getName());
        leagueElem.setAttribute("desc", league.getDescription());
        leagueElem.setAttribute("inviteOnly", String.valueOf(league.inviteOnly()));
        leagueElem.setAttribute("cost", String.valueOf(league.getCost()));
        leagueElem.setAttribute("start", DateUtils.FormatDate(start));
        leagueElem.setAttribute("end", DateUtils.FormatDate(end));

        // Detect RTMD league and add path/position data
        boolean isRTMD = leagueData instanceof RTMDLeague;
        RTMDLeague rtmdLeague = isRTMD ? (RTMDLeague) leagueData : null;
        List<PlayerStanding> leagueStandings = _leagueService.getLeagueStandings(league);

        if (isRTMD) {
            leagueElem.setAttribute("type", "RTMD");
            leagueElem.setAttribute("pathLength", String.valueOf(rtmdLeague.getPathLength()));
            leagueElem.setAttribute("cumulative", String.valueOf(rtmdLeague.isCumulative()));
            leagueElem.setAttribute("advancementMode", rtmdLeague.getParameters().raceAdvancementMode.toString());
            leagueElem.setAttribute("advanceFactor", String.valueOf(rtmdLeague.getAdvanceFactor()));

            // Current player's position
            if (inLeague) {
                int playerPosition = rtmdLeague.getPlayerPosition(resourceOwner.getName(), leagueStandings);
                leagueElem.setAttribute("playerPosition", String.valueOf(playerPosition));
            }

            // Path definition (modifier + visual card pairs)
            List<String> path = rtmdLeague.getPath();
            List<String> visualPath = rtmdLeague.getVisualPath();
            for (int i = 0; i < path.size(); i++) {
                Element siteElem = doc.createElement("metaSite");
                siteElem.setAttribute("position", String.valueOf(i + 1));
                siteElem.setAttribute("blueprintId", path.get(i));
                if (visualPath != null && i < visualPath.size()) {
                    siteElem.setAttribute("visualBlueprintId", visualPath.get(i));
                }
                try {
                    var bp = _library.getLotroCardBlueprint(path.get(i));
                    if (bp != null) {
                        siteElem.setAttribute("name", bp.getFullName());
                        var overrides = bp.getDeckBuildingOverrides();
                        if (overrides != null && overrides.shouldSkipSiteBlockValidation()) {
                            siteElem.setAttribute("siteOverride", "true");
                        }
                    }
                } catch (Exception ignored) {}
                leagueElem.appendChild(siteElem);
            }
        }

        for (LeagueSerieInfo serie : series) {
            Element serieElem = doc.createElement("serie");
            serieElem.setAttribute("type", serie.getName());
            serieElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
            serieElem.setAttribute("start", DateUtils.FormatDate(serie.getStart()));
            serieElem.setAttribute("end", DateUtils.FormatDate(serie.getEnd()));
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

            final List<PlayerStanding> serieStandings = _leagueService.getLeagueSerieStandings(league, serie);
            for (PlayerStanding standing : serieStandings) {
                Element standingElem = doc.createElement("standing");
                setStandingAttributes(standing, standingElem);
                serieElem.appendChild(standingElem);
            }

            leagueElem.appendChild(serieElem);
        }

        for (PlayerStanding standing : leagueStandings) {
            Element standingElem = doc.createElement("leagueStanding");
            setStandingAttributes(standing, standingElem);

            // Add position for RTMD leagues
            if (isRTMD) {
                int position = rtmdLeague.getPlayerPosition(standing.playerName, leagueStandings);
                standingElem.setAttribute("position", String.valueOf(position));
            }

            leagueElem.appendChild(standingElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponse(doc);
    }

    private void getNonExpiredLeagues(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        Document doc = documentBuilder.newDocument();
        Element leagues = doc.createElement("leagues");

        for (League league : _leagueService.getActiveLeagues()) {
            final LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
            final List<LeagueSerieInfo> series = leagueData.getSeries();

            var end = series.getLast().getEnd();
            var start = series.getFirst().getStart();
            var currentDate = DateUtils.Today();

            Element leagueElem = doc.createElement("league");
            boolean inLeague = _leagueService.isPlayerInLeague(league, resourceOwner);

            leagueElem.setAttribute("member", String.valueOf(inLeague));
            leagueElem.setAttribute("joinable", String.valueOf(!inLeague && !league.inviteOnly() && currentDate.isBefore(end)));
            leagueElem.setAttribute("draftable", String.valueOf(inLeague && leagueData.isSoloDraftLeague() && DateUtils.IsAfterStart(currentDate, start)));
            leagueElem.setAttribute("code", league.getCodeStr());
            leagueElem.setAttribute("name", league.getName());
            leagueElem.setAttribute("desc", league.getDescription());
            leagueElem.setAttribute("inviteOnly", String.valueOf(league.inviteOnly()));
            leagueElem.setAttribute("start", DateUtils.FormatDate(series.getFirst().getStart()));
            leagueElem.setAttribute("end", DateUtils.FormatDate(series.getLast().getEnd()));
            leagueElem.setAttribute("type", league.getType().toString());
            leagueElem.setAttribute("formatCode", series.getFirst().getFormat().getCode());

            if (league.getType() == League.LeagueType.RTMD) {
                RTMDLeague rtmd = (RTMDLeague) leagueData;
                List<PlayerStanding> standings = _leagueService.getLeagueStandings(league);
                int position = rtmd.getPlayerPosition(resourceOwner.getName(), standings);
                List<String> metasites = rtmd.getMetaSitesForPosition(position);
                List<String> visualCards = rtmd.getVisualCardsForPosition(position);
                leagueElem.setAttribute("pathLength", String.valueOf(rtmd.getPathLength()));
                leagueElem.setAttribute("cumulative", String.valueOf(rtmd.isCumulative()));
                leagueElem.setAttribute("advancementMode", rtmd.getParameters().raceAdvancementMode.toString());
                leagueElem.setAttribute("advanceFactor", String.valueOf(rtmd.getAdvanceFactor()));

                for (int i = 0; i < metasites.size(); i++) {
                    Element siteElem = doc.createElement("metaSite");
                    siteElem.setAttribute("position", String.valueOf(i + 1));
                    siteElem.setAttribute("blueprintId", metasites.get(i));
                    if (visualCards != null && i < visualCards.size()) {
                        siteElem.setAttribute("visualBlueprintId", visualCards.get(i));
                    }
                    try {
                        var bp = _library.getLotroCardBlueprint(metasites.get(i));
                        if (bp != null) {
                            siteElem.setAttribute("name", bp.getFullName());
                        }
                    } catch (Exception ignored) {}
                    leagueElem.appendChild(siteElem);
                }

                // Current player's position
                if (inLeague) {
                    int playerPosition = rtmd.getPlayerPosition(resourceOwner.getName(), standings);
                    leagueElem.setAttribute("playerPosition", String.valueOf(playerPosition));
                }
            }

            leagues.appendChild(leagueElem);
        }

        doc.appendChild(leagues);

        responseWriter.writeXmlResponse(doc);
    }

    public League getLeagueByType(String type) {
        for (League league : _leagueService.getActiveLeagues()) {
            if (league.getCodeStr().equals(type))
                return league;
        }
        return null;
    }

    private void setStandingAttributes(PlayerStanding standing, Element standingElem) {
        standingElem.setAttribute("player", standing.playerName);
        standingElem.setAttribute("standing", String.valueOf(standing.standing));
        standingElem.setAttribute("points", String.valueOf(standing.points));
        standingElem.setAttribute("gamesPlayed", String.valueOf(standing.gamesPlayed));
        DecimalFormat format = new DecimalFormat("##0.00%");
        standingElem.setAttribute("opponentWin", format.format(standing.opponentWinRate));
    }

}
