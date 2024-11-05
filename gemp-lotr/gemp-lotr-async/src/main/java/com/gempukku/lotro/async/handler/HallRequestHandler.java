package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft.DraftChannelVisitor;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.*;
import com.gempukku.lotro.league.LeagueSerieInfo;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.polling.LongPollingResource;
import com.gempukku.polling.LongPollingSystem;
import com.gempukku.util.JsonUtils;
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

public class HallRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CollectionsManager _collectionManager;
    private final LotroFormatLibrary _formatLibrary;
    private final HallServer _hallServer;
    private final LeagueService _leagueService;
    private final LotroCardBlueprintLibrary _library;
    private final LotroServer _lotroServer;
    private final LongPollingSystem longPollingSystem;

    private static final Logger _log = LogManager.getLogger(HallRequestHandler.class);

    public HallRequestHandler(Map<Type, Object> context, LongPollingSystem longPollingSystem) {
        super(context);
        _collectionManager = extractObject(context, CollectionsManager.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _hallServer = extractObject(context, HallServer.class);
        _leagueService = extractObject(context, LeagueService.class);
        _library = extractObject(context, LotroCardBlueprintLibrary.class);
        _lotroServer = extractObject(context, LotroServer.class);
        this.longPollingSystem = longPollingSystem;
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            getHall(request, responseWriter);
        } else if (uri.equals("") && request.method() == HttpMethod.POST) {
            createTable(request, responseWriter);
        } else if (uri.equals("/update") && request.method() == HttpMethod.POST) {
            updateHall(request, responseWriter);
        } else if (uri.equals("/formats/html") && request.method() == HttpMethod.GET) {
            getFormats(request, responseWriter);
        } else if (uri.equals("/errata/json") && request.method() == HttpMethod.GET) {
            getErrataInfo(request, responseWriter);
        } else if (uri.startsWith("/format/") && request.method() == HttpMethod.GET) {
            getFormat(request, uri.substring(8), responseWriter);
        } else if (uri.startsWith("/queue/") && request.method() == HttpMethod.POST) {
            if (uri.endsWith("/leave")) {
                leaveQueue(request, uri.substring(7, uri.length() - 6), responseWriter);
            } else {
                joinQueue(request, uri.substring(7), responseWriter);
            }
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            dropFromTournament(request, uri.substring(12, uri.length() - 6), responseWriter);
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/join") && request.method() == HttpMethod.POST) {
            joinTournamentLate(request, uri.substring(12, uri.length() - 5), responseWriter);
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/registerdeck") && request.method() == HttpMethod.POST) {
            registerSealedTournamentDeck(request, uri.substring(12, uri.length() - 13), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            leaveTable(request, uri.substring(1, uri.length() - 6), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            joinTable(request, uri.substring(1), responseWriter);
        } else {
            responseWriter.writeError(404);
        }
    }

    private void joinTable(HttpRequest request, String tableId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        String deckName = getFormParameterSafely(postDecoder, "deckName");

        try {
            _hallServer.joinTableAsPlayer(tableId, resourceOwner, deckName);
            responseWriter.writeXmlResponse(null);
        } catch (HallException e) {
            try {
                //Try again assuming it's a new player using the default deck library decks
                Player libraryOwner = _playerDao.getPlayer("Librarian");
                _hallServer.joinTableAsPlayerWithSpoofedDeck(tableId, resourceOwner, libraryOwner, deckName);
                responseWriter.writeXmlResponse(null);
                return;
            } catch (HallException ex) {
                if(!IgnoreError(ex)) {
                    _log.error("Error response for " + request.uri(), ex);
                }
            }
            catch (Exception ex) {
                _log.error("Additional error response for " + request.uri(), ex);
                throw ex;
            }
            responseWriter.writeXmlResponse(marshalException(e));
        }
        } finally {
            postDecoder.destroy();
        }
    }

    private void leaveTable(HttpRequest request, String tableId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        _hallServer.leaveAwaitingTable(resourceOwner, tableId);
        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void createTable(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String format = getFormParameterSafely(postDecoder, "format");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            String timer = getFormParameterSafely(postDecoder, "timer");
            String desc = getFormParameterSafely(postDecoder, "desc").trim();
            String isPrivateVal = getFormParameterSafely(postDecoder, "isPrivate");
            boolean isPrivate = Boolean.parseBoolean(isPrivateVal);
            String isInviteOnlyVal = getFormParameterSafely(postDecoder, "isInviteOnly");
            boolean isInviteOnly = Boolean.parseBoolean(isInviteOnlyVal);
            //To prevent annoyance, super long glacial games are hidden from everyone except
            // the participants and admins.
            boolean isHidden = timer.toLowerCase().equals(GameTimer.GLACIAL_TIMER.name());

            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            if(isInviteOnly) {
                if(desc.isEmpty()) {
                    responseWriter.writeXmlResponse(marshalException(new HallException("Invite-only games must have your intended opponent in the description")));
                    return;
                }

                if(desc.equalsIgnoreCase(resourceOwner.getName())) {
                    responseWriter.writeXmlResponse(marshalException(new HallException("Absolutely no playing with yourself!!  Private matches must be with someone else.")));
                    return;
                }

                try {
                    var player = _playerDao.getPlayer(desc);
                    if(player == null)
                    {
                        responseWriter.writeXmlResponse(marshalException(new HallException("Cannot find player '" + desc + "'. Check your spelling and capitalization and ensure it is exact.")));
                        return;
                    }
                }
                catch(RuntimeException ex) {
                    responseWriter.writeXmlResponse(marshalException(new HallException("Cannot find player '" + desc + "'. Check your spelling and capitalization and ensure it is exact.")));
                    return;
                }
            }



            try {
                _hallServer.createNewTable(format, resourceOwner, deckName, timer, desc, isInviteOnly, isPrivate, isHidden);
                responseWriter.writeXmlResponse(null);
            }
            catch (HallException e) {
                try
                {
                    //try again assuming it's a new player with one of the default library decks selected
                    Player librarian = _playerDao.getPlayer("Librarian");
                    _hallServer.spoofNewTable(format, resourceOwner, librarian, deckName, timer, "(New Player) " + desc, isInviteOnly, isPrivate, isHidden);
                    responseWriter.writeXmlResponse(null);
                    return;
                }
                catch (HallException ex) {
                    _log.error(ex);
                }
                _log.error(e);
                responseWriter.writeXmlResponse(marshalException(e));
            }
        }
        catch (Exception ex)
        {
            //This is a worthless error that doesn't need to be spammed into the log
            if(!IgnoreError(ex)) {
                _log.error("Error response for " + request.uri(), ex);
            }
            responseWriter.writeXmlResponse(marshalException(new HallException("Failed to create table. Please try again later.")));
        }
        finally {
            postDecoder.destroy();
        }
    }



    private boolean IgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if(msg != null && msg.contains("You don't have a deck registered yet") ||
                msg.contains("Your selected deck is not valid for this format"))
            return true;

        return false;
    }

    private void dropFromTournament(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            String response = _hallServer.dropFromTournament(tournamentId, resourceOwner);

            responseWriter.writeXmlResponse(marshalResponse(response));
        } finally {
            postDecoder.destroy();
        }
    }

    private void joinTournamentLate(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            String response = _hallServer.joinTournamentLate(tournamentId, resourceOwner, deckName);

            responseWriter.writeXmlResponse(marshalResponse(response));
        } finally {
            postDecoder.destroy();
        }
    }

    private void registerSealedTournamentDeck(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            String response = _hallServer.registerSealedTournamentDeck(tournamentId, resourceOwner, deckName);

            responseWriter.writeXmlResponse(marshalResponse(response));
        } finally {
            postDecoder.destroy();
        }
    }

    private void joinQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        String deckName = getFormParameterSafely(postDecoder, "deckName");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        try {
            _hallServer.joinQueue(queueId, resourceOwner, deckName);
            responseWriter.writeXmlResponse(null);
        } catch (HallException e) {
            if(!IgnoreError(e)) {
                _log.error("Error response for " + request.uri(), e);
            }
            responseWriter.writeXmlResponse(marshalException(e));
        }
        } finally {
            postDecoder.destroy();
        }
    }

    private void leaveQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        _hallServer.leaveQueue(queueId, resourceOwner);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private Document marshalException(HallException e) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element error = doc.createElement("error");
        error.setAttribute("message", e.getMessage());
        doc.appendChild(error);
        return doc;
    }

    private Document marshalResponse(String message) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Element response = doc.createElement("response");
        response.setAttribute("message", message);
        doc.appendChild(response);
        return doc;
    }

    private void getFormat(HttpRequest request, String format, ResponseWriter responseWriter) throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        LotroFormat lotroFormat = _formatLibrary.getFormat(format);
        appendFormat(result, lotroFormat);

        responseWriter.writeHtmlResponse(result.toString());
    }

    private void getFormats(HttpRequest request, ResponseWriter responseWriter) throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        for (LotroFormat lotroFormat : _formatLibrary.getHallFormats().values()) {
            appendFormat(result, lotroFormat);
        }

        responseWriter.writeHtmlResponse(result.toString());
    }

    private void appendFormat(StringBuilder result, LotroFormat lotroFormat) throws CardNotFoundException {
        result.append("<b>" + lotroFormat.getName() + "</b>");
        result.append("<ul>");
        result.append("<li>valid sets: ");
        for (Integer integer : lotroFormat.getValidSets())
            result.append(integer + ", ");
        result.append("</li>");
        result.append("<li>sites from block: " + lotroFormat.getSiteBlock().getHumanReadable() + "</li>");
        result.append("<li>Ring-bearer skirmish can be cancelled: " + (lotroFormat.canCancelRingBearerSkirmish() ? "yes" : "no") + "</li>");
        if (lotroFormat.getBannedCards().size() > 0) {
            result.append("<li>X-listed (can't be played): ");
            appendCards(result, lotroFormat.getBannedCards());
            result.append("</li>");
        }
        if (lotroFormat.getRestrictedCards().size() > 0) {
            result.append("<li>R-listed (can play just one copy): ");
            appendCards(result, lotroFormat.getRestrictedCards());
            result.append("</li>");
        }
        if (lotroFormat.getLimit2Cards().size() > 0) {
            result.append("<li>Limited to 2 in deck: ");
            List<String> limit2Cards = lotroFormat.getLimit2Cards();
            appendCards(result, limit2Cards);
            result.append("</li>");
        }
        if (lotroFormat.getLimit3Cards().size() > 0) {
            result.append("<li>Limited to 3 in deck: ");
            List<String> limit3Cards = lotroFormat.getLimit3Cards();
            appendCards(result, limit3Cards);
            result.append("</li>");
        }
        if (lotroFormat.getRestrictedCardNames().size() > 0) {
            result.append("<li>Restricted by card name: ");
            boolean first = true;
            for (String cardName : lotroFormat.getRestrictedCardNames()) {
                if (!first)
                    result.append(", ");
                result.append(cardName);
                first = false;
            }
            result.append("</li>");
        }
        if (!lotroFormat.getErrataCardMap().isEmpty()) {
            result.append("<li>Errata: ");
            appendCards(result, new ArrayList<>(new LinkedHashSet<>(lotroFormat.getErrataCardMap().values())));
            result.append("</li>");
        }
        if (lotroFormat.getValidCards().size() > 0) {
            result.append("<li>Additional valid: ");
            List<String> additionalValidCards = lotroFormat.getValidCards();
            appendCards(result, additionalValidCards);
            result.append("</li>");
        }
        result.append("</ul>");
    }

    private void appendCards(StringBuilder result, List<String> additionalValidCards) throws CardNotFoundException {
        if (!additionalValidCards.isEmpty()) {
            for (String blueprintId : additionalValidCards)
                result.append(GameUtils.getCardLink(blueprintId, _library.getLotroCardBlueprint(blueprintId)) + ", ");
            if (additionalValidCards.isEmpty())
                result.append("none,");
        }
    }

    private void getErrataInfo(HttpRequest request, ResponseWriter responseWriter) throws CardNotFoundException {

        var errata = _library.getErrata();
        String json = JsonUtils.Serialize(errata);

        responseWriter.writeJsonResponse(json);
    }

    private void getHall(HttpRequest request, ResponseWriter responseWriter) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());

        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        try {
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();

            Player player = getResourceOwnerSafely(request, null);

            Element hall = doc.createElement("hall");
            hall.setAttribute("currency", String.valueOf(_collectionManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode()).getCurrency()));

            _hallServer.signupUserForHall(resourceOwner, new SerializeHallInfoVisitor(doc, hall));
            for (Map.Entry<String, LotroFormat> format : _formatLibrary.getHallFormats().entrySet()) {
                //playtest formats are opt-in
                if (format.getKey().startsWith("test") && !player.hasType(Player.Type.PLAY_TESTER))
                    continue;

                Element formatElem = doc.createElement("format");
                formatElem.setAttribute("type", format.getKey());
                formatElem.appendChild(doc.createTextNode(format.getValue().getName()));
                hall.appendChild(formatElem);
            }
            for (League league : _leagueService.getActiveLeagues()) {
                final LeagueSerieInfo currentLeagueSerie = _leagueService.getCurrentLeagueSerie(league);
                if (currentLeagueSerie != null && _leagueService.isPlayerInLeague(league, resourceOwner)) {
                    Element formatElem = doc.createElement("format");
                    formatElem.setAttribute("type", String.valueOf(league.getCode()));
                    formatElem.appendChild(doc.createTextNode(league.getName()));
                    hall.appendChild(formatElem);
                }
            }

            doc.appendChild(hall);

            responseWriter.writeXmlResponse(doc);
        } catch (HttpProcessingException exp) {
            logHttpError(_log, exp.getStatus(), request.uri(), exp);
            responseWriter.writeError(exp.getStatus());
        } catch (Exception exp) {
            _log.error("Error response for " + request.uri(), exp);
            responseWriter.writeError(500);
        }
    }

    private void updateHall(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            int channelNumber = Integer.parseInt(getFormParameterSafely(postDecoder, "channelNumber"));

            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            processLoginReward(resourceOwner.getName());

            try {
                HallCommunicationChannel pollableResource = _hallServer.getCommunicationChannel(resourceOwner, channelNumber);
                HallUpdateLongPollingResource polledResource = new HallUpdateLongPollingResource(pollableResource, request, resourceOwner, responseWriter);
                longPollingSystem.processLongPollingResource(polledResource, pollableResource);
            }
            catch (SubscriptionExpiredException exp) {
                logHttpError(_log, 410, request.uri(), exp);
                responseWriter.writeError(410);
            }
            catch (SubscriptionConflictException exp) {
                logHttpError(_log, 409, request.uri(), exp);
                responseWriter.writeError(409);
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private class HallUpdateLongPollingResource implements LongPollingResource {
        private final HttpRequest _request;
        private final HallCommunicationChannel _hallCommunicationChannel;
        private final Player _resourceOwner;
        private final ResponseWriter _responseWriter;
        private boolean _processed;

        private HallUpdateLongPollingResource(HallCommunicationChannel hallCommunicationChannel, HttpRequest request, Player resourceOwner, ResponseWriter responseWriter) {
            _hallCommunicationChannel = hallCommunicationChannel;
            _request = request;
            _resourceOwner = resourceOwner;
            _responseWriter = responseWriter;
        }

        @Override
        public synchronized boolean wasProcessed() {
            return _processed;
        }

        @Override
        public synchronized void processIfNotProcessed() {
            if (!_processed) {
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                    Document doc = documentBuilder.newDocument();

                    Element hall = doc.createElement("hall");
                    _hallCommunicationChannel.processCommunicationChannel(_hallServer, _resourceOwner, new SerializeHallInfoVisitor(doc, hall));
                    hall.setAttribute("currency", String.valueOf(_collectionManager.getPlayerCollection(_resourceOwner, CollectionType.MY_CARDS.getCode()).getCurrency()));

                    doc.appendChild(hall);

                    Map<String, String> headers = new HashMap<>();
                    processDeliveryServiceNotification(_request, headers);

                    _responseWriter.writeXmlResponse(doc, headers);
                } catch (Exception exp) {
                    logHttpError(_log, 500, _request.uri(), exp);
                    _responseWriter.writeError(500);
                }
                _processed = true;
            }
        }
    }

    private static class SerializeDraftVisitor implements DraftChannelVisitor {
        private final Document _doc;
        private final Element _draft;

        private SerializeDraftVisitor(Document doc, Element draft) {
            _doc = doc;
            _draft = draft;
        }

        public void channelNumber(int channelNumber) {
            _draft.setAttribute("channelNumber", String.valueOf(channelNumber));
        }

        public void timeLeft(long timeLeft) {
            _draft.setAttribute("timeLeft", String.valueOf(timeLeft));
        }

        public void noCardChoice() {
        }

        public void cardChoice(CardCollection cardCollection) {
            for (CardCollection.Item possiblePick : cardCollection.getAll()) {
                for (int i = 0; i < possiblePick.getCount(); i++) {
                    Element pick = _doc.createElement("pick");
                    pick.setAttribute("blueprintId", possiblePick.getBlueprintId());
                    _draft.appendChild(pick);
                }
            }
        }

        public void chosenCards(CardCollection cardCollection) {
            for (CardCollection.Item cardInCollection : cardCollection.getAll()) {
                Element card = _doc.createElement("card");
                card.setAttribute("blueprintId", cardInCollection.getBlueprintId());
                card.setAttribute("count", String.valueOf(cardInCollection.getCount()));
                _draft.appendChild(card);
            }
        }
    }

    private static class SerializeHallInfoVisitor implements HallChannelVisitor {
        private final Document _doc;
        private final Element _hall;

        public SerializeHallInfoVisitor(Document doc, Element hall) {
            _doc = doc;
            _hall = hall;
        }

        @Override
        public void channelNumber(int channelNumber) {
            _hall.setAttribute("channelNumber", String.valueOf(channelNumber));
        }

        @Override
        public void newPlayerGame(String gameId) {
            Element newGame = _doc.createElement("newGame");
            newGame.setAttribute("id", gameId);
            _hall.appendChild(newGame);
        }

        @Override
        public void serverTime(String serverTime) {
            _hall.setAttribute("serverTime", serverTime);
        }

        @Override
        public void motdChanged(String motd) {
            _hall.setAttribute("motd", motd);
        }

        @Override
        public void addTournamentQueue(String queueId, Map<String, String> props) {
            Element queue = _doc.createElement("queue");
            queue.setAttribute("action", "add");
            queue.setAttribute("id", queueId);
            for (Map.Entry<String, String> attribute : props.entrySet())
                queue.setAttribute(attribute.getKey(), attribute.getValue());
            _hall.appendChild(queue);
        }

        @Override
        public void updateTournamentQueue(String queueId, Map<String, String> props) {
            Element queue = _doc.createElement("queue");
            queue.setAttribute("action", "update");
            queue.setAttribute("id", queueId);
            for (Map.Entry<String, String> attribute : props.entrySet())
                queue.setAttribute(attribute.getKey(), attribute.getValue());
            _hall.appendChild(queue);
        }

        @Override
        public void removeTournamentQueue(String queueId) {
            Element queue = _doc.createElement("queue");
            queue.setAttribute("action", "remove");
            queue.setAttribute("id", queueId);
            _hall.appendChild(queue);
        }

        @Override
        public void addTournament(String tournamentId, Map<String, String> props) {
            Element tournament = _doc.createElement("tournament");
            tournament.setAttribute("action", "add");
            tournament.setAttribute("id", tournamentId);
            for (Map.Entry<String, String> attribute : props.entrySet())
                tournament.setAttribute(attribute.getKey(), attribute.getValue());
            _hall.appendChild(tournament);
        }

        @Override
        public void updateTournament(String tournamentId, Map<String, String> props) {
            Element tournament = _doc.createElement("tournament");
            tournament.setAttribute("action", "update");
            tournament.setAttribute("id", tournamentId);
            for (Map.Entry<String, String> attribute : props.entrySet())
                tournament.setAttribute(attribute.getKey(), attribute.getValue());
            _hall.appendChild(tournament);
        }

        @Override
        public void removeTournament(String tournamentId) {
            Element tournament = _doc.createElement("tournament");
            tournament.setAttribute("action", "remove");
            tournament.setAttribute("id", tournamentId);
            _hall.appendChild(tournament);
        }

        @Override
        public void addTable(String tableId, Map<String, String> props) {
            Element table = _doc.createElement("table");
            table.setAttribute("action", "add");
            table.setAttribute("id", tableId);
            for (Map.Entry<String, String> attribute : props.entrySet())
                table.setAttribute(attribute.getKey(), attribute.getValue());
            _hall.appendChild(table);
        }

        @Override
        public void updateTable(String tableId, Map<String, String> props) {
            Element table = _doc.createElement("table");
            table.setAttribute("action", "update");
            table.setAttribute("id", tableId);
            for (Map.Entry<String, String> attribute : props.entrySet())
                table.setAttribute(attribute.getKey(), attribute.getValue());
            _hall.appendChild(table);
        }

        @Override
        public void removeTable(String tableId) {
            Element table = _doc.createElement("table");
            table.setAttribute("action", "remove");
            table.setAttribute("id", tableId);
            _hall.appendChild(table);
        }
    }
}
