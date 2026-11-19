package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddUntilModifierEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ModifyStrength implements EffectAppenderProducer {
    // The name of the source card's counter for a "(limit +N)" on strength.  It is the card's own for the phase
    // and is kept apart from the counter of the card's uses (limitPerPhase).
    private static final String LIMIT_COUNTER = "strengthLimit-";

    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "count", "select", "until", "memorize", "limitPerCardThisPhase");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), environment);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final TimeResolver.Time time = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");
        final int limit = FieldUtils.getInteger(effectObject.get("limitPerCardThisPhase"), "limitPerCardThisPhase", -1);
        if (limit > -1 && effectObject.get("until") != null)
            throw new InvalidCardDefinitionException("ModifyStrength: If limitPerCardThisPhase is defined, the until should not be, as it can only be applied until current phase");

        final EffectAppender cardResolver = CardResolver.resolveCards(select, valueSource, memory, "you", "Choose cards to modify strength of", environment);
        // Strength given for the current phase by a skirmish action is gone when the skirmish is.
        final boolean endsWithSkirmish = time.endsWithCurrentPhase(Phase.SKIRMISH);

        MultiEffectAppender result = new MultiEffectAppender() {
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                final EffectForecast given = EffectForecast.givenTo(cardResolver.forecast(actionContext), endsWithSkirmish);
                if (given.getVerdict() != EffectForecast.Verdict.UNKNOWN)
                    return given;
                // A capped amount whose cap is spent comes to nothing, whoever it is given to.  It is asked for
                // the card it will be worked out for, which is this effect's source.
                if (EffectForecast.isSpent(amountSource, actionContext, actionContext.getSource()))
                    return EffectForecast.AT_LIMIT;
                if (limit == -1)
                    return given;

                // A limited bonus is the source card's for the phase (CR 4.2 "limit"), whoever it is given to:
                // once the card has given its limit it gives nothing more.  The counter is read and not
                // counted on.
                final LotroGame game = actionContext.getGame();
                final int used = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(actionContext.getSource(), LIMIT_COUNTER, game.getGameState().getCurrentPhase()).getUsedLimit();
                if (used >= limit)
                    return EffectForecast.AT_LIMIT;
                return given;
            }
        };

        result.addEffectAppender(cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        final Evaluator evaluator = amountSource.getEvaluator(actionContext);
                        LotroGame game = actionContext.getGame();
                        final int amount = evaluator.evaluateExpression(game, actionContext.getSource());

                        if (limit != -1 && amount != 0) {
                            // "(limit +N)" is the source card's for the whole phase (CR 4.2 "limit"; Ketura on
                            // F-0125): one counter for the card, counted once for a use however many cards the
                            // use reaches, and what a use would add beyond the limit is ignored.
                            int absAmount = Math.abs(amount);
                            int multiplier = absAmount / amount;
                            List<Modifier> modifiers = new LinkedList<>();
                            if (!cardsFromMemory.isEmpty()) {
                                int incrementedBy = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(action.getActionSource(), LIMIT_COUNTER, game.getGameState().getCurrentPhase()).incrementToLimit(limit, absAmount);
                                if (incrementedBy > 0) {
                                    modifiers.add(new StrengthModifier(actionContext.getSource(), Filters.in(cardsFromMemory), null, multiplier * incrementedBy));
                                }
                            }
                            return new AddUntilModifierEffect(modifiers, time);
                        } else {
                            return new AddUntilModifierEffect(
                                    new StrengthModifier(actionContext.getSource(), Filters.in(cardsFromMemory), null, amount), time);
                        }
                    }
                });

        return result;
    }

}
