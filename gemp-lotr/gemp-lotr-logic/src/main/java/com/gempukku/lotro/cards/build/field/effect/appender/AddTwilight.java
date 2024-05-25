package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

public class AddTwilight implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "memorize");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final int min = valueSource.getMinimum(actionContext);
                        final int max = valueSource.getMaximum(actionContext);
                        if (min != max) {
                            return new PlayoutDecisionEffect(
                                    actionContext.getPerformingPlayer(),
                                    new IntegerAwaitingDecision(1, "Choose how much twilight to add", min, max) {
                                        @Override
                                        public void decisionMade(String result) throws DecisionResultInvalidException {
                                            final int twilight = getValidatedResult(result);
                                            actionContext.setValueToMemory(memorize, String.valueOf(twilight));
                                        }
                                    });
                        } else {
                            return new UnrespondableEffect() {
                                @Override
                                protected void doPlayEffect(LotroGame game) {
                                    actionContext.setValueToMemory(memorize, String.valueOf(min));
                                }
                            };
                        }
                    }
                });
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        int twilight = Integer.parseInt(actionContext.getValueFromMemory(memorize));
                        if (twilight > 0) {
                            return new AddTwilightEffect(actionContext.getSource(), twilight);
                        } else {
                            return null;
                        }
                    }
                });
        return result;
    }

}
