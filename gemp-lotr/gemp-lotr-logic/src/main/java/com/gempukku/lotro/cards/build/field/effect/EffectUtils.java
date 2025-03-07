package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PreEvaluateAble;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

public class EffectUtils {
    public static void validatePreEvaluate(boolean cost, JSONObject effectObject, PreEvaluateAble ... preEvaluateAbles) throws InvalidCardDefinitionException {
        boolean ignoreCostCheckFailure = FieldUtils.getBoolean(effectObject.get("ignoreCostCheckFailure"), "ignoreCostCheckFailure", false);
        if (cost && !ignoreCostCheckFailure) {
            for (PreEvaluateAble preEvaluateAble : preEvaluateAbles) {
                if (!preEvaluateAble.canPreEvaluate())
                    throw new InvalidCardDefinitionException("Can't pre-evaluate a cost");
            }
        }
    }

    public static void processRequirementsCostsAndEffects(JSONObject value, CardGenerationEnvironment environment, DefaultActionSource actionSource) throws InvalidCardDefinitionException {
        final JSONObject[] requirementArray = FieldUtils.getObjectArray(value.get("requires"), "requires");
        for (JSONObject requirement : requirementArray) {
            final Requirement conditionRequirement = environment.getRequirementFactory().getRequirement(requirement, environment);
            actionSource.addPlayRequirement(conditionRequirement);
        }

        processCostsAndEffects(value, environment, actionSource);
    }

    public static void processCostsAndEffects(JSONObject value, CardGenerationEnvironment environment, DefaultActionSource actionSource) throws InvalidCardDefinitionException {
        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(value.get("effect"), "effect");

        if (costArray.length == 0 && effectArray.length == 0)
            throw new InvalidCardDefinitionException("Action does not contain a cost, nor effect");

        final EffectAppenderFactory effectAppenderFactory = environment.getEffectAppenderFactory();
        for (JSONObject cost : costArray) {
            final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(true, cost, environment);
            actionSource.addPlayRequirement(
                    effectAppender::isPlayableInFull);
            actionSource.addCost(effectAppender);
        }

        for (JSONObject effect : effectArray) {
            final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(false, effect, environment);
            if (effectAppender.isPlayabilityCheckedForEffect())
                actionSource.addPlayRequirement(effectAppender::isPlayableInFull);
            actionSource.addEffect(effectAppender);
        }
    }
}
