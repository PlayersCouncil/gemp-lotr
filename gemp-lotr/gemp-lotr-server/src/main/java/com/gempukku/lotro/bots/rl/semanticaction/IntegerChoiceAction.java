package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.annotation.JSONType;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

@JSONType(typeName = "IntegerChoiceAction")
public class IntegerChoiceAction implements SemanticAction {
    private final int value;

    public IntegerChoiceAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        return Integer.toString(value);
    }
}
