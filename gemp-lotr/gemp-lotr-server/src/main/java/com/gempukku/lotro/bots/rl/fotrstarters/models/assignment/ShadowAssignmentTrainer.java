package com.gempukku.lotro.bots.rl.fotrstarters.models.assignment;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.AssignMinionsAction;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShadowAssignmentTrainer extends AbstractAssignmentTrainer {
    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return decision.getDecisionType().equals(AwaitingDecisionType.ASSIGN_MINIONS) &&
                !gameState.getCurrentPlayerId().equals(playerName);
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] freeCharIds = params.get("freeCharacters");
        String[] minionIds = params.get("minions");

        if (freeCharIds == null || minionIds == null || freeCharIds.length == 0 || minionIds.length == 0)
            return "";


        Map<String, String> freeChars = Stream.of(freeCharIds)
                .collect(Collectors.toMap(
                        Function.identity(),
                        s -> gameState.getBlueprintId(Integer.parseInt(s))
                ));
        Map<String, String> minions = Stream.of(minionIds)
                .collect(Collectors.toMap(
                        Function.identity(),
                        s -> gameState.getBlueprintId(Integer.parseInt(s))
                ));
        HashMap<String, List<String>> alreadyAssignedMap = new HashMap<>();
        HashMap<String, Integer> woundsOnFp = new HashMap<>();
        gameState.getAssignments().forEach(assignment -> {
            String fpBlueprint = assignment.getFellowshipCharacter().getBlueprintId();
            alreadyAssignedMap.put(fpBlueprint, new ArrayList<>());
            assignment.getShadowCharacters().forEach(shadow -> {
                alreadyAssignedMap.get(fpBlueprint).add(shadow.getBlueprintId());
            });
        });
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

        List<AssignmentPair> allAssignments = generateAllAssignments(alreadyAssignedMap, freeChars, minions);
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());

        double bestScore = Double.NEGATIVE_INFINITY;
        Map<String, List<String>> bestAssignment = null;

        for (AssignmentPair  assignment : allAssignments) {
            try {
                double[] assignmentVector = CardFeatures.getAssignmentFeatures(assignment.blueprintAssignment, woundsOnFp);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + assignmentVector.length);
                System.arraycopy(assignmentVector, 0, extended, stateVector.length, assignmentVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                if (probs[1] > bestScore) {
                    bestScore = probs[1];
                    bestAssignment = assignment.physicalAssignment;
                }
            } catch (CardNotFoundException ignore) {
            }
        }

        if (bestAssignment == null || bestAssignment.isEmpty())
            return "";

        return bestAssignment.entrySet().stream()
                .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(","));
    }

    private List<AssignmentPair> generateAllAssignments(HashMap<String, List<String>> alreadyAssignedMap,
                                                                   Map<String, String> freeChars,
                                                                   Map<String, String> minions) {
        List<AssignmentPair> results = new ArrayList<>();

        // Ensure every freeChar is initialized in the assignment map
        freeChars.forEach((physicalId, blueprintId) -> {
            if (!alreadyAssignedMap.containsKey(blueprintId)) {
                throw new IllegalArgumentException("Unknown fp card: " + blueprintId);
            }

        });

        // Deep copy to avoid mutating input map
        AssignmentPair current = new AssignmentPair(new HashMap<>(), new HashMap<>());
        for (String fp : alreadyAssignedMap.keySet()) {
            current.blueprintAssignment.put(fp, new ArrayList<>(alreadyAssignedMap.get(fp)));
        }

        generateAssignmentsRecursive(minions, new ArrayList<>(minions.keySet()),0, freeChars, current, results);
        return results;
    }

    private void generateAssignmentsRecursive(Map<String, String> minionsToAssign,
                                              List<String> minionKeys, int index,
                                              Map<String, String> freeChars,
                                              AssignmentPair current,
                                              List<AssignmentPair> results) {
        if (index >= minionsToAssign.size()) {
            // Deep copy the current assignment
            AssignmentPair copy = new AssignmentPair(new HashMap<>(), new HashMap<>());
            for (Map.Entry<String, List<String>> entry : current.blueprintAssignment.entrySet()) {
                copy.blueprintAssignment.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            for (Map.Entry<String, List<String>> entry : current.physicalAssignment.entrySet()) {
                copy.physicalAssignment.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            results.add(copy);
            return;
        }

        Map.Entry<String, String> currentMinion = new AbstractMap.SimpleEntry<>(minionKeys.get(index), minionsToAssign.get(minionKeys.get(index)));
        for (Map.Entry<String, String> fp : freeChars.entrySet()) {
            current.physicalAssignment.putIfAbsent(fp.getKey(), new ArrayList<>());
            current.physicalAssignment.get(fp.getKey()).add(currentMinion.getKey());
            current.blueprintAssignment.putIfAbsent(fp.getValue(), new ArrayList<>());
            current.blueprintAssignment.get(fp.getValue()).add(currentMinion.getValue());
            generateAssignmentsRecursive(minionsToAssign, minionKeys, index + 1, freeChars, current, results);
            current.physicalAssignment.get(fp.getKey()).remove(current.physicalAssignment.get(fp.getKey()).size() - 1); // backtrack
            current.blueprintAssignment.get(fp.getValue()).remove(current.blueprintAssignment.get(fp.getValue()).size() - 1); // backtrack
        }
    }

    @Override
    protected List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            AssignMinionsAction action = (AssignMinionsAction) step.action;
            Map<String, List<String>> assignmentMap = action.getAssignmentMap();
            Map<String, List<String>> alreadyAssignedMap = action.getAlreadyAssignedMap();

            alreadyAssignedMap.forEach((fp, shadow) -> {
                if (!assignmentMap.containsKey(fp)) {
                    assignmentMap.put(fp, new ArrayList<>());
                }
                assignmentMap.get(fp).addAll(shadow);
            });


            if (step.reward > 0) {
                // Chosen: good
                addLabeledPoints(data, assignmentMap, action.getWoundsOnFp(), step.state, 1);
            } else {
                // Chosen: bad
                addLabeledPoints(data, assignmentMap, action.getWoundsOnFp(), step.state, 0);
            }
        }

        return data;
    }

    private void addLabeledPoints(List<LabeledPoint> data, Map<String, List<String>> assignmentMap,
                                  Map<String, Integer> woundsMap, double[] state, int label) {
        try {
            double[] assignmentVector = CardFeatures.getAssignmentFeatures(assignmentMap, woundsMap);
            double[] extended = Arrays.copyOf(state, state.length + assignmentVector.length);
            System.arraycopy(assignmentVector, 0, extended, state.length, assignmentVector.length);
            data.add(new LabeledPoint(label, extended));
        } catch (CardNotFoundException ignore) {
        }
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return step.decision.getDecisionType().equals(AwaitingDecisionType.ASSIGN_MINIONS) &&
                step.action instanceof AssignMinionsAction ama &&
                !ama.isFreePeoplesAssignment();
    }

    private record AssignmentPair(Map<String, List<String>> blueprintAssignment,
                                 Map<String, List<String>> physicalAssignment) {}
}
