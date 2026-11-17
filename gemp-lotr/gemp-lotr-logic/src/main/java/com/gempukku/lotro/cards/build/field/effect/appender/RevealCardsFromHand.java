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
import com.gempukku.lotro.logic.effects.RevealCardsFromYourHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RevealCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "memorize", "hand");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(select, countSource, memorize, hand, hand, "Choose cards to reveal", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToReveal = actionContext.getCardsFromMemory(memorize);
                        // The hand is the one the definition names, which is not always that of the player acting:
                        // its owner is the one player who is not shown the cards, and the one the game log names.
                        return new RevealCardsFromYourHandEffect(actionContext.getSource(), handSource.getPlayer(actionContext), cardsToReveal);
                    }
                });

        return result;
    }

}
