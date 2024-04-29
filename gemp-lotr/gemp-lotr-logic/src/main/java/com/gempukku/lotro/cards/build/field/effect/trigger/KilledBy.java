package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ForEachKilledResult;
import org.json.simple.JSONObject;

public class KilledBy implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "by", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String byFilter = FieldUtils.getString(value.get("by"), "by", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource byFilterableSource = environment.getFilterFactory().generateFilter(byFilter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                Filterable byFilterable = byFilterableSource.getFilterable(actionContext);
                final boolean result = TriggerConditions.forEachKilledBy(actionContext.getGame(), actionContext.getEffectResult(), byFilterable, filterable);
                if (result && memorize != null) {
                    final PhysicalCard killedCard = ((ForEachKilledResult) actionContext.getEffectResult()).getKilledCard();
                    actionContext.setCardMemory(memorize, killedCard);
                }
                return result;
            }
        };
    }
}
