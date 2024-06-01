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
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect;
import com.gempukku.lotro.logic.effects.ShuffleCardsFromPlayIntoDeckEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShuffleCardsFromPlayIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "select", "count", "includeStacked", "memorize", "memorizeStacked");

        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean includeStacked = FieldUtils.getBoolean(effectObject.get("includeStacked"), "includeStacked", false);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String memorizeStackedCards = FieldUtils.getString(effectObject.get("memorizeStacked"), "memorizeStacked");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select, valueSource, memorize, player, "Choose cards to shuffle into your deck", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsInPlay = actionContext.getCardsFromMemory(memorize);

                        if(includeStacked) {
                            if (memorizeStackedCards != null) {
                                GameState gameState = actionContext.getGame().getGameState();
                                Set<PhysicalCard> stackedCards = new HashSet<>();
                                cardsInPlay.forEach(card -> stackedCards.addAll(gameState.getStackedCards(card)));
                                actionContext.setCardMemory(memorizeStackedCards, stackedCards);
                            }

                            return new ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect(actionContext.getSource(), playerSource.getPlayer(actionContext), cardsInPlay);
                        }
                        else {
                            return new ShuffleCardsFromPlayIntoDeckEffect(actionContext.getSource(), playerSource.getPlayer(actionContext), cardsInPlay);
                        }
                    }
                });

        return result;
    }

}
