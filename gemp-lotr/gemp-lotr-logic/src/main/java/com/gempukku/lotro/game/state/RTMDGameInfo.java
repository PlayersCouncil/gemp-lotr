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

    private final Map<String, List<MetaSitePair>> _playerMetaSites;

    /**
     * @param playerMetaSites map of playerId to their active meta-site pairs (visual + modifier),
     *                        ordered by path position
     */
    public RTMDGameInfo(Map<String, List<MetaSitePair>> playerMetaSites) {
        _playerMetaSites = Map.copyOf(playerMetaSites);
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
