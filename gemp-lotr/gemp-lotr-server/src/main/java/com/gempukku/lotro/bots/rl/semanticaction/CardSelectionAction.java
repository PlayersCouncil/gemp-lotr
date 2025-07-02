package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardSelectionAction implements SemanticAction {
    protected final List<String> chosenBlueprintIds = new ArrayList<>();
    protected final List<Integer> woundsOnChosen = new ArrayList<>();
    protected final List<String> notChosenBlueprintIds = new ArrayList<>();
    protected final List<Integer> woundsOnNotChosen = new ArrayList<>();

    public CardSelectionAction(String answer, AwaitingDecision decision, GameState gameState) {
        String[] individualCards = answer.split(",");

        for (String individualCard : individualCards) {
            if (!individualCard.isEmpty()) {
                chosenBlueprintIds.add(gameState.getBlueprintId(Integer.parseInt(individualCard)));
                for (PhysicalCard physicalCard : gameState.getAllCards()) {
                    if (physicalCard.getCardId() == Integer.parseInt(individualCard)) {
                        woundsOnChosen.add(gameState.getWounds(physicalCard));
                    }
                }
            }
        }

        if (decision instanceof CardsSelectionDecision csd) {
            List<String> allChoices = Arrays.asList(csd.getDecisionParameters().get("cardId"));
            allChoices.forEach(choice -> {
                if (!chosenBlueprintIds.contains(gameState.getBlueprintId(Integer.parseInt(choice)))) {
                    notChosenBlueprintIds.add(gameState.getBlueprintId(Integer.parseInt(choice)));
                    for (PhysicalCard physicalCard : gameState.getAllCards()) {
                        if (physicalCard.getCardId() == Integer.parseInt(choice)) {
                            woundsOnNotChosen.add(gameState.getWounds(physicalCard));
                        }
                    }
                }
            });
        }

        if (chosenBlueprintIds.size() != woundsOnChosen.size() || notChosenBlueprintIds.size() != woundsOnNotChosen.size()) {
            throw new IllegalArgumentException("Card and wound list have different sizes");
        }
    }

    public CardSelectionAction(String[] chosenBlueprintIds, String[] notChosenBlueprintIds, Integer[] woundsOnChosen, Integer[] woundsOnNotChosen) {
        this.chosenBlueprintIds.addAll(Arrays.asList(chosenBlueprintIds));
        this.notChosenBlueprintIds.addAll(Arrays.asList(notChosenBlueprintIds));
        this.woundsOnChosen.addAll(Arrays.asList(woundsOnChosen));
        this.woundsOnNotChosen.addAll(Arrays.asList(woundsOnNotChosen));
    }

    public List<String> getChosenBlueprintIds() {
        return chosenBlueprintIds;
    }

    public List<String> getNotChosenBlueprintIds() {
        return notChosenBlueprintIds;
    }

    public List<Integer> getWoundsOnChosen() {
        return woundsOnChosen;
    }

    public List<Integer> getWoundsOnNotChosen() {
        return woundsOnNotChosen;
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
        JSONObject obj = new JSONObject();
        obj.put("type", "CardSelectionAction");
        obj.put("chosenBlueprints", chosenBlueprintIds.toArray());
        obj.put("woundsOnChosen", woundsOnChosen.toArray());
        obj.put("notChosenBlueprints", notChosenBlueprintIds.toArray());
        obj.put("woundsOnNotChosen", woundsOnNotChosen.toArray());
        return obj;
    }

    public static CardSelectionAction fromJson(JSONObject obj) {
        return new CardSelectionAction(obj.getObject("chosenBlueprints", String[].class), obj.getObject("notChosenBlueprints", String[].class), obj.getObject("woundsOnChosen", Integer[].class), obj.getObject("woundsOnNotChosen", Integer[].class));
    }
}
