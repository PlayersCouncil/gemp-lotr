package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class AboutToAddBurden implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize");

        String filter = FieldUtils.getString(value.get("filter"), "filter");
        String memory = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable source = sourceFilter.getFilterable(actionContext);
                boolean result = TriggerConditions.isAddingBurden(actionContext.getEffect(), actionContext.getGame(), source);
                if (result && memory != null) {
                    AddBurdenEffect addBurdenEffect = (AddBurdenEffect) actionContext.getEffect();
                    actionContext.setCardMemory(memory, addBurdenEffect.getSource());
                }
                return result;
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
