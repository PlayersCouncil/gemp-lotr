package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.util.JsonUtils;
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
import java.util.concurrent.ConcurrentHashMap;

public class ServerStatsRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final GameHistoryService _gameHistoryService;

    private static final Logger _log = LogManager.getLogger(ServerStatsRequestHandler.class);

    // The endpoint is public and each request runs four scans of game_history, so answers are kept for a few minutes
    // per period (a past period never changes; the current one is at most this stale).  Bounded: cleared when full.
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;
    private static final int CACHE_MAX_ENTRIES = 500;
    private record CachedStats(String json, long at) {}
    private final Map<String, CachedStats> _cache = new ConcurrentHashMap<>();

    public ServerStatsRequestHandler(Map<Type, Object> context) {
        super(context);

        _gameHistoryService = extractObject(context, GameHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            // Public, like the rest of Server Info: a visitor who is not logged in can read the server's activity (a
            // hall link such as hall.html#info/stats opens it without asking them to log in).  Only totals per format
            // and period; nothing about any one player.
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String startDay = getQueryParameterSafely(queryDecoder, "startDay");
            String length = getQueryParameterSafely(queryDecoder, "length");

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

                String key = from.toLocalDate() + "/" + length;
                CachedStats cached = _cache.get(key);
                long now = System.currentTimeMillis();
                if (cached != null && now - cached.at() < CACHE_TTL_MS) {
                    responseWriter.writeJsonResponse(cached.json());
                    return;
                }

                var stats = new JSONDefs.PlayHistoryStats();
                stats.ActivePlayers = _gameHistoryService.getActivePlayersCount(from, to);
                stats.GamesCount = _gameHistoryService.getGamesPlayedCount(from, to);
                stats.BotGamesCount = _gameHistoryService.getBotGamesPlayedCount(from, to);
                stats.StartDate = from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.EndDate = to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.Stats = _gameHistoryService.getGameHistoryStatistics(from, to);

                String json = JsonUtils.Serialize(stats);
                if (_cache.size() >= CACHE_MAX_ENTRIES)
                    _cache.clear();
                _cache.put(key, new CachedStats(json, now));
                responseWriter.writeJsonResponse(json);
            } catch (Exception exp) {
                logHttpError(_log, 400, request.uri(), exp);
                throw new HttpProcessingException(400);
            }
        } else {
            throw new HttpProcessingException(404);
        }
    }
}
