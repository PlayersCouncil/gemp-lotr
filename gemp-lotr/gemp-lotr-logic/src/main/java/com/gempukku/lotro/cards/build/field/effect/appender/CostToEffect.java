package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.IdleOffers;
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
        // Which of the costs remember something (a card chosen, a number): a cost that stands after one of
        // those may read that memory, and cannot be asked whether it can be paid before the first is paid.
        final boolean[] costRemembers = new boolean[costArray.length];
        for (int i = 0; i < costArray.length; i++)
            costRemembers[i] = IdleOffers.remember(new JSONObject[]{costArray[i]});

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

            // What its own cost buys: with that doing nothing, so does this, whether or not the cost is paid.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return EffectForecast.together(actionContext, effectAppenders);
            }

            // As a branch of a choice it goes by its cost alone (F-0222; CR 4.2 "cost": "you must pay that cost
            // or you cannot use that card or special ability"): with a cost that cannot be paid nothing is
            // bought, and the branch is not one to be chosen.  With a cost that can be paid it is never certain
            // to do nothing, whatever the effect comes to (Q-0034): paying is doing something.  The costs are
            // asked in order, as isPlayableInFull asks them, and the asking stops at one that remembers
            // something, since the cost after it may read that memory.
            @Override
            public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
                if (!checkConditions(actionContext))
                    return true;
                // With no cost written it is only what it holds.
                if (costAppenders.length == 0)
                    return EffectForecast.areCertainToDoNothingIfChosen(actionContext, effectAppenders);

                for (int i = 0; i < costAppenders.length; i++) {
                    if (!costAppenders[i].isPlayableInFull(actionContext))
                        return true;
                    if (costRemembers[i])
                        return false;
                }

                return false;
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
