package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

public class AddThreats implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "player", "default");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        // Where the amount is a range, the player is asked for a number in it.  "default" is the number that
        // question is filled in with before he answers ("add threats up to the number of burdens you can spot":
        // the most, F-0419); with none given the question fills none in, as before.
        final Object defaultObject = effectObject.get("default");
        final ValueSource defaultSource = defaultObject != null ? ValueResolver.resolveEvaluator(defaultObject, environment) : null;
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        String memorize = "_temp";

        MultiEffectAppender result = new MultiEffectAppender() {
            // About to be carried out with as many threats on the table as there may be, it adds none.  How
            // many it would add is not worked out.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                if (actionContext.isAtResolution() && PlayConditions.getMaxAddThreatCount(actionContext.getGame()) <= 0)
                    return EffectForecast.NOTHING;
                return EffectForecast.UNKNOWN;
            }
        };
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        Evaluator evaluator = valueSource.getEvaluator(actionContext);
                        final int min = Math.min(PlayConditions.getMaxAddThreatCount(actionContext.getGame()), evaluator.getMinimum(actionContext.getGame(), null));
                        final int max = Math.min(PlayConditions.getMaxAddThreatCount(actionContext.getGame()), evaluator.getMaximum(actionContext.getGame(), null));
                        if (min != max) {
                            // The number filled in is held to what may be answered: fewer threats can be added
                            // than the definition's own number where the threat limit is near.
                            final Integer filledIn = defaultSource == null ? null
                                    : Math.max(min, Math.min(max, defaultSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null)));
                            return new PlayoutDecisionEffect(
                                    actionContext.getPerformingPlayer(),
                                    new IntegerAwaitingDecision(1, "Choose how many threats to add", min, max, filledIn) {
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
                        final int amount = valueSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null);
                        return PlayConditions.canAddThreat(actionContext.getGame(), actionContext.getSource(), amount);
                    }

                    @Override
                    public int getThreatsAddedAsCost(ActionContext actionContext) {
                        return Math.max(0, valueSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null));
                    }
                });
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        int threats = Integer.parseInt(actionContext.getValueFromMemory(memorize));
                        final String playerAddingThreats = playerSource.getPlayer(actionContext);
                        if (threats > 0) {
                            return new AddThreatsEffect(playerAddingThreats, actionContext.getSource(), threats);
                        } else {
                            return null;
                        }
                    }
                });
        return result;
    }
}
