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
     * The game has accepted an answer.  It has not yet carried on from it.
     */
    void decisionAnswered(String playerId, AwaitingDecision decision, String answer);
}
