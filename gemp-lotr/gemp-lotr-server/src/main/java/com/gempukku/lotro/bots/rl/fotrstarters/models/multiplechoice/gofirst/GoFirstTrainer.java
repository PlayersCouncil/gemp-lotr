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
import java.util.function.IntPredicate;

public class GoFirstTrainer implements Trainer {
    private static final String GO_FIRST = "Go first";
    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();
        System.out.println("Points to extract from: " + steps.size());
        int mcFound = 0;
        int gfFound = 0;
        for (LearningStep step : steps) {
            if (step.decision.getDecisionType() == AwaitingDecisionType.MULTIPLE_CHOICE) {
                mcFound++;
                String[] options = step.decision.getDecisionParameters().get("results");

                int goFirstIndex = -1;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equalsIgnoreCase(GO_FIRST)) {
                        goFirstIndex = i;
                        break;
                    }
                }

                if (goFirstIndex != -1 && step.action instanceof MultipleChoiceAction mca) {
                    gfFound++;
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
        System.out.println("MC steps found: " + mcFound);
        System.out.println("GF steps found: " + gfFound);
        return data;
    }

    @Override
    public SoftClassifier<double[]> trainWithPoints(List<LabeledPoint> points) {
        double[][] x = points.stream().map(LabeledPoint::x).toArray(double[][]::new);
        int[] y = points.stream().mapToInt(LabeledPoint::y).toArray();


        System.out.println("Training with: " + points.size());
        System.out.println("First better: " + points.stream().mapToInt(LabeledPoint::y).filter(new IntPredicate() {
            @Override
            public boolean test(int value) {
                return value == 1;
            }
        }).count());

        System.out.println("Second better: " + points.stream().mapToInt(LabeledPoint::y).filter(new IntPredicate() {
            @Override
            public boolean test(int value) {
                return value == 0;
            }
        }).count());

        return LogisticRegression.fit(x, y);
    }
}
