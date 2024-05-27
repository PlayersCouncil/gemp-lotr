package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class AddBurdens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "player");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String playerAddingBurden = playerSource.getPlayer(actionContext);
                final int amount = amountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (amount > 0) {
                    return new AddBurdenEffect(playerAddingBurden, actionContext.getSource(), amount);
                } else {
                    return null;
                }
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                LotroGame game = actionContext.getGame();
                return game.getModifiersQuerying().canAddBurden(game, playerSource.getPlayer(actionContext), actionContext.getSource());
            }
        };
    }

}
