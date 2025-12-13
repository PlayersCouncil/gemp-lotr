package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.results.RevealedCardFromDeckResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class RevealBottomCardsOfDrawDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _deck;
    private final int _count;

    public RevealBottomCardsOfDrawDeckEffect(PhysicalCard source, String deck, int count) {
        _source = source;
        _deck = deck;
        _count = count;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getDeck(_deck).size() >= _count;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        List<? extends PhysicalCard> deck = game.getGameState().getDeck(_deck);
        int count = Math.min(deck.size(), _count);
        LinkedList<PhysicalCard> bottomCards = new LinkedList<>(deck.subList(deck.size() - count, deck.size()));

        if (!bottomCards.isEmpty()) {
            final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwner(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, _deck +" revealed card(s) from bottom of deck", bottomCards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards from bottom of " + _deck + " deck - " + getAppendedNames(bottomCards));

            for (var card : bottomCards) {
                game.getActionsEnvironment().emitEffectResult(new RevealedCardFromDeckResult(_deck, _source, card));
            }
        }
        cardsRevealed(bottomCards);
        return new FullEffectResult(bottomCards.size() == _count);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> cards);
}
