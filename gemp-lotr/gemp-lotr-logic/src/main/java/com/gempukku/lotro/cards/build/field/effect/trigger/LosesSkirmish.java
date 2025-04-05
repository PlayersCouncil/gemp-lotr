package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.CharacterLostSkirmishResult;
import com.gempukku.lotro.logic.timing.results.SkirmishType;
import org.json.simple.JSONObject;

public class LosesSkirmish implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "against", "memorize", "overwhelm");

        String loser = FieldUtils.getString(value.get("filter"), "filter", "any");
        String against = FieldUtils.getString(value.get("against"), "against", "any");
        String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        boolean overwhelm = FieldUtils.getBoolean(value.get("overwhelm"), "overwhelm", false);

        final FilterableSource loserFilter = environment.getFilterFactory().generateFilter(loser, environment);
        final FilterableSource againstFilter = environment.getFilterFactory().generateFilter(against, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final boolean result = TriggerConditions.losesSkirmishInvolving(actionContext.getGame(), actionContext.getEffectResult(),
                        loserFilter.getFilterable(actionContext),
                        againstFilter.getFilterable(actionContext));
                if (result) {
                    CharacterLostSkirmishResult lostResult = (CharacterLostSkirmishResult) actionContext.getEffectResult();
                    if (overwhelm && lostResult.getSkirmishType() != SkirmishType.OVERWHELM)
                        return false;

                    if (memorize != null) {
                        actionContext.setCardMemory(memorize, lostResult.getLoser());
                    }
                }
                return result;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
