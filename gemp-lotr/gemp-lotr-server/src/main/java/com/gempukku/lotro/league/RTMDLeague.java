package com.gempukku.lotro.league;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.util.JsonUtils;

import java.util.Collections;
import java.util.List;

/**
 * A league representing the Race to Mount Doom league type.  This is based on standard Constructed league play,
 * but players advance via wins along a meta path, with those meta sites adding additional card effects
 * which increase the difficulty.
 */
public class RTMDLeague extends ConstructedLeague {
    public enum AdvanceType {
        WIN,
        SCORE;
    }
    private final LeagueParams _parameters;

    public RTMDLeague(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, LeagueParams parameters) {
        super(productLibrary, formatLibrary, parameters);
        _parameters = parameters;
    }

    public static RTMDLeague fromRawParameters(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, String parameters) {
        var parsedParams = JsonUtils.Convert(parameters, LeagueParams.class);
        if (parsedParams == null)
            throw new RuntimeException("Unable to parse raw parameters for RTMD league: " + parameters);
        return new RTMDLeague(productLibrary, formatLibrary, parsedParams);
    }

    // ---- RTMD-specific methods (accessed via cast from game creation pipeline) ----

    /**
     * Returns the ordered list of modifier blueprint IDs defining the race path.
     */
    public List<String> getPath() {
        return Collections.unmodifiableList(_parameters.racePath);
    }

    /**
     * Returns the ordered list of visual position card blueprint IDs (set 90), parallel to racePath.
     */
    public List<String> getVisualPath() {
        return Collections.unmodifiableList(_parameters.raceVisualPath);
    }

    /**
     * Returns the number of sites on the path (1-18).
     */
    public int getPathLength() {
        return _parameters.racePath.size();
    }

    /**
     * Whether all meta-sites from position 1 through the player's current position are active,
     * or just the current one.
     */
    public boolean isCumulative() {
        return _parameters.raceCumulative;
    }

    public int getIntensityFloor() { return _parameters.raceIntensityFloor; }

    public int getIntensityCeiling() { return _parameters.raceIntensityCeiling; }
    public int getAdvanceFactor() { return _parameters.raceAdvanceFactor; }

    /**
     * Derives a player's current path position from their win count in the league standings.
     * Position is 1-indexed: position 1 means 0 wins, position N means N-1 wins.
     *
     * @param playerName the player whose position to look up
     * @param standings the current league standings (as produced by LeagueService)
     * @return the player's current path position (1 through pathLength), or 1 if not found
     */
    public int getPlayerPosition(String playerName, List<PlayerStanding> standings) {
        for (PlayerStanding standing : standings) {
            if (standing.playerName.equals(playerName)) {
                int amount = 0;
                switch (_parameters.raceAdvancementMode) {
					case WIN -> amount = getWinsFromStanding(standing);
					case SCORE -> amount = standing.points;
				}
                return getPositionForProgress(amount);
            }
        }
        // Player not found in standings (e.g., just joined, no games yet)
        return 1;
    }

    /**
     * Returns the meta-site blueprint ID(s) that should be active for a player at the given position.
     * For singular mode, returns a single-element list. For cumulative, returns positions 1 through current.
     */
    public List<String> getMetaSitesForPosition(int position) {
        if (position < 1 || position > getPathLength())
            throw new IllegalArgumentException("Position " + position + " is out of range for path of length " + getPathLength());

        if (_parameters.raceCumulative) {
            return Collections.unmodifiableList(_parameters.racePath.subList(0, position));
        } else {
            return List.of(_parameters.racePath.get(position - 1));
        }
    }

    /**
     * Returns the visual position card blueprint ID(s) for the given position.
     * Parallel to getMetaSitesForPosition — same index, same cumulative logic.
     */
    public List<String> getVisualCardsForPosition(int position) {
        if (position < 1 || position > getPathLength())
            throw new IllegalArgumentException("Position " + position + " is out of range for path of length " + getPathLength());

        if (_parameters.raceCumulative) {
            return Collections.unmodifiableList(_parameters.raceVisualPath.subList(0, position));
        } else {
            return List.of(_parameters.raceVisualPath.get(position - 1));
        }
    }

    /**
     * Convenience method combining position lookup and meta-site resolution.
     */
    public List<String> getPlayerMetaSites(String playerName, List<PlayerStanding> standings) {
        int position = getPlayerPosition(playerName, standings);
        return getMetaSitesForPosition(position);
    }

    /**
     * Extracts win count from a PlayerStanding.
     * BestOfOneStandingsProducer uses 2 points for a win, 1 for a loss.
     * So: wins * 2 + losses * 1 = points, and wins + losses = gamesPlayed.
     * Therefore: wins = points - gamesPlayed.
     */
    private int getWinsFromStanding(PlayerStanding standing) {
        return standing.points - standing.gamesPlayed;
    }

    /**
     * Converts a point count to a path position.
     * Position 1 = 0 wins/score (starting position).
     * Each win/score threshold advances one position, capped at path length.
     */
    private int getPositionForProgress(int wins) {
        return Math.min((wins / getAdvanceFactor()) + 1, getPathLength());
    }
}
