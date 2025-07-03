package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

public class GoFirstTrainer extends AbstractMultipleChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return "Go first"; // Not used
    }

    @Override
    protected String getPositiveOption() {
        return "Go first";
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        if (!(step.action instanceof MultipleChoiceAction)) {
            return false;
        }

        String[] options = step.decision.getDecisionParameters().get("results");
        for (String option : options) {
            if (option.equalsIgnoreCase(getPositiveOption())) {
                return true;
            }
        }
        return false;
    }
}
