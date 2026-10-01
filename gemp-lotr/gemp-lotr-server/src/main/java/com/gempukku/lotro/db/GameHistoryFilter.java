package com.gempukku.lotro.db;

import java.time.LocalDate;

/**
 * Optional filters on a player's game history (My Account > Game History).  Every field may be null, meaning "any".
 *
 * @param format        exact format name as stored in game_history.format_name
 * @param opponent      opponent name, matched case-insensitively: as a prefix, or exactly when opponentExact is set
 * @param opponentExact match the opponent name exactly (still ignoring case) instead of as a prefix
 * @param event         {@link #CASUAL_EVENT} for casual games (no event, or a "Casual ..." pseudo-event), otherwise
 *                      the start of a league or tournament name, matched case-insensitively
 * @param from          first day (server time, UTC) on which the game ended, inclusive
 * @param to            last day (server time, UTC) on which the game ended, inclusive
 */
public record GameHistoryFilter(String format, String opponent, boolean opponentExact, String event,
                                LocalDate from, LocalDate to) {
    /** The event filter value that selects casual games. */
    public static final String CASUAL_EVENT = "casual";
    /** Longest text accepted for the format, opponent and event filters. */
    public static final int MAX_TEXT_LENGTH = 255;

    public static final GameHistoryFilter NONE = new GameHistoryFilter(null, null, false, null, null, null);

    public GameHistoryFilter {
        format = clean(format);
        opponent = clean(opponent);
        event = clean(event);
    }

    private static String clean(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public boolean isEmpty() {
        return format == null && opponent == null && event == null && from == null && to == null;
    }

    public boolean casualOnly() {
        return event != null && event.equalsIgnoreCase(CASUAL_EVENT);
    }

    /** @return null when usable, otherwise why not (for a 400 response) */
    public String validate() {
        if (tooLong(format) || tooLong(opponent) || tooLong(event))
            return "Filter text is too long.";
        if (from != null && to != null && from.isAfter(to))
            return "The 'from' date is after the 'to' date.";
        return null;
    }

    private static boolean tooLong(String value) {
        return value != null && value.length() > MAX_TEXT_LENGTH;
    }
}
