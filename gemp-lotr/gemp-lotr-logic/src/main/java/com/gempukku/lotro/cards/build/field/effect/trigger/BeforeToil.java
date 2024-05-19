package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.effects.discount.ToilDiscountEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class BeforeToil implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        String filter = FieldUtils.getString(value.get("filter"), "filter");

        final FilterableSource playedFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                Effect effect = actionContext.getEffect();
                Filterable filterable = playedFilter.getFilterable(actionContext);
                if (effect.getType() == Effect.Type.BEFORE_TOIL) {
                    final ToilDiscountEffect toilEffect = (ToilDiscountEffect) effect;
                    PhysicalCard payingFor = toilEffect.getPayingFor();
                    return Filters.changeToFilter(filterable).accepts(actionContext.getGame(), payingFor);
                }
                return false;
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
