package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.MemoryScopedEnvironment;
import com.gempukku.lotro.cards.build.PreEvaluateAble;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
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

    /**
     * Tells an action that a trigger sets off where the trigger remembers what set it off: the trigger's own
     * "memorize".  The other things a trigger may remember ("memorizeLoser", "memorizeBearer") are not what
     * set it off.
     */
    public static void nameWhatSetsOff(JSONObject trigger, DefaultActionSource actionSource) {
        for (Object entryObject : trigger.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
            if (String.valueOf(entry.getKey()).equalsIgnoreCase("memorize") && entry.getValue() instanceof String memory)
                actionSource.setSetOffByMemory(memory);
        }
    }

    /**
     * Reads the limit fields of an ability, a trigger or a response.  A card says one of two things with the
     * word "limit", and the definition says which is meant by the field it uses (Ketura on Q-0082 and Q-0093):
     * <ul>
     * <li>"limitPerPhase", "limitPerTurn", "limitPerSite": a limit on how often the ability may be used at all
     * ("limit once per turn", "limit once per phase", "limit once per site").  At its limit the action is
     * withheld, whatever it costs.  The count per site is cleared when the fellowship moves to another site
     * and at the end of the turn.</li>
     * <li>"effectLimitPerPhase", "effectLimitPerTurn": a limit on what the effect gives over several uses
     * ("limit +3", or "limit 1" of things added), written as the number of uses that still give something.
     * Where the action has a cost and is the player's to use or not, it stays on offer at its limit: the cost
     * is paid, nothing more is done, and the action is flagged as doing nothing (Q-0041).  With no cost, and on
     * a required trigger, it is withheld there like the other kind (Q-0034).</li>
     * </ul>
     * @param phase The phase a per-phase limit is counted in, or null for whichever phase the action is used in.
     * @param playersToUseOrNot False for a required trigger, which is nobody's to offer.
     */
    public static void processLimits(JSONObject value, DefaultActionSource actionSource, Phase phase, boolean playersToUseOrNot) throws InvalidCardDefinitionException {
        final int limitPerPhase = FieldUtils.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);
        final int limitPerSite = FieldUtils.getInteger(value.get("limitPerSite"), "limitPerSite", 0);
        final int effectLimitPerPhase = FieldUtils.getInteger(value.get("effectLimitPerPhase"), "effectLimitPerPhase", 0);
        final int effectLimitPerTurn = FieldUtils.getInteger(value.get("effectLimitPerTurn"), "effectLimitPerTurn", 0);
        final boolean offeredAtEffectLimit = playersToUseOrNot
                && FieldUtils.getObjectArray(value.get("cost"), "cost").length > 0;

        if (limitPerPhase > 0)
            actionSource.addLimitPerPhase(phase, limitPerPhase, false);
        if (limitPerTurn > 0)
            actionSource.addLimitPerTurn(limitPerTurn, false);
        if (limitPerSite > 0)
            actionSource.addLimitPerSite(limitPerSite);
        if (effectLimitPerPhase > 0)
            actionSource.addLimitPerPhase(phase, effectLimitPerPhase, offeredAtEffectLimit);
        if (effectLimitPerTurn > 0)
            actionSource.addLimitPerTurn(effectLimitPerTurn, offeredAtEffectLimit);
    }

    public static void processRequirementsCostsAndEffects(JSONObject value, CardGenerationEnvironment environment, DefaultActionSource actionSource) throws InvalidCardDefinitionException {
        final JSONObject[] requirementArray = FieldUtils.getObjectArray(value.get("requires"), "requires");
        for (JSONObject requirement : requirementArray) {
            final Requirement conditionRequirement = environment.getRequirementFactory().getRequirement(requirement, environment);
            actionSource.addPlayRequirement(conditionRequirement);
        }

        // "noEffectUnless" is written like "requires" and checked like it, but an action that fails it is still
        // offered: it is flagged as having no effect.  It is where a guard goes that kept an event, or an
        // ability with a cost, from being used to no purpose (Ketura on Q-0034: such an action is offered).
        final JSONObject[] noEffectUnlessArray = FieldUtils.getObjectArray(value.get("noEffectUnless"), "noEffectUnless");
        for (JSONObject requirement : noEffectUnlessArray)
            actionSource.addNoEffectUnless(environment.getRequirementFactory().getRequirement(requirement, environment));

        processCostsAndEffects(value, environment, actionSource);
    }

    public static void processCostsAndEffects(JSONObject value, CardGenerationEnvironment environment, DefaultActionSource actionSource) throws InvalidCardDefinitionException {
        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(value.get("effect"), "effect");

        if (costArray.length == 0 && effectArray.length == 0)
            throw new InvalidCardDefinitionException("Action does not contain a cost, nor effect");

        // Whether the action may be judged, before it is taken, to do nothing: only where none of its costs
        // could make what its effects need.
        actionSource.setJudgeable(NoEffectForesight.canForesee(costArray, effectArray));

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
