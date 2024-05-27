package com.gempukku.lotro.logic.modifiers.evaluator;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CountStackedEvaluator implements Evaluator {
    private final Filterable _stackedOn;
    private final Filterable[] _stackedCard;
    private Integer _limit;

    public CountStackedEvaluator(Filterable stackedOn, Filterable... stackedCard) {
        _stackedOn = stackedOn;
        _stackedCard = stackedCard;
    }

    @Override
    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
        int count = 0;
        for (PhysicalCard card : Filters.filterActive(game, _stackedOn)) {
            count += Filters.filter(game.getGameState().getStackedCards(card), game, _stackedCard).size();
        }
        if (_limit != null)
            return Math.min(_limit, count);
        return count;
    }
}
