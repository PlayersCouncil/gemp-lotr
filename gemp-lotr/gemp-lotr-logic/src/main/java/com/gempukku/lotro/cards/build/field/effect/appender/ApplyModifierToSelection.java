package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddUntilModifierEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ApplyModifierToSelection implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "modifier", "count", "selector", "player", "until", "memorize", "text");

        final JSONObject modifierObj = (JSONObject) effectObject.get("modifier");
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String selector = FieldUtils.getString(effectObject.get("selector"), "selector");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        if (text == null)
            throw new InvalidCardDefinitionException("You need to define text to show");

        MultiEffectAppender result = new MultiEffectAppender();

        ModifierSource modifierSource = environment.getModifierSourceFactory().getModifier(modifierObj, environment);

        result.addEffectAppender(
                CardResolver.resolveCards(selector, countSource, memory, player, text, environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);

                        final Modifier modifier = modifierSource.getModifier(actionContext);
                        return new AddUntilModifierEffect(modifier, until);
                    }
                });

        return result;
    }
}
