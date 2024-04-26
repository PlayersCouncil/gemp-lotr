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

public class ForEachShadowPlayer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "effect");

        final JSONObject effect = (JSONObject) effectObject.get("effect");

        if (effect == null)
            throw new InvalidCardDefinitionException("Effect is required for a ForEachShadowPlayer effect.");

        final EffectAppender effectAppender = environment.getEffectAppenderFactory().getEffectAppender(effect, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String[] shadowPlayers = GameUtils.getShadowPlayers(actionContext.getGame());

                SubAction subAction = new SubAction(action);
                for (String shadowPlayer : shadowPlayers) {
                    DelegateActionContext spActionContext = new DelegateActionContext(actionContext, shadowPlayer,
                            actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(),
                            actionContext.getEffect());
                    effectAppender.appendEffect(cost, action, spActionContext);
                }
                return new StackActionEffect(subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String[] shadowPlayers = GameUtils.getShadowPlayers(actionContext.getGame());

                for (String shadowPlayer : shadowPlayers) {
                    DelegateActionContext spActionContext = new DelegateActionContext(actionContext, shadowPlayer,
                            actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(),
                            actionContext.getEffect());
                    if (!effectAppender.isPlayableInFull(spActionContext))
                        return false;
                }

                return true;
            }
        };
    }
}
