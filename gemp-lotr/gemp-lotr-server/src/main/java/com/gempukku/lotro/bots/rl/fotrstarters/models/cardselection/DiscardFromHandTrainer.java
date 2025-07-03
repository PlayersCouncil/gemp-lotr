package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.common.Zone;

public class DiscardFromHandTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "discard";
    }

    @Override
    protected boolean useNotChosen() {
        return true;
    }

    @Override
    protected String getZoneString() {
        return Zone.HAND.getHumanReadable();
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return super.isStepRelevant(step) && !step.decision.getText().toLowerCase().contains("reconcile");
    }
}
