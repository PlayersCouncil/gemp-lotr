package com.gempukku.lotro.cards.build.field.effect.appender;

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
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.*;

public class PutCardsFromHandOnTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "hand", "select", "count", "reveal", "text");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean reveal = FieldUtils.getBoolean(effectObject.get("reveal"), "reveal", false);
        //What the player choosing the cards is asked.  A definition whose count may be 0 says here that choosing
        // none is allowed, in place of a yes/no asked beforehand.
        final String text = FieldUtils.getString(effectObject.get("text"), "text", "Choose cards from hand");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        MultiEffectAppender result = new MultiEffectAppender();

        final EffectAppender cardResolver = CardResolver.resolveCardsInHand(select, count, "_temp", player, hand, text, true, environment);
        result.addEffectAppender(cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final List<? extends PhysicalCard> cards = new ArrayList<>(actionContext.getCardsFromMemory("_temp"));
                        List<Effect> result = new LinkedList<>();
                        for (int i = 0; i < cards.size(); i++) {
                            result.add(
                                    //The player who chose the cards puts them down in the order he likes, as
                                    // PutCardsFromHandOnBottomOfDeck has it: not whoever is performing the action.
                                    new ChooseArbitraryCardsEffect(playerSource.getPlayer(actionContext),
                                            "Choose card to put on top of deck", cards, 1, 1) {
                                        @Override
                                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                            PhysicalCard card = selectedCards.iterator().next();
                                            // Removed from remaining
                                            for(var effect : result) {
                                                ((ChooseArbitraryCardsEffect)effect).removeCard(card);
                                            }

                                            GameState gameState = game.getGameState();
                                            if (reveal) {
                                                gameState.sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " from hand on the top of deck");
                                            } else {
                                                gameState.sendMessage(card.getOwner() + " puts a card from hand on the top of deck");
                                            }

                                            gameState.removeCardsFromZone(card.getOwner(), Collections.singleton(card));
                                            gameState.putCardOnTopOfDeck(card);
                                        }
                                    });
                        }
                        return result;
                    }
                });

        // Taught for Q-0224 (IdleOffers): with no card in that hand to choose, nothing is placed on the deck.
        return TaughtForecast.actsOnWhatItFindsInHand(result, PlayerResolver.resolvePlayer(hand), cardResolver);
    }

}
