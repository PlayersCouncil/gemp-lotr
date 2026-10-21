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
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.effects.StackCardFromPlayEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackCardsFromPlay implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "where", "count");

        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");

        if (where == null)
            throw new InvalidCardDefinitionException("You need to define where to stack the cards for StackCards effect");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        final EffectAppender whereResolver = CardResolver.resolveCard(where, "_temp1", "you", "Choose card to stack on", environment);
        final EffectAppender selectResolver = CardResolver.resolveCards(select, valueSource, "_temp2", "you", "Choose cards to stack", environment);

        // With nowhere to stack, the effect does nothing (F-0579), and says so to every forecast: the no-effect
        // flag, a prevention, a choice it is a branch of and the rule of Q-0224 alike.
        MultiEffectAppender result = new MultiEffectAppender() {
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                final EffectForecast found = whereResolver.forecast(actionContext);
                if (found.getReach() != null && found.getReach().isEmpty())
                    return EffectForecast.NOTHING;
                return super.forecast(actionContext);
            }
        };
        result.addEffectAppender(whereResolver);
        if (cost) {
            // As a cost the action is not offered without somewhere to stack (the where resolver's isPlayableInFull).
            result.addEffectAppender(selectResolver);
        } else {
            // The where is resolved first ("Choose card to stack on"); where it has found no card, nothing is
            // stacked, and what to stack is not asked (F-0579, Troll of Gorgoroth 8_108: "Exert Troll of Gorgoroth
            // to stack your besieger on a site you control", with no site controlled).
            result.addEffectAppender(
                    new DelayedAppender() {
                        @Override
                        protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                            if (actionContext.getCardFromMemory("_temp1") == null)
                                return null;
                            SubAction subAction = new SubAction(action);
                            selectResolver.appendEffect(cost, subAction, actionContext);
                            return new StackActionEffect(subAction);
                        }

                        @Override
                        public boolean isPlayableInFull(ActionContext actionContext) {
                            return selectResolver.isPlayableInFull(actionContext);
                        }

                        @Override
                        public EffectForecast forecast(ActionContext actionContext) {
                            return selectResolver.forecast(actionContext);
                        }

                        @Override
                        public boolean canPreEvaluate() {
                            return selectResolver.canPreEvaluate();
                        }
                    });
        }
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

        // Taught for Q-0224 (IdleOffers): with nothing to stack, or nothing to stack it on, nothing is stacked.
        return TaughtForecast.actsOnWhatItFinds(result, whereResolver, selectResolver);
    }

}
