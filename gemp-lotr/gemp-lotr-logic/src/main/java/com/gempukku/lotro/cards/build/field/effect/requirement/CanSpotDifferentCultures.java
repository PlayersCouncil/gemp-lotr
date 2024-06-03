package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import org.json.simple.JSONObject;

import java.util.HashSet;

public class CanSpotDifferentCultures implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "count");

        final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
        final int amount = FieldUtils.getInteger(object.get("count"), "count");

        FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            LotroGame game = actionContext.getGame();
            Filterable filterable = filterableSource.getFilterable(actionContext);
            var foundCultures = new HashSet<Culture>();

            var cards = Filters.filterActive(game, filterable);
            for(var card : cards) {
                foundCultures.add(card.getBlueprint().getCulture());
            }

            return foundCultures.size() >= amount;
        };
    }
}
