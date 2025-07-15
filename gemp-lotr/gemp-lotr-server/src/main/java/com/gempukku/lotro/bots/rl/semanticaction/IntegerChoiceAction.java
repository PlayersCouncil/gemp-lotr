package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class IntegerChoiceAction implements SemanticAction {
    private final int value;

    public IntegerChoiceAction(int value) {
        this.value = value;
    }

    public static IntegerChoiceAction fromJson(JSONObject obj) {
        return new IntegerChoiceAction(obj.getInteger("value"));
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        return Integer.toString(value);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "IntegerChoiceAction");
        obj.put("value", value);
        return obj;
    }
}
