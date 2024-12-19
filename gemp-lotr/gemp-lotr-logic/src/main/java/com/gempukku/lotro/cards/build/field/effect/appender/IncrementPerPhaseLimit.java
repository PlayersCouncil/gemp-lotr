package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.IncrementPhaseLimitEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class IncrementPerPhaseLimit implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "limit", "phase", "perPlayer");

        Phase phase = FieldUtils.getEnum(Phase.class, effectObject.get("phase"), "phase");
        final ValueSource limitSource = ValueResolver.resolveEvaluator(effectObject.get("limit"), 1, environment);
        final boolean perPlayer = FieldUtils.getBoolean(effectObject.get("perPlayer"), "perPlayer", false);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Evaluator evaluator = limitSource.getEvaluator(actionContext);
                final int limit = evaluator.evaluateExpression(actionContext.getGame(), actionContext.getSource());

                if (perPlayer)
                    return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, actionContext.getPerformingPlayer() + "_", limit);
                else
                    return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, limit);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final Evaluator evaluator = limitSource.getEvaluator(actionContext);
                final int limit = evaluator.evaluateExpression(actionContext.getGame(), actionContext.getSource());

                if (perPlayer)
                    return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, actionContext.getPerformingPlayer() + "_", limit);
                else
                    return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limit);
            }
        };
    }

}
