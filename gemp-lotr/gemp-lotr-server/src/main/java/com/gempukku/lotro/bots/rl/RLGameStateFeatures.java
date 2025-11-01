package com.gempukku.lotro.bots.rl;

import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface RLGameStateFeatures {
    // Extracts a flat feature vector encoding the current game state and decision context
    double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerId);
}
