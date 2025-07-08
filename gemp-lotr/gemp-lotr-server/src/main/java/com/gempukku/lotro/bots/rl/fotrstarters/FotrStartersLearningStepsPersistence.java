package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.LearningStepsPersistence;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.CardFromDiscardTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.StartingFellowshipTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.assignment.FpAssignmentTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.assignment.ShadowAssignmentTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction.*;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FotrStartersLearningStepsPersistence implements LearningStepsPersistence {
    private static final List<Class<? extends Trainer>> trainerClasses = List.of(
            GoFirstTrainer.class,
            MulliganTrainer.class,
            AnotherMoveTrainer.class,
            BurdenTrainer.class,
            ReconcileTrainer.class,
            SanctuaryTrainer.class,
            ArcheryWoundTrainer.class,
            AttachItemTrainer.class,
            SkirmishOrderTrainer.class,
            HealTrainer.class,
            DiscardFromHandTrainer.class,
            ExertTrainer.class,
            DiscardFromPlayTrainer.class,
            PlayFromHandTrainer.class,
            FallBackCardSelectionTrainer.class,
            CardFromDiscardTrainer.class,
            StartingFellowshipTrainer.class,
            FellowshipCardActionTrainer.class,
            ShadowCardActionTrainer.class,
            ManeuverCardActionTrainer.class,
            SkirmishCardActionTrainer.class,
            RegroupCardActionTrainer.class,
            OptionalResponsesCardActionTrainer.class,
            ShadowAssignmentTrainer.class,
            FpAssignmentTrainer.class
    );

    private static final Map<Class<? extends Trainer>, String> trainerFileMap =
            trainerClasses.stream().collect(Collectors.toMap(
                    Function.identity(),
                    FotrStartersLearningStepsPersistence::generateFileName
            ));

    private static String generateFileName(Class<? extends Trainer> cls) {
        String base = cls.getSimpleName()
                .replaceAll("Trainer$", "") // Remove "Trainer" suffix
                .replaceAll("([a-z])([A-Z])", "$1-$2") // CamelCase to kebab-case
                .toLowerCase();
        return "fotr-starters-" + base + ".jsonl";
    }

    @Override
    public void save(List<LearningStep> steps) {
        for (Map.Entry<Class<? extends Trainer>, String> entry : trainerFileMap.entrySet()) {
            Class<? extends Trainer> trainerClass = entry.getKey();
            String filename = entry.getValue();

            try {
                Trainer trainer = trainerClass.getDeclaredConstructor().newInstance(); // assumes default constructor
                saveFilteredSteps(filename, trainer, steps);
            } catch (Exception e) {
                e.printStackTrace(); // or log
            }
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

    public List<LearningStep> load(Trainer trainer) {
        String fileName = trainerFileMap.get(trainer.getClass());
        if (fileName == null)
            return List.of();

        List<LearningStep> steps = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                steps.add(LearningStep.fromJson(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return steps;
    }
}
