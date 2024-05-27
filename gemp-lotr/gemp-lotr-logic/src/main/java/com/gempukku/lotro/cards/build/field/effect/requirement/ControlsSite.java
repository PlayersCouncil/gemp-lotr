package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import org.json.simple.JSONObject;

public class ControlsSite implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);

        return (actionContext) -> {
            LotroGame game = actionContext.getGame();
            String playerId = playerSource.getPlayer(actionContext);
            return Filters.findFirstActive(game, Filters.siteControlled(playerId)) != null;
        };
    }
}
