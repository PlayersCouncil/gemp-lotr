package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public abstract class ChooseCardsFromSingleStackEffect extends AbstractEffect {
    private final Action _action;
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final PhysicalCard _stackedOn;
    private final Filterable _stackedCardFilter;

    public ChooseCardsFromSingleStackEffect(Action action, String playerId, int minimum, int maximum, PhysicalCard stackedOn, Filterable stackedCardFilter) {
        _action = action;
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _stackedOn = stackedOn;
        _stackedCardFilter = stackedCardFilter;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Choose stacked card";
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.countActive(game, Filters.sameCard(_stackedOn), Filters.hasStacked(_stackedCardFilter)) > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final LotroGame game) {
        var stackedCards = Filters.filter(game, game.getGameState().getStackedCards(_stackedOn), _stackedCardFilter);

        int maximum = Math.min(_maximum, stackedCards.size());

        final boolean success = stackedCards.size() >= _minimum;

        if (stackedCards.size() <= _minimum) {
            cardsChosen(game, stackedCards);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, getText(game), stackedCards, _minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            var stackedCards = getSelectedCardsByResponse(result);
                            cardsChosen(game, stackedCards);
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsChosen(LotroGame game, Collection<PhysicalCard> stackedCards);
}
