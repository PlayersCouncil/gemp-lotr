package com.gempukku.lotro.bots.rl.fotrstarters;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.LearningStepsPersistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FotrStartersLearningStepsPersistence implements LearningStepsPersistence {
    private static final String FILE_NAME = "fotr-starters-steps.jsonl";

    @Override
    public void save(List<LearningStep> steps) {
        try (FileWriter fw = new FileWriter(FILE_NAME, true)) {
            for (LearningStep step : steps) {
                    fw.write(step.toJson() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<LearningStep> load() {
        List<LearningStep> steps = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                LearningStep step = JSON.parseObject(line, LearningStep.class);
                steps.add(step);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Loaded " + steps.size());
        return steps;
    }
}
