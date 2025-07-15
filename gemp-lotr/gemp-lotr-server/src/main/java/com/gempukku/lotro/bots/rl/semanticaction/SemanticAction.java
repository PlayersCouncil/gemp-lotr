package com.gempukku.lotro.bots.rl.semanticaction;

import com.gempukku.util.JsonSerializable;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface SemanticAction extends JsonSerializable {
    // Converts this semantic action into a concrete action string for the given decision and game state
    String toDecisionString(AwaitingDecision decision, GameState gameState);
}
