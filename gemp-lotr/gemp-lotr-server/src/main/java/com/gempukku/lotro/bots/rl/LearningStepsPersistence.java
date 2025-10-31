package com.gempukku.lotro.bots.rl;

import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;

import java.util.List;

public interface LearningStepsPersistence {
    void save(List<LearningStep> steps);
    List<LearningStep> load(Trainer trainer);
}
