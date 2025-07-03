package com.gempukku.lotro.bots.rl.fotrstarters.models;

import com.gempukku.lotro.bots.rl.LearningStep;
import smile.classification.SoftClassifier;

import java.util.List;

public interface Trainer {
    boolean isStepRelevant(LearningStep step);
    SoftClassifier<double[]> train(List<LearningStep> steps);

    static boolean equal(Trainer t1, Trainer t2) {
        return t1.getClass().equals(t2.getClass());
    }
}
