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
        FieldUtils.validateAllowedFields(value, "trigger", "requires", "noEffectUnless", "cost", "effect", "text", "limitPerPhase", "limitPerTurn", "limitPerSite", "effectLimitPerPhase", "effectLimitPerTurn", "phase", "fromDiscard");

        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");

        if (triggerArray.length == 0)
            throw new InvalidCardDefinitionException("At least one trigger is required on Response effects.");

        final String text = FieldUtils.getString(value.get("text"), "text");

        final Phase phase = FieldUtils.getEnum(Phase.class, value.get("phase"), "phase");

        final boolean fromDiscard = FieldUtils.getBoolean(value.get("fromDiscard"), "fromDiscard", false);

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            if (text != null) {
                triggerActionSource.setText(text);
            }

            // Q-0224 (IdleOffers): a Response ability of a card in play.
            triggerActionSource.setWithheldWhenIdle(!fromDiscard);
            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.nameWhatSetsOff(trigger, triggerActionSource);
            EffectUtils.processLimits(value, triggerActionSource, phase, true);
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