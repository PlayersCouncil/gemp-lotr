package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collections;
import java.util.List;

public class PutRandomCardFromHandOnBottomOfDeckEffect extends AbstractEffect {
    private final String _playerId;

    public PutRandomCardFromHandOnBottomOfDeckEffect(String playerId) {
        _playerId = playerId;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getHand(_playerId).size() > 0;
    }

    @Override
    public String getText(LotroGame game) {
        return "Put random card from hand on bottom of deck";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            final List<PhysicalCard> randomCards = GameUtils.getRandomCards(game, _playerId, Zone.HAND, gameState.getHand(_playerId), 1);
            for (PhysicalCard randomCard : randomCards) {
                gameState.sendMessage(randomCard.getOwner() + " puts a card at random from hand on bottom of their deck");
                gameState.removeCardsFromZone(randomCard.getOwner(), Collections.singleton(randomCard));
                gameState.putCardOnBottomOfDeck(randomCard);
                putCardFromHandOnBottomOfDeckCallback(randomCard);
            }

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    protected void putCardFromHandOnBottomOfDeckCallback(PhysicalCard card) {

    }
}
