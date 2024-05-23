package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.ReplaceSiteResult;
import org.json.simple.JSONObject;

public class ReplacesSite implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "player", "number");

        String player = FieldUtils.getString(value.get("player"), "player");
        ValueSource numberResolver = ValueResolver.resolveEvaluator(value.get("number"), 0, environment);

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player, environment) : null;

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                EffectResult effectResult = actionContext.getEffectResult();
                if (effectResult.getType() != EffectResult.Type.REPLACE_SITE)
                    return false;

                ReplaceSiteResult replaceSiteResult = (ReplaceSiteResult) effectResult;
                int number = numberResolver.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (number != 0 && replaceSiteResult.getSiteNumber() != number)
                    return false;

                String playerId = playerSource != null ? playerSource.getPlayer(actionContext) : null;
                if (playerId != null && !playerId.equals(replaceSiteResult.getPlayerId()))
                    return false;

                return true;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
