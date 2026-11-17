package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.Effect;

/**
 * The action of a card whose own text lets it be played from somewhere other than the hand, where that text is not
 * a special ability ("If you have initiative during the Shadow phase, you may play this minion from your discard
 * pile").  It is offered among the phase actions as a special ability used from that place is, and pays any costs
 * the text names before the card is played, but it is a way of playing the card: it is no ActivateCardAction, so
 * no ActivateCardResult is emitted for "a special ability is used" to answer, prevent or cancel, and its type is
 * PLAY_CARD, so a ban on special abilities does not stop it and a ban on playing the card does.
 */
public class PlayPermissionAction extends AbstractCostToEffectAction {
    private final PhysicalCard _physicalCard;
    private final String _player;

    public PlayPermissionAction(PhysicalCard physicalCard, String performingPlayer) {
        _physicalCard = physicalCard;
        _player = performingPlayer;
        // Worded as an ActivateCardAction is, so that the player is shown what he was shown before.
        setText("Use " + GameUtils.getFullName(_physicalCard));
    }

    @Override
    public String getPerformingPlayer() {
        return _player;
    }

    @Override
    public Type getType() {
        return Type.PLAY_CARD;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _physicalCard;
    }

    @Override
    public Effect nextEffect(LotroGame game) {
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
