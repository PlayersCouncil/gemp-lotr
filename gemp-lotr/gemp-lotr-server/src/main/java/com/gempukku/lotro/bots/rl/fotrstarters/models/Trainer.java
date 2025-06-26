package com.gempukku.lotro.bots.rl.fotrstarters.models;

import com.gempukku.lotro.bots.rl.LearningStep;
import smile.classification.SoftClassifier;

import java.util.List;

public interface Trainer {
    List<LabeledPoint> extractTrainingData(List<LearningStep> steps);
    SoftClassifier<double[]> trainWithPoints(List<LabeledPoint> points);
    default SoftClassifier<double[]> train(List<LearningStep> steps) {
        return trainWithPoints(extractTrainingData(steps));
    }
}
