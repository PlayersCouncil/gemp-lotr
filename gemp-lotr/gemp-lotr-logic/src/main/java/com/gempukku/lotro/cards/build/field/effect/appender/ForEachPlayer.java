package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DelegateActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ForEachPlayer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "effect");

        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");

        if (effectArray.length == 0)
            throw new InvalidCardDefinitionException("Effect is required for a ForEachPlayer effect.");

        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                SubAction subAction = new SubAction(action);
                for (String playerId : GameUtils.getAllPlayers(actionContext.getGame())) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        DelegateActionContext playerActionContext = new DelegateActionContext(actionContext, playerId,
                                actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(),
                                actionContext.getEffect());
                        effectAppender.appendEffect(cost, action, playerActionContext);
                    }
                }
                return new StackActionEffect(subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                for (String playerId : GameUtils.getAllPlayers(actionContext.getGame())) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        DelegateActionContext playerActionContext = new DelegateActionContext(actionContext, playerId,
                                actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(),
                                actionContext.getEffect());
                        if (!effectAppender.isPlayableInFull(playerActionContext))
                            return false;
                    }
                }

                return true;
            }
        };
    }
}
