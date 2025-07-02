package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardSelectionAssignedAction extends CardSelectionAction {
    private final List<Integer> minionsOnChosen = new ArrayList<>();
    private final List<Integer> strengthOfMinionsOnChosen = new ArrayList<>();

    public CardSelectionAssignedAction(String answer, AwaitingDecision decision, GameState gameState) {
        super(answer, decision, gameState);
        String[] individualCards = answer.split(",");

        for (String individualCard : individualCards) {
            for (Assignment assignment : gameState.getAssignments()) {
                if (assignment.getFellowshipCharacter().getCardId() == Integer.parseInt(individualCard)) {
                    minionsOnChosen.add(assignment.getShadowCharacters().size());
                    strengthOfMinionsOnChosen.add(assignment.getShadowCharacters().stream().mapToInt(value -> value.getBlueprint().getStrength()).sum());
                }
            }
        }
    }

    public CardSelectionAssignedAction(String[] chosenBlueprintIds, String[] notChosenBlueprintIds, Integer[] woundsOnChosen, Integer[] woundsOnNotChosen, Integer[] minionsOnChosen, Integer[] strengthOfMinionsOnChosen) {
        super(chosenBlueprintIds, notChosenBlueprintIds, woundsOnChosen, woundsOnNotChosen);
        this.minionsOnChosen.addAll(Arrays.asList(minionsOnChosen));
        this.strengthOfMinionsOnChosen.addAll(Arrays.asList(strengthOfMinionsOnChosen));
    }

    public List<Integer> getMinionsOnChosen() {
        return minionsOnChosen;
    }

    public List<Integer> getStrengthOfMinionsOnChosen() {
        return strengthOfMinionsOnChosen;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "CardSelectionAssignedAction");
        obj.put("chosenBlueprints", chosenBlueprintIds.toArray());
        obj.put("woundsOnChosen", woundsOnChosen.toArray());
        obj.put("notChosenBlueprints", notChosenBlueprintIds.toArray());
        obj.put("woundsOnNotChosen", woundsOnNotChosen.toArray());
        obj.put("minionsOnChosen", minionsOnChosen.toArray());
        obj.put("strengthOfMinionsOnChosen", strengthOfMinionsOnChosen.toArray());
        return obj;
    }

    public static CardSelectionAssignedAction fromJson(JSONObject obj) {
        return new CardSelectionAssignedAction(obj.getObject("chosenBlueprints", String[].class), obj.getObject("notChosenBlueprints", String[].class)
                , obj.getObject("woundsOnChosen", Integer[].class), obj.getObject("woundsOnNotChosen", Integer[].class)
                , obj.getObject("minionsOnChosen", Integer[].class), obj.getObject("strengthOfMinionsOnChosen", Integer[].class));
    }
}
