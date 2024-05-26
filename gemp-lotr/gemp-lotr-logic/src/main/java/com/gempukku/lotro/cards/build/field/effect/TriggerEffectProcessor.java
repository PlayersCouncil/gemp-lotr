package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.AbstractEffectAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerChecker;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.IncrementPhaseLimitEffect;
import com.gempukku.lotro.logic.effects.IncrementTurnLimitEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class TriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "trigger", "optional", "requires", "cost", "effect", "text", "player", "limitPerTurn", "limitPerPhase", "phase", "identifierSuffix");

        final String text = FieldUtils.getString(value.get("text"), "text");
        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");
        if (triggerArray.length == 0)
            throw new InvalidCardDefinitionException("Trigger effect without trigger definition");
        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);
        final int limitPerTurn = FieldUtils.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);
        final int limitPerPhase = FieldUtils.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final Phase phase = FieldUtils.getEnum(Phase.class, value.get("phase"), "phase");

        final String player = FieldUtils.getString(value.get("player"), "player");
        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player, environment) : null;
        final String identifierSuffix = FieldUtils.getString(value.get("identifierSuffix"), "identifierSuffix");

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            if (playerSource != null) {
                triggerActionSource.setPlayingPlayer(playerSource);
            }
            if (text != null) {
                triggerActionSource.setText(text);
            }
            triggerActionSource.addPlayRequirement(triggerChecker);

            if (limitPerPhase > 0) {
                triggerActionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limitPerPhase));
                triggerActionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, limitPerPhase);
                            }
                        });
            }
            if (limitPerTurn > 0) {
                triggerActionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limitPerTurn));
                triggerActionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementTurnLimitEffect(actionContext.getSource(), limitPerTurn);
                            }
                        });
            }

            EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

            if (before) {
                if (optional)
                    blueprint.appendOptionalBeforeTrigger(triggerActionSource);
                else
                    blueprint.appendRequiredBeforeTrigger(triggerActionSource);
            } else {
                if (optional)
                    blueprint.appendOptionalAfterTrigger(triggerActionSource);
                else
                    blueprint.appendRequiredAfterTrigger(triggerActionSource);
            }
        }
    }
}
