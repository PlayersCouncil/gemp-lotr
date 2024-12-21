package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ChooseHowManyBurdensToSpot implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        if (memorize == null)
            throw new InvalidCardDefinitionException("ChooseHowManyBurdensToSpot requires a field to memorize the value");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final int count = actionContext.getGame().getGameState().getBurdens();
                return new PlayoutDecisionEffect(actionContext.getPerformingPlayer(),
                        new IntegerAwaitingDecision(1, "Choose how many burdens to spot", 0, count, count) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final int spotCount = getValidatedResult(result);
                                actionContext.setValueToMemory(memorize, String.valueOf(spotCount));
                            }
                        });
            }
        };
    }
}
