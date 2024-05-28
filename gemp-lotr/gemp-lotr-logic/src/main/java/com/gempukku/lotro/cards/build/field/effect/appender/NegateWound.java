package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.NegateWoundEffect;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class NegateWound implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select");

        final String select = FieldUtils.getString(effectObject.get("select"), "select", "all(any)");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select,
                        (actionContext) -> {
                            final WoundCharactersEffect woundEffect = (WoundCharactersEffect) actionContext.getEffect();
                            return Filters.in(woundEffect.getAffectedCardsMinusPrevented(actionContext.getGame()));
                        }, actionContext -> new ConstantEvaluator(1), "_temp", "you", "Choose characters to negate wound to", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        final WoundCharactersEffect woundEffect = (WoundCharactersEffect) actionContext.getEffect();

                        return new NegateWoundEffect(woundEffect, Filters.in(cards));
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        return true;
                    }
                });

        return result;
    }

}
