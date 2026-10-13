package com.gempukku.lotro.game.state;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests of the arithmetic in RTMDGameInfo.LeaguePlacement: no game is played and no card is on a table.
 *
 * "Placed in the top 10% of the current league" (Race Text 94_5, the one card whose text ends in this arithmetic)
 * is a number of places: the number of players in the standings times the percentage, rounded UP, and so never
 * less than one place.  The leader of a small league always qualifies, and where a tenth of the league is no
 * whole number of places the place the fraction falls in counts as well.
 */
public class RTMDGameInfoTest {
    @Test
    public void TheTopPercentageCutoffIsRoundedUpAndIsNeverLessThanOnePlace() {
        // 10% of 10 players is 1 place exactly, and nothing is rounded.  This is the control: a cutoff that
        // always took one place more than the whole part would let the second of ten in.
        assertTrue(new RTMDGameInfo.LeaguePlacement(1, 10).isWithinTopPercentage(10));
        assertFalse(new RTMDGameInfo.LeaguePlacement(2, 10).isWithinTopPercentage(10));

        // 10% of 4 players is 0.4 of a place: the leader is in all the same, and nobody after him.
        assertTrue(new RTMDGameInfo.LeaguePlacement(1, 4).isWithinTopPercentage(10));
        assertFalse(new RTMDGameInfo.LeaguePlacement(2, 4).isWithinTopPercentage(10));

        // 10% of 25 players is 2.5 places, rounded up to 3.
        assertTrue(new RTMDGameInfo.LeaguePlacement(3, 25).isWithinTopPercentage(10));
        assertFalse(new RTMDGameInfo.LeaguePlacement(4, 25).isWithinTopPercentage(10));

        // 10% of 11 players is 1.1 places, rounded up to 2: rounded to the nearest place it would be 1, which
        // the 2.5 of 25 players (3 either way) cannot tell from rounding up.
        assertTrue(new RTMDGameInfo.LeaguePlacement(2, 11).isWithinTopPercentage(10));
        assertFalse(new RTMDGameInfo.LeaguePlacement(3, 11).isWithinTopPercentage(10));

        // The percentage asked for is the one used: 30% of 11 players is 3.3 places, rounded up to 4.
        assertTrue(new RTMDGameInfo.LeaguePlacement(4, 11).isWithinTopPercentage(30));
        assertFalse(new RTMDGameInfo.LeaguePlacement(5, 11).isWithinTopPercentage(30));
    }
}
