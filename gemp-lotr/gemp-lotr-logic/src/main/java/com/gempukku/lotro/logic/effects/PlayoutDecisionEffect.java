package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.timing.AbstractSuccessfulEffect;
import com.gempukku.lotro.logic.timing.Effect;

public class PlayoutDecisionEffect extends AbstractSuccessfulEffect {
    private final String _playerId;
    private final AwaitingDecision _decision;

    public PlayoutDecisionEffect(String playerId, AwaitingDecision decision) {
        _playerId = playerId;
        _decision = decision;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public void playEffect(LotroGame game) {
        // A number that can only be one number is taken without asking (F-0147): "remove X threats" with one
        // threat to remove, "choose how many to spot" with none to spot.
        if (_decision instanceof IntegerAwaitingDecision numberDecision) {
            final Integer onlyAnswer = numberDecision.getOnlyAnswer();
            if (onlyAnswer != null) {
                try {
                    numberDecision.decisionMade(String.valueOf(onlyAnswer));
                    return;
                } catch (DecisionResultInvalidException exp) {
                    // The decision will not have its own only answer: let the player be asked after all.
                }
            }
        }
        game.getUserFeedback().sendAwaitingDecision(_playerId, _decision);
    }
}
