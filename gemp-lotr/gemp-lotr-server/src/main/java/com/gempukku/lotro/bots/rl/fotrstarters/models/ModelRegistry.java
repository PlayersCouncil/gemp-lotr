package com.gempukku.lotro.bots.rl.fotrstarters.models;

import smile.classification.SoftClassifier;

public class ModelRegistry {
    private SoftClassifier<double[]> goFirstModel;
    private SoftClassifier<double[]> mulliganModel;
    private SoftClassifier<double[]> anotherMoveModel;
    private SoftClassifier<double[]> burdensBidModel;
    private SoftClassifier<double[]> reconcileModel;
    private SoftClassifier<double[]> sanctuaryModel;
    private SoftClassifier<double[]> archeryModel;
    private SoftClassifier<double[]> attachItemModel;
    private SoftClassifier<double[]> skirmishOrderModel;
    private SoftClassifier<double[]> healModel;
    private SoftClassifier<double[]> discardFromHandModel;
    private SoftClassifier<double[]> exertModel;

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

    public SoftClassifier<double[]> getSanctuaryModel() {
        return sanctuaryModel;
    }

    public void setSanctuaryModel(SoftClassifier<double[]> sanctuaryModel) {
        this.sanctuaryModel = sanctuaryModel;
    }

    public SoftClassifier<double[]> getArcheryModel() {
        return archeryModel;
    }

    public void setArcheryModel(SoftClassifier<double[]> archeryModel) {
        this.archeryModel = archeryModel;
    }

    public SoftClassifier<double[]> getAttachItemModel() {
        return attachItemModel;
    }

    public void setAttachItemModel(SoftClassifier<double[]> attachItemModel) {
        this.attachItemModel = attachItemModel;
    }

    public SoftClassifier<double[]> getSkirmishOrderModel() {
        return skirmishOrderModel;
    }

    public void setSkirmishOrderModel(SoftClassifier<double[]> skirmishOrderModel) {
        this.skirmishOrderModel = skirmishOrderModel;
    }

    public SoftClassifier<double[]> getHealModel() {
        return healModel;
    }

    public void setHealModel(SoftClassifier<double[]> healModel) {
        this.healModel = healModel;
    }

    public SoftClassifier<double[]> getDiscardFromHandModel() {
        return discardFromHandModel;
    }

    public void setDiscardFromHandModel(SoftClassifier<double[]> discardFromHandModel) {
        this.discardFromHandModel = discardFromHandModel;
    }

    public SoftClassifier<double[]> getExertModel() {
        return exertModel;
    }

    public void setExertModel(SoftClassifier<double[]> exertModel) {
        this.exertModel = exertModel;
    }
}
