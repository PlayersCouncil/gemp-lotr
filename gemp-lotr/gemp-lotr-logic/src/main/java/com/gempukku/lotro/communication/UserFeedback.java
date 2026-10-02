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
}
