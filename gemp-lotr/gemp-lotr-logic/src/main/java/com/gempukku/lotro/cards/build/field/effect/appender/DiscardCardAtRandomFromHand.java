package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DiscardCardAtRandomFromHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class DiscardCardAtRandomFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "count", "forced");

        final String player = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");

        // Taught for Q-0224 (IdleOffers), by Q-0231: no card is discarded at random from an empty hand, nor from
        // a hand this card may not discard from ("Shadow cards may not discard cards from your hand", Frodo,
        // Courteous Halfling 4_301): what DiscardCardAtRandomFromHandEffect itself asks before it discards.  The
        // number of cards is not worked out: one card that can be discarded is something done.
        return TaughtForecast.teach(new DelayedAppender() {
            @Override
            protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String playerId = playerSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                List<Effect> result = new LinkedList<>();
                for (int i = 0; i < count; i++)
                    result.add(new DiscardCardAtRandomFromHandEffect(actionContext.getSource(), playerId, forced));

                return result;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                final String playerId = playerSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(game, null);
                return game.getGameState().getHand(playerId).size() >= count
                        && (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(game, playerId, actionContext.getSource()));
            }
        }, actionContext -> {
            final LotroGame game = actionContext.getGame();
            final String playerId = playerSource.getPlayer(actionContext);
            if (game.getGameState().getHand(playerId).isEmpty())
                return EffectForecast.NOTHING;
            if (forced && !game.getModifiersQuerying().canDiscardCardsFromHand(game, playerId, actionContext.getSource()))
                return EffectForecast.NOTHING;
            return EffectForecast.UNKNOWN;
        });
    }

}
