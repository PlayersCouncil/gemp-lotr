package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.DiscardCardFromHandResult;

import java.util.Collections;
import java.util.List;

public class DiscardCardAtRandomFromHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final boolean _forced;

    public DiscardCardAtRandomFromHandEffect(PhysicalCard source, String playerId, boolean forced) {
        _source = source;
        _playerId = playerId;
        _forced = forced;
    }

    @Override
    public String getText(LotroGame game) {
        return "Discard card at random from hand";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getHand(_playerId).size() > 0
                && (!_forced || game.getModifiersQuerying().canDiscardCardsFromHand(game, _playerId, _source));
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            List<? extends PhysicalCard> hand = gameState.getHand(_playerId);
            PhysicalCard randomCard = GameUtils.getRandomCards(game, _playerId, Zone.HAND, hand, 1).getFirst();
            gameState.sendMessage(_playerId + " randomly discards " + GameUtils.getCardLink(randomCard));
            gameState.removeCardsFromZone(_source.getOwner(), Collections.singleton(randomCard));
            gameState.addCardToZone(game, randomCard, Zone.DISCARD);
            game.getActionsEnvironment().emitEffectResult(new DiscardCardFromHandResult(_source, randomCard, _playerId, _forced));
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
