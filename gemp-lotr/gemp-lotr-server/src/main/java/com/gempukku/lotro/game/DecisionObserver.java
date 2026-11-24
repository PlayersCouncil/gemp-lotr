package com.gempukku.lotro.game;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

/**
 * Told about every decision a game puts to its players, and every answer the game accepts.  Between them those are
 * everything that steers a game from outside, so this is the place to keep a record of a game, or to follow one.
 *
 * Both calls are made by whoever is running the game, on the thread that is running it, with the game paused.
 */
public interface DecisionObserver {
    /**
     * A decision is about to be put to a player.  Nobody has been told yet: not the player, not anybody watching.
     */
    void decisionIssued(String playerId, AwaitingDecision decision);

    /**
     * A player's answer is about to be given to the decision they were asked.  The decision may yet refuse it, in
     * which case decisionAnswered is not called and the decision is asked again.  Whatever the decision does on
     * taking the answer (and some do a good deal: a mulligan shuffles and deals) happens after this and before
     * decisionAnswered, so this is the last moment at which the game is as it was when the decision was waiting.
     */
    default void decisionAnswering(String playerId, AwaitingDecision decision) { }

    /**
     * The game has accepted an answer.  It has not yet carried on from it.
     */
    void decisionAnswered(String playerId, AwaitingDecision decision, String answer);

    /**
     * A decision that was put to a player (or was waiting to be) has been taken back unanswered: the game will ask
     * something afresh in its place (TurnProcedure.askAgain, for a scenario's sandbox).  Nothing in an ordinary game
     * does this.  Nothing is written in a journal for it: whatever had the decision taken back writes its own note.
     */
    default void decisionWithdrawn(String playerId, AwaitingDecision decision) { }
}
