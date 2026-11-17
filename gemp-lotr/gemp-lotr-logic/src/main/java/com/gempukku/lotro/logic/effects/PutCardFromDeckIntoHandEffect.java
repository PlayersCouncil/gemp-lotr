package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.results.DrawCardOrPutIntoHandResult;

import java.util.Collections;

public class PutCardFromDeckIntoHandEffect extends AbstractEffect {
    private final String _takingPlayer;
    private final PhysicalCard _card;
    private final boolean _reveal;
    private final boolean _inPlaceOfDraw;

    /**
     * @param takingPlayer the player carrying the effect out, who need not be the owner of the card: a bar on
     *                     taking cards into hand is read for this player (RuleUtils.isBarredFromTakingIntoHand)
     */
    public PutCardFromDeckIntoHandEffect(String takingPlayer, PhysicalCard card, boolean reveal) {
        this(takingPlayer, card, reveal, false);
    }

    /**
     * @param inPlaceOfDraw true when the card is taken in place of a card its owner was about to draw ("about to
     *                      draw a card ... look at the top three cards of your draw deck instead. Take [one]
     *                      into your hand"): that card is the card drawn, for a text that answers a draw
     *                      (Q-0205: B).  Every other card taken from a draw deck is taken into hand and not drawn.
     */
    public PutCardFromDeckIntoHandEffect(String takingPlayer, PhysicalCard card, boolean reveal, boolean inPlaceOfDraw) {
        _takingPlayer = takingPlayer;
        _card = card;
        _reveal = reveal;
        _inPlaceOfDraw = inPlaceOfDraw;
    }

    @Override
    public String getText(LotroGame game) {
        return "Put card from deck into hand";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return _card.getZone() == Zone.DECK && !cantTakeIntoHand(game);
    }

    private boolean cantTakeIntoHand(LotroGame game) {
        return RuleUtils.isBarredFromTakingIntoHand(game, _takingPlayer, _card);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if ((!game.getFormat().hasRuleOfFour() || game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, _card.getOwner()))
                && _card.getZone() == Zone.DECK && !cantTakeIntoHand(game)) {
            GameState gameState = game.getGameState();
            if(_reveal) {
                gameState.sendMessage(_card.getOwner() + " puts " + GameUtils.getCardLink(_card) + " from deck into their hand");
            }
            else {
                gameState.sendMessage(_card.getOwner() + " puts a card from deck into their hand");
            }
            gameState.removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            gameState.addCardToZone(game, _card, Zone.HAND);
            game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_card.getOwner(), _inPlaceOfDraw));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
