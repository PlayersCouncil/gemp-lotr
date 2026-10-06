package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.MemoryScopedEnvironment;
import com.gempukku.lotro.cards.build.PreEvaluateAble;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    /**
     * The environment for building the costs of an action that a trigger sets off: a memory the trigger fills
     * ("memorize: winner") is filled before the costs are checked, so a cost may select from it. A name that one
     * of the costs fills as well is left out, since which filling a later cost would read cannot be told.
     */
    public static CardGenerationEnvironment costEnvironmentOfTriggeredAction(CardGenerationEnvironment environment, Object trigger, JSONObject[] costArray) {
        final Set<String> filledByTrigger = new HashSet<>();
        collectMemorized(trigger, filledByTrigger);
        if (filledByTrigger.isEmpty())
            return environment;
        final Set<String> filledByCosts = new HashSet<>();
        collectMemorized(costArray, filledByCosts);
        filledByTrigger.removeAll(filledByCosts);
        return new MemoryScopedEnvironment(environment, filledByTrigger::contains);
    }

    /**
     * The environment for building the costs of a trigger that an action adds (AddTrigger). Those costs are
     * checked when the added trigger is, and by then the action that added it has been carried out and its
     * memory filled, so a cost may select from any memory that the added trigger's own costs do not fill.
     */
    public static CardGenerationEnvironment costEnvironmentOfAddedTrigger(CardGenerationEnvironment environment, JSONObject[] costArray) {
        final Set<String> filledByCosts = new HashSet<>();
        collectMemorized(costArray, filledByCosts);
        return new MemoryScopedEnvironment(environment, memory -> !filledByCosts.contains(memory));
    }

    /**
     * Gathers, in lower case, every memory name written under a key that begins with "memor" ("memorize",
     * "memorizeBearer", MemorizeNumber's "memory", ...) anywhere inside the given piece of a definition. Keys
     * that only read a memory are caught too, which errs on the side of calling a memory filled.
     */
    private static void collectMemorized(Object definition, Set<String> names) {
        if (definition instanceof JSONObject object) {
            for (Object entryObject : object.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
                if (entry.getValue() instanceof String name && String.valueOf(entry.getKey()).toLowerCase().startsWith("memor"))
                    names.add(name.toLowerCase());
                else
                    collectMemorized(entry.getValue(), names);
            }
        } else if (definition instanceof Object[] array) {
            for (Object element : array)
                collectMemorized(element, names);
        } else if (definition instanceof Iterable<?> iterable) {
            for (Object element : iterable)
                collectMemorized(element, names);
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
        final CardGenerationEnvironment costEnvironment = costEnvironmentOfTriggeredAction(environment, value.get("trigger"), costArray);
        for (JSONObject cost : costArray) {
            final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(true, cost, costEnvironment);
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
