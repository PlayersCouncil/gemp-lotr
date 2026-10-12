package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddUntilModifierEffect;
import com.gempukku.lotro.logic.modifiers.CantGainKeywordsModifier;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

/**
 * "Unable to gain game text keywords until <a time>" (Into the Caves 17_100; Q-0282: A). The cards chosen keep
 * the game text keywords they have when this is carried out and gain none until the time given
 * (CantGainKeywordsModifier). What they have is read as the effect is carried out, so a keyword the same text
 * makes them lose first (a `removeKeyword` before this) is not among those kept.
 */
public class CantGainKeywords implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "memorize", "until");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        final EffectAppender cardResolver =
                CardResolver.resolveCards(select, valueSource, memory, "you", "Choose cards that cannot gain keywords", environment);

        MultiEffectAppender result = new MultiEffectAppender() {
            // About to be carried out with no card to bar, it does nothing.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return EffectForecast.foundAtResolution(actionContext, cardResolver);
            }
        };

        result.addEffectAppender(cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory(memory);
                        return new AddUntilModifierEffect(
                                new CantGainKeywordsModifier(actionContext.getSource(), actionContext.getGame(), cards), until);
                    }
                });

        return result;
    }
}
