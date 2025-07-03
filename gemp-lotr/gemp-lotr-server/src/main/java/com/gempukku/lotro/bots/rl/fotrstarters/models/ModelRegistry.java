package com.gempukku.lotro.bots.rl.fotrstarters.models;

import smile.classification.SoftClassifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModelRegistry {
    private final Map<Class<? extends Trainer>, SoftClassifier<double[]>> modelMap = new HashMap<>();

    public <T extends Trainer> void registerModel(Class<T> trainerClass, SoftClassifier<double[]> model) {
        modelMap.put(trainerClass, model);
    }

    public <T extends Trainer> SoftClassifier<double[]> getModel(Class<T> trainerClass) {
        return modelMap.get(trainerClass);
    }

    public boolean hasModel(Class<? extends Trainer> trainerClass) {
        return modelMap.containsKey(trainerClass);
    }

    public Set<Class<? extends Trainer>> getRegisteredTrainerTypes() {
        return Collections.unmodifiableSet(modelMap.keySet());
    }
}
