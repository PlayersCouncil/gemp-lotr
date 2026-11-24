package com.gempukku.lotro.bots.rl.fotrstarters.models;

import smile.classification.SoftClassifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ModelIO {
    private static final String MODEL_DIR = "bot-models";

    public static void saveModel(Class<?> trainerClass, SoftClassifier<double[]> model) {
        //(made when a model is first saved, not whenever this class is first used: reading models makes no folder)
        try {
            Files.createDirectories(Paths.get(MODEL_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create model directory", e);
        }
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
        return loadModel(new File(MODEL_DIR), trainerClass);
    }

    /** A model from a folder of them other than the server's own (a test's: the repository's bot-models). */
    public static SoftClassifier<double[]> loadModel(File folder, Class<?> trainerClass) {
        File file = new File(folder, trainerClass.getSimpleName() + ".model");
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
