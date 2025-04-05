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
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.DiscardUtils;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import org.json.simple.JSONObject;

import java.util.*;

public class PutCardsFromPlayOnBottomOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "select", "count");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select, valueSource, "_temp", player, "Choose cards in play", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final List<? extends PhysicalCard> cards = new ArrayList<>(actionContext.getCardsFromMemory("_temp"));
                        List<Effect> result = new LinkedList<>();
                        for (int i = 0; i < cards.size(); i++) {
                            result.add(
                                    new ChooseArbitraryCardsEffect(actionContext.getPerformingPlayer(),
                                            "Choose card to put beneath draw deck", cards, 1, 1) {
                                        @Override
                                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                            var card = selectedCards.iterator().next();
                                            // Removed from remaining
                                            for(var effect : result) {
                                                ((ChooseArbitraryCardsEffect)effect).removeCard(card);
                                            }

                                            var gameState = game.getGameState();

                                            var discardedFromPlayCards = new HashSet<PhysicalCard>();
                                            var toMoveToDiscardCards = new HashSet<PhysicalCard>();

                                            DiscardUtils.cardsToChangeZones(game, Collections.singleton(card), discardedFromPlayCards, toMoveToDiscardCards);

                                            var removeFromPlay = new HashSet<>(toMoveToDiscardCards);
                                            removeFromPlay.add(card);

                                            gameState.removeCardsFromZone(card.getOwner(), removeFromPlay);

                                            for (PhysicalCard attachedCard : toMoveToDiscardCards) {
                                                gameState.addCardToZone(game, attachedCard, Zone.DISCARD);
                                            }

                                            gameState.sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " from play on the bottom of deck");
                                            gameState.putCardOnBottomOfDeck(card);

                                            for (PhysicalCard discardedCard : discardedFromPlayCards) {
                                                game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));
                                            }
                                        }
                                    });
                        }
                        return result;
                    }
                });

        return result;
    }

}
