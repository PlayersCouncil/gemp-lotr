package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.annotation.JSONType;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@JSONType(typeName = "AssignMinionsAction")
public class AssignMinionsAction implements SemanticAction {
    private final HashMap<String, List<String>> assignmentMap = new HashMap<>();

    public AssignMinionsAction(String answer, GameState gameState) {
        String[] assignments = answer.split(",");
        for (String assignment : assignments) {
            String[] cards = assignment.split(" ");
            String fp = gameState.getBlueprintId(Integer.parseInt(cards[0]));
            List<String> minions = new ArrayList<>();
            for (int i = 1; i < cards.length; i++) {
                minions.add(gameState.getBlueprintId(Integer.parseInt(cards[i])));
            }
            assignmentMap.put(fp, minions);
        }
    }

    public AssignMinionsAction(HashMap<String, List<String>> assignmentMap) {
        this.assignmentMap.putAll(assignmentMap);
    }

    public HashMap<String, List<String>> getAssignmentMap() {
        return assignmentMap;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.ASSIGN_MINIONS) {
            throw new IllegalArgumentException("Wrong decision type.");
        }
        // Does not work well with multiple of same minions

        String[] freeCharIds = decision.getDecisionParameters().get("freeCharacters");
        String[] minionIds = decision.getDecisionParameters().get("minions");
        List<String> freeChars = new ArrayList<>(List.of(freeCharIds));
        List<String> minions = new ArrayList<>(List.of(minionIds));
        List<String> freeCharBlueprints = new ArrayList<>();
        freeChars.forEach(freeChar -> freeCharBlueprints.add(gameState.getBlueprintId(Integer.parseInt(freeChar))));
        List<String> minionBlueprints = new ArrayList<>();
        minions.forEach(minion -> minionBlueprints.add(gameState.getBlueprintId(Integer.parseInt(minion))));

        Map<String, List<String>> assignments = new HashMap<>();
        assignmentMap.forEach((fpBlueprint, minionBlueprints1) -> {
            String fp = freeChars.remove(freeCharBlueprints.indexOf(fpBlueprint));
            freeCharBlueprints.remove(fpBlueprint);
            assignments.put(fp, new ArrayList<>());
            minionBlueprints1.forEach(minionBlueprint -> {
                String minion = minions.remove(minionBlueprints.indexOf(minionBlueprint));
                minionBlueprints.remove(minionBlueprint);
                assignments.get(fp).add(minion);
            });
        });


        return assignments.entrySet().stream()
                .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(","));
    }
}
