package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.modifiers.FPCulturesSpotCountModifier;
import org.json.simple.JSONObject;

public class FPCultureCount implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "amount");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final ValueSource amount = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new FPCulturesSpotCountModifier(actionContext.getSource(),
                RequirementCondition.createCondition(requirements, actionContext),
                amount.getEvaluator(actionContext));
    }
}
