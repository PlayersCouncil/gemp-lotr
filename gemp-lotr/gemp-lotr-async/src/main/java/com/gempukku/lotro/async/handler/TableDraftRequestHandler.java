package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.DraftPlayer;
import com.gempukku.lotro.draft3.TableDraft;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.SoloTableDraftTournament;
import com.gempukku.lotro.tournament.TableDraftTournament;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TableDraftRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CollectionsManager _collectionsManager;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LotroCardBlueprintLibrary _cardLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final ProductLibrary _productLibrary;
    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final TableDraftDefinitions _tableDraftDefinitions;

    private final Map<String, Map<String, DraftPlayer>> eventMap = new HashMap<>();

    private static final Logger _log = LogManager.getLogger(TableDraftRequestHandler.class);

    public TableDraftRequestHandler(Map<Type, Object> context) {
        super(context);
        _tournamentService = extractObject(context, TournamentService.class);
        _leagueService = extractObject(context, LeagueService.class);
        _cardLibrary = extractObject(context, LotroCardBlueprintLibrary.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _collectionsManager = extractObject(context, CollectionsManager.class);
        _tableDraftDefinitions = extractObject(context, TableDraftDefinitions.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            makePick(request, uri.substring(1), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getAvailablePicks(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getAvailablePicks(HttpRequest request, String eventId, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        if (eventMap.containsKey(eventId) && eventMap.get(eventId).containsKey(resourceOwner.getName())) {
            // Check saved players first
            DraftPlayer thisPlayer = eventMap.get(eventId).get(resourceOwner.getName());
            writeXml(responseWriter, thisPlayer);

            clearCacheIfDraftFinished(eventId, resourceOwner, thisPlayer);
        } else {
            // Look for data in db and then save
            TableDraft tableDraft;

            League league = findLeagueById(eventId);
            Tournament tournament = findTournamentById(eventId);

            if (league != null) {
                throw new HttpProcessingException(404);
//            LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
//            var leagueStart = leagueData.getSeries().getFirst().getStart();
//
//            if (!leagueData.isSoloDraftLeague() || DateUtils.Today().isBefore(leagueStart))
//                throw new HttpProcessingException(404);
//
//            SoloDraftLeague soloDraftLeague = (SoloDraftLeague) leagueData;
//            collectionType = soloDraftLeague.getCollectionType();
//            soloDraft = soloDraftLeague.getSoloDraft();
            } else if (tournament != null) {
                if ((tournament instanceof SoloTableDraftTournament) && (tournament.getTournamentStage() == Tournament.Stage.DECK_BUILDING || tournament.getTournamentStage() == Tournament.Stage.DECK_REGISTRATION)) {
                    tableDraft = ((SoloTableDraftTournament) tournament).getTableDraft(resourceOwner.getName());
                } else if (tournament instanceof TableDraftTournament && tournament.getTournamentStage() == Tournament.Stage.DRAFT) {
                    tableDraft = ((TableDraftTournament) tournament).getTableDraft();
                } else {
                    throw new HttpProcessingException(404);
                }
            } else {
                throw new HttpProcessingException(404);
            }

            if (!eventMap.containsKey(eventId)) {
                eventMap.put(eventId, new HashMap<>());
            }
            if (!eventMap.get(eventId).containsKey(resourceOwner.getName())) {
                eventMap.get(eventId).put(resourceOwner.getName(), tableDraft.getPlayer(resourceOwner.getName()));
            }

            writeXml(responseWriter, tableDraft.getPlayer(resourceOwner.getName()));
        }
    }

    private League findLeagueById(String leagueId) {
        for (League activeLeague : _leagueService.getActiveLeagues()) {
            if (activeLeague.getCodeStr().equals(leagueId))
                return activeLeague;
        }
        return null;
    }

    private Tournament findTournamentById(String tournamentId) {
        for (Tournament activeTournament : _tournamentService.getLiveTournaments()) {
            if (activeTournament.getTournamentId().equals(tournamentId)) {
                return activeTournament;
            }
        }
        return null;
    }

    private void makePick(HttpRequest request, String eventId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            String selectedChoiceId = getFormParameterSafely(postDecoder, "choiceId");

            if (eventMap.containsKey(eventId) && eventMap.get(eventId).containsKey(resourceOwner.getName())) {
                // Check saved players first
                DraftPlayer thisPlayer = eventMap.get(eventId).get(resourceOwner.getName());
                // Declare intent of picking (and pick if all declared)
                thisPlayer.chooseCard(selectedChoiceId);
                writeXml(responseWriter, thisPlayer);

                clearCacheIfDraftFinished(eventId, resourceOwner, thisPlayer);
            } else {
                TableDraft tableDraft;

                League league = findLeagueById(eventId);
                Tournament tournament = findTournamentById(eventId);

                if (league != null) {
                    // Leagues will be supported later
                    throw new HttpProcessingException(404);
//                LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
//                var leagueStart = leagueData.getSeries().getFirst().getStart();
//
//                if (!leagueData.isSoloDraftLeague() || DateUtils.Today().isBefore(leagueStart))
//                    throw new HttpProcessingException(404);
//
//                SoloDraftLeague soloDraftLeague = (SoloDraftLeague) leagueData;
//                collectionType = soloDraftLeague.getCollectionType();
//                soloDraft = soloDraftLeague.getSoloDraft();
                } else if (tournament != null) {
                    if ((tournament instanceof SoloTableDraftTournament) && (tournament.getTournamentStage() == Tournament.Stage.DECK_BUILDING || tournament.getTournamentStage() == Tournament.Stage.DECK_REGISTRATION)) {
                        tableDraft = ((SoloTableDraftTournament) tournament).getTableDraft(resourceOwner.getName());
                    } else if (tournament instanceof TableDraftTournament && tournament.getTournamentStage() == Tournament.Stage.DRAFT) {
                        tableDraft = ((TableDraftTournament) tournament).getTableDraft();
                    } else {
                        throw new HttpProcessingException(404);
                    }
                } else {
                    throw new HttpProcessingException(404);
                }

                if (tableDraft.isFinished()) {
                    throw new HttpProcessingException(404);
                }

                // Check if card is present
                if (!tableDraft.getPlayer(resourceOwner.getName()).getCardsToPickFrom().contains(selectedChoiceId)) {
                    throw new HttpProcessingException(400);
                }

                // Declare intent of picking (and pick if all declared)
                tableDraft.getPlayer(resourceOwner.getName()).chooseCard(selectedChoiceId);

                writeXml(responseWriter, tableDraft.getPlayer(resourceOwner.getName()));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void clearCacheIfDraftFinished(String eventId, Player resourceOwner, DraftPlayer thisPlayer) {
        if (thisPlayer.draftFinished()) {
            if (eventMap.containsKey(eventId)) {
                eventMap.get(eventId).remove(resourceOwner.getName());
                if (eventMap.get(eventId).isEmpty()) {
                    eventMap.remove(eventId);
                }
            }
        }
    }

    private void writeXml(ResponseWriter responseWriter, DraftPlayer draftPlayer) throws ParserConfigurationException {
        responseWriter.writeXmlResponse(draftPlayer.getDocument(_cardLibrary, _formatLibrary));
    }
}
