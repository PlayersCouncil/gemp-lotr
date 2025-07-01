package com.gempukku.lotro.bots.rl.fotrstarters.models;

import smile.classification.SoftClassifier;

public class ModelRegistry {
    private SoftClassifier<double[]> goFirstModel;
    private SoftClassifier<double[]> mulliganModel;
    private SoftClassifier<double[]> anotherMoveModel;
    private SoftClassifier<double[]> burdensBidModel;
    private SoftClassifier<double[]> reconcileModel;

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

    public SoftClassifier<double[]> getAnotherMoveModel() {
        return anotherMoveModel;
    }

    public void setAnotherMoveModel(SoftClassifier<double[]> anotherMoveModel) {
        this.anotherMoveModel = anotherMoveModel;
    }

    public SoftClassifier<double[]> getBurdensBidModel() {
        return burdensBidModel;
    }

    public void setBurdensBidModel(SoftClassifier<double[]> burdensBidModel) {
        this.burdensBidModel = burdensBidModel;
    }

    public SoftClassifier<double[]> getReconcileModel() {
        return reconcileModel;
    }

    public void setReconcileModel(SoftClassifier<double[]> reconcileModel) {
        this.reconcileModel = reconcileModel;
    }
}
