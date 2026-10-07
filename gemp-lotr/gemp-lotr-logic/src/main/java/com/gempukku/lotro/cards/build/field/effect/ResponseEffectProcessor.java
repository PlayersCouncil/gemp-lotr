package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerChecker;
import com.gempukku.lotro.common.Phase;
import org.json.simple.JSONObject;

public class ResponseEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "trigger", "requires", "noEffectUnless", "cost", "effect", "text", "limitPerPhase", "limitPerTurn", "phase", "fromDiscard");

        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");

        if (triggerArray.length == 0)
            throw new InvalidCardDefinitionException("At least one trigger is required on Response effects.");

        final String text = FieldUtils.getString(value.get("text"), "text");

        final Phase phase = FieldUtils.getEnum(Phase.class, value.get("phase"), "phase");
        final int limitPerPhase = FieldUtils.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);
        // An ability with a cost stays on offer at its limit: the cost may be worth paying for its own sake
        // (Ketura on Q-0041).  With no cost it is withheld there, as before (Q-0034).
        final boolean offeredAtLimit = FieldUtils.getObjectArray(value.get("cost"), "cost").length > 0;

        final boolean fromDiscard = FieldUtils.getBoolean(value.get("fromDiscard"), "fromDiscard", false);

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            if (text != null) {
                triggerActionSource.setText(text);
            }

            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.nameWhatSetsOff(trigger, triggerActionSource);
            if (limitPerPhase > 0)
                triggerActionSource.addLimitPerPhase(phase, limitPerPhase, offeredAtLimit);
            if (limitPerTurn > 0)
                triggerActionSource.addLimitPerTurn(limitPerTurn, offeredAtLimit);
            EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

            if(fromDiscard) {
                if (before) {
                    blueprint.appendInDiscardBeforeActivatedTrigger(triggerActionSource);
                } else {
                    blueprint.appendInDiscardAfterActivatedTrigger(triggerActionSource);
                }
            }
            else {
                if (before) {
                    blueprint.appendBeforeActivatedTrigger(triggerActionSource);
                } else {
                    blueprint.appendAfterActivatedTrigger(triggerActionSource);
                }
            }
        }
    }
}