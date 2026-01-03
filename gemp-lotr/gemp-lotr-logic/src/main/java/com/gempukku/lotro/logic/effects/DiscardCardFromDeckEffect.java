package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.DiscardCardFromDeckResult;

import java.util.Collections;

public class DiscardCardFromDeckEffect extends AbstractEffect {
    private final PhysicalCard _card;
    private final PhysicalCard _source;
    private final String _playerId;
    private final boolean _forced;

    public DiscardCardFromDeckEffect(PhysicalCard source, String playerId, PhysicalCard card, boolean forced) {
        _source = source;
        _playerId = playerId;
        _card = card;
        _forced = forced;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return _card.getZone() == Zone.DECK;
    }

    @Override
    public String getText(LotroGame game) {
        return "Discard " + GameUtils.getFullName(_card) + " from deck";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            gameState.addCardToZone(game, _card, Zone.DISCARD);
            gameState.sendMessage(GameUtils.getCardLink(_card) + " gets discarded from deck");

            game.getActionsEnvironment().emitEffectResult(new DiscardCardFromDeckResult(_source, _card, _forced));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
