package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ChooseANumber implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "text", "from", "to", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String displayText = FieldUtils.getString(effectObject.get("text"), "text", "Choose a number");
        final ValueSource fromSource = ValueResolver.resolveEvaluator(effectObject.get("from"), 0, environment);
        Object to = effectObject.get("to");
        final ValueSource toSource = to != null ? ValueResolver.resolveEvaluator(to, environment) : null;

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        if (memorize == null)
            throw new InvalidCardDefinitionException("ChooseANumber requires a field to memorize the value");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                int min = fromSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                Integer max = toSource != null ? toSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null) : null;
                return new PlayoutDecisionEffect(playerSource.getPlayer(actionContext),
                        new IntegerAwaitingDecision(1, GameUtils.substituteText(displayText, actionContext),
                                min, max) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                actionContext.setValueToMemory(memorize, String.valueOf(getValidatedResult(result)));
                            }
                        });
            }
        };
    }
}