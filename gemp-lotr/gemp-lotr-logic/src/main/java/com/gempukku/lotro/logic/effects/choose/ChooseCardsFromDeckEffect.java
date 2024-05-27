package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public abstract class ChooseCardsFromDeckEffect extends AbstractEffect {
    private final String _playerId;
    private final String _deckId;
    private final boolean showAll;
    private final int _minimum;
    private final int _maximum;
    private final Filter _filter;

    public ChooseCardsFromDeckEffect(String playerId, String deckId, boolean showAll, int minimum, int maximum, Filterable... filters) {
        _playerId = playerId;
        _deckId = deckId;
        this.showAll = showAll;
        _minimum = minimum;
        _maximum = maximum;
        _filter = Filters.and(filters);
    }

    @Override
    public String getText(LotroGame game) {
        return "Choose card from deck";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game.getGameState().getDeck(_deckId), game, _filter);
        return cards.size() >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final LotroGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game.getGameState().getDeck(_deckId), game, _filter);

        boolean success = cards.size() >= _minimum;

        int minimum = Math.min(_minimum, cards.size());

        if (_maximum == 0) {
            cardsSelected(game, Collections.emptySet());
        } else if (cards.size() == minimum) {
            cardsSelected(game, cards);
        } else {
            if (showAll) {
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new ArbitraryCardsSelectionDecision(1, "Choose cards from deck", game.getGameState().getDeck(_deckId), new LinkedList<>(cards), minimum, _maximum) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                cardsSelected(game, getSelectedCardsByResponse(result));
                            }
                        });
            } else {
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new ArbitraryCardsSelectionDecision(1, "Choose cards from deck", new LinkedList<>(cards), minimum, _maximum) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                cardsSelected(game, getSelectedCardsByResponse(result));
                            }
                        });
            }
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsSelected(LotroGame game, Collection<PhysicalCard> cards);
}
