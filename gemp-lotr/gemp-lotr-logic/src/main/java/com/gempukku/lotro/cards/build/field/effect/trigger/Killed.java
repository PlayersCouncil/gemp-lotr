package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ForEachKilledResult;
import com.gempukku.lotro.logic.timing.results.WoundResult;
import org.json.simple.JSONObject;

public class Killed implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "killer", "inSkirmish", "cause", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String killer = FieldUtils.getString(value.get("killer"), "killer", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final boolean inSkirmish = FieldUtils.getBoolean(value.get("inSkirmish"), "inSkirmish", false);
        final KillEffect.Cause cause = FieldUtils.getEnum(KillEffect.Cause.class, value.get("cause"), "cause");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource killerFilterableSource = environment.getFilterFactory().generateFilter(killer, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                Filterable killerFilterable = killerFilterableSource.getFilterable(actionContext);
                boolean result = false;

                if(killerFilterable == Filters.any) {
                    result = TriggerConditions.forEachKilled(actionContext.getGame(), actionContext.getEffectResult(), inSkirmish, cause, filterable);
                }
                else {
                    result = TriggerConditions.forEachKilledBy(actionContext.getGame(), actionContext.getEffectResult(), inSkirmish, cause, killerFilterable, filterable);
                }

                if (result && memorize != null) {
                    PhysicalCard killedCard = null;
                    if(actionContext.getEffectResult() instanceof ForEachKilledResult killedResult) {
                        killedCard = killedResult.getKilledCard();
                    }
                    if(actionContext.getEffectResult() instanceof WoundResult woundResult) {
                        killedCard = woundResult.getWoundedCard();
                    }

                    if(killedCard != null) {
                        actionContext.setCardMemory(memorize, killedCard);
                    }
                }
                return result;
            }
        };
    }
}
