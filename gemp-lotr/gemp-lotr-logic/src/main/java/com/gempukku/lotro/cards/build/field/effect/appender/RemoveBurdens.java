package com.gempukku.lotro.cards.build.field.effect.appender;

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
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.RemoveBurdenEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.FailedEffect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

public class RemoveBurdens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "amount");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);

        String memorize = "_temp";

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        Evaluator evaluator = valueSource.getEvaluator(actionContext);

                        // As a cost, the full minimum must actually be payable; otherwise the cost fails and the
                        // owning action does not proceed to its effects.  Without this, "remove a burden" with 0
                        // burdens would silently resolve as removing nothing and the effect would still fire.
                        // As an effect, removing fewer (or zero) burdens than requested is a fizzle, not a failure.
                        if (cost && actionContext.getGame().getGameState().getBurdens() < evaluator.getMinimum(actionContext.getGame(), null))
                            return new FailedEffect();

                        final int min = Math.min(actionContext.getGame().getGameState().getBurdens(), evaluator.getMinimum(actionContext.getGame(), null));
                        final int max = Math.min(actionContext.getGame().getGameState().getBurdens(), evaluator.getMaximum(actionContext.getGame(), null));
                        if (min != max) {
                            return new PlayoutDecisionEffect(
                                    actionContext.getPerformingPlayer(),
                                    new IntegerAwaitingDecision(1, "Choose how many burdens to remove", min, max) {
                                        @Override
                                        public void decisionMade(String result) throws DecisionResultInvalidException {
                                            final int burdens = getValidatedResult(result);
                                            actionContext.setValueToMemory(memorize, String.valueOf(burdens));
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
                        final int burdens = valueSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null);
                        final LotroGame game = actionContext.getGame();
                        return game.getModifiersQuerying().canRemoveBurden(game, actionContext.getSource())
                                && game.getGameState().getBurdens() >= burdens;
                    }
                });
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final String removingPlayer = playerSource.getPlayer(actionContext);
                        int burdens = Integer.parseInt(actionContext.getValueFromMemory(memorize));
                        if (burdens > 0)
                            return new RemoveBurdenEffect(removingPlayer, actionContext.getSource(), burdens);
                        else
                            return null;
                    }
                });

        return result;
    }
}
