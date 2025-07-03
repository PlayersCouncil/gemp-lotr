package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;

public class HealTrainer extends AbstractCardSelectionTrainer {

    @Override
    protected String getTextTrigger() {
        return "to heal";
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        // reuse base relevance check and add exclusion
        return super.isStepRelevant(step) && !step.decision.getText().toLowerCase().contains("sanctuary healing");
    }
}
