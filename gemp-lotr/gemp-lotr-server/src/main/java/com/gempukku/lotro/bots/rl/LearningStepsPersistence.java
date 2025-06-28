package com.gempukku.lotro.bots.rl;

import java.util.List;

public interface LearningStepsPersistence {
    void save(List<LearningStep> steps);
    List<LearningStep> load();
}
