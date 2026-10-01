package com.gempukku.lotro.league;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.state.RTMDGameInfo;

import java.util.List;

/**
 * Out-of-game facts about a player that Race to Mount Doom meta-site cards can refer to, resolved
 * from the league standings as they stand when a game is created.  Account names the cards single
 * out are listed in the card definitions themselves, not here.
 */
public final class RTMDTitles {

    private RTMDTitles() {
    }

    /**
     * The player's placement in the given standings, or null if they do not appear in them.
     * Standings ranks are 1-based and tied players share a rank.
     */
    public static RTMDGameInfo.LeaguePlacement placementOf(String playerName, List<PlayerStanding> standings) {
        if (standings == null || standings.isEmpty())
            return null;

        for (PlayerStanding standing : standings) {
            if (standing.playerName.equals(playerName))
                return standing.standing >= 1 ? new RTMDGameInfo.LeaguePlacement(standing.standing, standings.size()) : null;
        }

        return null;
    }
}
