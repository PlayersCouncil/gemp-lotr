package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class CanPlayFromDiscard implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "discount", "filter");

        final ValueSource discountValue = ValueResolver.resolveEvaluator(object.get("discount"), 0, environment);
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            final int discount = discountValue.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            return PlayConditions.canPlayFromDiscard(actionContext.getPerformingPlayer(), actionContext.getGame(),
                    discount, filterableSource.getFilterable(actionContext));
        };
    }
}
