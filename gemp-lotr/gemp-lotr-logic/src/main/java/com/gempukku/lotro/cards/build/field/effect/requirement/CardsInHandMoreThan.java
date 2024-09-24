package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

public class CardsInHandMoreThan implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "count", "player");

        final int count = FieldUtils.getInteger(object.get("count"), "count");
        final String player = FieldUtils.getString(object.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return (actionContext) -> {
            final String playerId = playerSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getHand(playerId).size() > count;
        };
    }
}
