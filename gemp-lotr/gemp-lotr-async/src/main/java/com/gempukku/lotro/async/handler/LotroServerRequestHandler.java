package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.DateUtils;
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
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
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

    private boolean isTest() {
        return Boolean.parseBoolean(System.getProperty("test"));
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

    private String getLoggedUser(HttpRequest request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("loggedUser")) {
                    String value = cookie.value();
                    if (value != null) {
                        return _loggedUserHolder.getLoggedUser(value);
                    }
                }
            }
        }
        return null;
    }

    protected final void processDeliveryServiceNotification(HttpRequest request, Map<String, String> headersToAdd) {
        String logged = getLoggedUser(request);
        if (logged != null && _transferDAO.hasUndeliveredPackages(logged))
            headersToAdd.put("Delivery-Service-Package", "true");
    }

    protected final Player getResourceOwnerSafely(HttpRequest request, String participantId) throws HttpProcessingException {
        String loggedUser = getLoggedUser(request);
        if (isTest() && loggedUser == null)
            loggedUser = participantId;

        if (loggedUser == null)
            throw new HttpProcessingException(401);

        Player resourceOwner = _playerDao.getPlayer(loggedUser);

        if (resourceOwner == null)
            throw new HttpProcessingException(401);

        if (resourceOwner.hasType(Player.Type.ADMIN) && participantId != null && !participantId.equals("null") && !participantId.equals("")) {
            resourceOwner = _playerDao.getPlayer(participantId);
            if (resourceOwner == null)
                throw new HttpProcessingException(401);
        }
        return resourceOwner;
    }

    protected final Player getLibrarian() throws HttpProcessingException {
        Player resourceOwner = _playerDao.getPlayer("Librarian");

        if (resourceOwner == null)
            throw new HttpProcessingException(401);

        return resourceOwner;
    }

    protected String getQueryParameterSafely(QueryStringDecoder queryStringDecoder, String parameterName) {
        List<String> parameterValues = queryStringDecoder.parameters().get(parameterName);
        if (parameterValues != null && parameterValues.size() > 0)
            return parameterValues.get(0);
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

    protected Map<String, String> logUserReturningHeaders(String remoteIp, String login) throws SQLException {
        _playerDao.updateLastLoginIp(login, remoteIp);

        String sessionId = _loggedUserHolder.logUser(login);
        return Collections.singletonMap(SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }
}
