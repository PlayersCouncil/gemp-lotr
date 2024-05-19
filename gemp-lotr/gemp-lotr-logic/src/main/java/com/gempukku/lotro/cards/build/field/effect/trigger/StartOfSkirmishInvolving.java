package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class StartOfSkirmishInvolving implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");
        final String filterString = FieldUtils.getString(value.get("filter"), "filter");
        final FilterableSource filter = environment.getFilterFactory().generateFilter(filterString, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.startOfPhase(actionContext.getGame(), actionContext.getEffectResult(), Phase.SKIRMISH)
                        && Filters.countActive(actionContext.getGame(), Filters.inSkirmish, filter.getFilterable(actionContext)) > 0;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
