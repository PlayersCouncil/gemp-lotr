package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ExertResult;
import org.json.simple.JSONObject;

public class ExertsForSpecialAbility implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "source", "itsOwn", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String source = FieldUtils.getString(value.get("source"), "source", "any");
        final boolean itsOwn = FieldUtils.getBoolean(value.get("itsOwn"), "itsOwn", false);
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource sourceSource = environment.getFilterFactory().generateFilter(source, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                Filter sourceFilter = Filters.changeToFilter(sourceSource.getFilterable(actionContext));
                boolean result = TriggerConditions.forEachExerted(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (result) {
                    ExertResult exertResult = (ExertResult) actionContext.getEffectResult();
                    final Action exertingAction = exertResult.getAction();
                    if (itsOwn) {
                        PhysicalCard exertedCard = exertResult.getExertedCard();
                        if (exertingAction == null || exertingAction.getType() != Action.Type.SPECIAL_ABILITY
                                || exertedCard != exertingAction.getActionSource())
                            result = false;
                    } else {
                        if (exertingAction == null || exertingAction.getType() != Action.Type.SPECIAL_ABILITY
                                || !sourceFilter.accepts(actionContext.getGame(), exertingAction.getActionSource()))
                            result = false;
                    }
                }
                if (result && memorize != null) {
                    final PhysicalCard exertedCard = ((ExertResult) actionContext.getEffectResult()).getExertedCard();
                    actionContext.setCardMemory(memorize, exertedCard);
                }
                return result;
            }
        };
    }
}
