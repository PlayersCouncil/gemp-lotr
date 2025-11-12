package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class CanSpot implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "count", "hindered", "filter");

        final int count = FieldUtils.getInteger(object.get("count"), "count", 1);
        final boolean hindered = FieldUtils.getBoolean(object.get("hindered"), "hindered", false);
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            if(hindered) {
                return PlayConditions.canSpot(actionContext.getGame(), count, SpotOverride.INCLUDE_HINDERED, filterable);
            }
            else {
                return PlayConditions.canSpot(actionContext.getGame(), count, SpotOverride.NONE, filterable);
            }

        };
    }
}
