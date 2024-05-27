package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.RemoveCardsFromDeckEffect;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class RemoveCardsInDeckFromGame implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "player", "deck", "shuffle", "showAll");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        boolean shuffle = FieldUtils.getBoolean(effectObject.get("shuffle"), "shuffle");
        boolean showAll = FieldUtils.getBoolean(effectObject.get("showAll"), "showAll");

        PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(filter, null, valueSource, "_temp", player, deck, showAll, "Choose cards from deck to remove from game", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        String player = playerSource.getPlayer(actionContext);
                        return new RemoveCardsFromDeckEffect(player, actionContext.getSource(), new ArrayList<>(cards));
                    }
                });
        if (shuffle)
            result.addEffectAppender(
                    new DelayedAppender() {
                        @Override
                        protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                            return new ShuffleDeckEffect(actionContext.getPerformingPlayer());
                        }
                    });

        return result;

    }
}
