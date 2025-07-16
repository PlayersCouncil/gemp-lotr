package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseFromArbitraryCardsAction implements SemanticAction {
    private final List<String> chosenBlueprintIds = new ArrayList<>();
    private final List<String> notChosenBlueprintIds = new ArrayList<>();

    public ChooseFromArbitraryCardsAction(String answer, AwaitingDecision awaitingDecision) {
        String[] individualCards = answer.split(",");
        List<String> usedCards = new ArrayList<>();
        String[] blueprintIds = awaitingDecision.getDecisionParameters().get("blueprintId");
        List<String> selectableIds = new ArrayList<>();
        List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
        List<String> selectable = Arrays.stream(awaitingDecision.getDecisionParameters().get("selectable")).toList();

        for (int i = 0; i < cardIds.size() && i < selectable.size(); i++) {
            if (Boolean.parseBoolean(selectable.get(i))) {
                selectableIds.add(cardIds.get(i));
            }
        }

        for (int i = 0; i < cardIds.size(); i++) {
            boolean chosen = false;
            for (String individualCard : individualCards) {
                if (cardIds.get(i).equals(individualCard) && !usedCards.contains(individualCard)) {
                    chosenBlueprintIds.add(blueprintIds[i]);
                    usedCards.add(individualCard);
                    chosen = true;
                }
            }
            if (!chosen && selectableIds.contains(cardIds.get(i))) {
                notChosenBlueprintIds.add(blueprintIds[i]);
            }
        }
    }

    public ChooseFromArbitraryCardsAction(String[] chosenBlueprintIds, String[] notChosenBlueprintIds) {
        this.chosenBlueprintIds.addAll(Arrays.asList(chosenBlueprintIds));
        this.notChosenBlueprintIds.addAll(Arrays.asList(notChosenBlueprintIds));
    }

    public List<String> getChosenBlueprintIds() {
        return chosenBlueprintIds;
    }

    public List<String> getNotChosenBlueprintIds() {
        return notChosenBlueprintIds;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS) {
            throw new IllegalArgumentException("Wrong decision type.");
        }
        String[] blueprintIds = decision.getDecisionParameters().get("blueprintId");
        String[] cardIds = decision.getDecisionParameters().get("cardId");
        List<String> usedCards = new ArrayList<>();


        List<String> picked = new ArrayList<>();
        for (String chosenBlueprintId : chosenBlueprintIds) {
            for (int i = 0; i < blueprintIds.length; i++) {
                if (blueprintIds[i].equals(chosenBlueprintId) && !usedCards.contains(cardIds[i])) {
                    picked.add(cardIds[i]);
                    usedCards.add(cardIds[i]);
                    break;
                }
            }
        }

        return String.join(",", picked);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "ArbitraryCardsAction");
        obj.put("chosenBlueprints", chosenBlueprintIds.toArray());
        obj.put("notChosenBlueprints", notChosenBlueprintIds.toArray());
        return obj;
    }

    public static ChooseFromArbitraryCardsAction fromJson(JSONObject obj) {
        return new ChooseFromArbitraryCardsAction(obj.getObject("chosenBlueprints", String[].class), obj.getObject("notChosenBlueprints", String[].class));
    }
}
