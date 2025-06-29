package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;

public class AnotherMoveTrainer implements Trainer {
    private static final String ANOTHER_MOVE = "another move";
    private static final String YES = "Yes";

    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();
        for (LearningStep step : steps) {
            if (isStepRelevant(step)) {
                int label;
                if (((MultipleChoiceAction) step.action).getChosenOption().equals(YES)) {
                    label = step.reward > 0 ? 1 : 0;
                } else {
                    label = step.reward > 0 ? 0 : 1;
                }

                data.add(new LabeledPoint(label, step.state));
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
        if (step.decision.getDecisionType() == AwaitingDecisionType.MULTIPLE_CHOICE) {
            return step.decision.getText().contains(ANOTHER_MOVE) && step.action instanceof MultipleChoiceAction;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(AnotherMoveTrainer.class);
    }
}
