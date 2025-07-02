package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.LearningStepsPersistence;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection.*;
import com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice.BurdenTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.AnotherMoveTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.GoFirstTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.MulliganTrainer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FotrStartersLearningStepsPersistence implements LearningStepsPersistence {
    private static final Map<Trainer, String> trainerFileMap = new HashMap<>();

    static {
        trainerFileMap.put(new GoFirstTrainer(), "fotr-starters-go-first.jsonl");
        trainerFileMap.put(new MulliganTrainer(), "fotr-starters-mulligan.jsonl");
        trainerFileMap.put(new AnotherMoveTrainer(), "fotr-starters-another-move.jsonl");
        trainerFileMap.put(new BurdenTrainer(), "fotr-starters-burdens.jsonl");
        trainerFileMap.put(new ReconcileTrainer(), "fotr-starters-reconcile.jsonl");
        trainerFileMap.put(new SanctuaryTrainer(), "fotr-starters-sanctuary.jsonl");
        trainerFileMap.put(new ArcheryWoundTrainer(), "fotr-starters-archery.jsonl");
        trainerFileMap.put(new AttachItemTrainer(), "fotr-starters-attach-item.jsonl");
        trainerFileMap.put(new SkirmishOrderTrainer(), "fotr-starters-skirmish-order.jsonl");
        trainerFileMap.put(new HealTrainer(), "fotr-starters-heal.jsonl");
        // Add other trainers here when needed
    }

    @Override
    public void save(List<LearningStep> steps) {
        for (Map.Entry<Trainer, String> entry : trainerFileMap.entrySet()) {
            Trainer trainer = entry.getKey();
            String filename = entry.getValue();

            saveFilteredSteps(filename, trainer, steps);
        }
        try (FileWriter fw = new FileWriter("fotr-starters-all.jsonl", true)) {
            for (LearningStep step : steps) {
                    fw.write(step.toJson() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFilteredSteps(String filename, Trainer trainer, List<LearningStep> steps) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            for (LearningStep step : steps) {
                if (trainer.isStepRelevant(step)) {
                    fw.write(step.toJson() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<LearningStep> load(Trainer trainer) {
        List<LearningStep> steps = new ArrayList<>();

        trainerFileMap.forEach((trainerInMap, fileName) -> {
            if (trainerInMap.equals(trainer)) {
                try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        LearningStep step = LearningStep.fromJson(line);
                        steps.add(step);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return steps;
    }
}
