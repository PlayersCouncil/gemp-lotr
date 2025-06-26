package com.gempukku.lotro.bots.rl.semanticaction;

import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class IntegerChoiceAction implements SemanticAction {
    private final int value;

    public IntegerChoiceAction(int value) {
        this.value = value;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        return Integer.toString(value);
    }
}
