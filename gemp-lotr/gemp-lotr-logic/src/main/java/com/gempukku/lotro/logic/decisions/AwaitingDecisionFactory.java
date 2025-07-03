package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;

public class AwaitingDecisionFactory {
    public static AwaitingDecision fromJson(JSONObject obj) {
        String type = obj.getString("type");
        return switch (type) {
            case "MultipleChoiceAwaitingDecision" -> MultipleChoiceAwaitingDecision.fromJson(obj);
            case "IntegerAwaitingDecision" -> IntegerAwaitingDecision.fromJson(obj);
            case "CardsSelectionDecision" -> CardsSelectionDecision.fromJson(obj);
            case "ArbitraryCardsSelectionDecision" -> ArbitraryCardsSelectionDecision.fromJson(obj);
            // add others later
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }
}
