package com.gempukku.lotro.bots.rl.fotrstarters.models.assignment;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
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

public abstract class AbstractAssignmentTrainer extends AbstractTrainer {
    protected abstract boolean isForFp();

    protected abstract void generateAssignmentsRecursive(Map<String, String> minionsToAssign,
                                                         List<String> minionKeys, int index,
                                                         Map<String, String> freeChars,
                                                         AssignmentInfo current,
                                                         List<AssignmentInfo> results,
                                                         GameState gameState);


    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        boolean correctPlayer = (isForFp() && gameState.getCurrentPlayerId().equals(playerName)) ||
                (!isForFp() && !gameState.getCurrentPlayerId().equals(playerName));

        return decision.getDecisionType().equals(AwaitingDecisionType.ASSIGN_MINIONS) &&
                correctPlayer;
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return step.decision.getDecisionType().equals(AwaitingDecisionType.ASSIGN_MINIONS) &&
                step.action instanceof AssignMinionsAction ama &&
                ama.isFreePeoplesAssignment() == isForFp();
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
            assignment.getShadowCharacters().forEach(shadow -> alreadyAssignedMap.get(fpBlueprint).add(shadow.getBlueprintId()));
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

        List<AssignmentInfo> allAssignments = generateAllAssignments(alreadyAssignedMap, freeChars, minions, gameState);
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());

        double bestScore = Double.NEGATIVE_INFINITY;
        Map<String, List<String>> bestAssignment = null;

        for (AssignmentInfo assignment : allAssignments) {
            try {
                double[] assignmentVector = CardFeatures.getAssignmentFeatures(assignment.blueprintAssignment(), woundsOnFp,
                        assignment.numberOfUnassignedMinions(), assignment.strengthOfUnassignedMinions());
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + assignmentVector.length);
                System.arraycopy(assignmentVector, 0, extended, stateVector.length, assignmentVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                if (probs[1] > bestScore) {
                    bestScore = probs[1];
                    bestAssignment = assignment.physicalAssignment();
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


    private List<AssignmentInfo> generateAllAssignments(HashMap<String, List<String>> alreadyAssignedMap,
                                                        Map<String, String> freeChars,
                                                        Map<String, String> minions,
                                                        GameState gameState) {
        List<AssignmentInfo> results = new ArrayList<>();

        // Ensure every freeChar is initialized in the assignment map
        freeChars.forEach((physicalId, blueprintId) -> {
            if (!alreadyAssignedMap.containsKey(blueprintId)) {
                throw new IllegalArgumentException("Unknown fp card: " + blueprintId);
            }

        });

        // Deep copy to avoid mutating input map
        AssignmentInfo current = new AssignmentInfo(new HashMap<>(), new HashMap<>(), 0 , 0);
        for (String fp : alreadyAssignedMap.keySet()) {
            current.blueprintAssignment().put(fp, new ArrayList<>(alreadyAssignedMap.get(fp)));
        }

        generateAssignmentsRecursive(minions, new ArrayList<>(minions.keySet()), 0, freeChars, current, results, gameState);
        return results;
    }

    @Override
    protected List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            AssignMinionsAction action = (AssignMinionsAction) step.action;
            Map<String, List<String>> assignmentMap = action.getAssignmentMap();
            Map<String, List<String>> alreadyAssignedMap = action.getAlreadyAssignedMap();
            int numberOfUnassignedMinions = action.getNumberOfUnassignedMinions();
            int strengthOfUnassignedMinions = action.getStrengthOfUnassignedMinions();


            alreadyAssignedMap.forEach((fp, shadow) -> {
                if (!assignmentMap.containsKey(fp)) {
                    assignmentMap.put(fp, new ArrayList<>());
                }
                assignmentMap.get(fp).addAll(shadow);
            });


            if (step.reward > 0) {
                // Chosen: good
                addLabeledPoints(data, assignmentMap, numberOfUnassignedMinions, strengthOfUnassignedMinions, action.getWoundsOnFp(), step.state, 1);
            } else {
                // Chosen: bad
                addLabeledPoints(data, assignmentMap, numberOfUnassignedMinions, strengthOfUnassignedMinions, action.getWoundsOnFp(), step.state, 0);
            }
        }

        return data;
    }

    private void addLabeledPoints(List<LabeledPoint> data, Map<String, List<String>> assignmentMap,
                                  int numberOfUnassignedMinions, int strengthOfUnassignedMinions,
                                  Map<String, Integer> woundsMap, double[] state, int label) {
        try {
            double[] assignmentVector = CardFeatures.getAssignmentFeatures(assignmentMap, woundsMap,
                    numberOfUnassignedMinions, strengthOfUnassignedMinions);
            double[] extended = Arrays.copyOf(state, state.length + assignmentVector.length);
            System.arraycopy(assignmentVector, 0, extended, state.length, assignmentVector.length);
            data.add(new LabeledPoint(label, extended));
        } catch (CardNotFoundException ignore) {
        }
    }

    protected record AssignmentInfo(Map<String, List<String>> blueprintAssignment,
                                    Map<String, List<String>> physicalAssignment,
                                    int numberOfUnassignedMinions,
                                    int strengthOfUnassignedMinions) {
    }
}
