package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.DrawCardOrPutIntoHandResult;

import java.util.Collections;

public class PutCardFromDiscardIntoHandEffect extends AbstractEffect {
    private final PhysicalCard _card;

    public PutCardFromDiscardIntoHandEffect(PhysicalCard card) {
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
        return game.getModifiersQuerying().hasFlagActive(game,
                ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, _card.getOwner());
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
