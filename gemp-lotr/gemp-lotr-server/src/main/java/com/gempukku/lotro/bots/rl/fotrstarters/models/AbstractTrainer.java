package com.gempukku.lotro.bots.rl.fotrstarters.models;

import com.gempukku.lotro.bots.rl.DecisionAnswerer;
import com.gempukku.lotro.bots.rl.LearningStep;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.List;

public abstract class AbstractTrainer implements Trainer, DecisionAnswerer {

    abstract protected List<LabeledPoint> extractTrainingData(List<LearningStep> steps);

    public SoftClassifier<double[]> trainWithPoints(List<LabeledPoint> points) {
        double[][] x = points.stream().map(LabeledPoint::x).toArray(double[][]::new);
        int[] y = points.stream().mapToInt(LabeledPoint::y).toArray();
        return LogisticRegression.fit(x, y);
    }

    @Override
    public SoftClassifier<double[]> train(List<LearningStep> steps) {
        return trainWithPoints(extractTrainingData(steps));
    }
}
