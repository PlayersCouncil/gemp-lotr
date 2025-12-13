package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.WoundResult;
import org.json.simple.JSONObject;

public class TakesWound implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "source", "threat", "memorize");

        String source = FieldUtils.getString(value.get("source"), "source", "any");
        String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        boolean threat = FieldUtils.getBoolean(value.get("threat"), "threat", false);

        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(source, environment);
        final FilterableSource targetFilterable = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = targetFilterable.getFilterable(actionContext);
                final Filterable sourceFilterable = sourceFilter.getFilterable(actionContext);
                final boolean result = TriggerConditions.forEachWounded(actionContext.getGame(), actionContext.getEffectResult(), threat, filterable);
                if(result && !source.equals("any")) {
                    var sources = ((WoundResult) actionContext.getEffectResult()).getSources();
                    if (sources.stream().noneMatch(woundSource -> Filters.accepts(actionContext.getGame(), woundSource,
							sourceFilterable))) {
                        return false;
                    }
                }
                if (result && memorize != null) {
                    final PhysicalCard woundedCard = ((WoundResult) actionContext.getEffectResult()).getWoundedCard();
                    actionContext.setCardMemory(memorize, woundedCard);
                }
                return result;
            }
        };
    }
}
