package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
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

public class PutCardsFromHandOnBottomOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "select", "count", "reveal");

        final String playerName = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean reveal = FieldUtils.getBoolean(effectObject.get("reveal"), "reveal", false);
        final PlayerSource player = PlayerResolver.resolvePlayer(playerName);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(select, count, "_temp", playerName, playerName, "Choose cards from hand", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final List<? extends PhysicalCard> cards = new ArrayList<>(actionContext.getCardsFromMemory("_temp"));
                        List<Effect> result = new LinkedList<>();

                        for (int i = 0; i < cards.size(); i++) {
                            result.add(
                                    new ChooseArbitraryCardsEffect(player.getPlayer(actionContext),
                                            "Choose card to put beneath draw deck", cards, 1, 1) {
                                        @Override
                                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                            PhysicalCard card = selectedCards.iterator().next();
                                            // Removed from remaining
                                            for(var effect : result) {
                                                ((ChooseArbitraryCardsEffect)effect).removeCard(card);
                                            }

                                            GameState gameState = game.getGameState();
                                            if (reveal) {
                                                gameState.sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " from hand on the bottom of deck");
                                            } else {
                                                gameState.sendMessage(card.getOwner() + " puts a card from hand on the bottom of deck");
                                            }

                                            gameState.removeCardsFromZone(card.getOwner(), Collections.singleton(card));
                                            gameState.putCardOnBottomOfDeck(card);
                                        }
                                    });
                        }
                        return result;
                    }
                });

        return result;
    }

}
