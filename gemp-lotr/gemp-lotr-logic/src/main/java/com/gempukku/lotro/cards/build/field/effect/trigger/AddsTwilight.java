package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class AddsTwilight implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "cause");

        String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final AddTwilightEffect.Cause cause = FieldUtils.getEnum(AddTwilightEffect.Cause.class, value.get("cause"), "cause");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable source = sourceFilter.getFilterable(actionContext);
                return TriggerConditions.addedTwilight(actionContext.getGame(), actionContext.getEffectResult(), cause, source);
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
