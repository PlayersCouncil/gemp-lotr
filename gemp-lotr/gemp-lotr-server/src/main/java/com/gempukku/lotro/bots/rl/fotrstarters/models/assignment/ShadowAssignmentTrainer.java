package com.gempukku.lotro.bots.rl.fotrstarters.models.assignment;

import com.gempukku.lotro.game.state.GameState;

import java.util.*;

public class ShadowAssignmentTrainer extends AbstractAssignmentTrainer {
    @Override
    protected boolean isForFp() {
        return false;
    }

    @Override
    protected void generateAssignmentsRecursive(Map<String, String> minionsToAssign,
                                                List<String> minionKeys, int index,
                                                Map<String, String> freeChars,
                                                AssignmentInfo current,
                                                List<AssignmentInfo> results,
                                                GameState gameState) {
        if (index >= minionsToAssign.size()) {
            // Deep copy the current assignment
            AssignmentInfo copy = new AssignmentInfo(new HashMap<>(), new HashMap<>(), 0, 0);
            for (Map.Entry<String, List<String>> entry : current.blueprintAssignment().entrySet()) {
                copy.blueprintAssignment().put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            for (Map.Entry<String, List<String>> entry : current.physicalAssignment().entrySet()) {
                copy.physicalAssignment().put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            results.add(copy);
            return;
        }

        Map.Entry<String, String> currentMinion = new AbstractMap.SimpleEntry<>(minionKeys.get(index), minionsToAssign.get(minionKeys.get(index)));
        for (Map.Entry<String, String> fp : freeChars.entrySet()) {
            current.physicalAssignment().putIfAbsent(fp.getKey(), new ArrayList<>());
            current.physicalAssignment().get(fp.getKey()).add(currentMinion.getKey());
            current.blueprintAssignment().putIfAbsent(fp.getValue(), new ArrayList<>());
            current.blueprintAssignment().get(fp.getValue()).add(currentMinion.getValue());
            generateAssignmentsRecursive(minionsToAssign, minionKeys, index + 1, freeChars, current, results, gameState);
            current.physicalAssignment().get(fp.getKey()).remove(current.physicalAssignment().get(fp.getKey()).size() - 1); // backtrack
            current.blueprintAssignment().get(fp.getValue()).remove(current.blueprintAssignment().get(fp.getValue()).size() - 1); // backtrack
        }
    }
}
