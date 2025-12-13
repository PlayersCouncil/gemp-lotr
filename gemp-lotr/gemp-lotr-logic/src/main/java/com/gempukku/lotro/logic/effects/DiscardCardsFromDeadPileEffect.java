package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.results.DiscardCardFromDeadPileResult;

import java.util.Collection;
import java.util.HashSet;

public class DiscardCardsFromDeadPileEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final Collection<? extends PhysicalCard> _cards;
    public DiscardCardsFromDeadPileEffect(PhysicalCard source, String playerId, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _playerId = playerId;
        _cards = cards;
    }

    @Override
    public String getText(LotroGame game) {
        return "Discard from dead pile - " + getAppendedTextNames(_cards);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        for (PhysicalCard card : _cards) {
            if (card.getZone() != Zone.DEAD)
                return false;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        var gameState = game.getGameState();

        var discardedCards = new HashSet<PhysicalCard>();
        for (PhysicalCard card : _cards) {
            if (card.getZone() == Zone.DEAD) {
                discardedCards.add(card);
            }
        }

        if (!discardedCards.isEmpty()) {
            gameState.sendMessage(getAppendedNames(discardedCards) + " " + GameUtils.be(discardedCards) + " discarded from the dead pile");
        }
        String sourcePlayer = null;
        if (_source != null) {
            sourcePlayer = _source.getOwner();
        }
        gameState.removeCardsFromZone(sourcePlayer, discardedCards);
        for (PhysicalCard card : discardedCards) {
            gameState.addCardToZone(game, card, Zone.DISCARD);
            game.getActionsEnvironment().emitEffectResult(new DiscardCardFromDeadPileResult(_source, card, _playerId));
        }

        return new FullEffectResult(discardedCards.size() == _cards.size());
    }
}
