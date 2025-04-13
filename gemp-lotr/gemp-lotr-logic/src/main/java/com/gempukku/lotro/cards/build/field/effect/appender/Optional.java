package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.YesNoDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class Optional implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "text", "requires", "cost", "effect");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        final JSONObject[] costArray = FieldUtils.getObjectArray(effectObject.get("cost"), "cost");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        final JSONObject[] requiresArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");

        if (text == null)
            throw new InvalidCardDefinitionException("There is a text required for optional effects");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(true, costArray, environment);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(cost, effectArray, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(requiresArray, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                ActionContext delegate = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());

                if(!isPlayableInFull(actionContext))
                    return null;

                SubAction subAction = new SubAction(action);

                subAction.appendCost(
                        new PlayoutDecisionEffect(choosingPlayer,
                                new YesNoDecision(GameUtils.substituteText(text, actionContext)) {
                            @Override
                            protected void yes() {
                                for (EffectAppender costAppender : costAppenders)
                                    costAppender.appendEffect(true, subAction, actionContext);

                                for (EffectAppender effectAppender : effectAppenders) {
                                    effectAppender.appendEffect(cost, subAction, delegate);
                                }
                            }
                        }));
                return new StackActionEffect(subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                ActionContext delegate = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());

                if(!checkRequirements(delegate))
                    return false;

                for (EffectAppender costAppender : costAppenders) {
                    if (!costAppender.isPlayableInFull(delegate))
                        return false;
                }

                for (EffectAppender effectAppender : effectAppenders) {
                    if (effectAppender.isPlayabilityCheckedForEffect()
                            && !effectAppender.isPlayableInFull(delegate))
                        return false;
                }

                return true;
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                for (EffectAppender effectAppender : effectAppenders) {
                    if (effectAppender.isPlayabilityCheckedForEffect())
                        return true;
                }
                return false;
            }

            private boolean checkRequirements(ActionContext actionContext) {
                for (Requirement req : requirements) {
                    if (!req.accepts(actionContext))
                        return false;
                }
                return true;
            }
        };
    }
}
