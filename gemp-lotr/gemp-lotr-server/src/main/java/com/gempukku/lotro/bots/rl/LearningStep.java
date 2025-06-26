package com.gempukku.lotro.bots.rl;

import com.gempukku.lotro.bots.rl.semanticaction.SemanticAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class LearningStep {
    public final double[] state;
    public final String playerId;
    public final SemanticAction action;
    public final AwaitingDecision decision;
    public double reward;

    public LearningStep(double[] state, SemanticAction action, String playerId, AwaitingDecision decision) {
        this.state = state;
        this.action = action;
        this.playerId = playerId;
        this.decision = decision;
    }
}
