package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class DiscardFromHandTrainer extends AbstractCardSelectionTrainer {
    private static final String RECONCILE = "reconcile";

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
        return super.isStepRelevant(step) && !step.decision.getText().toLowerCase().contains(RECONCILE);
    }

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return super.appliesTo(gameState, decision, playerName) && !decision.getText().toLowerCase().contains(RECONCILE);
    }
}
