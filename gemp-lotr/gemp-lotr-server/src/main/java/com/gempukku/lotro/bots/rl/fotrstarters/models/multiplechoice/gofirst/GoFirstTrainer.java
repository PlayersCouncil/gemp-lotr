package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.gofirst;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;

public class GoFirstTrainer implements Trainer {
    private static final String GO_FIRST = "Go first";
    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();
        for (LearningStep step : steps) {
            if (step.decision.getDecisionType() == AwaitingDecisionType.MULTIPLE_CHOICE) {
                String[] options = step.decision.getDecisionParameters().get("results");

                int goFirstIndex = -1;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equalsIgnoreCase(GO_FIRST)) {
                        goFirstIndex = i;
                        break;
                    }
                }

                if (goFirstIndex != -1 && step.action instanceof MultipleChoiceAction mca) {
                    // Check if the action taken corresponds to choosing "Go first"
                    int label;
                    if (mca.getChosenOption().equals(GO_FIRST)) {
                        label = step.reward > 0 ? 1 : 0;
                    } else {
                        label = step.reward > 0 ? 0 : 1;
                    }

                    data.add(new LabeledPoint(label, step.state));
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
}
