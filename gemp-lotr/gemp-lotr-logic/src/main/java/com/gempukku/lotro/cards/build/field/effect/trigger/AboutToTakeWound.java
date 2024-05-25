package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class AboutToTakeWound implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "source", "filter", "memorize");

        String source = FieldUtils.getString(value.get("source"), "source", "any");
        String filter = FieldUtils.getString(value.get("filter"), "filter");
        String memory = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(source, environment);
        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable sourceFilterable = sourceFilter.getFilterable(actionContext);
                final Filterable affected = affectedFilter.getFilterable(actionContext);
                boolean result = getResult(actionContext, sourceFilterable, affected);
                if (result && memory != null) {
                    WoundCharactersEffect woundCharactersEffect = (WoundCharactersEffect) actionContext.getEffect();
                    actionContext.setCardMemory(memory, woundCharactersEffect.getAffectedCardsMinusPrevented(actionContext.getGame()));
                }
                return result;
            }

            private static boolean getResult(ActionContext actionContext, Filterable sourceFilterable, Filterable affected) {
                if (sourceFilterable == Filters.any)
                    return TriggerConditions.isGettingWounded(actionContext.getEffect(), actionContext.getGame(), affected);
                else
                    return TriggerConditions.isGettingWoundedBy(actionContext.getEffect(), actionContext.getGame(), sourceFilterable, affected);
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
