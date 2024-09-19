package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueData;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.league.SoloDraftLeague;
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
import java.time.ZonedDateTime;
import java.util.*;

public class SoloDraftRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CollectionsManager _collectionsManager;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LotroCardBlueprintLibrary _cardLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final ProductLibrary _productLibrary;
    private final LeagueService _leagueService;

    private static final Logger _log = LogManager.getLogger(SoloDraftRequestHandler.class);

    public SoloDraftRequestHandler(Map<Type, Object> context) {
        super(context);
        _leagueService = extractObject(context, LeagueService.class);
        _cardLibrary = extractObject(context, LotroCardBlueprintLibrary.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _collectionsManager = extractObject(context, CollectionsManager.class);
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

    private void getAvailablePicks(HttpRequest request, String leagueType, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        League league = findLeagueByType(leagueType);

        if (league == null)
            throw new HttpProcessingException(404);

        LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
        var leagueStart = leagueData.getSeries().getFirst().getStart();

        if (!leagueData.isSoloDraftLeague() || DateUtils.Today().isBefore(leagueStart))
            throw new HttpProcessingException(404);

        SoloDraftLeague soloDraftLeague = (SoloDraftLeague) leagueData;
        CollectionType collectionType = soloDraftLeague.getCollectionType();

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, collectionType.getCode());

        Iterable<SoloDraft.DraftChoice> availableChoices;

        boolean finished = (Boolean) collection.getExtraInformation().get("finished");
        if (!finished) {
            int stage = ((Number) collection.getExtraInformation().get("stage")).intValue();
            long playerSeed = ((Number) collection.getExtraInformation().get("seed")).longValue();
            List<String> draftPoolList = (List<String>) collection.getExtraInformation().get("draftPool");

            DefaultCardCollection draftPool = new DefaultCardCollection();
            if (draftPoolList != null)
                for (String card : draftPoolList)
                    draftPool.addItem(card, 1);

            SoloDraft soloDraft = soloDraftLeague.getSoloDraft();
            availableChoices = soloDraft.getAvailableChoices(playerSeed, stage, draftPool);
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

    private League findLeagueByType(String leagueType) {
        for (League activeLeague : _leagueService.getActiveLeagues()) {
            if (activeLeague.getCodeStr().equals(leagueType))
                return activeLeague;
        }
        return null;
    }

    private void makePick(HttpRequest request, String leagueType, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        String selectedChoiceId = getFormParameterSafely(postDecoder, "choiceId");

        League league = findLeagueByType(leagueType);

        if (league == null)
            throw new HttpProcessingException(404);

        LeagueData leagueData = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions);
        var leagueStart = leagueData.getSeries().getFirst().getStart();

        if (!leagueData.isSoloDraftLeague() || DateUtils.Today().isBefore(leagueStart))
            throw new HttpProcessingException(404);

        SoloDraftLeague soloDraftLeague = (SoloDraftLeague) leagueData;
        CollectionType collectionType = soloDraftLeague.getCollectionType();

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, collectionType.getCode());
        boolean finished = (Boolean) collection.getExtraInformation().get("finished");
        if (finished)
            throw new HttpProcessingException(404);

        int stage = ((Number) collection.getExtraInformation().get("stage")).intValue();
        long playerSeed = ((Number) collection.getExtraInformation().get("seed")).longValue();
        List<String> draftPoolList = (List<String>) collection.getExtraInformation().get("draftPool");
        DefaultCardCollection draftPool = new DefaultCardCollection();
        
        if (draftPoolList != null)
            for (String card : draftPoolList)
                draftPool.addItem(card, 1);

        SoloDraft soloDraft = soloDraftLeague.getSoloDraft();
        Iterable<SoloDraft.DraftChoice> possibleChoices = soloDraft.getAvailableChoices(playerSeed, stage, draftPool);

        SoloDraft.DraftChoice draftChoice = getSelectedDraftChoice(selectedChoiceId, possibleChoices);
        if (draftChoice == null)
            throw new HttpProcessingException(400);

        CardCollection selectedCards = soloDraft.getCardsForChoiceId(selectedChoiceId, playerSeed, stage);
        Map<String, Object> extraInformationChanges = new HashMap<>();
        boolean hasNextStage = soloDraft.hasNextStage(playerSeed, stage);
        extraInformationChanges.put("stage", stage + 1);
        if (!hasNextStage)
            extraInformationChanges.put("finished", true);

        if (draftPoolList != null) {
            List<String> draftPoolListUpdate = new ArrayList<>();
            for (CardCollection.Item item : draftPool.getAll()) {
                String blueprint = item.getBlueprintId();
                for (int i = 0; i < draftPool.getItemCount(blueprint); i++)
                    draftPoolListUpdate.add(blueprint);
            }

            if (draftPoolList != draftPoolListUpdate) 
                extraInformationChanges.put("draftPool",draftPoolListUpdate);
        }

        _collectionsManager.addItemsToPlayerCollection(false, "Draft pick", resourceOwner, collectionType, selectedCards.getAll(), extraInformationChanges);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element pickResultElem = doc.createElement("pickResult");
        doc.appendChild(pickResultElem);

        for (CardCollection.Item item : selectedCards.getAll()) {
            Element pickedCard = doc.createElement("pickedCard");
            pickedCard.setAttribute("blueprintId", item.getBlueprintId());
            pickedCard.setAttribute("count", String.valueOf(item.getCount()));
            pickResultElem.appendChild(pickedCard);
        }

        if (hasNextStage) {
            Iterable<SoloDraft.DraftChoice> availableChoices = soloDraft.getAvailableChoices(playerSeed, stage + 1, draftPool);
            appendAvailablePics(doc, pickResultElem, availableChoices);
        }

        responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void appendAvailablePics(Document doc, Element rootElem, Iterable<SoloDraft.DraftChoice> availablePics) {
        for (SoloDraft.DraftChoice availableChoice : availablePics) {
            String choiceId = availableChoice.getChoiceId();
            String blueprintId = availableChoice.getBlueprintId();
            String choiceUrl = availableChoice.getChoiceUrl();
            Element availablePick = doc.createElement("availablePick");
            availablePick.setAttribute("id", choiceId);
            if (blueprintId != null)
                availablePick.setAttribute("blueprintId", blueprintId);
            if (choiceUrl != null)
                availablePick.setAttribute("url", choiceUrl);
            rootElem.appendChild(availablePick);
        }
    }

    private SoloDraft.DraftChoice getSelectedDraftChoice(String choiceId, Iterable<SoloDraft.DraftChoice> availableChoices) {
        for (SoloDraft.DraftChoice availableChoice : availableChoices) {
            if (availableChoice.getChoiceId().equals(choiceId))
                return availableChoice;
        }
        return null;
    }
}
