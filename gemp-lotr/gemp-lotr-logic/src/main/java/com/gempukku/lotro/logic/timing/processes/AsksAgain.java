package com.gempukku.lotro.logic.timing.processes;

import com.gempukku.lotro.game.state.LotroGame;

/**
 * A game process that asks its player a decision directly (not from inside an action on the stack), and can ask
 * that player the same thing again from scratch: what is offered worked out afresh against the board as it now
 * stands.  Nothing in an ordinary game uses this.  A scenario's sandbox does: the person stops the game at such a
 * decision, changes the board by hand, and starts it again, and the decision that was waiting has to be asked again
 * of the board as it now is (TurnProcedure.askAgain).
 * <p>
 * Asking again is not answering: no choice or pass is made by it, no loop step is told anything (see each
 * implementation), and the process carries on from it as from its first asking.  If the player now has nothing to
 * do and would be passed through the phase, the process passes for them, exactly as its first asking would have.
 */
public interface AsksAgain {
    /**
     * @return Whether the decision this process last put to its player is still waiting for its answer.
     */
    boolean isAsking();

    /**
     * @return The player that decision was put to.
     */
    String askedPlayer();

    /**
     * Puts the decision to the same player again, worked out afresh.  To be called only once the waiting decision
     * has been withdrawn (TurnProcedure.askAgain does both).
     * @param game The game.
     */
    void askAgain(LotroGame game);
}
