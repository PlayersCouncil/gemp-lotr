package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class ChooseStackedCardsEffect extends AbstractEffect {
    private final Action _action;
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filterable _stackedOnFilter;
    private final Filterable _stackedCardFilter;

    public ChooseStackedCardsEffect(Action action, String playerId, int minimum, int maximum, Filterable stackedOnFilter, Filterable stackedCardFilter) {
        _action = action;
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _stackedOnFilter = stackedOnFilter;
        _stackedCardFilter = stackedCardFilter;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Choose stacked card";
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.countActive(game, _stackedOnFilter, Filters.hasStacked(_stackedCardFilter)) > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final LotroGame game) {
        List<PhysicalCard> stackedCards = new LinkedList<>();

        for (PhysicalCard stackedOnCard : Filters.filterActive(game, _stackedOnFilter))
            stackedCards.addAll(Filters.filter(game, game.getGameState().getStackedCards(stackedOnCard), _stackedCardFilter));

        int maximum = Math.min(_maximum, stackedCards.size());

        final boolean success = stackedCards.size() >= _minimum;

        if (stackedCards.size() <= _minimum) {
            cardsChosen(game, stackedCards);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, getText(game), stackedCards, _minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> stackedCards = getSelectedCardsByResponse(result);
                            cardsChosen(game, stackedCards);
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsChosen(LotroGame game, Collection<PhysicalCard> stackedCards);
}
