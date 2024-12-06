package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.Player;
import com.gempukku.util.JsonUtils;
import com.google.common.collect.Iterables;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class DeliveryRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final TransferDAO _transferDAO;
    private final MarkdownParser _parser;

    private static final Logger _log = LogManager.getLogger(DeliveryRequestHandler.class);

    public DeliveryRequestHandler(Map<Type, Object> context) {
        super(context);
        _transferDAO = extractObject(context, TransferDAO.class);
        _parser = extractObject(context, MarkdownParser.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith("/packContents") && request.method() == HttpMethod.GET) {
            getPackContentDelivery(request, responseWriter);
        } else if (uri.startsWith("/announcements") && request.method() == HttpMethod.GET) {
            getAnnouncementDelivery(request, responseWriter);
        } else if (uri.startsWith("/announcements/add") && request.method() == HttpMethod.POST) {
            processAnnouncement(request, responseWriter, false);
        } else if (uri.startsWith("/announcements/preview") && request.method() == HttpMethod.POST) {
            processAnnouncement(request, responseWriter, true);
        } else if (uri.startsWith("/announcements/snooze") && request.method() == HttpMethod.POST) {
            snoozeAnnouncement(request, responseWriter);
        } else if (uri.startsWith("/announcements/dismiss") && request.method() == HttpMethod.POST) {
            dismissAnnouncement(request, responseWriter);
        } else if (uri.startsWith("/misc") && request.method() == HttpMethod.GET) {
            getNonPackNonAnnouncementDeliveries(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    /**
     * Processes the passed parameters for a theoretical announcement.  Based on the preview parameter, this will
     * either create the announcement for real, or just return the parsed values to the client so the admin can preview
     * the input.
     * @param request the request
     * @param responseWriter the response writer
     * @param preview If true, no announcement will be created and the client will have an XML payload returned representing
     *                what the announcement would be upon creation.  If false, the league will be created for real.
     * @throws Exception
     */
    private void processAnnouncement(HttpRequest request,  ResponseWriter responseWriter, boolean preview) throws HttpProcessingException, IOException {
        validateAdmin(request);

        var postDecoder = new HttpPostRequestDecoder(request);

        String title = getFormParameterSafely(postDecoder, "title");
        String content = getFormParameterSafely(postDecoder, "content");
        String startStr = getFormParameterSafely(postDecoder, "start");
        String untilStr = getFormParameterSafely(postDecoder, "until");

        Throw400IfStringNull("title", title);
        Throw400IfStringNull("content", content);
        Throw400IfStringNull("start", startStr);

        ZonedDateTime start = null;
        try {
            start = DateUtils.ParseDate(startStr);
        }
        catch(DateTimeParseException ex) {
            Throw400IfValidationFails("start", startStr, false);
        }

        ZonedDateTime until = null;
        try {
            until = DateUtils.ParseDate(untilStr);
        }
        catch(DateTimeParseException ex) {
            Throw400IfValidationFails("until", untilStr, false);
        }

        Throw400IfValidationFails("until", untilStr, DateUtils.Now().isBefore(until), "Until date/time must be in the future.");
        Throw400IfValidationFails("until", untilStr, start.isBefore(until), "Until date/time must be after the start.");


        if(!preview) {
            _transferDAO.addServerAnnouncement(title, content, start, until);
            responseWriter.sendJsonOK();
            return;
        }

        //We aren't creating the league for real, so instead we will return the league in JSON format for the
        // admin panel preview.

        var announcement = new DBDefs.Announcement();
        announcement.title = title;
        announcement.start = start.toLocalDateTime();
        announcement.until = until.toLocalDateTime();

        announcement.content = _parser.renderMarkdown(content, false);

        responseWriter.writeJsonResponse(JsonUtils.Serialize(announcement));
    }

    private void getAnnouncementDelivery(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            var delivery = _transferDAO.getUndeliveredAnnouncement(resourceOwner);

            if(delivery == null)
                throw new HttpProcessingException(404);

            responseWriter.writeJsonResponse(JsonUtils.Serialize(delivery));
        } finally {
            postDecoder.destroy();
        }
    }

    private void dismissAnnouncement(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            _transferDAO.dismissAnnouncement(_transferDAO.getCurrentAnnouncement(), resourceOwner);

            responseWriter.sendOK();
        } finally {
            postDecoder.destroy();
        }
    }

    private void snoozeAnnouncement(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            _transferDAO.snoozeAnnouncement(_transferDAO.getCurrentAnnouncement(), resourceOwner);

            responseWriter.sendOK();
        } finally {
            postDecoder.destroy();
        }
    }

    private void getPackContentDelivery(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            Map<String, ? extends CardCollection> delivery = _transferDAO.consumeUndeliveredPackContents(resourceOwner);

            responseWriter.writeXmlResponse(serializeCollectionsAsXML(delivery));
        } finally {
            postDecoder.destroy();
        }
    }

    private void getNonPackNonAnnouncementDeliveries(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException, ParserConfigurationException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            Map<String, ? extends CardCollection> delivery = _transferDAO.consumeUndeliveredPackages(resourceOwner);

            responseWriter.writeXmlResponse(serializeCollectionsAsXML(delivery));
        } finally {
            postDecoder.destroy();
        }
    }

    private Document serializeCollectionsAsXML(Map<String, ? extends CardCollection> collections) throws HttpProcessingException, ParserConfigurationException {
        if (collections == null)
            throw new HttpProcessingException(404);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element deliveryElem = doc.createElement("delivery");
        for (Map.Entry<String, ? extends CardCollection> collectionTypeItems : collections.entrySet()) {
            String collectionType = collectionTypeItems.getKey();
            CardCollection items = collectionTypeItems.getValue();

            if (Iterables.size(items.getAll()) > 0) {
                Element collectionTypeElem = doc.createElement("collectionType");
                collectionTypeElem.setAttribute("name", collectionType);
                for (CardCollection.Item item : items.getAll()) {
                    String blueprintId = item.getBlueprintId();
                    if (item.getType() == CardCollection.Item.Type.CARD) {
                        Element card = doc.createElement("card");
                        card.setAttribute("count", String.valueOf(item.getCount()));
                        card.setAttribute("blueprintId", blueprintId);
                        collectionTypeElem.appendChild(card);
                    } else {
                        Element pack = doc.createElement("pack");
                        pack.setAttribute("count", String.valueOf(item.getCount()));
                        pack.setAttribute("blueprintId", blueprintId);
                        collectionTypeElem.appendChild(pack);
                    }
                }
                deliveryElem.appendChild(collectionTypeElem);
            }
        }

        doc.appendChild(deliveryElem);

        return doc;
    }
}
