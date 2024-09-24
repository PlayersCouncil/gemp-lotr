package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ReorderTopCardsOfDeckEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ReorderTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "player", "deck");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final PlayerSource deckSource = PlayerResolver.resolvePlayer(deck);

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final Evaluator count = valueSource.getEvaluator(actionContext);
                return actionContext.getGame().getGameState().getDeck(deckSource.getPlayer(actionContext)).size() >= count.evaluateExpression(actionContext.getGame(), null);
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                return new ReorderTopCardsOfDeckEffect(action, playerSource.getPlayer(actionContext), deckSource.getPlayer(actionContext), count);
            }
        };
    }
}
