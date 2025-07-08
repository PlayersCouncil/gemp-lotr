package com.gempukku.lotro.bots.rl.fotrstarters.models.assignment;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;

import java.util.*;

public class FpAssignmentTrainer extends AbstractAssignmentTrainer {
    @Override
    protected boolean isForFp() {
        return true;
    }

    @Override
    protected void generateAssignmentsRecursive(Map<String, String> minionsToAssign,
                                                List<String> minionKeys, int index,
                                                Map<String, String> freeChars,
                                                AssignmentInfo current,
                                                List<AssignmentInfo> results,
                                                GameState gameState) {
        generateAssignmentsRecursive(minionsToAssign, minionKeys, index, freeChars, current, results, new ArrayList<>(), gameState);
    }

    private void generateAssignmentsRecursive(Map<String, String> minionsToAssign,
                                              List<String> minionKeys, int index,
                                              Map<String, String> freeChars,
                                              AssignmentInfo current,
                                              List<AssignmentInfo> results,
                                              List<String> unassignedMinions,
                                              GameState gameState) {
        if (index >= minionKeys.size()) {
            // Deep copy the current assignment
            int unassignedStrength = unassignedMinions.stream().mapToInt(minionId -> {
                for (PhysicalCard physicalCard : gameState.getInPlay()) {
                    if (physicalCard.getCardId() == Integer.parseInt(minionId)) {
                        return physicalCard.getBlueprint().getStrength();
                    }
                }
                return 0;
            }).sum();
            AssignmentInfo copy = new AssignmentInfo(new HashMap<>(), new HashMap<>(), unassignedMinions.size(), unassignedStrength); // strength placeholder

            for (Map.Entry<String, List<String>> entry : current.blueprintAssignment().entrySet()) {
                copy.blueprintAssignment().put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            for (Map.Entry<String, List<String>> entry : current.physicalAssignment().entrySet()) {
                copy.physicalAssignment().put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            results.add(copy);
            return;
        }

        String minionPhysId = minionKeys.get(index);
        String minionBlueprint = minionsToAssign.get(minionPhysId);
        Map.Entry<String, String> currentMinion = new AbstractMap.SimpleEntry<>(minionPhysId, minionBlueprint);

        for (Map.Entry<String, String> fp : freeChars.entrySet()) {
            String fpPhysId = fp.getKey();
            String fpBlueprintId = fp.getValue();

            current.physicalAssignment().putIfAbsent(fpPhysId, new ArrayList<>());
            if (current.physicalAssignment().get(fpPhysId).isEmpty()) {
                current.physicalAssignment().get(fpPhysId).add(currentMinion.getKey());
                current.blueprintAssignment().putIfAbsent(fpBlueprintId, new ArrayList<>());
                current.blueprintAssignment().get(fpBlueprintId).add(currentMinion.getValue());

                generateAssignmentsRecursive(minionsToAssign, minionKeys, index + 1, freeChars, current, results, unassignedMinions, gameState);

                // backtrack
                current.physicalAssignment().get(fpPhysId).remove(current.physicalAssignment().get(fpPhysId).size() - 1);
                current.blueprintAssignment().get(fpBlueprintId).remove(current.blueprintAssignment().get(fpBlueprintId).size() - 1);
            }
        }

        // Let the current minion remain unassigned
        unassignedMinions.add(minionPhysId);
        generateAssignmentsRecursive(minionsToAssign, minionKeys, index + 1, freeChars, current, results, unassignedMinions, gameState);
        unassignedMinions.remove(unassignedMinions.size() - 1); // backtrack
    }
}
