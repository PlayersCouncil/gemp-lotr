package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.timing.EffectResult;
import org.json.simple.JSONObject;

public class AfterAllSkirmishes implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return actionContext.getEffectResult().getType() == EffectResult.Type.AFTER_ALL_SKIRMISHES;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
