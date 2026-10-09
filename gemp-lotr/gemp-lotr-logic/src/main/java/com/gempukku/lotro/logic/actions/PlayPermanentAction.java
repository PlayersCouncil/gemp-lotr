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

    private PlayCardEffect _playCardEffect;
    private boolean _cardPlayed;

    private boolean _playCompleted;
    private boolean _cardStayedInPlay;

    private boolean _cardDiscarded;

    private boolean _discountResolved;
    private boolean _discountApplied;

    private final Zone _fromZone;
    private final Zone _toZone;
    private PhysicalCard _playedFromCard;

    private boolean _deckLookedThrough;

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

    /**
     * Says that the player has looked through the draw deck this card is being played from.  The look is over once
     * the card has been taken out of the deck, and that is when the deck is reshuffled (CR 4.2 "draw deck": "When you
     * finish looking through it, reshuffle it"): before the card is paid for, and so before the card's own text or
     * anything else answers its play.  A card played from a deck that nobody has said was looked through leaves the
     * rest of the deck in order: a card revealed from the top or the bottom of a deck and then played, and the cards
     * of a starting fellowship, whose deck is shuffled once the last of them is down.
     */
    public void setDeckLookedThrough() {
        _deckLookedThrough = true;
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
            if (playedFromZone == Zone.DECK && _deckLookedThrough) {
                game.getGameState().sendMessage(_permanentPlayed.getOwner() + " shuffles their deck");
                game.getGameState().shuffleDeck(_permanentPlayed.getOwner());
            }
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

        // The card has been placed: its own text on entering play is given its turn, and then its play is
        // announced, each once everything that answered the step before has finished and whatever else has
        // happened meanwhile.
        if (_cardPlayed) {
            Effect step = _playCardEffect.nextStep();
            if (step != null)
                return step;
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

            completePlay(game);
        } else {
            if (!_cardDiscarded) {
                _cardDiscarded = true;
                game.getGameState().removeCardsFromZone(_permanentPlayed.getOwner(), Collections.singleton(_permanentPlayed));
                game.getGameState().addCardToZone(game, _permanentPlayed, Zone.DISCARD);
            }
        }

        return null;
    }

    /**
     * The last step of playing a card (CR 4.2 "playing a card": "The card is played. ... other cards are in play").
     * It is reached once every response to the play has finished, and is where the play is judged: a card that has
     * left play by now, by whatever means, was not played (and its play was never announced if the card had
     * already gone when it came to be).
     */
    private void completePlay(LotroGame game) {
        if (!_playCompleted) {
            _playCompleted = true;
            _cardStayedInPlay = _playCardEffect.completePlay(game);
        }
    }

    /**
     * Whether this play happened, for whatever depends on it: an ability whose cost was to play the card, an "if
     * you do".  It did only if the card is still in play when its play is complete, that is after every response
     * to it has finished.  The twilight and whatever else was paid to play the card stay paid either way.
     */
    @Override
    public boolean wasCarriedOut() {
        return _playCompleted && _cardStayedInPlay;
    }
}
