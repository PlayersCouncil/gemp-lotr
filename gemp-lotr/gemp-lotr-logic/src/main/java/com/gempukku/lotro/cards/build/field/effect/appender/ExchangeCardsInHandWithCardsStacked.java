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
import java.util.List;
import java.util.Set;

public class ExchangeCardsInHandWithCardsStacked implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "selectInHand", "selectInStacked", "countInHand", "countInStacked");

        final String selectInHand = FieldUtils.getString(effectObject.get("selectInHand"), "selectInHand");
        final ValueSource countHand = ValueResolver.resolveEvaluator(effectObject.get("countInHand"), 1, environment);
        final String selectInStacked = FieldUtils.getString(effectObject.get("selectInStacked"), "selectInStacked");

        final ValueSource stackedCardCount = ValueResolver.resolveEvaluator("1", environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(selectInHand, countHand, "_tempHand", "you", "you", "Choose cards to exchange in hand", environment));
        result.addEffectAppender(
                CardResolver.resolveCards(selectInStacked, stackedCardCount, "_tempStacked", "you", "Choose card to exchange stacked cards from", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        String performingPlayer = actionContext.getPerformingPlayer();
                        Collection<? extends PhysicalCard> handCards = actionContext.getCardsFromMemory("_tempHand");
                        PhysicalCard stackedCard = actionContext.getCardFromMemory("_tempStacked");
                        if (stackedCard == null)
                            return null;
                        List<PhysicalCard> stackedCards = actionContext.getGame().getGameState().getStackedCards(stackedCard);
                        return new AbstractEffect() {
                            @Override
                            protected FullEffectResult playEffectReturningResult(LotroGame game) {
                                Set<PhysicalCard> cardsToRemove = new HashSet<>();
                                cardsToRemove.addAll(handCards);
                                cardsToRemove.addAll(stackedCards);
                                game.getGameState().sendMessage(performingPlayer + " exchanges " + GameUtils.getAppendedNames(stackedCards) + " from stacked with " + GameUtils.getAppendedNames(handCards) + " in hand");
                                game.getGameState().removeCardsFromZone(performingPlayer, cardsToRemove);
                                for (PhysicalCard stackedCard : stackedCards) {
                                    game.getGameState().addCardToZone(game, stackedCard, Zone.HAND);
                                }
                                for (PhysicalCard handCard : handCards) {
                                    game.getGameState().stackCard(game, handCard, stackedCard);
                                }
                                return null;
                            }

                            @Override
                            public boolean isPlayableInFull(LotroGame game) {
                                return true;
                            }
                        };
                    }
                });

        return result;
    }
}
