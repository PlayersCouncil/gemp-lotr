package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAssignedAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkirmishOrderTrainer implements Trainer {
    private static final String SKIRMISH = "next skirmish to resolve";

    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            CardSelectionAssignedAction action = (CardSelectionAssignedAction) step.action;

            for (int i = 0; i < action.getChosenBlueprintIds().size(); i++) {
                try {
                    double[] blueprintVector = CardFeatures.getFpAssignedCardFeatures(action.getChosenBlueprintIds().get(i), action.getWoundsOnChosen().get(i), action.getMinionsOnChosen().get(i), action.getStrengthOfMinionsOnChosen().get(i));
                    double[] extended = Arrays.copyOf(step.state, step.state.length + blueprintVector.length);
                    System.arraycopy(blueprintVector, 0, extended, step.state.length, blueprintVector.length);

                    int label = step.reward > 0 ? 1 : 0;

                    data.add(new LabeledPoint(label, extended));
                } catch (CardNotFoundException ignore) {

                }
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
                && step.decision.getText().contains(SKIRMISH)
                && step.action instanceof CardSelectionAssignedAction;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(SkirmishOrderTrainer.class);
    }
}
