package com.gempukku.lotro.game.state;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Extra game info for Race to Mount Doom leagues.
 * Carries per-player meta-site pairs (visual position card + modifier card)
 * resolved upstream from league standings before the game starts.
 */
public class RTMDGameInfo extends GameExtraInfo {

    /**
     * A paired visual position card and modifier card.
     */
    public record MetaSitePair(String visualBlueprintId, String modifierBlueprintId) {}

    /**
     * Where a player stood in the league standings when this game was created: a 1-based rank
     * (ties share a rank) out of the number of players in those standings.  Resolved upstream;
     * see LeagueService.buildRTMDGameInfo in the server module for how it is populated.
     */
    public record LeaguePlacement(int rank, int participants) {

        /**
         * True if this placement is inside the top {@code percentage}% of the league.  The cutoff
         * is rounded up and is never smaller than one place, so the leader of a small league always
         * qualifies.
         */
        public boolean isWithinTopPercentage(int percentage) {
            if (rank < 1 || participants < 1 || percentage <= 0)
                return false;

            int cutoff = Math.max(1, (int) Math.ceil(participants * percentage / 100.0));
            return rank <= cutoff;
        }
    }

    private final Map<String, List<MetaSitePair>> _playerMetaSites;
    private final Map<String, LeaguePlacement> _playerPlacements;

    /**
     * @param playerMetaSites map of playerId to their active meta-site pairs (visual + modifier),
     *                        ordered by path position
     */
    public RTMDGameInfo(Map<String, List<MetaSitePair>> playerMetaSites) {
        this(playerMetaSites, Collections.emptyMap());
    }

    /**
     * @param playerMetaSites map of playerId to their active meta-site pairs (visual + modifier),
     *                        ordered by path position
     * @param playerPlacements map of playerId to their placement in the current league standings;
     *                         players absent from the map have no placement
     */
    public RTMDGameInfo(Map<String, List<MetaSitePair>> playerMetaSites, Map<String, LeaguePlacement> playerPlacements) {
        _playerMetaSites = Map.copyOf(playerMetaSites);
        _playerPlacements = (playerPlacements == null) ? Collections.emptyMap() : Map.copyOf(playerPlacements);
    }

    /**
     * Returns the given player's placement in the current league standings, or null if they have none.
     */
    public LeaguePlacement getLeaguePlacement(String playerId) {
        return _playerPlacements.get(playerId);
    }

    /**
     * Returns the meta-site pairs for the given player, or an empty list if not found.
     */
    public List<MetaSitePair> getMetaSites(String playerId) {
        return _playerMetaSites.getOrDefault(playerId, Collections.emptyList());
    }

    /**
     * Returns all player meta-site data.
     */
    public Map<String, List<MetaSitePair>> getAllMetaSites() {
        return _playerMetaSites;
    }
}
