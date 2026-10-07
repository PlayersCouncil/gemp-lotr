package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.InactiveReason;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ReturnCardsToHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Map;

public class ReturnToHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "count", "player", "override");

        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        // "override: hindered", as on Discard: hindered cards are returned too, for a text that names the cards by
        // nothing but "card" or a side ("return each Free Peoples card that companion bears"; Ketura on Q-0098).
        final Map<InactiveReason, Boolean> override = FieldUtils.getSpotOverride(effectObject.get("override"), "override", SpotOverride.NONE);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select, override,
                        actionContext -> (Filter) (game, physicalCard) -> game.getModifiersQuerying().canBeReturnedToHand(game, physicalCard, actionContext.getSource()),
                        valueSource, "_temp", player, "Choose cards to return to hand", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory("_temp");
                        return new ReturnCardsToHandEffect(actionContext.getSource(), override, Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
