package com.gempukku.lotro.game;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;
import java.util.Map;

/**
 * Decides, each time a game comes to rest, which of the decisions it is waiting on are put to their players now.
 *
 * A game with no arbiter puts every decision to its player the moment it is asked.  A game with one holds them back
 * until the game has stopped working, and then lets the arbiter look at everything that is waiting at once.  That
 * matters when a game asks for more than one thing before it stops (both players at the same time, or a second
 * question asked from inside the answering of the first): something that follows a game from a script can then work
 * out which question the script is expecting, instead of having to guess from whichever happened to be asked first.
 */
public interface DecisionArbiter {
    /**
     * @param waiting The decisions that are waiting and have not been put to their players, by player, in the order
     *                they were asked.  Never empty.
     * @return The players whose decisions are to be put to them now, in the order to do it.  A decision that is not
     * released is offered again the next time the game comes to rest.
     */
    List<String> release(Map<String, AwaitingDecision> waiting);
}
