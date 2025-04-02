package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.RevealCardFromTopOfDeckResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class RevealTopCardsOfDrawDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _count;

    public RevealTopCardsOfDrawDeckEffect(PhysicalCard source, String playerId, int count) {
        _source = source;
        _playerId = playerId;
        _count = count;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getDeck(_playerId).size() >= _count;
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
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        List<? extends PhysicalCard> deck = game.getGameState().getDeck(_playerId);
        int count = Math.min(deck.size(), _count);
        LinkedList<PhysicalCard> topCards = new LinkedList<>(deck.subList(0, count));
        if (!topCards.isEmpty()) {
            final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwner(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, _playerId + " revealed card(s) from top of deck", topCards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards from top of " + _playerId + " deck - " + getAppendedNames(topCards));
            for (PhysicalCard topCard : topCards) {
                game.getActionsEnvironment().emitEffectResult(
                        new RevealCardFromTopOfDeckResult(_playerId, topCard));
            }
        }
        cardsRevealed(topCards);
        return new FullEffectResult(topCards.size() == _count);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> revealedCards);
}
