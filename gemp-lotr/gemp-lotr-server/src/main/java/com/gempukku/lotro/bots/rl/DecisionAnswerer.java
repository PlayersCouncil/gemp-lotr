package com.gempukku.lotro.bots.rl;

import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface DecisionAnswerer {
    boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName);
    String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry);

     class ScoredCard {
        public String cardId;
        public double score;

        public ScoredCard(String cardId, double score) {
            this.cardId = cardId;
            this.score = score;
        }

        @Override
        public String toString() {
            return "ScoredCard{" +
                    "cardId='" + cardId + '\'' +
                    ", score=" + score +
                    '}';
        }
    }
}
