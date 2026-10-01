package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.Player;

import java.time.ZonedDateTime;
import java.util.List;

public interface GameHistoryDAO {
    public int addGameHistory(String winner, int winnerId, String loser, int loserId, String winReason, String loseReason, String winRecordingId, String loseRecordingId, String formatName, String tournament, String winnerDeckName, String loserDeckName, ZonedDateTime startDate, ZonedDateTime endDate, int version);
    public DBDefs.GameHistory getGameHistory(String recordID);
    public boolean doesReplayIDExist(String id);
    public List<DBDefs.GameHistory> getGameHistoryForPlayer(Player player, int start, int count);
    public int getGameHistoryForPlayerCount(Player player);

    public List<DBDefs.GameHistory> getGamesForTournament(String tournamentName);

    public int getActivePlayersCount(ZonedDateTime from, ZonedDateTime to);

    public int getGamesPlayedCount(ZonedDateTime from, ZonedDateTime to);

    /** Games in the period with a bot on either side (bot accounts' names start with "~"). */
    default int getBotGamesPlayedCount(ZonedDateTime from, ZonedDateTime to) {
        throw new UnsupportedOperationException("getBotGamesPlayedCount");
    }

    public List<DBDefs.FormatStats> GetAllGameFormatData(ZonedDateTime from, ZonedDateTime to);

    public List<PlayerStatistic> getCasualPlayerStatistics(Player player);

    public List<PlayerStatistic> getCompetitivePlayerStatistics(Player player);

    List<DBDefs.GameHistory> getLastGames(String formatName, int count);

    // ---- tabs-account: filtered, paged game history (My Account > Game History) ----

    /** One page of the player's games matching the filter, newest first. */
    List<DBDefs.GameHistory> getGameHistoryForPlayer(Player player, GameHistoryFilter filter, int start, int count);

    /** How many of the player's games match the filter. */
    int getGameHistoryForPlayerCount(Player player, GameHistoryFilter filter);

    /** Formats the player has games in, most played first. */
    List<String> getGameHistoryFormats(Player player);

    /** Leagues and tournaments (not casual) the player has games in, most recent first, at most limit of them. */
    List<String> getGameHistoryEvents(Player player, int limit);
}
