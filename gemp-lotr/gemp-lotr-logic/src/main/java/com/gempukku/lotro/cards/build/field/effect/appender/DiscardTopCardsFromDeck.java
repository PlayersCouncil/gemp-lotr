package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DiscardTopCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DiscardTopCardsFromDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "count", "forced", "memorize");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        // Taught for Q-0224 (IdleOffers): nothing is discarded from the top of an empty draw deck (F-0211).  This
        // is not the optional draw of Q-0029, which stays on offer.
        return TaughtForecast.teach(new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                // Don't check if can discard top cards, since it's a cost
                final LotroGame game = actionContext.getGame();
                return game.getGameState().getDeck(deckId).size() >= count
                        && (!forced || game.getModifiersQuerying().canDiscardCardsFromTopOfDeck(game, actionContext.getPerformingPlayer(), actionContext.getSource()));
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new DiscardTopCardFromDeckEffect(actionContext.getSource(), deckId, count, forced) {
                    @Override
                    protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, cards);
                    }
                };
            }
        }, actionContext -> actionContext.getGame().getGameState().getDeck(playerSource.getPlayer(actionContext)).isEmpty()
                ? EffectForecast.NOTHING : EffectForecast.UNKNOWN);
    }

}
