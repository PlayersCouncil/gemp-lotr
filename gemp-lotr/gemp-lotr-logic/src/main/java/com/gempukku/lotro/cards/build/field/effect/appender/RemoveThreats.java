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
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.RemoveThreatsEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

public class RemoveThreats implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);

        String memorize = "_temp";

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        Evaluator evaluator = valueSource.getEvaluator(actionContext);
                        final int min = Math.min(actionContext.getGame().getGameState().getThreats(), evaluator.getMinimum(actionContext.getGame(), null));
                        final int max = Math.min(actionContext.getGame().getGameState().getThreats(), evaluator.getMaximum(actionContext.getGame(), null));
                        if (min != max) {
                            return new PlayoutDecisionEffect(
                                    actionContext.getPerformingPlayer(),
                                    new IntegerAwaitingDecision(1, "Choose how many threats to remove", min, max) {
                                        @Override
                                        public void decisionMade(String result) throws DecisionResultInvalidException {
                                            final int threats = getValidatedResult(result);
                                            actionContext.setValueToMemory(memorize, String.valueOf(threats));
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

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final int threats = valueSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null);
                        final LotroGame game = actionContext.getGame();
                        return game.getModifiersQuerying().canRemoveThreat(game, actionContext.getSource())
                                && game.getGameState().getThreats() >= threats;
                    }
                });
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        int threats = Integer.parseInt(actionContext.getValueFromMemory(memorize));
                        if (threats > 0)
                            return new RemoveThreatsEffect(actionContext.getSource(), threats);
                        else
                            return null;
                    }
                });

        return result;
    }

}
