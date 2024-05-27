package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ShouldSkipPhaseModifier;
import org.json.simple.JSONObject;

public class SkipPhase implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "phase");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new ShouldSkipPhaseModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext), phase);
            }
        };
    }
}
