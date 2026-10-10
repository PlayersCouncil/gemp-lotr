package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.PutCardFromDeckIntoHandEffect;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.RangeEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromDeckIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "shuffle", "reveal", "showAll");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final boolean shuffle = FieldUtils.getBoolean(effectObject.get("shuffle"), "shuffle");
        final boolean reveal = FieldUtils.getBoolean(effectObject.get("reveal"), "reveal");
        boolean showAll = FieldUtils.getBoolean(effectObject.get("showAll"), "showAll");

        // A card looked for in the draw deck need not be found ("There is no penalty if you don't find ... a card
        // you are looking for in your draw deck"), so a count of N chosen from the deck by a filter is "up to N".
        // Cards already picked out and held in memory (the top cards looked at, a card chosen by an earlier effect)
        // are not being looked for, and are taken as the count says.
        final boolean searchesDeck = select.startsWith("choose(") && !select.startsWith("choose(memory(");
        final ValueSource choiceSource = !searchesDeck ? valueSource : actionContext -> {
            final Evaluator count = valueSource.getEvaluator(actionContext);
            return new RangeEvaluator(new ConstantEvaluator(0), count::getMaximum);
        };
        // What the effect needs of the deck before it can be carried out in full is not changed by that: a card
        // that takes "from your draw deck or discard pile" still does not offer a deck with nothing in it to take.
        // This appender is only ever asked whether it could be carried out; it is never added to the result.
        final EffectAppender existenceCheck = !searchesDeck ? null :
                CardResolver.resolveCardsInDeck(select, null, valueSource, "_temp", "you", "you", showAll, "Choose cards from deck to put into hand", environment);

        final EffectAppender cardResolver =
                CardResolver.resolveCardsInDeck(select, null, choiceSource, "_temp", "you", "you", showAll, "Choose cards from deck to put into hand", environment);

        MultiEffectAppender result = new MultiEffectAppender() {
            // About to be carried out with no card to take (none chosen from the deck by an earlier effect), it
            // does nothing, unless it shuffles the deck all the same.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                if (shuffle)
                    return EffectForecast.UNKNOWN;
                return EffectForecast.foundAtResolution(actionContext, cardResolver);
            }
        };

        result.addEffectAppender(cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        return existenceCheck == null || existenceCheck.isPlayableInFull(actionContext);
                    }

                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        // The player whose action this is takes the cards.
                        for (PhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromDeckIntoHandEffect(actionContext.getPerformingPlayer(), card, reveal));
                        }

                        return result;
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
