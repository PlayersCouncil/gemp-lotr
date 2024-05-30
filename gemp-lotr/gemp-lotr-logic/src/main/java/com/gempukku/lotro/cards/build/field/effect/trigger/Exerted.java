package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ExertResult;
import org.json.simple.JSONObject;

public class Exerted implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize", "source");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final String byFilter = FieldUtils.getString(value.get("source"), "source", "any");


        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource byFilterableSource = environment.getFilterFactory().generateFilter(byFilter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable exerted = filterableSource.getFilterable(actionContext);
                final Filterable exertedBy = byFilterableSource.getFilterable(actionContext);
                final boolean result = TriggerConditions.forEachExertedBy(actionContext.getGame(), actionContext.getEffectResult(), exertedBy, exerted);
                if (result && memorize != null) {
                    final PhysicalCard exertedCard = ((ExertResult) actionContext.getEffectResult()).getExertedCard();
                    actionContext.setCardMemory(memorize, exertedCard);
                }
                return result;
            }
        };
    }
}
