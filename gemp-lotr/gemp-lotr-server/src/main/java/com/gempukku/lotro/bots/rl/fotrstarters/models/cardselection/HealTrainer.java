package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

    public class HealTrainer extends AbstractCardSelectionTrainer {
        private static final String SANCTUARY_HEALING = "sanctuary healing";

        @Override
        protected String getTextTrigger() {
            return "to heal";
        }

        @Override
        public boolean isStepRelevant(LearningStep step) {
            // reuse base relevance check and add exclusion
            return super.isStepRelevant(step) && !step.decision.getText().toLowerCase().contains(SANCTUARY_HEALING);
        }

        @Override
        public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
            return super.appliesTo(gameState, decision, playerName) && !decision.getText().toLowerCase().contains(SANCTUARY_HEALING);
        }
    }
