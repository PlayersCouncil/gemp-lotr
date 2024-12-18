package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.effects.PayTwilightCostEffect;
import com.gempukku.lotro.logic.effects.PlayCardEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collections;

public class PlayPermanentAction extends AbstractCostToEffectAction {
    private final PhysicalCard _permanentPlayed;
    private int _twilightModifier;
    private final boolean _ignoreRoamingPenalty;

    private boolean _cardRemoved;

    private Effect _playCardEffect;
    private boolean _cardPlayed;

    private boolean _cardDiscarded;

    private boolean _discountResolved;
    private boolean _discountApplied;

    private final Zone _fromZone;
    private final Zone _toZone;
    private PhysicalCard _playedFromCard;

    public PlayPermanentAction(PhysicalCard card, Zone zone, int twilightModifier, boolean ignoreRoamingPenalty) {
        _permanentPlayed = card;
        setText("Play " + GameUtils.getFullName(_permanentPlayed));
        setPerformingPlayer(card.getOwner());
        _twilightModifier = twilightModifier;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;

        if (card.getZone() == Zone.STACKED)
            _playedFromCard = card.getStackedOn();
        else if (card.getZone() == Zone.ATTACHED)
            _playedFromCard = card.getAttachedTo();

        _fromZone = card.getZone();
        _toZone = zone;
    }

    @Override
    public Type getType() {
        return Type.PLAY_CARD;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _permanentPlayed;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _permanentPlayed;
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _permanentPlayed.getZone();
            _permanentPlayed.setPlayedFromZone(playedFromZone);

            game.getGameState().sendMessage(_permanentPlayed.getOwner() + " plays " + GameUtils.getCardLink(_permanentPlayed) + " from " + playedFromZone.getHumanReadable());
            game.getGameState().removeCardsFromZone(_permanentPlayed.getOwner(), Collections.singleton(_permanentPlayed));
            if (playedFromZone == Zone.HAND)
                game.getGameState().addCardToZone(game, _permanentPlayed, Zone.VOID_FROM_HAND);
            else
                game.getGameState().addCardToZone(game, _permanentPlayed, Zone.VOID);
        }

        if (!_discountResolved) {
            final DiscountEffect discount = getNextPotentialDiscount();
            if (discount != null) {
                if (_permanentPlayed.getBlueprint().getSide() == Side.SHADOW) {
                    int twilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, _permanentPlayed, null, _twilightModifier, _ignoreRoamingPenalty);
                    int requiredDiscount = Math.max(0, twilightCost - game.getGameState().getTwilightPool() - getProcessedDiscount() - getPotentialDiscount(game));
                    discount.setMinimalRequiredDiscount(requiredDiscount);
                }
                return discount;
            } else {
                _discountResolved = true;
            }
        }

        if (!_discountApplied) {
            _discountApplied = true;
            _twilightModifier -= getProcessedDiscount();
            insertCost(new PayTwilightCostEffect(_permanentPlayed, _twilightModifier, _ignoreRoamingPenalty));
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_cardPlayed) {
                _cardPlayed = true;
                _playCardEffect = new PlayCardEffect(_permanentPlayed.getOwner(), _fromZone, _permanentPlayed, _toZone, _playedFromCard, isPaidToil());
                return _playCardEffect;
            }

            Effect effect = getNextEffect();
            if (effect != null)
                return effect;
        } else {
            if (!_cardDiscarded) {
                _cardDiscarded = true;
                game.getGameState().removeCardsFromZone(_permanentPlayed.getOwner(), Collections.singleton(_permanentPlayed));
                game.getGameState().addCardToZone(game, _permanentPlayed, Zone.DISCARD);
            }
        }

        return null;
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect != null && _playCardEffect.wasCarriedOut();
    }
}
