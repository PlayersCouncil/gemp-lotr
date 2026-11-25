package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.PlayerLock;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

public class LotroServerRequestHandler {
    protected PlayerDAO _playerDao;
    protected LoggedUserHolder _loggedUserHolder;
    private final TransferDAO _transferDAO;
    private final CollectionsManager _collectionManager;

    private static final Logger _log = LogManager.getLogger(LotroServerRequestHandler.class);

    public LotroServerRequestHandler(Map<Type, Object> context) {
        _playerDao = extractObject(context, PlayerDAO.class);
        _loggedUserHolder = extractObject(context, LoggedUserHolder.class);
        _transferDAO = extractObject(context, TransferDAO.class);
        _collectionManager = extractObject(context, CollectionsManager.class);
    }

    protected final void processLoginReward(String loggedUser) throws Exception {
        if (loggedUser != null) {
            Player player = _playerDao.getPlayer(loggedUser);
            synchronized (PlayerLock.getLock(player)) {
                int currentDate = DateUtils.getCurrentDate();
                int latestMonday = DateUtils.getMondayBeforeOrOn(currentDate);

                Integer lastReward = player.getLastLoginReward();
                if (lastReward == null) {
                    _playerDao.setLastReward(player, latestMonday);
                    _collectionManager.addCurrencyToPlayerCollection(true, "Singup reward", player, CollectionType.MY_CARDS, 20000);
                } else {
                    if (latestMonday != lastReward) {
                        if (_playerDao.updateLastReward(player, lastReward, latestMonday))
                            _collectionManager.addCurrencyToPlayerCollection(true, "Weekly reward", player, CollectionType.MY_CARDS, 5000);
                    }
                }
            }
        }
    }

    /** The session cookie's name, and the path it is set for: every address of the server, and nothing else. */
    protected static final String SESSION_COOKIE = "loggedUser";
    private static final String SESSION_COOKIE_PATH = "/gemp-lotr-server";

    /** The value of the request's session cookie, or null. */
    protected final String getSessionId(HttpRequest request) {
        String cookieHeader = request.headers().get(COOKIE);
        if (cookieHeader != null) {
            for (Cookie cookie : ServerCookieDecoder.STRICT.decode(cookieHeader)) {
                if (cookie.name().equals(SESSION_COOKIE) && cookie.value() != null)
                    return cookie.value();
            }
        }
        return null;
    }

    /** The request's session (PC-ACCOUNT.md), or null. */
    protected final LoggedUserHolder.Session getSession(HttpRequest request) {
        String sessionId = getSessionId(request);
        return sessionId == null ? null : _loggedUserHolder.getSession(sessionId);
    }

    private String getLoggedUser(HttpRequest request) {
        LoggedUserHolder.Session session = getSession(request);
        return session == null ? null : session.getPlayer();
    }

    /**
     * The Set-Cookie value that names `sessionId` as the browser's session. Not readable by scripts, not sent with
     * another site's requests (a link here from another site still carries it), and kept to https when the request
     * came over it (nginx says so in X-Forwarded-Proto; plain http stays possible for local development).
     */
    protected final String sessionCookie(HttpRequest request, String sessionId) {
        return sessionCookie(cameOverHttps(request), sessionId);
    }

