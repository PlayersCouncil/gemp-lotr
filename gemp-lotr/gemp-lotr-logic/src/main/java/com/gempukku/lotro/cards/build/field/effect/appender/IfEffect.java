package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import java.util.Arrays;
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
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "check", "true", "false");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("check"), "check");
        final JSONObject[] trueEffects = FieldUtils.getObjectArray(effectObject.get("true"), "true");
        final JSONObject[] falseEffects = FieldUtils.getObjectArray(effectObject.get("false"), "false");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final EffectAppender[] trueEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(cost, trueEffects, environment);
        final EffectAppender[] falseEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(cost, falseEffects, environment);
        // "If the limit is not reached, count a use and do this", with nothing to do otherwise: the one check
        // that can be made before the action is taken, since no cost of the action lowers a counter.
        final boolean loneLimitCheck = conditionArray.length == 1 && falseEffects.length == 0 && isLimitCheck(conditionArray[0]);
        // "At sites 1 to 5 ...; at any other site ...": where the fellowship is does not change while an action
        // is paid for, so a check of that and nothing else can be made beforehand as well.
        final boolean checksOnlyTheSite = conditionArray.length > 0 && Arrays.stream(conditionArray).allMatch(IfEffect::isSiteCheck);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                EffectAppender[] effects = checkRequirements(actionContext) ? trueEffectAppenders : falseEffectAppenders;

                if(effects == null || effects.length == 0)
                    return null;

                SubAction subAction = new SubAction(action);
                for (EffectAppender effectAppender : effects)
                    effectAppender.appendEffect(cost, subAction, actionContext);

                return new StackActionEffect(subAction);
            }

            // The check is not made beforehand as a rule: paying the action's costs may change how it comes
            // out.  So the effect does nothing only if neither branch would do anything, and it reaches whoever
            // either branch reaches.  The exceptions are a limit that is already reached, and a check of
            // nothing but the site the fellowship is at.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                if (loneLimitCheck && !checkRequirements(actionContext))
                    return EffectForecast.AT_LIMIT;
                if (checksOnlyTheSite)
                    return EffectForecast.together(actionContext, checkRequirements(actionContext) ? trueEffectAppenders : falseEffectAppenders);
                return EffectForecast.together(actionContext, trueEffectAppenders, falseEffectAppenders);
            }

            // As a branch of a choice it is asked as the choice comes up, and the check made now is the one
            // that will be made if the branch is taken: it does what the side the check picks does, and
            // nothing where that side has nothing in it.
            @Override
            public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
                return EffectForecast.areCertainToDoNothingIfChosen(actionContext,
                        checkRequirements(actionContext) ? trueEffectAppenders : falseEffectAppenders);
            }

            private boolean checkRequirements(ActionContext actionContext) {
                for (Requirement condition : conditions) {
                    if (!condition.accepts(actionContext))
                        return false;
                }
                return true;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {

                boolean check = checkRequirements(actionContext);
                EffectAppender[] effects = check ? trueEffectAppenders : falseEffectAppenders;

                if(effects == null || effects.length == 0)
                    return check;

                for (EffectAppender effectAppender : effects) {
                    if (!effectAppender.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }
        };
    }

    private static boolean isSiteCheck(JSONObject condition) {
        return String.valueOf(condition.get("type")).equalsIgnoreCase("location")
                && !CardResolver.readsMemory(String.valueOf(condition.get("filter")));
    }

    private static boolean isLimitCheck(JSONObject condition) {
        final String type = String.valueOf(condition.get("type")).toLowerCase();
        return type.equals("perphaselimit") || type.equals("perturnlimit");
    }

}
