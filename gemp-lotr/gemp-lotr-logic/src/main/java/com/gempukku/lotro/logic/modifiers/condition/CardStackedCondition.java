package com.gempukku.lotro.logic.modifiers.condition;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Condition;

public class CardStackedCondition implements Condition {
    private final Filter _stackedOn;
    private final Filter _cardStacked;
    private final int _minimumCount;

    public CardStackedCondition(int minimumCount, Filterable stackedOn, Filterable... cardStacked) {
        _minimumCount = minimumCount;
        _stackedOn = Filters.changeToFilter(stackedOn);
        _cardStacked = Filters.and(cardStacked);
    }

    @Override
    public boolean isFullfilled(LotroGame game) {
        return Filters.findFirstActive(game, _stackedOn, Filters.hasStacked(_minimumCount, _cardStacked)) != null;
    }
}
