package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraft;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.SoloTableDraftTournament;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentService;
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
import java.util.*;

public class TableDraftRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CollectionsManager _collectionsManager;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LotroCardBlueprintLibrary _cardLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final ProductLibrary _productLibrary;
    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final TableDraftDefinitions _tableDraftDefinitions;

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

        TableDraft tableDraft;
        CollectionType collectionType;

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
            if (!(tournament instanceof SoloTableDraftTournament) || (tournament.getTournamentStage() != Tournament.Stage.DECK_BUILDING && tournament.getTournamentStage() != Tournament.Stage.DECK_REGISTRATION)) {
                throw new HttpProcessingException(404);
            }

            collectionType = tournament.getCollectionType();
            tableDraft = ((SoloTableDraftTournament) tournament).getTableDraft(resourceOwner.getName());
        } else {
            throw new HttpProcessingException(404);
        }


        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, collectionType.getCode());

        List<String> availableChoices;

        if (!tableDraft.isFinished()) {
            availableChoices = tableDraft.getPlayer(resourceOwner.getName()).getCardsToPickFrom();
        } else {
            availableChoices = Collections.emptyList();
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element availablePicksElem = doc.createElement("availablePicks");
        doc.appendChild(availablePicksElem);

        appendAvailablePics(doc, availablePicksElem, availableChoices);

        responseWriter.writeXmlResponse(doc);
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

            TableDraft tableDraft;
            CollectionType collectionType;

            League league = findLeagueById(eventId);
            Tournament tournament = findTournamentById(eventId);

            if (league != null) {
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
                if (!(tournament instanceof SoloTableDraftTournament) || (tournament.getTournamentStage() != Tournament.Stage.DECK_BUILDING && tournament.getTournamentStage() != Tournament.Stage.DECK_REGISTRATION)) {
                    throw new HttpProcessingException(404);
                }

                collectionType = tournament.getCollectionType();
                tableDraft = ((SoloTableDraftTournament) tournament).getTableDraft(resourceOwner.getName());
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

            // Declare intent of picking
            if (tableDraft.getPlayer(resourceOwner.getName()).chooseCard(selectedChoiceId)) {
                // All at table picked a card
                // TODO contact each player, this only works for solo draft
                DefaultCardCollection pickedCardCollection = new DefaultCardCollection();
                pickedCardCollection.addItem(selectedChoiceId, 1);

                _collectionsManager.addItemsToPlayerCollection(false, "Draft pick", resourceOwner, collectionType, pickedCardCollection.getAll());

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                Document doc = documentBuilder.newDocument();

                Element pickResultElem = doc.createElement("pickResult");
                doc.appendChild(pickResultElem);

                for (CardCollection.Item item : pickedCardCollection.getAll()) {
                    Element pickedCard = doc.createElement("pickedCard");
                    pickedCard.setAttribute("blueprintId", item.getBlueprintId());
                    pickedCard.setAttribute("count", String.valueOf(item.getCount()));
                    pickResultElem.appendChild(pickedCard);
                }

                // Look at next booster
                if (!tableDraft.isFinished()) {
                    appendAvailablePics(doc, pickResultElem, tableDraft.getPlayer(resourceOwner.getName()).getCardsToPickFrom());
                }
                responseWriter.writeXmlResponse(doc);
            } else {
                // Table didn't pick
                // TODO highlight chosen card
            }

        } finally {
            postDecoder.destroy();
        }
    }

    private void appendAvailablePics(Document doc, Element rootElem, Iterable<String> availablePics) {
        for (String availableChoice : availablePics) {
            Element availablePick = doc.createElement("availablePick");
            availablePick.setAttribute("id", availableChoice);
            availablePick.setAttribute("blueprintId", availableChoice);
//            if (choiceUrl != null)
//                availablePick.setAttribute("url", choiceUrl);
            rootElem.appendChild(availablePick);
        }
    }
}
