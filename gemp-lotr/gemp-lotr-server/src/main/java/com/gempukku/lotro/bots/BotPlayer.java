package com.gempukku.lotro.bots;

import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface BotPlayer {
    String chooseAction(GameState gameState, AwaitingDecision awaitingDecision);

    /**
     * Same as {@link #chooseAction(GameState, AwaitingDecision)}, with the live game's assignment rules available so
     * the bot can avoid answers the engine would reject. Bots that do not use them fall back to the plain version.
     */
    default String chooseAction(GameState gameState, AwaitingDecision awaitingDecision, AssignmentLegality legality) {
        return chooseAction(gameState, awaitingDecision);
    }

    String getName();
}
