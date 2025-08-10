package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
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
    protected String getNegativeOption() {
        return "Go second";
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

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        String[] options = decision.getDecisionParameters().get("results");
        for (String option : options) {
            if (option.equalsIgnoreCase(getPositiveOption())) {
                return true;
            }
        }
        return false;
    }
}
