package com.gempukku.lotro.hall;

import com.gempukku.lotro.db.PlayerDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The player search behind the Casual table's invite picker (GET /hall/players): registered player names starting
 * with what was typed, for any logged-in player.  Names only; at least {@link #MIN_PREFIX} characters, at most
 * {@link #MAX_RESULTS} names, and never the player who is searching.
 */
public final class PlayerNameSearch {
    public static final int MIN_PREFIX = 2;
    public static final int MAX_RESULTS = 10;

    private PlayerNameSearch() {
    }

    /** {@code requested} clamped to 1..{@link #MAX_RESULTS}; anything unreadable gives the maximum. */
    public static int clampLimit(String requested) {
        if (requested == null || requested.isBlank())
            return MAX_RESULTS;
        try {
            return Math.max(1, Math.min(MAX_RESULTS, Integer.parseInt(requested.trim())));
        } catch (NumberFormatException exp) {
            return MAX_RESULTS;
        }
    }

    /**
     * Names starting with {@code prefix} (ignoring case): an exact match first, then shorter names, then
     * alphabetically, as {@link PlayerDAO#findPlayerNames} orders them.  Empty for a prefix shorter than
     * {@link #MIN_PREFIX}.
     */
    public static List<String> byPrefix(PlayerDAO playerDao, String prefix, String searcher, int limit) {
        List<String> result = new ArrayList<>();
        String typed = prefix == null ? "" : prefix.trim();
        if (typed.length() < MIN_PREFIX)
            return result;
        limit = Math.max(1, Math.min(MAX_RESULTS, limit));
        String folded = typed.toLowerCase(Locale.ROOT);
        // findPlayerNames matches anywhere in the name but lists prefix matches before the rest, so the first
        // limit + 1 rows hold every prefix match that can make the cut (one more, in case one is the searcher).
        List<String> candidates = playerDao.findPlayerNames(typed, limit + 1);
        if (candidates == null)
            return result;
        for (String name : candidates) {
            if (name == null || !name.toLowerCase(Locale.ROOT).startsWith(folded))
                continue;
            if (searcher != null && name.equalsIgnoreCase(searcher))
                continue;
            if (result.contains(name))
                continue;
            result.add(name);
            if (result.size() >= limit)
                break;
        }
        return result;
    }
}
