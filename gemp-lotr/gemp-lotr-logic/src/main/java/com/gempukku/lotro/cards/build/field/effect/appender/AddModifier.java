package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.cards.build.field.effect.requirement.SarumanFirstSentenceActive;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddUntilModifierEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class AddModifier implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "modifier", "until");

        final JSONObject modifierObj = (JSONObject) effectObject.get("modifier");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        ModifierSource modifierSource = environment.getModifierSourceFactory().getModifier(modifierObj, environment);

        final EffectAppender result = new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Modifier modifier = modifierSource.getModifier(actionContext);
                return new AddUntilModifierEffect(modifier, until);
            }
        };

        // Taught for Q-0224 (IdleOffers), engine pass 32: the switch "the first sentence of Saruman's game text does
        // not apply" is read by card definitions alone (the requirement sarumanFirstSentenceActive), so thrown with
        // no other card in play whose definition reads it, it does nothing: Saruman's Staff 4_174 borne by Saruman,
        // Of Many Colours 12_54 or Agent of the Dark Lord 12_144, whose texts do not read it (Legacy Ruling 6: the
        // ability "does not have any effect" there; F-0426, Q-0246).  The card whose action it is is left out of
        // the count, since the Staff reads the switch only to say it is not thrown already.
        if (modifierObj != null && "sarumanfirstsentenceinactive".equalsIgnoreCase(String.valueOf(modifierObj.get("type"))))
            return TaughtForecast.teach(result, actionContext -> {
                final PhysicalCard source = actionContext.getSource();
                for (PhysicalCard card : Filters.filterActive(actionContext.getGame(), Filters.any)) {
                    if (card != source && SarumanFirstSentenceActive.isReadBy(card.getBlueprint()))
                        return EffectForecast.UNKNOWN;
                }
                return EffectForecast.NOTHING;
            });
        return result;
    }
}
