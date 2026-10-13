package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import org.json.simple.JSONObject;

public class DrawCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "player");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        final EffectAppender draw = new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                final Evaluator evaluator = count.getEvaluator(null);
                final int cardCount = evaluator.evaluateExpression(actionContext.getGame(), null);
                return actionContext.getGame().getGameState().getDeck(drawPlayer).size() >= cardCount
                        && !RuleUtils.isBarredFromDrawing(actionContext.getGame(), drawPlayer);
            }

            // From an empty draw deck nothing is drawn.  How many cards it would draw is not worked out, beyond
            // whether a cap on the number is spent.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                if (actionContext.getGame().getGameState().getDeck(drawPlayer).isEmpty())
                    return EffectForecast.NOTHING_FROM_A_DRAW_DECK;
                // Nor by a player who may not take cards into hand from the draw deck (a draw written on a card
                // is never a reconcile's).
                if (RuleUtils.isBarredFromDrawing(actionContext.getGame(), drawPlayer))
                    return EffectForecast.NOTHING_FROM_A_DRAW_DECK;
                // Nor is anything drawn past the Rule of 4.  That is said only of a draw that is about to be
                // made (to a prevention, which then is not put to anybody): the no-effect flag of an action
                // on offer is left as it was.
                final LotroGame game = actionContext.getGame();
                if (actionContext.isAtResolution() && game.getFormat().hasRuleOfFour()
                        && !game.getModifiersQuerying().canDrawCardNoIncrement(game, drawPlayer))
                    return EffectForecast.NOTHING_FROM_A_DRAW_DECK;
                // A capped number of cards whose cap is spent is no cards.
                return EffectForecast.isSpent(count, actionContext, null)
                        ? EffectForecast.AT_LIMIT : EffectForecast.UNKNOWN;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                final Evaluator evaluator = count.getEvaluator(actionContext);
                final int cardsDrawn = evaluator.evaluateExpression(actionContext.getGame(), null);
                return new DrawCardsEffect(action, drawPlayer, cardsDrawn);
            }
        };

        // Taught for Q-0224 (IdleOffers): a player who may draw no more (the Rule of 4 spent, or a card that bars
        // him from drawing) draws nothing, so an optional trigger or a no-cost ability whose whole effect is a draw
        // is not offered to him (Ketura on Q-0262: B).  An empty draw deck is not that: the offer is still made
        // (Q-0029), as the effect's own forecast says.
        return TaughtForecast.teach(draw, actionContext ->
                RuleUtils.mayDrawACard(actionContext.getGame(), playerSource.getPlayer(actionContext))
                        ? draw.forecast(actionContext) : EffectForecast.NOTHING);
    }

}
