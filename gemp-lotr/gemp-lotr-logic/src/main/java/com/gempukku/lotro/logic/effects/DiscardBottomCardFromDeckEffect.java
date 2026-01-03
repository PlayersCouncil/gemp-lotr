package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.DiscardCardFromDeckResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardBottomCardFromDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _count;
    private final boolean _forced;

    public DiscardBottomCardFromDeckEffect(PhysicalCard source, String playerId, int count, boolean forced) {
        _source = source;
        _playerId = playerId;
        _count = count;
        _forced = forced;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getDeck(_playerId).size() >= _count
                && (!_forced || game.getModifiersQuerying().canDiscardCardsFromTopOfDeck(game, _playerId, _source));
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        //Compare DiscardTopCardFromDeckEffect; may need to alter this if later an effect is created
        // that prevents the ability of people to discard from the bottom of the deck
        if (!_forced || game.getGameState().getDeck(_playerId).size() > _count) {
            GameState gameState = game.getGameState();
            List<PhysicalCard> cardsDiscarded = new LinkedList<>();

            for (int i = 0; i < _count; i++) {
                PhysicalCard card = gameState.removeBottomDeckCard(_playerId);
                if (card != null) {
                    cardsDiscarded.add(card);
                    gameState.addCardToZone(game, card, Zone.DISCARD);
                }
            }

            if (cardsDiscarded.size() > 0) {
                gameState.sendMessage(_playerId + " discards bottom cards from their deck - " + getAppendedNames(cardsDiscarded));
                cardsDiscardedCallback(cardsDiscarded);
            }

            for (PhysicalCard discardedCard : cardsDiscarded) {
                game.getActionsEnvironment().emitEffectResult(new DiscardCardFromDeckResult(_source, discardedCard, _forced));
            }

            return new FullEffectResult(_count == cardsDiscarded.size());
        }
        return new FullEffectResult(false);
    }

    protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {

    }
}
