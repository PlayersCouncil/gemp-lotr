package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.DrawCardOrPutIntoHandResult;
import org.json.simple.JSONObject;

public class PlayerDrawsCard implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "player");

        final String player = FieldUtils.getString(value.get("player"), "player");

        PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);
        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                String playerId = playerSource.getPlayer(actionContext);

                EffectResult effectResult = actionContext.getEffectResult();
                if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
                    DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
                    return drawResult.getPlayerId().equals(playerId);
                }
                return false;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
