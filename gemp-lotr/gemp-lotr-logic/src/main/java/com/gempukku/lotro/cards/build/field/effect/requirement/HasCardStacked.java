package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class HasCardStacked implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "count", "filter", "on");

        final int count = FieldUtils.getInteger(object.get("count"), "count", 1);
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String onFilter = FieldUtils.getString(object.get("on"), "on", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(onFilter, environment);
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
            LotroGame game = actionContext.getGame();
            final Collection<PhysicalCard> matchingStackedOn = Filters.filterActive(game, onFilterable);
            for (PhysicalCard stackedOnCard : matchingStackedOn) {
                if (Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, new Filterable[]{filterable}).size() >= count)
                    return true;
            }
            return false;
        };
    }
}
