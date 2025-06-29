package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

public class CardSelectionAction implements SemanticAction {
    private final List<String> chosenBlueprintIds = new ArrayList<>();

    public CardSelectionAction(String answer, GameState gameState) {
        String[] individualCards = answer.split(",");

        for (String individualCard : individualCards) {
            if (!individualCard.isEmpty()) {
                chosenBlueprintIds.add(gameState.getBlueprintId(Integer.parseInt(individualCard)));
            }
        }
    }

    public CardSelectionAction(List<String> getChosenBlueprintIds) {
        this.chosenBlueprintIds.addAll(getChosenBlueprintIds);
    }

    public List<String> getChosenBlueprintIds() {
        return chosenBlueprintIds;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION) {
            throw new IllegalArgumentException("Wrong decision type.");
        }

        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        if (chosenBlueprintIds.size() < min || chosenBlueprintIds.size() > max) {
            throw new IllegalArgumentException("Chosen number out of bounds.");
        }

        if (chosenBlueprintIds.isEmpty()) {
            return "";
        }

        List<String> chosenBlueprintIdsCopy = new ArrayList<>(chosenBlueprintIds);

        List<String> chosenIds = new ArrayList<>();
        for (String cardId : decision.getDecisionParameters().get("cardId")) {
            if (chosenBlueprintIdsCopy.contains(gameState.getBlueprintId(Integer.parseInt(cardId)))) {
                chosenIds.add(cardId);
                chosenBlueprintIdsCopy.remove(gameState.getBlueprintId(Integer.parseInt(cardId)));
            }
        }

        if (chosenBlueprintIdsCopy.isEmpty()) {
            return String.join(",", chosenIds);
        } else {
            throw new IllegalArgumentException("Cards to pick are not present in the decision.");
        }
    }

    @Override
    public JSONObject toJson() {
        return null;
    }
}
