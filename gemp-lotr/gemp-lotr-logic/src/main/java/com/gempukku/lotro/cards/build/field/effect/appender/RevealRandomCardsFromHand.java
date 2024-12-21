package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.RevealRandomCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.List;

public class RevealRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "forced", "count", "memorize");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");
        final String memorized = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new RevealRandomCardsFromHandEffect(actionContext.getPerformingPlayer(), handPlayer, actionContext.getSource(), count) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        actionContext.setCardMemory(memorized, revealedCards);
                    }
                };
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (actionContext.getGame().getGameState().getHand(handPlayer).size() < count)
                    return false;
                return !forced
                        || actionContext.getGame().getModifiersQuerying().canLookOrRevealCardsInHand(actionContext.getGame(), handPlayer, actionContext.getPerformingPlayer());
            }
        };
    }
}