    /** As above, for an answer written after the request is gone: `secure` is {@link #cameOverHttps} of it. */
    protected final String sessionCookie(boolean secure, String sessionId) {
        DefaultCookie cookie = new DefaultCookie(SESSION_COOKIE, sessionId);
        cookie.setPath(SESSION_COOKIE_PATH);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Lax);
        cookie.setSecure(secure);
        return ServerCookieEncoder.STRICT.encode(cookie);
    }

    protected final boolean cameOverHttps(HttpRequest request) {
        return "https".equalsIgnoreCase(request.headers().get("X-Forwarded-Proto"));
    }

    /** The Set-Cookie value that takes the session cookie away. */
    protected final String noSessionCookie(HttpRequest request) {
        DefaultCookie cookie = new DefaultCookie(SESSION_COOKIE, "");
        cookie.setPath(SESSION_COOKIE_PATH);
        cookie.setHttpOnly(true);
        cookie.setSameSite(CookieHeaderNames.SameSite.Lax);
        cookie.setSecure("https".equalsIgnoreCase(request.headers().get("X-Forwarded-Proto")));
        cookie.setMaxAge(0);
        return ServerCookieEncoder.STRICT.encode(cookie);
    }

    protected final void processDeliveryServiceNotification(Player player, Map<String, String> headersToAdd) {
        if(player == null)
            return;

        if (_transferDAO.hasUndeliveredPackages(player)) {
            headersToAdd.put("Delivery-Service-Package", "true");
        }

        if (_transferDAO.hasUndeliveredSealedContents(player)) {
            headersToAdd.put("Delivery-Service-Opened-Pack", "true");
        }

        if (_transferDAO.hasUndeliveredAnnouncement(player)) {
            headersToAdd.put("Delivery-Service-Announcement", "true");
        }

        if (_transferDAO.hasUndeliveredLeagueNotifications(player)) {
            headersToAdd.put("Delivery-Service-League", "true");
        }
    }

    /**
     * The player acting: the one the session cookie's session plays as, and nothing else (PC-ACCOUNT.md, "The player
     * comes from the session cookie").  401 without a session, or one that plays as nobody.  A {@code participantId}
     * parameter no longer names anyone: it used to let an admin act as any player, and with {@code -Dtest=true} let
     * anyone act as anyone with no login at all (Ketura, 2026-11-23: "needs ripped out").  Clients may still send
     * it; it is ignored.
     */
    protected final Player getResourceOwnerSafely(HttpRequest request) throws HttpProcessingException {
        String loggedUser = getLoggedUser(request);
        if (loggedUser == null)
            throw new HttpProcessingException(401);

        Player resourceOwner = _playerDao.getPlayer(loggedUser);
        if (resourceOwner == null)
            throw new HttpProcessingException(401);
        return resourceOwner;
    }

    protected final Player getLibrarian() throws HttpProcessingException {
        Player resourceOwner = _playerDao.getPlayer("Librarian");

        if (resourceOwner == null)
            throw new HttpProcessingException(401, "Librarian user not found.");

        return resourceOwner;
    }

    protected String getQueryParameterSafely(QueryStringDecoder queryStringDecoder, String parameterName) {
        List<String> parameterValues = queryStringDecoder.parameters().get(parameterName);
        if (parameterValues != null && !parameterValues.isEmpty())
            return parameterValues.getFirst();
        else
            return null;
    }

    protected List<String> getFormMultipleParametersSafely(HttpPostRequestDecoder postRequestDecoder, String parameterName) throws HttpPostRequestDecoder.NotEnoughDataDecoderException, IOException {
        List<String> result = new LinkedList<>();
        List<InterfaceHttpData> datas = postRequestDecoder.getBodyHttpDatas(parameterName);
        if (datas == null)
            return Collections.emptyList();
        for (InterfaceHttpData data : datas) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;
                result.add(attribute.getValue());
            }

        }
        return result;
    }

    protected String getFormParameterSafely(HttpPostRequestDecoder postRequestDecoder, String parameterName) throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
        InterfaceHttpData data = postRequestDecoder.getBodyHttpData(parameterName);
        if (data == null)
            return null;
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            return attribute.getValue();
        } else {
            return null;
        }
    }

    protected List<String> getFormParametersSafely(HttpPostRequestDecoder postRequestDecoder, String parameterName) throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
        List<InterfaceHttpData> datas = postRequestDecoder.getBodyHttpDatas(parameterName);
        if (datas == null)
            return null;
        List<String> result = new LinkedList<>();
        for (InterfaceHttpData data : datas) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;
                result.add(attribute.getValue());
            }
        }
        return result;
    }

    protected <T> T extractObject(Map<Type, Object> context, Class<T> clazz) {
        Object value = context.get(clazz);
        return (T) value;
    }

    /** A password login: a new session playing as `login`, and the header that gives the browser its cookie. */
    protected Map<String, String> logUserReturningHeaders(HttpRequest request, String remoteIp, String login) throws SQLException {
        return logUserReturningHeaders(cameOverHttps(request), remoteIp, login);
    }

    /** As above, for an answer written after the request is gone: `https` is {@link #cameOverHttps} of it. */
    protected Map<String, String> logUserReturningHeaders(boolean https, String remoteIp, String login) throws SQLException {
        _playerDao.updateLastLoginIp(login, remoteIp);

        String sessionId = _loggedUserHolder.logUser(login);
        return Collections.singletonMap(SET_COOKIE.toString(), sessionCookie(https, sessionId));
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter is empty or null,
     * then HTTP 400 (Bad Request) will be thrown to inform the user that they need to fix their call.
     * This should only be used for required fields.  If a string is optional, then of course it should be fine to send
     * a blank entry.
     * @param paramName The name of the API parameter being evaluated.  This is purely used to attach a sensible
     *                  error message to the error.
     * @param value The value being validated.  Should be a non-empty non-null string.
     * @throws HttpProcessingException This function throws HTTP 400 (Bad Request) if the value is not a valid string.
     */
    protected void Throw400IfStringNull(String paramName, String value) throws HttpProcessingException {
        if(StringUtils.isEmpty(value)) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' cannot be blank.");
        }
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter is not a
     * representation of a list of valid strings, then HTTP 400 (Bad Request) will be thrown to inform the user
     * that they need to fix their call.
     * This is for a batch of strings, such as those submitted via form data.
     * This should only be used for required fields.  If a string is optional, then of course it should be fine to send
     * a blank entry.
     * @param paramName The name of the API parameter being evaluated.  This is purely used to attach a sensible
     *                  error message to the error.
     * @param values The values being validated.  Should be a list of valid non-empty non-null strings.
     * @throws HttpProcessingException This function throws HTTP 400 (Bad Request) if any value is not a valid string.
     */
    protected void Throw400IfAnyStringNull(String paramName, List<String> values) throws HttpProcessingException {
        for (String value : values) {
            if(StringUtils.isEmpty(value)) {
                throw new HttpProcessingException(400, "Parameter '" + paramName + "' cannot be blank.");
            }
        }
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter is not a
     * representation of a valid integer, then HTTP 400 (Bad Request) will be thrown to inform the user
     * that they need to fix their call.
     * @param paramName The name of the API parameter being evaluated.  This is purely used to attach a sensible
     *                  error message to the error.
     * @param value The value being validated.  Should be a valid integer.
     * @return For convenience, the converted int will be returned, meaning that higher-level functions can
     *  validate and convert in a single function call.
     * @throws HttpProcessingException This function throws HTTP 400 (Bad Request) if the value is not a valid integer.
     */
    protected int Throw400IfNullOrNonInteger(String paramName, String value) throws HttpProcessingException {
        if(StringUtils.isEmpty(value)) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' cannot be blank.");
        }
        int newValue;
        try {
            newValue = Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' must be a valid numeric integer.");
        }

        return newValue;
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter is not a
     * representation of a valid float, then HTTP 400 (Bad Request) will be thrown to inform the user
     * that they need to fix their call.
     * @param paramName The name of the API parameter being evaluated.  This is purely used to attach a sensible
     *                  error message to the error.
     * @param value The value being validated.  Should be a valid float.
     * @return For convenience, the converted float will be returned, meaning that higher-level functions can
     *  validate and convert in a single function call.
     * @throws HttpProcessingException This function throws HTTP 400 (Bad Request) if the value is not a valid float.
     */
    protected float Throw400IfNullOrNonFloat(String paramName, String value) throws HttpProcessingException {
        if(StringUtils.isEmpty(value)) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' cannot be blank.");
        }
        float newValue;
        try {
            newValue = Float.parseFloat(value);
        }
        catch (NumberFormatException ex) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' must be a valid numeric float.");
        }

        return newValue;
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter is not a
     * representation of a list of valid integers, then HTTP 400 (Bad Request) will be thrown to inform the user
     * that they need to fix their call.
     * This is for a batch of integers, such as those submitted via form data.
     * @param paramName The name of the API parameter being evaluated.  This is purely used to attach a sensible
     *                  error message to the error.
     * @param values The values being validated.  Should be a list of valid integers.
     * @return For convenience, the converted ints will be returned, meaning that higher-level functions can
     *  validate and convert in a single function call.
     * @throws HttpProcessingException This function throws HTTP 400 (Bad Request) if any value is not a valid integer.
     */
    protected List<Integer> Throw400IfAnyNullOrNonInteger(String paramName, List<String> values) throws HttpProcessingException {
        List<Integer> newValues = new ArrayList<>();

        for(String value : values) {
            if(StringUtils.isEmpty(value)) {
                throw new HttpProcessingException(400, "Parameter '" + paramName + "' cannot be blank.");
            }
            int newValue;
            try {
                newValue = Integer.parseInt(value);
            }
            catch (NumberFormatException ex) {
                throw new HttpProcessingException(400, "Parameter '" + paramName + "' must be a valid numeric integer: '" + value + "'.");
            }
            newValues.add(newValue);
        }

        return newValues;
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter is not a
     * representation of a valid boolean variable, then HTTP 400 (Bad Request) will be thrown to inform the user
     * that they need to fix their call.
     * @param paramName The name of the API parameter being evaluated.  This is purely used to attach a sensible
     *                  error message to the error.
     * @param value The value being validated.  Should be some representation of "true" or "false".
     * @return For convenience, the converted boolean will be returned, meaning that higher-level functions can
     *  validate and convert in a single function call.
     * @throws HttpProcessingException This function throws HTTP 400 (Bad Request) if the value is not a valid boolean.
     */
    protected boolean Throw400IfNullOrNonBoolean(String paramName, String value) throws HttpProcessingException {
        if(StringUtils.isEmpty(value)) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' cannot be blank.");
        }
        boolean newValue;
        try {
            newValue = Boolean.parseBoolean(value);
        }
        catch (NumberFormatException ex) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' must be a valid boolean value ('true' or 'false').");
        }

        return newValue;
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter fails to
     * pass custom validation, then HTTP 400 (Bad Request) will be thrown to inform the user
     * that they need to fix their call.
     * @param paramName The name of the API parameter being evaluated.  This is purely used to attach a sensible
     *                  error message to the error.
     * @param value The value being validated.  Will be returned to the user for debugging purposes.
     * @param validIf Whether the parameter value has passed validation (typically this is a simple inline check).
     * @return For convenience, the converted boolean will be returned, meaning that higher-level functions can
     *  validate and convert in a single function call.
     * @throws HttpProcessingException This function throws HTTP 400 (Bad Request) if the value is not a valid boolean.
     */
    protected void Throw400IfValidationFails(String paramName, String value, boolean validIf, String errorMsg) throws HttpProcessingException {
        if(StringUtils.isEmpty(value)) {
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' cannot be blank.");
        }

        if(validIf)
            return;

        if(errorMsg != null && !errorMsg.isEmpty())
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' value '" + value + "' failed validation: " + errorMsg);
        else
            throw new HttpProcessingException(400, "Parameter '" + paramName + "' value not recognized: '" + value + "'");
    }

    protected void Throw400IfValidationFails(String paramName, String value, boolean validIf) throws HttpProcessingException {
        Throw400IfValidationFails(paramName, value, validIf, null);
    }

    /**
     * This function is for validating incoming parameters on POST requests.  If the passed parameter is missing or is
     * not a representation of a valid boolean variable, then it will default to the given value.
     * @param paramName The name of the API parameter being evaluated.  This is ignored and is only kept for consistency
     *                  with other similar APIs.
     * @param value The value being validated.  Should be some representation of "true" or "false".
     * @return For convenience, the converted boolean will be returned, meaning that higher-level functions can
     *  validate and convert in a single function call.
     */
    protected boolean ParseBoolean(String paramName, String value, boolean Default) throws HttpProcessingException {
        if(value.isBlank()) {
            return Default;
        }

        return Boolean.parseBoolean(value.toLowerCase().trim());
    }

    /**
     * Verifies the request is from a full admin user and nothing less.
     * @param request the HTTP Request sent from communication.js
     * @throws HttpProcessingException This function throws HTTP 403 (Forbidden) if the user is not a full admin.
     */
    protected void validateAdmin(HttpRequest request) throws HttpProcessingException {
        Player player = getResourceOwnerSafely(request);

        if (!player.hasType(Player.Type.ADMIN))
            throw new HttpProcessingException(403);
    }

    /**
     * Verifies the request is from an admin (or league admin) user.
     * @param request the HTTP Request sent from communication.js
     * @throws HttpProcessingException This function throws HTTP 403 (Forbidden) if the user is not a league admin.
     */
    protected void validateEventAdmin(HttpRequest request) throws HttpProcessingException {
        Player player = getResourceOwnerSafely(request);

        if (!player.hasType(Player.Type.ADMIN) && !player.hasType(Player.Type.LEAGUE_ADMIN))
            throw new HttpProcessingException(403);
    }
}
