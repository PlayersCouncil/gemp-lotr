package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.FailedEffect;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Repeat implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "times", "effect");
        // Whether the definition has the repeat among costs ("for each Nazgûl you can spot, the Free Peoples
        // player must exert a companion to ...").
        final boolean amongCosts = cost;

        final ValueSource timesSource = ValueResolver.resolveEvaluator(effectObject.get("times"), environment);
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");

        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(cost, effectArray, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final int times = timesSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (times > 0) {
                    // A repeated cost that cannot be paid in full is not begun: the cost is unpaid, and nothing
                    // has been paid toward it (Ketura on F-0425).  What asks whether it can be paid has said no
                    // before this, except where the board has changed since it was asked: a payment made
                    // between a player's answer and this one's turn.
                    if (amongCosts && cost && !isPlayableInFull(actionContext))
                        return new FailedEffect();

                    SubAction subAction = new SubAction(action);
                    for (int i = 0; i < times; i++) {
                        for (EffectAppender effectAppender : effectAppenders)
                            effectAppender.appendEffect(false, subAction, actionContext);
                    }
                    return new StackActionEffect(subAction);
                } else {
                    return null;
                }
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                if (amongCosts)
                    return canBePaidEveryTime(actionContext);

                for (EffectAppender effectAppender : effectAppenders) {
                    if (effectAppender.isPlayabilityCheckedForEffect()
                            && !effectAppender.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }

            // A repeated cost can be paid only if every repetition can (Ketura on F-0425; CR 4.2 "cost": "you
            // must pay that cost or you cannot use that card or special ability").  Each repetition is asked on
            // the board as those before it will leave it, as far as its parts can tell what one paying does
            // (EffectAppender.tallyWoundsOfPayingOnce): of exertions that is exact, and anything else a
            // repetition uses up is not counted.  Morgul Regiment 7_197 is the one card with a repeat among
            // costs.  A repeat among effects is asked as before, by the parts an action is withheld without.
            private boolean canBePaidEveryTime(ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                final GameState gameState = game.getGameState();
                final int times = timesSource.getEvaluator(actionContext).evaluateExpression(game, null);

                final Map<PhysicalCard, Integer> paid = new HashMap<>();
                for (int i = 0; i < times; i++) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        final Map<PhysicalCard, Integer> placed = new HashMap<>();
                        final boolean payable = gameState.withWoundsToCome(paid, () -> {
                            if (!effectAppender.isPlayableInFull(actionContext))
                                return false;
                            effectAppender.tallyWoundsOfPayingOnce(actionContext, placed);
                            return true;
                        });
                        if (!payable)
                            return false;
                        for (Map.Entry<PhysicalCard, Integer> entry : placed.entrySet())
                            paid.merge(entry.getKey(), entry.getValue(), Integer::sum);
                    }
                }

                return true;
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                return Arrays.stream(effectAppenders).anyMatch(EffectAppender::isPlayabilityCheckedForEffect);
            }

            // As a branch of a choice it keeps the choice asked: what is done one time round may be what the
            // next acts on.
            @Override
            public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
                return false;
            }

            // However many times: nothing done once is nothing done each time.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return EffectForecast.together(actionContext, effectAppenders);
            }
        };
    }
}
