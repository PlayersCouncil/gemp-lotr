package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.InitiativeChangeResult;
import org.json.simple.JSONObject;

public class LosesInitiative implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "side");
        final Side side = FieldUtils.getSide(value.get("side"), "side");

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                EffectResult effectResult = actionContext.getEffectResult();
                if (effectResult.getType() == EffectResult.Type.INITIATIVE_CHANGE) {
                    InitiativeChangeResult initiativeChangeResult = (InitiativeChangeResult) effectResult;
                    if (initiativeChangeResult.getSide() != side)
                        return true;
                }
                return false;
            }
        };
    }
}
