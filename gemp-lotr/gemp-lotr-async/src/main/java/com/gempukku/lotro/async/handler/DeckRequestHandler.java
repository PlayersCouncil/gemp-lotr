package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.db.DeckDAO;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.SealedEventDefinition;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DeckRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final DeckDAO _deckDao;
    private final SortAndFilterCards _sortAndFilterCards;
    private final LotroCardBlueprintLibrary _library;
    private final LotroFormatLibrary _formatLibrary;
    private final SoloDraftDefinitions _draftLibrary;
    private final LotroServer _lotroServer;

    private static final Logger _log = LogManager.getLogger(DeckRequestHandler.class);

    public DeckRequestHandler(Map<Type, Object> context) {
        super(context);
        _deckDao = extractObject(context, DeckDAO.class);
        _sortAndFilterCards = new SortAndFilterCards();
        _library = extractObject(context, LotroCardBlueprintLibrary.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _lotroServer = extractObject(context, LotroServer.class);
        _draftLibrary = extractObject(context, SoloDraftDefinitions.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/list") && request.method() == HttpMethod.GET) {
            listDecks(request, responseWriter);
        } else if (uri.equals("/libraryList") && request.method() == HttpMethod.GET) {
            listLibraryDecks(request, responseWriter);
        } else if (uri.equals("") && request.method() == HttpMethod.GET) {
            getDeck(request, responseWriter);
        } else if (uri.equals("") && request.method() == HttpMethod.POST) {
            saveDeck(request, responseWriter);
        } else if (uri.equals("/library") && request.method() == HttpMethod.GET) {
            getLibraryDeck(request, responseWriter);
        } else if (uri.equals("/share") && request.method() == HttpMethod.GET) {
            shareDeck(request, responseWriter);
        } else if (uri.equals("/html") && request.method() == HttpMethod.GET) {
            getDeckInHtml(request, responseWriter);
        } else if (uri.equals("/libraryHtml") && request.method() == HttpMethod.GET) {
            getLibraryDeckInHtml(request, responseWriter);
        } else if (uri.equals("/rename") && request.method() == HttpMethod.POST) {
            renameDeck(request, responseWriter);
        } else if (uri.equals("/delete") && request.method() == HttpMethod.POST) {
            deleteDeck(request, responseWriter);
        } else if (uri.equals("/stats") && request.method() == HttpMethod.POST) {
            getDeckStats(request, responseWriter);
        } else if (uri.equals("/formats") && request.method() == HttpMethod.POST) {
            getAllFormats(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }



    private void getAllFormats(HttpRequest request, ResponseWriter responseWriter) throws IOException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String includeEventsStr = getFormParameterSafely(postDecoder, "includeEvents");
            boolean includeEvents = includeEventsStr != null && includeEventsStr.equalsIgnoreCase("true");

            String json = "{}";

            if(includeEvents)
            {
                JSONDefs.FullFormatReadout data = new JSONDefs.FullFormatReadout();
                data.Formats = _formatLibrary.getAllFormats().values().stream()
                        .map(LotroFormat::Serialize)
                        .collect(Collectors.toMap(x-> x.code, x-> x));
                data.SealedTemplates = _formatLibrary.GetAllSealedTemplates().values().stream()
                        .map(SealedEventDefinition::Serialize)
                        .collect(Collectors.toMap(x-> x.name, x-> x));
                data.DraftTemplates = _draftLibrary.getAllSoloDrafts().values().stream()
                        .map(soloDraft -> new JSONDefs.ItemStub(soloDraft.getCode(), soloDraft.getFormat()))
                        .collect(Collectors.toMap(x-> x.code, x-> x));

                json = JsonUtils.Serialize(data);
            }
            else {
                JSONDefs.FullFormatReadout data = new JSONDefs.FullFormatReadout();
                data.Formats = _formatLibrary.getAllFormats().values().stream()
                        .map(LotroFormat::Serialize)
                        .collect(Collectors.toMap(x-> x.code, x-> x));

                json = JsonUtils.Serialize(data);
            }

            responseWriter.writeJsonResponse(json);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getDeckStats(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException, CardNotFoundException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String targetFormat = getFormParameterSafely(postDecoder, "targetFormat");
            String contents = getFormParameterSafely(postDecoder, "deckContents");

            //check for valid access
            getResourceOwnerSafely(request, participantId);

            LotroDeck deck = _lotroServer.createDeckWithValidate("tempDeck", contents, targetFormat, "");
            if (deck == null)
                throw new HttpProcessingException(400);

            int fpCount = 0;
            int shadowCount = 0;
            for (String card : deck.getDrawDeckCards()) {
                Side side = _library.getLotroCardBlueprint(card).getSide();
                if (side == Side.SHADOW)
                    shadowCount++;
                else if (side == Side.FREE_PEOPLE)
                    fpCount++;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<b>Free People</b>: " + fpCount + ", <b>Shadow</b>: " + shadowCount + "<br/>");

            StringBuilder valid = new StringBuilder();
            StringBuilder invalid = new StringBuilder();

            LotroFormat format = validateFormat(targetFormat);
            if(format == null || targetFormat == null)
            {
                responseWriter.writeHtmlResponse("Invalid format: " + targetFormat);
                return;
            }

            List<String> validation = format.validateDeck(deck);
            List<String> errataValidation = null;
            if (!format.getErrataCardMap().isEmpty()) {
                LotroDeck deckWithErrata = format.applyErrata(deck);
                errataValidation = format.validateDeck(deckWithErrata);
            }
            if(validation.size() == 0) {
                valid.append("<b>" + format.getName() + "</b>: <font color='green'>Valid</font><br/>");
            }
            else if(errataValidation != null && errataValidation.size() == 0) {
                valid.append("<b>" + format.getName() + "</b>: <font color='green'>Valid</font> <font color='yellow'>(with errata automatically applied)</font><br/>");
                String output = String.join("<br>", validation).replace("\n", "<br>");
                invalid.append("<font color='yellow'>" + output + "</font><br/>");
            }
            else {
                String output = String.join("<br>", validation).replace("\n", "<br>");
                invalid.append("<b>" + format.getName() + "</b>: <font color='red'>" + output + "</font><br/>");
            }

            sb.append(valid);
            sb.append(invalid);

            responseWriter.writeHtmlResponse(sb.toString());
        } finally {
            postDecoder.destroy();
        }
    }

    private void deleteDeck(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            _deckDao.deleteDeckForPlayer(resourceOwner, deckName);

            responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void renameDeck(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException, ParserConfigurationException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            String oldDeckName = getFormParameterSafely(postDecoder, "oldDeckName");

            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            LotroDeck deck = _deckDao.renameDeck(resourceOwner, oldDeckName, deckName);
            if (deck == null)
                throw new HttpProcessingException(404);

            responseWriter.writeXmlResponse(serializeDeck(deck));
        } finally {
            postDecoder.destroy();
        }
    }

    private void saveDeck(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException, ParserConfigurationException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            String targetFormat = getFormParameterSafely(postDecoder, "targetFormat");
            String notes = getFormParameterSafely(postDecoder, "notes");
            String contents = getFormParameterSafely(postDecoder, "deckContents");

            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            LotroFormat validatedFormat = validateFormat(targetFormat);

            LotroDeck lotroDeck = _lotroServer.createDeckWithValidate(deckName, contents, validatedFormat.getName(), notes);
            if (lotroDeck == null)
                throw new HttpProcessingException(400);

            _deckDao.saveDeckForPlayer(resourceOwner, deckName, validatedFormat.getName(), notes, lotroDeck);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();
            Element deckElem = doc.createElement("ok");
            doc.appendChild(deckElem);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void shareDeck(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, CardNotFoundException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");
        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        var result = LotroDeck.GenerateDeckSharingURL(deckName, resourceOwner.getName());

        responseWriter.writeHtmlResponse(result);
    }

    private void getDeckInHtml(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, CardNotFoundException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");
        String shareCode = getQueryParameterSafely(queryDecoder, "id");

        Player resourceOwner;
        LotroDeck deck;

        if (shareCode != null)
        {
            String code = new String(Base64.getDecoder().decode(shareCode), StandardCharsets.UTF_8);
            String[] fields = code.split("\\|");
            if(fields.length != 2)
                throw new HttpProcessingException(400);

            String user = fields[0];
            String deckName2 = fields[1];

            resourceOwner = _playerDao.getPlayer(user);
            deck = _deckDao.getDeckForPlayer(resourceOwner, deckName2);
        }
        else {
            resourceOwner = getResourceOwnerSafely(request, participantId);
            deck = _deckDao.getDeckForPlayer(resourceOwner, deckName);
        }

        if (deck == null)
            throw new HttpProcessingException(404);

        String result = convertDeckToHTML(deck, resourceOwner.getName());

        responseWriter.writeHtmlResponse(result);
    }

    private void getLibraryDeckInHtml(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, CardNotFoundException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");

        LotroDeck deck = _deckDao.getDeckForPlayer(getLibrarian(), deckName);

        if (deck == null)
            throw new HttpProcessingException(404);

        String result = convertDeckToHTML(deck, null);

        responseWriter.writeHtmlResponse(result);
    }

    public String convertDeckToHTML(LotroDeck deck, String author) throws CardNotFoundException {

        if (deck == null)
            return null;

        StringBuilder result = new StringBuilder();
        result.append("""
<html>
    <style>
        body {
            margin:50;
        }
        
        .tooltip {
          border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
          color:#0000FF;
        }
        
        .tooltip span, .tooltip title {
            display:none;
        }
        .tooltip:hover span:not(.click-disabled),.tooltip:active span:not(.click-disabled) {
            display:block;
            position:fixed;
            overflow:hidden;
            background-color: #FAEBD7;
            width:auto;
            z-index:9999;
            top:20%;
            left:350px;
        }
        /* This prevents tooltip images from automatically shrinking if they are near the window edge.*/
        .tooltip span > img {
            max-width:none !important;
            overflow:hidden;
        }
                        
    </style>
    <body>""");
        result.append("<h1>" + StringEscapeUtils.escapeHtml3(deck.getDeckName()) + "</h1>");
        result.append("<h2>Format: " + StringEscapeUtils.escapeHtml3(deck.getTargetFormat()) + "</h2>");
        if(author != null) {
            result.append("<h2>Author: " + StringEscapeUtils.escapeHtml3(author) + "</h2>");
        }
        String ringBearer = deck.getRingBearer();
        if (ringBearer != null)
            result.append("<b>Ring-bearer:</b> " + generateCardTooltip(_library.getLotroCardBlueprint(ringBearer), ringBearer) + "<br/>");
        String ring = deck.getRing();
        if (ring != null)
            result.append("<b>Ring:</b> " + generateCardTooltip(_library.getLotroCardBlueprint(ring), ring) + "<br/>");

        var format = _formatLibrary.getFormatByName(deck.getTargetFormat());
        if(format != null && format.usesMaps()) {
            String map = deck.getMap();
            if(map != null)
                result.append("<b>Map:</b> " + generateCardTooltip(_library.getLotroCardBlueprint(map), map) + "<br/>");
        }

        DefaultCardCollection deckCards = new DefaultCardCollection();
        for (String card : deck.getDrawDeckCards())
            deckCards.addItem(_library.getBaseBlueprintId(card), 1);
        for (String site : deck.getSites())
            deckCards.addItem(_library.getBaseBlueprintId(site), 1);

        result.append("<br/>");
        result.append("<b>Adventure deck:</b><br/>");
        for (CardCollection.Item item : _sortAndFilterCards.process("cardType:SITE sort:siteNumber,twilight", deckCards.getAll(), _library, _formatLibrary))
            result.append(generateCardTooltip(item) + "<br/>");

        result.append("<br/>");
        result.append("<b>Free Peoples Draw Deck:</b><br/>");
        for (CardCollection.Item item : _sortAndFilterCards.process("side:FREE_PEOPLE sort:cardType,culture,name", deckCards.getAll(), _library, _formatLibrary))
            result.append(item.getCount() + "x " + generateCardTooltip(item) + "<br/>");

        result.append("<br/>");
        result.append("<b>Shadow Draw Deck:</b><br/>");
        for (CardCollection.Item item : _sortAndFilterCards.process("side:SHADOW sort:cardType,culture,name", deckCards.getAll(), _library, _formatLibrary))
            result.append(item.getCount() + "x " + generateCardTooltip(item) + "<br/>");

        result.append("<h3>Notes</h3><br>" + deck.getNotes().replace("\n", "<br/>"));

        result.append("</body></html>");

        return result.toString();
    }

    private String generateCardTooltip(CardCollection.Item item) throws CardNotFoundException {
        return generateCardTooltip(_library.getLotroCardBlueprint(item.getBlueprintId()), item.getBlueprintId());
    }

    private String generateCardTooltip(LotroCardBlueprint bp, String bpid) throws CardNotFoundException {
        String[] parts = bpid.split("_");
        int setnum = Integer.parseInt(parts[0]);
        String set = String.format("%02d", setnum);
        String subset = "S";
        int version = 0;
        if(setnum >= 50 && setnum <= 69) {
            setnum -= 50;
            set = String.format("%02d", setnum);
            subset = "E";
            version = 1;
        }
        else if(setnum >= 70 && setnum <= 89) {
            setnum -= 70;
            set = String.format("%02d", setnum);
            subset = "E";
            version = 1;
        }
        else if(setnum >= 100 && setnum <= 149) {
            setnum -= 100;
            set = "V" + setnum;
        }
        int cardnum = Integer.parseInt(parts[1].replace("*", "").replace("T", ""));

        String id = "LOTR-EN" + set + subset + String.format("%03d", cardnum) + "." + String.format("%01d", version);
        String result = "<span class=\"tooltip\">" + GameUtils.getFullName(bp)
                + "<span><img class=\"ttimage\" src=\"https://wiki.lotrtcgpc.net/images/" + id + "_card.jpg\" ></span></span>";

        return result;
    }

    private void getDeck(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        responseWriter.writeXmlResponse(serializeDeck(resourceOwner, deckName));
    }

    private void getLibraryDeck(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");

        responseWriter.writeXmlResponse(serializeDeck(getLibrarian(), deckName));
    }

    private void listDecks(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        List<Map.Entry<LotroFormat, String>> decks = GetDeckNamesAndFormats(resourceOwner);
        SortDecks(decks);

        Document doc = ConvertDeckNamesToXML(decks);
        responseWriter.writeXmlResponse(doc);
    }

    private Document ConvertDeckNamesToXML(List<Map.Entry<LotroFormat, String>> deckNames) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        Element decksElem = doc.createElement("decks");

        for (Map.Entry<LotroFormat, String> pair : deckNames) {
            Element deckElem = doc.createElement("deck");
            deckElem.setTextContent(pair.getValue());
            deckElem.setAttribute("targetFormat", pair.getKey().getName());
            decksElem.appendChild(deckElem);
        }
        doc.appendChild(decksElem);
        return doc;
    }

    private List<Map.Entry<LotroFormat, String>> GetDeckNamesAndFormats(Player player)
    {
        Set<Map.Entry<String, String>> names = new HashSet(_deckDao.getPlayerDeckNames(player));

        return names.stream()
                .map(pair -> new AbstractMap.SimpleEntry<>(_formatLibrary.getFormatByName(pair.getKey()), pair.getValue()))
                .collect(Collectors.toList());
    }

    private void SortDecks(List<Map.Entry<LotroFormat, String>> decks)
    {
        decks.sort(Comparator.comparing((deck) -> {
            LotroFormat format = deck.getKey();
            return String.format("%02d", format.getOrder()) + format.getName() + deck.getValue();
        }));
    }

    private void listLibraryDecks(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, ParserConfigurationException {
        List<Map.Entry<LotroFormat, String>> starterDecks = new ArrayList<>();
        List<Map.Entry<LotroFormat, String>> championshipDecks = new ArrayList<>();

        List<Map.Entry<LotroFormat, String>> decks = GetDeckNamesAndFormats(getLibrarian());

        for (Map.Entry<LotroFormat, String> pair : decks) {

            if (pair.getValue().contains("Starter"))
                starterDecks.add(pair);
            else
                championshipDecks.add(pair);
        }

        SortDecks(starterDecks);
        SortDecks(championshipDecks);

        //Keeps all the championship decks at the bottom of the list
        starterDecks.addAll(championshipDecks);

        Document doc = ConvertDeckNamesToXML(starterDecks);

        // Write the XML response
        responseWriter.writeXmlResponse(doc);
    }

    private Document serializeDeck(Player player, String deckName) throws ParserConfigurationException {
        LotroDeck deck = _deckDao.getDeckForPlayer(player, deckName);

        return serializeDeck(deck);
    }

    private LotroFormat validateFormat(String name)
    {
        LotroFormat validatedFormat = _formatLibrary.getFormat(name);
        if(validatedFormat == null)
        {
            try {
                validatedFormat = _formatLibrary.getFormatByName(name);
            }
            catch(Exception ex)
            {
                validatedFormat = _formatLibrary.getFormatByName("Anything Goes");
            }
        }

        return validatedFormat;
    }

    private Document serializeDeck(LotroDeck deck) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element deckElem = doc.createElement("deck");
        doc.appendChild(deckElem);

        if (deck == null)
            return doc;

        LotroFormat validatedFormat = validateFormat(deck.getTargetFormat());

        Element targetFormat = doc.createElement("targetFormat");
        targetFormat.setAttribute("formatName", validatedFormat.getName());
        targetFormat.setAttribute("formatCode", validatedFormat.getCode());
        deckElem.appendChild(targetFormat);

        Element notes = doc.createElement("notes");
        notes.setTextContent(deck.getNotes());
        deckElem.appendChild(notes);

        if (deck.getRingBearer() != null) {
            Element ringBearer = doc.createElement("ringBearer");
            ringBearer.setAttribute("blueprintId", deck.getRingBearer());
            deckElem.appendChild(ringBearer);
        }

        if (deck.getRing() != null) {
            Element ring = doc.createElement("ring");
            ring.setAttribute("blueprintId", deck.getRing());
            deckElem.appendChild(ring);
        }

        if (deck.getMap() != null) {
            Element map = doc.createElement("map");
            map.setAttribute("blueprintId", deck.getMap());
            deckElem.appendChild(map);
        }

        for (CardItem cardItem : _sortAndFilterCards.process("sort:siteNumber,twilight", createCardItems(deck.getSites()), _library, _formatLibrary)) {
            Element site = doc.createElement("site");
            site.setAttribute("blueprintId", cardItem.getBlueprintId());
            deckElem.appendChild(site);
        }

        for (CardItem cardItem : _sortAndFilterCards.process("sort:cardType,culture,name", createCardItems(deck.getDrawDeckCards()), _library, _formatLibrary)) {
            Element card = doc.createElement("card");
            String side;
            try {
                side = _library.getLotroCardBlueprint(cardItem.getBlueprintId()).getSide().toString();
            } catch (CardNotFoundException e) {
                side = "FREE_PEOPLE";
            }
            catch (NullPointerException e) {
                _log.debug("Non-sided card?? " + cardItem.getBlueprintId());
                side = "FREE_PEOPLE";
            }
            card.setAttribute("side", side);
            card.setAttribute("blueprintId", cardItem.getBlueprintId());
            deckElem.appendChild(card);
        }

        return doc;
    }

    private List<CardItem> createCardItems(List<String> blueprintIds) {
        List<CardItem> cardItems = new LinkedList<>();
        for (String blueprintId : blueprintIds)
            cardItems.add(new BasicCardItem(blueprintId));

        return cardItems;
    }
}
