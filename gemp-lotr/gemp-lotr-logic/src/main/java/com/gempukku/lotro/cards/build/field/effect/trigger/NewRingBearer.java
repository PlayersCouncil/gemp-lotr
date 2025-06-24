package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.NewRingBearerResult;
import org.json.simple.JSONObject;

public class NewRingBearer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {

                if(actionContext.getEffectResult().getType() != EffectResult.Type.NEW_RING_BEARER)
                    return false;

                final Filterable filterable = filterableSource.getFilterable(actionContext);
                var result = (NewRingBearerResult) actionContext.getEffectResult();

                return Filters.and(filterable).accepts(actionContext.getGame(), result.getNewRingBearer());
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
