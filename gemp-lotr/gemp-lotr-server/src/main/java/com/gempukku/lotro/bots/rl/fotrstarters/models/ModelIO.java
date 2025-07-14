package com.gempukku.lotro.bots.rl.fotrstarters.models;

import smile.classification.SoftClassifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ModelIO {
    private static final String MODEL_DIR = "bot-models";

    static {
        try {
            Files.createDirectories(Paths.get(MODEL_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create model directory", e);
        }
    }

    public static void saveModel(Class<?> trainerClass, SoftClassifier<double[]> model) {
        File file = getModelFile(trainerClass);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(model);
            System.out.println("Saved model: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save model for " + trainerClass.getSimpleName());
            e.printStackTrace();
        }
    }

    public static SoftClassifier<double[]> loadModel(Class<?> trainerClass) {
        File file = getModelFile(trainerClass);
        if (!file.exists())
            return null;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            return (SoftClassifier<double[]>) obj;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Failed to load model for " + trainerClass.getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    private static File getModelFile(Class<?> trainerClass) {
        return new File(MODEL_DIR, trainerClass.getSimpleName() + ".model");
    }
}
