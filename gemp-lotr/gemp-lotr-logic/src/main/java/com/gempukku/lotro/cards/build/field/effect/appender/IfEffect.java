package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class IfEffect implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "check", "true", "false");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("check"), "check");
        final JSONObject[] trueEffects = FieldUtils.getObjectArray(effectObject.get("true"), "true");
        final JSONObject[] falseEffects = FieldUtils.getObjectArray(effectObject.get("false"), "false");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final EffectAppender[] trueEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(trueEffects, environment);
        final EffectAppender[] falseEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(falseEffects, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                EffectAppender[] effects = checkConditions(actionContext) ? trueEffectAppenders : falseEffectAppenders;
                SubAction subAction = new SubAction(action);
                for (EffectAppender effectAppender : effects)
                    effectAppender.appendEffect(cost, subAction, actionContext);

                return new StackActionEffect(subAction);
            }

            private boolean checkConditions(ActionContext actionContext) {
                for (Requirement condition : conditions) {
                    if (!condition.accepts(actionContext))
                        return false;
                }
                return true;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                EffectAppender[] effects = checkConditions(actionContext) ? trueEffectAppenders : falseEffectAppenders;
                for (EffectAppender effectAppender : effects) {
                    if (!effectAppender.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }
        };
    }

}
