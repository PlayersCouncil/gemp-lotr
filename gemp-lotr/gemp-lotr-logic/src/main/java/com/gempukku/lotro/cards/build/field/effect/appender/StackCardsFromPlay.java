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
import com.gempukku.lotro.logic.effects.StackCardFromPlayEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackCardsFromPlay implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "where", "count");

        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");

        if (where == null)
            throw new InvalidCardDefinitionException("You need to define where to stack the cards for StackCards effect");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(where, "_temp1", "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                CardResolver.resolveCards(select, valueSource, "_temp2", "you", "Choose cards to stack", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard card = actionContext.getCardFromMemory("_temp1");
                        if (card != null) {
                            final Collection<? extends PhysicalCard> cardsInHand = actionContext.getCardsFromMemory("_temp2");

                            List<Effect> result = new LinkedList<>();
                            for (PhysicalCard physicalCard : cardsInHand) {
                                result.add(new StackCardFromPlayEffect(physicalCard, card));
                            }

                            return result;
                        }
                        return null;
                    }
                });

        return result;
    }

}
