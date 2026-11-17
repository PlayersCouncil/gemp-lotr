package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.PutCardFromDiscardIntoHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromDiscardIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "player", "discard", "memorize", "text");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String discard = FieldUtils.getString(effectObject.get("discard"), "discard", player);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        //What the player choosing the cards is asked, where the definition has words of its own for it.  A
        // definition whose count may be 0 says here that choosing none is allowed, in place of a yes/no asked
        // beforehand.
        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        // The player the definition has choose the cards is the one who takes them, whoever's hand they go to:
        // "each Shadow player may take ... into hand from his or her discard pile" on a Free Peoples card is the
        // Shadow player's taking, and "return that minion to its owner's hand" is the taking of the player whose
        // card says it.
        final PlayerSource takingPlayerSource = PlayerResolver.resolvePlayer(player);

        final EffectAppender cardResolver =
                CardResolver.resolveCardsInDiscard(select, null, null, valueSource, memorize, player, discard, "Choose cards from discard", text, environment);

        MultiEffectAppender result = new MultiEffectAppender() {
            // About to be carried out with no card to take (none chosen, or the one chosen gone from the pile),
            // it does nothing.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return EffectForecast.foundAtResolution(actionContext, cardResolver);
            }
        };

        result.addEffectAppender(cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory(memorize);
                        List<Effect> result = new LinkedList<>();
                        final String takingPlayer = takingPlayerSource.getPlayer(actionContext);
                        for (PhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromDiscardIntoHandEffect(takingPlayer, card));
                        }

                        return result;
                    }
                });

        return result;

    }
}
