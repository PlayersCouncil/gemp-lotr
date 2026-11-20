package com.gempukku.lotro.game;

import java.util.List;

/**
 * What taking a game back to an earlier point would undo, from the point of view of the player asking.
 *
 * @param tier How much it asks of the other players (see Tier).
 * @param reasons Why it is that tier and not a lower one, in words for the players; empty for GREEN.
 * @param barrier Set if the game cannot be taken back that far at all, whoever agrees: says what stands in the way.
 */
public record RevertAssessment(Tier tier, List<String> reasons, String barrier) {
    public enum Tier {
        /** Only the asking player's own choices are undone.  Nobody else has done or learned anything. */
        GREEN,
        /** Nobody has learned anything, but another player has answered something, or chance has been used. */
        YELLOW,
        /** The asking player has seen something they had not seen: a card drawn, a card another player showed. */
        RED
    }

    public boolean possible() {
        return barrier == null;
    }
}
