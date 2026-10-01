package com.gempukku.lotro.game;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.GameHistoryDAO;
import com.gempukku.lotro.db.GameHistoryFilter;
import com.gempukku.lotro.db.PlayerStatistic;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameHistoryService {
    private final GameHistoryDAO _gameHistoryDAO;
    private final Map<String, Integer> _playerGameCount = new ConcurrentHashMap<>();

    public GameHistoryService(GameHistoryDAO gameHistoryDAO) {
        _gameHistoryDAO = gameHistoryDAO;
    }

    public int addGameHistory(DBDefs.GameHistory gh) {
        return addGameHistory(gh.winner, gh.winnerId, gh.loser, gh.loserId, gh.win_reason, gh.lose_reason, gh.win_recording_id, gh.lose_recording_id,
                gh.format_name, gh.tournament, gh.winner_deck_name, gh.loser_deck_name, gh.GetUTCStartDate(), gh.GetUTCEndDate(), gh.replay_version);
    }

    public int addGameHistory(String winner, int winnerId, String loser, int loserId, String winReason, String loseReason, String winRecordingId, String loseRecordingId, String formatName, String tournament, String winnerDeckName, String loserDeckName, ZonedDateTime startDate, ZonedDateTime endDate, int version) {
        int id = _gameHistoryDAO.addGameHistory(winner, winnerId, loser, loserId, winReason, loseReason, winRecordingId, loseRecordingId, formatName, tournament, winnerDeckName, loserDeckName, startDate, endDate, version);
        Integer winnerCount = _playerGameCount.get(winner);
        Integer loserCount = _playerGameCount.get(loser);
        if (winnerCount != null)
            _playerGameCount.put(winner, winnerCount + 1);
        if (loserCount != null)
            _playerGameCount.put(loser, loserCount + 1);

        return id;
    }

    public boolean doesReplayIDExist(String id) {
        return _gameHistoryDAO.doesReplayIDExist(id);
    }

    public DBDefs.GameHistory getGameHistory(String recordID) {
        return _gameHistoryDAO.getGameHistory(recordID);
    }

    public int getGameHistoryForPlayerCount(Player player) {
        Integer result = _playerGameCount.get(player.getName());
        if (result != null)
            return result;
        int count = _gameHistoryDAO.getGameHistoryForPlayerCount(player);
        _playerGameCount.put(player.getName(), count);
        return count;
    }

    public List<DBDefs.GameHistory> getGameHistoryForPlayer(Player player, int start, int count) {
        return _gameHistoryDAO.getGameHistoryForPlayer(player, start, count);
    }

    public int getActivePlayersCount(ZonedDateTime from, ZonedDateTime duration) {
        return _gameHistoryDAO.getActivePlayersCount(from, duration);
    }

    public int getGamesPlayedCount(ZonedDateTime from, ZonedDateTime duration) {
        return _gameHistoryDAO.getGamesPlayedCount(from, duration);
    }

    /** games with a bot on either side; they are also in getGamesPlayedCount */
    public int getBotGamesPlayedCount(ZonedDateTime from, ZonedDateTime to) {
        return _gameHistoryDAO.getBotGamesPlayedCount(from, to);
    }

    public List<DBDefs.FormatStats> getGameHistoryStatistics(ZonedDateTime from, ZonedDateTime to) {
        return _gameHistoryDAO.GetAllGameFormatData(from, to);
    }

    public List<PlayerStatistic> getCasualPlayerStatistics(Player player) {
        return _gameHistoryDAO.getCasualPlayerStatistics(player);
    }

    public List<PlayerStatistic> getCompetitivePlayerStatistics(Player player) {
        return _gameHistoryDAO.getCompetitivePlayerStatistics(player);
    }

    // ---- tabs-account: filtered, paged game history (My Account > Game History) ----

    /** Largest page the game history endpoint hands out, whatever the client asks for. */
    public static final int MAX_HISTORY_PAGE_SIZE = 100;
    public static final int DEFAULT_HISTORY_PAGE_SIZE = 20;
    /** At most this many leagues / tournaments are offered as event filter suggestions. */
    public static final int MAX_EVENT_OPTIONS = 100;

    /**
     * One page of a player's history.
     * @param matching  games matching the filter (what the pager counts)
     * @param total     all of the player's games
     * @param start     offset of the first entry, after clamping
     * @param count     page size used, after clamping
     */
    public record HistoryPage(List<DBDefs.GameHistory> entries, int matching, int total, int start, int count) {
    }

    /**
     * The page of the player's games matching the filter.  The page size is clamped to 1..{@link #MAX_HISTORY_PAGE_SIZE}
     * and the start to 0 or more; with no filter the (cached) total doubles as the matching count.
     */
    public HistoryPage getGameHistoryPage(Player player, GameHistoryFilter filter, int start, int count) {
        if (filter == null)
            filter = GameHistoryFilter.NONE;
        int pageSize = Math.max(1, Math.min(MAX_HISTORY_PAGE_SIZE, count));
        int offset = Math.max(0, start);

        int total = getGameHistoryForPlayerCount(player);
        int matching = filter.isEmpty() ? total : _gameHistoryDAO.getGameHistoryForPlayerCount(player, filter);
        List<DBDefs.GameHistory> entries = (matching > 0 && offset < matching) || total < 0
                ? _gameHistoryDAO.getGameHistoryForPlayer(player, filter, offset, pageSize)
                : List.of();
        return new HistoryPage(entries, matching, total, offset, pageSize);
    }

    /** Formats the player has games in, most played first. */
    public List<String> getGameHistoryFormats(Player player) {
        return _gameHistoryDAO.getGameHistoryFormats(player);
    }

    /** Leagues and tournaments the player has games in, most recent first (at most {@link #MAX_EVENT_OPTIONS}). */
    public List<String> getGameHistoryEvents(Player player) {
        return _gameHistoryDAO.getGameHistoryEvents(player, MAX_EVENT_OPTIONS);
    }
}
