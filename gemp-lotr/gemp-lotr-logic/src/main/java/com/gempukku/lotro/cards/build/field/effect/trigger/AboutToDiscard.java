package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class AboutToDiscard implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "source", "filter", "opponent");

        String source = FieldUtils.getString(value.get("source"), "source", "any");
        String filter = FieldUtils.getString(value.get("filter"), "filter");
        boolean opponent = FieldUtils.getBoolean(value.get("opponent"), "opponent", false);

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(source, environment);
        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                if (opponent) {
                    return TriggerConditions.isGettingDiscardedByOpponent(actionContext.getEffect(), actionContext.getGame(),
                            actionContext.getPerformingPlayer(),
                            sourceFilter.getFilterable(actionContext),
                            affectedFilter.getFilterable(actionContext));
                } else {
                    return TriggerConditions.isGettingDiscardedBy(actionContext.getEffect(), actionContext.getGame(),
                            sourceFilter.getFilterable(actionContext),
                            affectedFilter.getFilterable(actionContext));
                }
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
