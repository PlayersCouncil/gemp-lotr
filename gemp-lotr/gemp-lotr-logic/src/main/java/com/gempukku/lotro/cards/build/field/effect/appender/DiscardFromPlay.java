package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardFromPlay implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "count", "select", "memorize", "memorizeStackedCards");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource discardingPlayer = PlayerResolver.resolvePlayer(player, environment);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        if (select == null)
            throw new InvalidCardDefinitionException("'select' is required for Discard effect.");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String stackedCardsMemory = FieldUtils.getString(effectObject.get("memorizeStackedCards"), "memorizeStackedCards");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select,
                        (actionContext) -> Filters.canBeDiscarded(actionContext.getPerformingPlayer(), actionContext.getSource()),
                        valueSource, memory, player, "Choose cards to discard", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final String discardingPlayerId = discardingPlayer.getPlayer(actionContext);
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        if (stackedCardsMemory != null) {
                            List<PhysicalCard> stackedCards = new LinkedList<>();
                            for (PhysicalCard physicalCard : cardsFromMemory) {
                                stackedCards.addAll(actionContext.getGame().getGameState().getStackedCards(physicalCard));
                            }

                            actionContext.setCardMemory(stackedCardsMemory, stackedCards);
                        }
                        return new DiscardCardsFromPlayEffect(discardingPlayerId, actionContext.getSource(), Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
