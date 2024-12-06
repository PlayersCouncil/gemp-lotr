package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import org.json.simple.JSONObject;

public class ModifyMoveLimit implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "amount");

        final JSONObject[] requiresArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(requiresArray, environment);

        return (actionContext) -> {
            final Evaluator evaluator = valueSource.getEvaluator(actionContext);
            return new MoveLimitModifier(actionContext.getSource(),
                    evaluator.evaluateExpression(actionContext.getGame(), null),
                    RequirementCondition.createCondition(requirements, actionContext));
        };
    }
}
