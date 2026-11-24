package com.gempukku.lotro.communication;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface UserFeedback {
    public void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision);

    public boolean hasPendingDecisions();

    /**
     * Called by the game each time it has stopped working: it is now waiting on one or more decisions, or it is
     * over.  Does nothing by default.
     */
    default void atRest() { }

    /**
     * Told of an answer a decision has taken, once it has taken it, by whoever put the decision to its player.  The
     * game's own feedback keeps the answers given while an action of a loop's round is carried out, to give them
     * again when the loop is run (CR 4.2 "loops"; LoopFeedback).  Does nothing by default.
     * @param playerId The player who answered.
     * @param decision The decision.
     * @param answer The answer it took.
     */
    default void decisionAnswered(String playerId, AwaitingDecision decision, String answer) { }

    /**
     * Takes back the decision a player is being asked, which is now not to be answered at all: the game will ask
     * something afresh in its place (TurnProcedure.askAgain).  Nothing in an ordinary game does this.
     * @param playerId The player.
     * @throws UnsupportedOperationException from feedback that cannot (the default).
     */
    default void withdrawAwaitingDecision(String playerId) {
        throw new UnsupportedOperationException("This feedback cannot withdraw a decision");
    }
}
