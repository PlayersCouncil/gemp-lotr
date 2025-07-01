package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.*;

public class ReconcileTrainer implements Trainer {
    private static final String RECONCILE = "Reconcile";

    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            if (step.reward > 0) {
                CardSelectionAction action = (CardSelectionAction) step.action;
                Set<String> selected = new HashSet<>(action.getChosenBlueprintIds());
                Set<String> notSelected = new HashSet<>(action.getNotChosenBlueprintIds());

                selected.forEach(blueprintId -> {
                    try {
                        double[] blueprintVector = BlueprintFeatures.getBlueprintFeatures(blueprintId);
                        double[] extended = Arrays.copyOf(step.state, step.state.length + blueprintVector.length);
                        System.arraycopy(blueprintVector, 0, extended, step.state.length, blueprintVector.length);

                        data.add(new LabeledPoint(1, extended));
                    } catch (CardNotFoundException ignore) {

                    }
                });

                notSelected.forEach(blueprintId -> {
                    try {
                        double[] blueprintVector = BlueprintFeatures.getBlueprintFeatures(blueprintId);
                        double[] extended = Arrays.copyOf(step.state, step.state.length + blueprintVector.length);
                        System.arraycopy(blueprintVector, 0, extended, step.state.length, blueprintVector.length);

                        data.add(new LabeledPoint(0, extended));
                    } catch (CardNotFoundException ignore) {

                    }
                });
            }
        }

        return data;
    }

    @Override
    public SoftClassifier<double[]> trainWithPoints(List<LabeledPoint> points) {
        double[][] x = points.stream().map(LabeledPoint::x).toArray(double[][]::new);
        int[] y = points.stream().mapToInt(LabeledPoint::y).toArray();
        return LogisticRegression.fit(x, y);
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return step.decision.getDecisionType() == AwaitingDecisionType.CARD_SELECTION
                && step.decision.getText().contains(RECONCILE)
                && step.action instanceof CardSelectionAction;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(ReconcileTrainer.class);
    }
}
