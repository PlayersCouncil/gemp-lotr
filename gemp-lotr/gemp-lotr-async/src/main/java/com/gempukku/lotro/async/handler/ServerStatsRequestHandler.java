package com.gempukku.lotro.async.handler;

import com.alibaba.fastjson.JSON;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.game.Player;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TimeZone;

public class ServerStatsRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final GameHistoryService _gameHistoryService;

    private static final Logger _log = LogManager.getLogger(ServerStatsRequestHandler.class);

    public ServerStatsRequestHandler(Map<Type, Object> context) {
        super(context);

        _gameHistoryService = extractObject(context, GameHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String participantId = getQueryParameterSafely(queryDecoder, "participantId");
            String startDay = getQueryParameterSafely(queryDecoder, "startDay");
            String length = getQueryParameterSafely(queryDecoder, "length");

            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));

                //This convoluted conversion is actually necessary, for it to be flexible enough to take
                //human-level dates such as 2023-2-13 (note the lack of zero padding)
                var from = ZonedDateTime.ofInstant(format.parse(startDay).toInstant(), ZoneOffset.UTC);

                ZonedDateTime to = from;

                switch (length) {
                    case "month" -> to = from.plusMonths(1);
                    case "week" -> to = from.plusDays(7);
                    case "day" -> to = from.plusDays(1);
                    default -> throw new HttpProcessingException(400);
                }

                var stats = new JSONDefs.PlayHistoryStats();
                stats.ActivePlayers = _gameHistoryService.getActivePlayersCount(from, to);
                stats.GamesCount = _gameHistoryService.getGamesPlayedCount(from, to);
                stats.StartDate = from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.EndDate = to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.Stats = _gameHistoryService.getGameHistoryStatistics(from, to);

                responseWriter.writeJsonResponse(JSON.toJSONString(stats));
            } catch (Exception exp) {
                logHttpError(_log, 400, request.uri(), exp);
                throw new HttpProcessingException(400);
            }
        } else {
            throw new HttpProcessingException(404);
        }
    }
}
