package com.gempukku.lotro.bots.rl.fotrstarters.models;

import smile.classification.SoftClassifier;

public class ModelRegistry {
    private SoftClassifier<double[]> goFirstModel;
    private SoftClassifier<double[]> mulliganModel;
    private SoftClassifier<double[]> integerModel;

    public SoftClassifier<double[]> getGoFirstModel() {
        return goFirstModel;
    }

    public void setGoFirstModel(SoftClassifier<double[]> goFirstModel) {
        this.goFirstModel = goFirstModel;
    }

    public SoftClassifier<double[]> getMulliganModel() {
        return mulliganModel;
    }

    public void setMulliganModel(SoftClassifier<double[]> mulliganModel) {
        this.mulliganModel = mulliganModel;
    }

    public SoftClassifier<double[]> getIntegerModel() {
        return integerModel;
    }

    public void setIntegerModel(SoftClassifier<double[]> integerModel) {
        this.integerModel = integerModel;
    }
}
