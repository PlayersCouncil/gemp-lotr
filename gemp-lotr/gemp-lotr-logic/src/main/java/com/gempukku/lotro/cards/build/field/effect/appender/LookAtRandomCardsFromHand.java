package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.LookAtRandomCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.List;

public class LookAtRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "count", "memorize");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand");
        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorized = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        if (hand == null)
            throw new InvalidCardDefinitionException("LookAtRandomCardsFromHand requires that 'hand' be specified.");

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(game, null);

                if (actionContext.getGame().getGameState().getHand(handPlayer).size() < count)
                    return false;

                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, actionContext.getPerformingPlayer());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new LookAtRandomCardsFromHandEffect(actionContext.getPerformingPlayer(), handPlayer, actionContext.getSource(), count) {
                    @Override
                    protected void cardsSeen(List<PhysicalCard> seenCards) {
                        actionContext.setCardMemory(memorized, seenCards);
                    }
                };
            }
        };
    }

}
