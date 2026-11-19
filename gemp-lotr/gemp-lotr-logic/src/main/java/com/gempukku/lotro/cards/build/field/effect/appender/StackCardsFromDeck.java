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
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.effects.StackCardFromDeckEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.RangeEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackCardsFromDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "where", "count", "shuffle", "showAll");

        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean shuffle = FieldUtils.getBoolean(effectObject.get("shuffle"), "shuffle");
        boolean showAll = FieldUtils.getBoolean(effectObject.get("showAll"), "showAll");

        // A card looked for in the draw deck need not be found ("There is no penalty if you don't find ... a card
        // you are looking for in your draw deck"), so a count of N chosen from the deck by a filter is "up to N".
        // Cards already picked out and held in memory are not being looked for, and are taken as the count says.
        final boolean searchesDeck = select.startsWith("choose(") && !select.startsWith("choose(memory(");
        final ValueSource choiceSource = !searchesDeck ? valueSource : actionContext -> {
            final Evaluator count = valueSource.getEvaluator(actionContext);
            return new RangeEvaluator(new ConstantEvaluator(0), count::getMaximum);
        };
        // What the effect needs of the deck before it can be carried out in full is not changed by that: a card
        // that stacks "from your draw deck or discard pile" still does not offer a deck with nothing in it to stack.
        // This appender is only ever asked whether it could be carried out; it is never added to the result.
        final EffectAppender existenceCheck = !searchesDeck ? null :
                CardResolver.resolveCardsInDeck(select, null, valueSource, "_temp2", "you", "you", showAll, "Choose cards to stack", environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(where, "_temp1", "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(select, null, choiceSource, "_temp2", "you", "you", showAll, "Choose cards to stack", environment));
        result.addEffectAppender(
                new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return existenceCheck == null || existenceCheck.isPlayableInFull(actionContext);
            }

            @Override
            protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final PhysicalCard card = actionContext.getCardFromMemory("_temp1");
                if (card != null) {
                    final Collection<? extends PhysicalCard> cardsInDeck = actionContext.getCardsFromMemory("_temp2");

                    List<Effect> result = new LinkedList<>();
                    for (PhysicalCard physicalCard : cardsInDeck) {
                        result.add(new StackCardFromDeckEffect(physicalCard, card));
                    }

                    return result;
                }
                return null;
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
