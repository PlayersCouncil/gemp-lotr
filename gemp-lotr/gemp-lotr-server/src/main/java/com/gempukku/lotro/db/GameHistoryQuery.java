package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DateUtils;

import java.time.LocalDate;
import java.util.*;

/**
 * Builds the SQL for one player's filtered game history.  Every value the player supplies goes in as a named
 * parameter; the SQL text is assembled only from the fixed fragments below, so no filter value can change the
 * statement.  Kept apart from {@link DbGameHistoryDAO} so the statements can be unit-tested without a database.
 * <p>
 * game_history uses a binary (case-sensitive) collation, so the case-insensitive matches compare LOWER(column) with a
 * parameter that is lower-cased here.  LIKE patterns escape the player's own % and _ with '!'.
 */
public final class GameHistoryQuery {
    static final String COLUMNS = "id, winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, "
            + "format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version";
    /** Casual games: no event, or one of the "Casual ..." pseudo-events (the same test My Stats uses). */
    static final String CASUAL_CONDITION = "(tournament IS NULL OR tournament LIKE 'Casual %')";
    static final char LIKE_ESCAPE = '!';

    private final String _where;
    private final Map<String, Object> _parameters;

    private GameHistoryQuery(String where, Map<String, Object> parameters) {
        _where = where;
        _parameters = Collections.unmodifiableMap(parameters);
    }

    public static GameHistoryQuery forPlayer(String playerName, GameHistoryFilter filter) {
        if (filter == null)
            filter = GameHistoryFilter.NONE;
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        params.put("me", playerName);
        conditions.add("(winner = :me OR loser = :me)");

        if (filter.format() != null) {
            params.put("format", filter.format());
            conditions.add("format_name = :format");
        }
        if (filter.opponent() != null) {
            String opponent = filter.opponent().toLowerCase(Locale.ROOT);
            if (filter.opponentExact()) {
                params.put("opponent", opponent);
                conditions.add("((winner = :me AND LOWER(loser) = :opponent) OR (loser = :me AND LOWER(winner) = :opponent))");
            } else {
                params.put("opponent", escapeLike(opponent) + "%");
                conditions.add("((winner = :me AND LOWER(loser) LIKE :opponent ESCAPE '!') OR (loser = :me AND LOWER(winner) LIKE :opponent ESCAPE '!'))");
            }
        }
        if (filter.casualOnly()) {
            conditions.add(CASUAL_CONDITION);
        } else if (filter.event() != null) {
            params.put("event", escapeLike(filter.event().toLowerCase(Locale.ROOT)) + "%");
            conditions.add("LOWER(tournament) LIKE :event ESCAPE '!'");
        }
        if (filter.from() != null) {
            params.put("fromDate", startOfDay(filter.from()));
            conditions.add("end_date >= :fromDate");
        }
        if (filter.to() != null) {
            params.put("toDate", startOfDay(filter.to().plusDays(1)));
            conditions.add("end_date < :toDate");
        }
        return new GameHistoryQuery(String.join(" AND ", conditions), params);
    }

    /** One page, newest first; binds :start and :count in addition to {@link #parameters()}. */
    public String selectSql() {
        return "SELECT " + COLUMNS + " FROM game_history WHERE " + _where
                + " ORDER BY end_date DESC, id DESC LIMIT :start, :count";
    }

    public String countSql() {
        return "SELECT COUNT(*) FROM game_history WHERE " + _where;
    }

    public String where() {
        return _where;
    }

    /** Named parameters for the statements, in the order they were added. */
    public Map<String, Object> parameters() {
        return _parameters;
    }

    /** The formats the player has games in, most played first.  Binds :me. */
    public static String formatsSql() {
        return "SELECT format_name FROM game_history WHERE (winner = :me OR loser = :me) AND format_name IS NOT NULL"
                + " GROUP BY format_name ORDER BY COUNT(*) DESC, format_name";
    }

    /** The leagues and tournaments (not casual) the player has games in, most recent first.  Binds :me and :limit. */
    public static String eventsSql() {
        return "SELECT tournament FROM game_history WHERE (winner = :me OR loser = :me) AND tournament IS NOT NULL"
                + " AND tournament NOT LIKE 'Casual %' GROUP BY tournament ORDER BY MAX(end_date) DESC LIMIT :limit";
    }

    static String escapeLike(String value) {
        StringBuilder result = new StringBuilder(value.length() + 4);
        for (char c : value.toCharArray()) {
            if (c == LIKE_ESCAPE || c == '%' || c == '_')
                result.append(LIKE_ESCAPE);
            result.append(c);
        }
        return result.toString();
    }

    private static String startOfDay(LocalDate day) {
        return day.atStartOfDay().format(DateUtils.DateTimeFormat);
    }
}
