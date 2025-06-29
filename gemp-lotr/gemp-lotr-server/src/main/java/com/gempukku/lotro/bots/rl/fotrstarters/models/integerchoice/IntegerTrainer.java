package com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.semanticaction.IntegerChoiceAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IntegerTrainer implements Trainer {
    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step))
                continue;

            Map<String, String[]> params = step.decision.getDecisionParameters();

            // Parse min and max (nullable)
            int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
            Integer max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : null;

            int chosenValue = ((IntegerChoiceAction) step.action).getValue();

            // Map chosen value to a label (zero-based)
            int label = chosenValue - min;
            if (label < 0 || (max != null && chosenValue > max))
                throw new IllegalStateException("Chosen value out of expected range");

            data.add(new LabeledPoint(label, step.state));
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
        if (step.decision.getDecisionType() == AwaitingDecisionType.INTEGER) {
            return step.action instanceof IntegerChoiceAction;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(IntegerTrainer.class);
    }
}
