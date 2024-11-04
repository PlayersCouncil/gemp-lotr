package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import org.json.simple.JSONObject;

public class ControlsSite implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "count");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final ValueSource countSource = ValueResolver.resolveEvaluator(object.get("count"), 1, environment);
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return (actionContext) -> {
            final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            return GameUtils.getControlledSitesCountByPlayer(actionContext.getGame(),
                    playerSource.getPlayer(actionContext)) >= count;
        };
    }
}
