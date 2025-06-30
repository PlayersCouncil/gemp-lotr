package com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.semanticaction.IntegerChoiceAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BurdenTrainer implements Trainer {
    private static final String BURDENS = "burdens to bid";
    private static final Set<Integer> uniqueBids = new HashSet<>();

    public static int getUniqueBids() {
        return uniqueBids.size();
    }

    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step))
                continue;

            if (step.reward <= 0) // Only predict based on winning bids
                continue;

            int chosenValue = ((IntegerChoiceAction) step.action).getValue();
            uniqueBids.add(chosenValue);
            data.add(new LabeledPoint(chosenValue, step.state));
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
        return step.decision.getDecisionType() == AwaitingDecisionType.INTEGER &&
            step.decision.getText().contains(BURDENS) &&
            step.action instanceof IntegerChoiceAction;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(BurdenTrainer.class);
    }
}
