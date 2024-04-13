package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.merchant.MerchantService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Type;
import java.util.*;

public class MerchantRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CollectionsManager _collectionsManager;
    private final SortAndFilterCards _sortAndFilterCards;
    private final MerchantService _merchantService;
    private final LotroCardBlueprintLibrary _library;
    private final LotroFormatLibrary _formatLibrary;

    private static final Logger _log = LogManager.getLogger(MerchantRequestHandler.class);

    public MerchantRequestHandler(Map<Type, Object> context) {
        super(context);

        _collectionsManager = extractObject(context, CollectionsManager.class);
        _sortAndFilterCards = new SortAndFilterCards();
        _merchantService = extractObject(context, MerchantService.class);
        _library = extractObject(context, LotroCardBlueprintLibrary.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);

    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            getMerchantOffers(request, responseWriter);
        } else if (uri.equals("/buy") && request.method() == HttpMethod.POST) {
            buy(request, responseWriter);
        } else if (uri.equals("/sell") && request.method() == HttpMethod.POST) {
            sell(request, responseWriter);
        } else if (uri.equals("/tradeFoil") && request.method() == HttpMethod.POST) {
            tradeInFoil(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void tradeInFoil(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String blueprintId = getFormParameterSafely(postDecoder, "blueprintId");

            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            try {
                _merchantService.tradeForFoil(resourceOwner, blueprintId);
                responseWriter.writeHtmlResponse("OK");
            } catch (Exception exp) {
                _log.error("Error response for " + request.uri(), exp);
                responseWriter.writeXmlResponse(marshalException(exp));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void sell(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String blueprintId = getFormParameterSafely(postDecoder, "blueprintId");
            int price = Integer.parseInt(getFormParameterSafely(postDecoder, "price"));

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();

            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            try {
                _merchantService.merchantBuysCard(resourceOwner, blueprintId, price);
                responseWriter.writeHtmlResponse("OK");
            } catch (Exception exp) {
                _log.error("Error response for " + request.uri(), exp);
                responseWriter.writeXmlResponse(marshalException(exp));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void buy(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String blueprintId = getFormParameterSafely(postDecoder, "blueprintId");
            int price = Integer.parseInt(getFormParameterSafely(postDecoder, "price"));

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();


            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            try {
                _merchantService.merchantSellsCard(resourceOwner, blueprintId, price);
                responseWriter.writeHtmlResponse("OK");
            } catch (Exception exp) {
                _log.error("Error response for " + request.uri(), exp);
                responseWriter.writeXmlResponse(marshalException(exp));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void getMerchantOffers(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String filter = getQueryParameterSafely(queryDecoder, "filter");
        int ownedMin = Integer.parseInt(getQueryParameterSafely(queryDecoder, "ownedMin"));
        int start = Integer.parseInt(getQueryParameterSafely(queryDecoder, "start"));
        int count = Integer.parseInt(getQueryParameterSafely(queryDecoder, "count"));

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode());

        Set<BasicCardItem> cardItems = new HashSet<>();
        if (ownedMin <= 0) {
            cardItems.addAll(_merchantService.getSellableItems());
            final Iterable<CardCollection.Item> items = collection.getAll();
            for (CardCollection.Item item : items)
                cardItems.add(new BasicCardItem(item.getBlueprintId()));
        } else {
            for (CardCollection.Item item : collection.getAll()) {
                if (item.getCount() >= ownedMin)
                    cardItems.add(new BasicCardItem(item.getBlueprintId()));
            }
        }

        List<BasicCardItem> filteredResult = _sortAndFilterCards.process(filter, cardItems, _library, _formatLibrary);

        List<CardItem> pageToDisplay = new ArrayList<>();
        for (int i = start; i < start + count; i++) {
            if (i >= 0 && i < filteredResult.size())
                pageToDisplay.add(filteredResult.get(i));
        }

        MerchantService.PriceGuarantee priceGuarantee = _merchantService.priceCards(resourceOwner, pageToDisplay);
        Map<String, Integer> buyPrices = priceGuarantee.getBuyPrices();
        Map<String, Integer> sellPrices = priceGuarantee.getSellPrices();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element merchantElem = doc.createElement("merchant");
        merchantElem.setAttribute("currency", String.valueOf(_collectionsManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode()).getCurrency()));
        merchantElem.setAttribute("count", String.valueOf(filteredResult.size()));
        doc.appendChild(merchantElem);

        for (CardItem cardItem : pageToDisplay) {
            String blueprintId = cardItem.getBlueprintId();

            Element elem;
            if (blueprintId.contains("_"))
                elem = doc.createElement("card");
            else
                elem = doc.createElement("pack");

            elem.setAttribute("count", String.valueOf(collection.getItemCount(blueprintId)));
            if (blueprintId.contains("_") && !blueprintId.endsWith("*") && collection.getItemCount(blueprintId) >= 4)
                elem.setAttribute("tradeFoil", "true");
            elem.setAttribute("blueprintId", blueprintId);
            Integer buyPrice = buyPrices.get(blueprintId);
            if (buyPrice != null && collection.getItemCount(blueprintId) > 0)
                elem.setAttribute("buyPrice", buyPrice.toString());
            Integer sellPrice = sellPrices.get(blueprintId);
            if (sellPrice != null)
                elem.setAttribute("sellPrice", sellPrice.toString());
            merchantElem.appendChild(elem);
        }

        responseWriter.writeXmlResponse(doc);
    }

    private Document marshalException(Exception e) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element error = doc.createElement("error");
        error.setAttribute("message", e.getMessage());
        doc.appendChild(error);
        return doc;
    }

}
