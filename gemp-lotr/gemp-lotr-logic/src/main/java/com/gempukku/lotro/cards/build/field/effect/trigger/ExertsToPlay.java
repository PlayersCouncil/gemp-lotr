package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ExertResult;
import org.json.simple.JSONObject;

public class ExertsToPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize", "toPlay");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final String toPlayFilter = FieldUtils.getString(value.get("toPlay"), "toPlay", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource toPlayFilterableSource = environment.getFilterFactory().generateFilter(toPlayFilter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                Filterable toPlayFilterable = toPlayFilterableSource.getFilterable(actionContext);
                boolean result = TriggerConditions.forEachExerted(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (result) {
                    ExertResult exertResult = (ExertResult) actionContext.getEffectResult();
                    if (exertResult.getAction() != null && exertResult.getAction().getType() == Action.Type.PLAY_CARD) {
                        PhysicalCard playedCard = exertResult.getAction().getActionSource();
                        if (playedCard == null || !Filters.changeToFilter(toPlayFilterable).accepts(actionContext.getGame(), playedCard)) {
                            result = false;
                        }
                    } else {
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
