package com.gempukku.lotro.bots.rl.fotrstarters.models;

import smile.classification.SoftClassifier;

public class ModelRegistry {
    private SoftClassifier<double[]> goFirstModel;

    public SoftClassifier<double[]> getGoFirstModel() {
        return goFirstModel;
    }

    public void setGoFirstModel(SoftClassifier<double[]> goFirstModel) {
        this.goFirstModel = goFirstModel;
    }
}
