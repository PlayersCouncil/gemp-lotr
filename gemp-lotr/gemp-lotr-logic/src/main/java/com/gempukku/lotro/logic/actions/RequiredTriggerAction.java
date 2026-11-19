package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.function.BooleanSupplier;

public class RequiredTriggerAction extends AbstractCostToEffectAction {
    private final PhysicalCard _physicalCard;

    private boolean _sentMessage;
    private String _message;
    // Asked when the trigger's turn comes to be carried out: whether a limit on its card's uses, which it was
    // within when it was raised, has been used up since by another trigger of the same event.
    private BooleanSupplier _spentCheck;

    public RequiredTriggerAction(PhysicalCard physicalCard) {
        _physicalCard = physicalCard;
        if (physicalCard != null) {
            setText("Required trigger from " + GameUtils.getCardLink(_physicalCard));
            _message = GameUtils.getCardLink(_physicalCard) + " required triggered effect is used";
        }
    }

    @Override
    public Type getType() {
        return Type.TRIGGER;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _physicalCard;
    }

    public void setMessage(String message) {
        _message = message;
    }

    public void setSpentCheck(BooleanSupplier spentCheck) {
        _spentCheck = spentCheck;
    }

    /**
     * Whether this trigger is no longer to be carried out: its card is limited to so many uses (a phase, a
     * turn, a site) and another trigger raised with it has taken the last of them.
     */
    public boolean isSpent() {
        return _spentCheck != null && _spentCheck.getAsBoolean();
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null)
                game.getGameState().activatedCard(getPerformingPlayer(), _physicalCard);
            if (_message != null)
                game.getGameState().sendMessage(_message);
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            Effect effect = getNextEffect();
            if (effect != null)
                return effect;
        }
        return null;
    }
}
