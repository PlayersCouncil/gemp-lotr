package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.DoWhileSubAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class DoWhile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "check", "effect");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("check"), "check");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");

        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(cost, effectArray, environment);

        // Taught for Q-0224 (IdleOffers): what is done at least once, and would do nothing the first time, does
        // nothing (it is repeated only for what it did: shapes.md, F-0322).
        // No test exercises this (engine pass 27): where a printed card has it in an offer that pays nothing
        // (Race Text 94_4), a requirement written by hand answers first (IdleOffers).
        return TaughtForecast.teach(new DelayedAppender() {
            private boolean conditionsMatch(ActionContext actionContext) {
                for (Requirement condition : conditions) {
                    if (!condition.accepts(actionContext))
                        return false;
                }
                return true;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                DoWhileSubAction doWhile = new DoWhileSubAction(action,
                        () -> conditionsMatch(actionContext),
                        costToEffectAction -> {
                            for (EffectAppender effectAppender : effectAppenders)
                                effectAppender.appendEffect(false, costToEffectAction, actionContext);
                        });
                return new StackActionEffect(doWhile);
            }
        }, actionContext -> EffectForecast.together(actionContext, effectAppenders));
    }
}
