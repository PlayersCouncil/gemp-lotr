package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.DeckDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.DraftChannelVisitor;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.FormatDefinitions;
import com.gempukku.lotro.game.formats.FormatSummary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.*;
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
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Type;
import java.util.*;

public class HallRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CollectionsManager _collectionManager;
    private final DeckDAO _deckDao;
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
        _deckDao = extractObject(context, DeckDAO.class);
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
        } else if (uri.equals("/solo") && request.method() == HttpMethod.POST) {
            createSoloTable(request, responseWriter);
        } else if (uri.equals("/update") && request.method() == HttpMethod.POST) {
            updateHall(request, responseWriter);
        } else if (uri.equals("/formats/html") && request.method() == HttpMethod.GET) {
            getFormats(request, responseWriter);
        } else if (uri.equals("/formats/json") && request.method() == HttpMethod.GET) {
            getFormatDefinitions(responseWriter);
        } else if (uri.equals("/errata/json") && request.method() == HttpMethod.GET) {
            getErrataInfo(request, responseWriter);
        } else if (uri.equals("/players") && request.method() == HttpMethod.GET) {
            searchPlayers(request, responseWriter);
        } else if (uri.startsWith("/format/") && request.method() == HttpMethod.GET) {
            getFormat(request, uri.substring(8), responseWriter);
        } else if (uri.startsWith("/queue/") && request.method() == HttpMethod.POST) {
            if (uri.endsWith("/start")) {
                startQueue(request, uri.substring(7, uri.length() - 6), responseWriter);
            } else if (uri.endsWith("/leave")) {
                leaveQueue(request, uri.substring(7, uri.length() - 6), responseWriter);
            } else if (uri.endsWith("/ready")) {
                confirmReadyCheckQueue(request, uri.substring(7, uri.length() - 6), responseWriter);
            } else {
                joinQueue(request, uri.substring(7), responseWriter);
            }
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            dropFromTournament(request, uri.substring(12, uri.length() - 6), responseWriter);
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/join") && request.method() == HttpMethod.POST) {
            joinTournamentLate(request, uri.substring(12, uri.length() - 5), responseWriter);
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/registerdeck") && request.method() == HttpMethod.POST) {
            registerLimitedTournamentDeck(request, uri.substring(12, uri.length() - 13), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            leaveTable(request, uri.substring(1, uri.length() - 6), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            joinTable(request, uri.substring(1), responseWriter);
        } else {
            responseWriter.writeError(404);
        }
    }

    // ---- deck source (Play flows) ----
    // The Play and Join forms say where the chosen deck lives: deckSource=player (one of the player's own decks) or
    // deckSource=library (a Deck Library deck, owned by the Librarian).  Requests without the parameter come from
    // older clients; for those, create and join keep the old behaviour of retrying with the Librarian's deck of the
    // same name when the player's own deck fails.
    private static final String DECK_SOURCE_LIBRARY = "library";
    private static final String DECK_SOURCE_PLAYER = "player";

    /** The deck's owner for an explicit deckSource, or null when the client did not say (legacy request). */
    private Player resolveDeckOwner(String deckSource, Player player) throws HttpProcessingException {
        if (deckSource == null || deckSource.isEmpty())
            return null;
        if (deckSource.equalsIgnoreCase(DECK_SOURCE_LIBRARY))
            return getLibrarian();
        if (deckSource.equalsIgnoreCase(DECK_SOURCE_PLAYER))
            return player;
        throw new HttpProcessingException(400, "Parameter 'deckSource' must be 'player' or 'library'.");
    }

    private void joinTable(HttpRequest request, String tableId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        String deckName = getFormParameterSafely(postDecoder, "deckName");
        Player deckOwner = resolveDeckOwner(getFormParameterSafely(postDecoder, "deckSource"), resourceOwner);

        if (deckOwner != null) {
            try {
                _hallServer.joinTableAsPlayer(tableId, resourceOwner, deckOwner, deckName);
                responseWriter.writeXmlResponse(null);
            } catch (HallException e) {
                if (!IgnoreError(e))
                    _log.error("Error response for " + request.uri(), e);
                responseWriter.writeXmlResponse(marshalException(e));
            }
            return;
        }

        // legacy client: no deckSource
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
            String deckSource = getFormParameterSafely(postDecoder, "deckSource");
            String timer = getFormParameterSafely(postDecoder, "timer");
            // No markup, no control characters, at most 100 characters, whatever the client sent.
            String desc = HallServer.sanitizeTableDescription(getFormParameterSafely(postDecoder, "desc"));
            String isPrivateVal = getFormParameterSafely(postDecoder, "isPrivate");
            boolean isPrivate = Boolean.parseBoolean(isPrivateVal);
            String isInviteOnlyVal = getFormParameterSafely(postDecoder, "isInviteOnly");
            boolean isInviteOnly = Boolean.parseBoolean(isInviteOnlyVal);
            //To prevent annoyance, super long glacial games are hidden from everyone except
            // the participants and admins.  (The form sends the code "glacial"; the timer's name is "Glacial".)
            boolean isHidden = GameTimer.ResolveTimer(timer) == GameTimer.GLACIAL_TIMER;

            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            Player deckOwner = resolveDeckOwner(deckSource, resourceOwner);

            if(isInviteOnly) {
                // The invitee's name is the description (the hall's invite convention).  It may be typed in, so it is
                // checked here and replaced by the account's exact name.
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
                        responseWriter.writeXmlResponse(marshalException(new HallException("Cannot find player '" + desc + "'. Check your spelling and ensure the name is exact.")));
                        return;
                    }
                    if (player.getName() != null && player.getName().equalsIgnoreCase(desc))
                        desc = player.getName();
                }
                catch(RuntimeException ex) {
                    responseWriter.writeXmlResponse(marshalException(new HallException("Cannot find player '" + desc + "'. Check your spelling and ensure the name is exact.")));
                    return;
                }
            }

            if (deckOwner != null) {
                // explicit deck source: no retry with the other owner
                String tableDesc = TableHolder.tableDescription(desc, isInviteOnly, hasOwnDecks(resourceOwner));
                try {
                    _hallServer.createNewTable(format, resourceOwner, deckOwner, deckName, timer, tableDesc, isInviteOnly, isPrivate, isHidden);
                    responseWriter.writeXmlResponse(null);
                } catch (HallException e) {
                    if (!IgnoreError(e))
                        _log.error(e);
                    responseWriter.writeXmlResponse(marshalException(e));
                }
                return;
            }

            // legacy client: no deckSource
            try {
                _hallServer.createNewTable(format, resourceOwner, deckName, timer, desc, isInviteOnly, isPrivate, isHidden);
                responseWriter.writeXmlResponse(null);
            }
            catch (HallException e) {
                try
                {
                    //try again assuming it's a new player with one of the default library decks selected
                    Player librarian = _playerDao.getPlayer("Librarian");
                    _hallServer.spoofNewTable(format, resourceOwner, librarian, deckName, timer,
                            TableHolder.tableDescription(desc, isInviteOnly, hasOwnDecks(resourceOwner)), isInviteOnly, isPrivate, isHidden);
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
        catch (HttpProcessingException ex) {
            throw ex;
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

    // Whether the player has any decks of their own; only a player with none is flagged "(New Player)".  If the decks
    // cannot be read, the player is not flagged.
    private boolean hasOwnDecks(Player player) {
        try {
            return !_deckDao.getPlayerDeckNames(player).isEmpty();
        } catch (RuntimeException ex) {
            _log.warn("Could not read the decks of " + player.getName() + " for the table description", ex);
            return true;
        }
    }

    private void createSoloTable(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String format = getFormParameterSafely(postDecoder, "format");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            String deckSource = getFormParameterSafely(postDecoder, "deckSource");
            String isPrivateVal = getFormParameterSafely(postDecoder, "isPrivate");
            String botDeckName = getFormParameterSafely(postDecoder, "botDeckName");
            boolean isPrivate = Boolean.parseBoolean(isPrivateVal);

            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            Player deckOwner = resolveDeckOwner(deckSource, resourceOwner);

            try {
                _hallServer.createNewSoloTable(format, resourceOwner, deckOwner != null ? deckOwner : resourceOwner, deckName, botDeckName, isPrivate);
                responseWriter.writeXmlResponse(null);
            }
            catch (HallException e) {
                if (!IgnoreError(e))
                    _log.error(e);
                responseWriter.writeXmlResponse(marshalException(e));
            }
        }
        catch (HttpProcessingException ex) {
            throw ex;
        }
        catch (Exception ex)
        {
            //This is a worthless error that doesn't need to be spammed into the log
            if(!IgnoreError(ex)) {
                _log.error("Error response for " + request.uri(), ex);
            }
            responseWriter.writeXmlResponse(marshalException(new HallException("Failed to create table. The bot only supports Fellowship Block format.")));
        }
        finally {
            postDecoder.destroy();
        }
    }



    private boolean IgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if(msg != null && (msg.contains("You don't have a deck registered yet") ||
                msg.contains("Your selected deck is not valid for this format") ||
                msg.contains("This queue cannot be started early by ")))
            return true;

        return false;
    }

    private void dropFromTournament(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            try {
                String response = _hallServer.dropFromTournament(tournamentId, resourceOwner);
                responseWriter.writeXmlResponse(marshalResponse(response));
            } catch (HallException e) {
                // a refusal the player reads (still playing a match, already dropped, tournament over...)
                _log.debug("Tournament drop refused for " + request.uri() + ": " + e.getMessage());
                responseWriter.writeXmlResponse(marshalException(e));
            }
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
            Player deckOwner = resolveDeckOwner(getFormParameterSafely(postDecoder, "deckSource"), resourceOwner);

            try {
                String response = _hallServer.joinTournamentLate(tournamentId, resourceOwner, deckOwner != null ? deckOwner : resourceOwner, deckName);
                responseWriter.writeXmlResponse(marshalResponse(response));
            } catch (HallException e) {
                if (!IgnoreError(e))
                    _log.debug("Late join refused for " + request.uri() + ": " + e.getMessage());
                responseWriter.writeXmlResponse(marshalException(e));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void registerLimitedTournamentDeck(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            Player deckOwner = resolveDeckOwner(getFormParameterSafely(postDecoder, "deckSource"), resourceOwner);

            try {
                String response = _hallServer.registerLimitedTournamentDeck(tournamentId, resourceOwner, deckOwner != null ? deckOwner : resourceOwner, deckName);
                responseWriter.writeXmlResponse(marshalResponse(response));
            } catch (HallException e) {
                if (!IgnoreError(e))
                    _log.debug("Deck registration refused for " + request.uri() + ": " + e.getMessage());
                responseWriter.writeXmlResponse(marshalException(e));
            }
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
        Player deckOwner = resolveDeckOwner(getFormParameterSafely(postDecoder, "deckSource"), resourceOwner);

        try {
            _hallServer.joinQueue(queueId, resourceOwner, deckOwner != null ? deckOwner : resourceOwner, deckName);
            responseWriter.writeXmlResponse(null);
        } catch (HallException e) {
            // a refusal the player reads (queue full, sign-up not open, not enough currency, invalid deck...)
            _log.debug("Queue join refused for " + request.uri() + ": " + e.getMessage());
            responseWriter.writeXmlResponse(marshalException(e));
        }
        } finally {
            postDecoder.destroy();
        }
    }

    private void startQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");

            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            try {
                _hallServer.startQueueEarly(queueId, resourceOwner);
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

    private void confirmReadyCheckQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);
            _hallServer.confirmReadyCheck(queueId, resourceOwner);
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

    /**
     * Help › Format Definitions: every hall format, in the Play menu's order, each in a
     * {@code <section class="format-entry" id="format-<code>">} so the Play popup's format (i) can link straight to it
     * (FormatSummary.anchorId; the page builds its contents list from these sections).
     */
    private void getFormats(HttpRequest request, ResponseWriter responseWriter) throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        List<LotroFormat> formats = new ArrayList<>(_formatLibrary.getHallFormats().values());
        formats.sort(Comparator.comparingInt(LotroFormat::getOrder));
        for (LotroFormat lotroFormat : formats) {
            appendFormat(result, lotroFormat);
        }

        responseWriter.writeHtmlResponse(result.toString());
    }

    /**
     * GET /hall/formats/json: Help › Format Definitions' data, {"formats": [...]} for every hall format in the Play
     * menu's order (FormatDefinitions: the Play popup's facts, the section anchor, errata flags and the card lists
     * with names and sets).
     */
    private void getFormatDefinitions(ResponseWriter responseWriter) {
        responseWriter.writeJsonResponse(JsonUtils.Serialize(
                FormatDefinitions.describeAll(_formatLibrary.getHallFormats().values(), _library)));
    }

    private void appendFormat(StringBuilder result, LotroFormat lotroFormat) throws CardNotFoundException {
        String code = lotroFormat.getCode();
        result.append("<section class=\"format-entry\" id=\"" + FormatSummary.anchorId(code) + "\" data-format=\""
                + escapeHtml(code) + "\">");
        result.append("<h2 class=\"format-name\">" + escapeHtml(lotroFormat.getName()) + "</h2>");
        result.append("<ul>");
        String sets = FormatSummary.describeSets(lotroFormat.getValidSets());
        if (sets != null)
            result.append("<li>" + escapeHtml(sets) + "</li>");
        result.append("<li>Sites: " + escapeHtml(lotroFormat.getSiteBlock().getHumanReadable()) + "</li>");
        result.append("<li>Ring-bearer skirmish cancel: " + (lotroFormat.canCancelRingBearerSkirmish() ? "Yes" : "No") + "</li>");
        if (lotroFormat.winWhenShadowReconciles())
            result.append("<li>The game ends after Regroup actions are made (instead of at the start of Regroup)</li>");
        if (lotroFormat.discardPileIsPublic())
            result.append("<li>Discard piles are public information for both sides</li>");
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
        // The errata themselves are on Help › PC Errata; listing every errata'd card here was a wall of text.
        var errata = FormatDefinitions.errata(lotroFormat);
        if (Boolean.TRUE.equals(errata.get("pc")))
            result.append("<li>Errata: PC Errata (see Help &rsaquo; PC Errata)</li>");
        else if (Boolean.TRUE.equals(errata.get("pcCardsLegal")))
            result.append("<li>Errata: PC Errata versions are legal alongside the originals (see Help &rsaquo; PC Errata)</li>");
        if (Boolean.TRUE.equals(errata.get("playtest")))
            result.append("<li>Errata: playtest errata</li>");
        if (lotroFormat.getValidCards().size() > 0) {
            result.append("<li>Additional valid: ");
            List<String> additionalValidCards = lotroFormat.getValidCards();
            appendCards(result, additionalValidCards);
            result.append("</li>");
        }
        result.append("</ul>");
        result.append("</section>");
    }

    private static String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * GET /hall/players?prefix=..&limit=.. (logged in): {"players": [name, ...]}, registered players whose name starts
     * with prefix (at least 2 characters; at most 10 names; never the searcher).  The Casual table's invite picker
     * uses it after the players in the hall.
     */
    private void searchPlayers(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Player resourceOwner = getResourceOwnerSafely(request, getQueryParameterSafely(queryDecoder, "participantId"));

        String prefix = getQueryParameterSafely(queryDecoder, "prefix");
        int limit = PlayerNameSearch.clampLimit(getQueryParameterSafely(queryDecoder, "limit"));

        var result = new LinkedHashMap<String, Object>();
        result.put("players", PlayerNameSearch.byPrefix(_playerDao, prefix, resourceOwner.getName(), limit));
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
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

        var recentErrata = _formatLibrary.getFormat("pc_errata").getRecentErrata();

        var errataInfo = new HashMap<String, Object>();
        errataInfo.put("all", _library.getErrata());
        errataInfo.put("recent", recentErrata);
        // Help › PC Errata (pc-errata): the filterable rows ("entries"), the hall formats ("formats") and "counts"
        errataInfo.putAll(com.gempukku.lotro.game.formats.ErrataCatalog.build(_library, _formatLibrary));

        String json = JsonUtils.Serialize(errataInfo);

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

            Element hall = doc.createElement("hall");
            // The My Account tab shows this (hall.pocketValue).  The format and league lists this response used to
            // carry went unread: the Play popup fetches them from /hall/formats and /league.
            hall.setAttribute("currency", String.valueOf(_collectionManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode()).getCurrency()));

            _hallServer.signupUserForHall(resourceOwner, new SerializeHallInfoVisitor(doc, hall));

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
                    processDeliveryServiceNotification(_resourceOwner, headers);

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
            // Epoch ms, so the client can measure table ages (createdAt) against the server clock, not its own.
            _hall.setAttribute("serverTimeMs", String.valueOf(System.currentTimeMillis()));
        }

        @Override
        public void motdChanged(String motd) {
            _hall.setAttribute("motd", motd);
        }

        @Override
        public void shutdownMode(boolean shutdown) {
            // on every hall answer while it lasts (absent otherwise): the connection readout turns yellow "Shutdown"
            if (shutdown)
                _hall.setAttribute("shutdown", "true");
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
