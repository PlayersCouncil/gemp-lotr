package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.results.DrawCardOrPutIntoHandResult;

import java.util.Collections;

public class PutCardFromDiscardIntoHandEffect extends AbstractEffect {
    private final String _takingPlayer;
    private final PhysicalCard _card;

    /**
     * @param takingPlayer the player carrying the effect out, who need not be the owner of the card: a bar on
     *                     taking cards into hand is read for this player (RuleUtils.isBarredFromTakingIntoHand)
     */
    public PutCardFromDiscardIntoHandEffect(String takingPlayer, PhysicalCard card) {
        _takingPlayer = takingPlayer;
        _card = card;
    }

    @Override
    public String getText(LotroGame game) {
        return "Put card from discard into hand";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return _card.getZone() == Zone.DISCARD && !cantTakeIntoHand(game);
    }

    private boolean cantTakeIntoHand(LotroGame game) {
        return RuleUtils.isBarredFromTakingIntoHand(game, _takingPlayer, _card);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if ((!game.getFormat().hasRuleOfFour() || game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, _card.getOwner()))
                && _card.getZone() == Zone.DISCARD && !cantTakeIntoHand(game)) {
            GameState gameState = game.getGameState();
            gameState.sendMessage(_card.getOwner() + " puts " + GameUtils.getCardLink(_card) + " from discard into their hand");
            gameState.removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            gameState.addCardToZone(game, _card, Zone.HAND);
            game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_card.getOwner()));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
