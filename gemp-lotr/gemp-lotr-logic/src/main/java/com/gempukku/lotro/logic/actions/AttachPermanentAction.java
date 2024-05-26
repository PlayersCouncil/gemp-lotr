package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collections;

public class AttachPermanentAction extends AbstractCostToEffectAction {
    private final PhysicalCard _cardToAttach;

    private boolean _cardRemoved;

    private final ChooseActiveCardEffect _chooseTargetEffect;
    private boolean _targetChosen;

    private Effect _playCardEffect;
    private boolean _cardPlayed;

    private boolean _cardDiscarded;

    private boolean _exertTarget;

    private boolean _discountResolved;
    private boolean _discountApplied;

    private int _twilightModifier;
    private final Zone _playedFrom;
    private PhysicalCard _target;

    public AttachPermanentAction(final LotroGame game, final PhysicalCard card, Filter filter, final int twilightModifier) {
        _cardToAttach = card;
        setText("Play " + GameUtils.getFullName(_cardToAttach));
        _playedFrom = card.getZone();
        _twilightModifier = twilightModifier;

        _chooseTargetEffect =
                new ChooseActiveCardEffect(null, card.getOwner(), "Attach " + GameUtils.getFullName(card) + ". Choose target to attach to", filter) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard target) {
                        _target = target;
                        if (_exertTarget) {
                            appendCost(
                                    new ExertCharactersEffect(AttachPermanentAction.this, target, target));
                        }

                        game.getGameState().sendMessage(card.getOwner() + " plays " + GameUtils.getCardLink(card) + " from " + _playedFrom.getHumanReadable() + " on " + GameUtils.getCardLink(target));
                    }
                };
    }

    @Override
    public Type getType() {
        return Type.PLAY_CARD;
    }

    public PhysicalCard getTarget() {
        return _target;
    }

    public void setExertTarget(boolean exertTarget) {
        _exertTarget = exertTarget;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _cardToAttach;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _cardToAttach;
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _cardToAttach.getZone();
            game.getGameState().removeCardsFromZone(_cardToAttach.getOwner(), Collections.singleton(_cardToAttach));
            if (playedFromZone == Zone.HAND)
                game.getGameState().addCardToZone(game, _cardToAttach, Zone.VOID_FROM_HAND);
            else
                game.getGameState().addCardToZone(game, _cardToAttach, Zone.VOID);
            if (playedFromZone == Zone.DECK) {
                game.getGameState().sendMessage(_cardToAttach.getOwner() + " shuffles their deck");
                game.getGameState().shuffleDeck(_cardToAttach.getOwner());
            }
        }

        if (!_targetChosen) {
            _targetChosen = true;
            return _chooseTargetEffect;
        }

        if (!_discountResolved) {
            final DiscountEffect discount = getNextPotentialDiscount();
            if (discount != null) {
                if (_cardToAttach.getBlueprint().getSide() == Side.SHADOW) {
                    int twilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, _cardToAttach, _target, _twilightModifier, false);
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
            insertCost(new PayPlayOnTwilightCostEffect(_cardToAttach, _target, _twilightModifier));
        }

        if (_target != null) {
            if (!isCostFailed()) {
                Effect cost = getNextCost();
                if (cost != null)
                    return cost;

                if (!_cardPlayed) {
                    _cardPlayed = true;
                    _playCardEffect = new PlayCardEffect(_cardToAttach.getOwner(), _playedFrom, _cardToAttach, _target, null, isPaidToil());

                    return _playCardEffect;
                }

                Effect effect = getNextEffect();
                if (effect != null)
                    return effect;
            } else {
                if (!_cardDiscarded) {
                    _cardDiscarded = true;
                    game.getGameState().removeCardsFromZone(_cardToAttach.getOwner(), Collections.singleton(_cardToAttach));
                    game.getGameState().addCardToZone(game, _cardToAttach, Zone.DISCARD);
                }
            }
        } else {
            if (!_cardDiscarded) {
                _cardDiscarded = true;
                game.getGameState().removeCardsFromZone(_cardToAttach.getOwner(), Collections.singleton(_cardToAttach));
                game.getGameState().addCardToZone(game, _cardToAttach, Zone.DISCARD);
            }
        }

        return null;
    }
}
