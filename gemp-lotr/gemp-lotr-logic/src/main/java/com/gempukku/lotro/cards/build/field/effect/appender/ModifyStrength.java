package com.gempukku.lotro.cards.build.field.effect.appender;

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
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "count", "select", "until", "memorize", "limitPerCardThisPhase");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), environment);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final TimeResolver.Time time = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");
        final int limit = FieldUtils.getInteger(effectObject.get("limitPerCardThisPhase"), "limitPerCardThisPhase", -1);
        if (limit > -1 && effectObject.get("until") != null)
            throw new InvalidCardDefinitionException("If limitPerCardThisPhase is defined, the until should not be, as it can only be applied until current phase");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select, valueSource, memory, "you", "Choose cards to modify strength of", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        final Evaluator evaluator = amountSource.getEvaluator(actionContext);
                        LotroGame game = actionContext.getGame();
                        final int amount = evaluator.evaluateExpression(game, actionContext.getSource());

                        if (limit != -1) {
                            int absAmount = Math.abs(amount);
                            int multiplier = absAmount / amount;
                            List<Modifier> modifiers = new LinkedList<>();
                            for (PhysicalCard affectedCard : cardsFromMemory) {
                                int incrementedBy = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(action.getActionSource(), affectedCard.getCardId() + "-", game.getGameState().getCurrentPhase()).incrementToLimit(limit, absAmount);
                                if (incrementedBy > 0) {
                                    modifiers.add(new StrengthModifier(actionContext.getSource(), affectedCard, null, multiplier * incrementedBy));
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
