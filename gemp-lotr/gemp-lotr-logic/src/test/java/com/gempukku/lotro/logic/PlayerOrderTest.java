package com.gempukku.lotro.logic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests of PlayerOrder by itself: no game is played and no card is on a table.
 *
 * A PlayerOrder is made from the players in the order of their turns, which goes clockwise: Comprehensive Rules
 * 4.2, "turn sequence", "When one player finishes his turn, the next player in clockwise rotation (to his left)
 * takes a turn and so on.  Although the turn order rotates to the left (clockwise), note that many other
 * procedures in the game actually rotate to the right (counter-clockwise)."  The counter-clockwise order is the
 * one GameUtils.getAllPlayers walks from the player whose turn it is, and so the order in which an effect "for
 * each player" reaches the players.  A game of two cannot tell the two directions apart; three names can.
 * (GameUtils.getAllPlayers itself, which takes a game, is read from each player's turn in the server module's
 * GameUtilsAllPlayersTests.)
 */
public class PlayerOrderTest {
    // Every name the order gives, until it gives no more.
    private static List<String> walk(PlayOrder order) {
        List<String> names = new ArrayList<>();
        String next;
        while ((next = order.getNextPlayer()) != null)
            names.add(next);
        return names;
    }

    @Test
    public void TheCounterClockwiseOrderStartsWithThePlayerNamedAndGoesToHisRightNamingEachPlayerOnce() {
        // A has the first turn, B (on A's left) the second and C the third: so A is on B's right, C on A's
        // right and B on C's right.
        var order = new PlayerOrder(List.of("A", "B", "C"));

        // From the middle of the turn order: B, then the player before him, then the one before that, round
        // the end of the list.  In turn order from B it would read [B, C, A]; in seating order, [A, B, C].
        assertEquals(List.of("B", "A", "C"), walk(order.getCounterClockwisePlayOrder("B", false)));
        // From each end of the list.  Every list has the three names once each and then stops.
        assertEquals(List.of("A", "C", "B"), walk(order.getCounterClockwisePlayOrder("A", false)));
        assertEquals(List.of("C", "B", "A"), walk(order.getCounterClockwisePlayOrder("C", false)));

        // The control: the same three in the order of their turns, from the same player.
        assertEquals(List.of("B", "C", "A"), walk(order.getClockwisePlayOrder("B", false)));
    }
}
