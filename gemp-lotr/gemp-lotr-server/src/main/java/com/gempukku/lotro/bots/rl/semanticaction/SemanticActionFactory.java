package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;

public class SemanticActionFactory {
    public static SemanticAction fromJson(JSONObject obj) {
        String type = obj.getString("type");
        return switch (type) {
            case "MultipleChoiceAction" -> MultipleChoiceAction.fromJson(obj);
            case "IntegerChoiceAction" -> IntegerChoiceAction.fromJson(obj);
            case "CardSelectionAssignedAction" -> CardSelectionAssignedAction.fromJson(obj);
            case "CardSelectionAction" -> CardSelectionAction.fromJson(obj);
            case "ArbitraryCardsAction" -> ChooseFromArbitraryCardsAction.fromJson(obj);
            // add others later
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }
}
