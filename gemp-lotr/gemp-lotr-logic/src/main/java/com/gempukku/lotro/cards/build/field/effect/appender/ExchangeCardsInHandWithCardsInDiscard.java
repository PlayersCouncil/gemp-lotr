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
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExchangeCardsInHandWithCardsInDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "selectInHand", "selectInDiscard", "countInHand", "countInDiscard");

        final String selectInHand = FieldUtils.getString(effectObject.get("selectInHand"), "selectInHand");
        final ValueSource countHand = ValueResolver.resolveEvaluator(effectObject.get("countInHand"), 1, environment);
        final String selectInDiscard = FieldUtils.getString(effectObject.get("selectInDiscard"), "selectInDiscard");
        final ValueSource countDiscard = ValueResolver.resolveEvaluator(effectObject.get("countInDiscard"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(selectInHand, countHand, "_tempHand", "you", "you", "Choose cards to exchange in hand", environment));
        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(selectInDiscard, countDiscard, "_tempDiscard", "you", "Choose cards to exchange in discard", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        String performingPlayer = actionContext.getPerformingPlayer();
                        Collection<? extends PhysicalCard> handCards = actionContext.getCardsFromMemory("_tempHand");
                        Collection<? extends PhysicalCard> discardCards = actionContext.getCardsFromMemory("_tempDiscard");
                        return new AbstractEffect() {
                            @Override
                            protected FullEffectResult playEffectReturningResult(LotroGame game) {
                                int hand = countHand.getEvaluator(actionContext).evaluateExpression(game, null);
                                if(handCards.size() < hand) {
                                    game.getGameState().sendMessage(performingPlayer + " did not have " + hand + " card in hand to exchange.");
                                    return new FullEffectResult(false);
                                }

                                Set<PhysicalCard> cardsToRemove = new HashSet<>();
                                cardsToRemove.addAll(handCards);
                                cardsToRemove.addAll(discardCards);
                                game.getGameState().sendMessage(performingPlayer + " exchanges " + GameUtils.getAppendedNames(discardCards) + " from discard with " + GameUtils.getAppendedNames(handCards) + " in hand");
                                game.getGameState().removeCardsFromZone(performingPlayer, cardsToRemove);
                                for (PhysicalCard discardCard : discardCards) {
                                    game.getGameState().addCardToZone(game, discardCard, Zone.HAND);
                                }
                                for (PhysicalCard handCard : handCards) {
                                    game.getGameState().addCardToZone(game, handCard, Zone.DISCARD);
                                }
                                return new FullEffectResult(true);
                            }

                            @Override
                            public boolean isPlayableInFull(LotroGame game) {
                                int hand = countHand.getEvaluator(actionContext).evaluateExpression(game, null);
                                return handCards.size() >= hand;
                            }
                        };
                    }
                });

        return result;
    }
}
