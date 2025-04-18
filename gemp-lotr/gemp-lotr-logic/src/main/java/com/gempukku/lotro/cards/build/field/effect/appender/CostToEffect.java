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

public class CostToEffect implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "cost", "effect", "requires");

        final JSONObject[] costArray = FieldUtils.getObjectArray(effectObject.get("cost"), "cost");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");

        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(true, costArray, environment);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(false, effectArray, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {

                if(!checkConditions(actionContext))
                    return null;

                SubAction subAction = new SubAction(action);

                for (EffectAppender costAppender : costAppenders)
                    costAppender.appendEffect(true, subAction, actionContext);
                for (EffectAppender effectAppender : effectAppenders)
                    effectAppender.appendEffect(false, subAction, actionContext);

                return new StackActionEffect(subAction);
            }

            private boolean checkConditions(ActionContext actionContext) {
                for (Requirement req : requirements) {
                    if (!req.accepts(actionContext))
                        return false;
                }
                return true;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {

                if(!checkConditions(actionContext))
                    return false;

                for (EffectAppender costAppender : costAppenders) {
                    if (!costAppender.isPlayableInFull(actionContext))
                        return false;
                }

                for (EffectAppender effectAppender : effectAppenders) {
                    if (effectAppender.isPlayabilityCheckedForEffect()
                            && !effectAppender.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                //This is done intentinally, even though one would think that you would want to
                // check the "cost" part of "CostToEffect".  The reason for this is because CostToEffect
                // is frequently used as a wrapper in contexts where the Cost cannot actually be checked
                // ahead of time, such as when memory values are crucial to the execution (which of course
                // are not popuulated before the action itself is executed at time of checking).
                return false;
            }
        };
    }
}
