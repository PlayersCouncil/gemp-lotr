package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignMinionsAction implements SemanticAction {
    private final HashMap<String, List<String>> assignmentMap = new HashMap<>();
    private final boolean isFreePeoplesAssignment;
    private final HashMap<String, List<String>> alreadyAssignedMap = new HashMap<>();
    private final HashMap<String, Integer> woundsOnFp = new HashMap<>();

    public AssignMinionsAction(String answer, GameState gameState, boolean isFreePeoplesAssignment) {
        this.isFreePeoplesAssignment = isFreePeoplesAssignment;

        gameState.getAssignments().forEach(assignment -> {
            String fpBlueprint = assignment.getFellowshipCharacter().getBlueprintId();
            alreadyAssignedMap.put(fpBlueprint, new ArrayList<>());
            assignment.getShadowCharacters().forEach(shadow -> {
                alreadyAssignedMap.get(fpBlueprint).add(shadow.getBlueprintId());
            });
        });

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

        gameState.getInPlay().forEach(fpCharacter -> {
            if ((fpCharacter.getBlueprint().getCardType().equals(CardType.COMPANION) ||
                    fpCharacter.getBlueprint().getCardType().equals(CardType.ALLY)) &&
                    fpCharacter.getOwner().equals(gameState.getCurrentPlayerId())) {
                woundsOnFp.put(fpCharacter.getBlueprintId(), gameState.getWounds(fpCharacter));
                if (!alreadyAssignedMap.containsKey(fpCharacter.getBlueprintId())) {
                    alreadyAssignedMap.put(fpCharacter.getBlueprintId(), new ArrayList<>());
                }
            }
        });
    }

    public AssignMinionsAction(HashMap<String, List<String>> assignmentMap, boolean isFreePeoplesAssignment,
                               HashMap<String, List<String>> alreadyAssignedMap, HashMap<String, Integer> woundsOnFp) {
        this.isFreePeoplesAssignment = isFreePeoplesAssignment;
        this.assignmentMap.putAll(assignmentMap);
        this.alreadyAssignedMap.putAll(alreadyAssignedMap);
        this.woundsOnFp.putAll(woundsOnFp);
    }

    public HashMap<String, List<String>> getAssignmentMap() {
        return assignmentMap;
    }

    public boolean isFreePeoplesAssignment() {
        return isFreePeoplesAssignment;
    }

    public HashMap<String, List<String>> getAlreadyAssignedMap() {
        return alreadyAssignedMap;
    }

    public HashMap<String, Integer> getWoundsOnFp() {
        return woundsOnFp;
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

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "AssignMinionsAction");
        obj.put("assignmentMap", assignmentMap);
        obj.put("isFreePeoplesAssignment", isFreePeoplesAssignment);
        obj.put("alreadyAssignedMap", alreadyAssignedMap);
        obj.put("woundsOnFp", woundsOnFp);
        return obj;
    }

    public static AssignMinionsAction fromJson(JSONObject obj) {
        HashMap<String, List<String>> assignmentMap = obj.getObject("assignmentMap", HashMap.class);
        boolean isFreePeoplesAssignment = obj.getBoolean("isFreePeoplesAssignment");
        HashMap<String, List<String>> alreadyAssignedMap = obj.getObject("alreadyAssignedMap", HashMap.class);
        HashMap<String, Integer> wounds = obj.getObject("woundsOnFp", HashMap.class);

        return new AssignMinionsAction(assignmentMap, isFreePeoplesAssignment, alreadyAssignedMap, wounds);
    }
}
