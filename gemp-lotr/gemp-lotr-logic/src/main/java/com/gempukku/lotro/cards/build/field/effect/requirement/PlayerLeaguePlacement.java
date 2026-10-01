package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.state.RTMDGameInfo;
import org.json.simple.JSONObject;

/**
 * True if the resolved player was placed within the top {@code percentage}% of the current league
 * standings when this game was created.  Always false outside a league game that supplies standings.
 */
public class PlayerLeaguePlacement implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "percentage");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final int percentage = FieldUtils.getInteger(object.get("percentage"), "percentage");

        return PlayerScope.requirement(player, (actionContext, playerId) -> {
            if (!(actionContext.getGame().getExtraInfo() instanceof RTMDGameInfo rtmdInfo))
                return false;

            final RTMDGameInfo.LeaguePlacement placement = rtmdInfo.getLeaguePlacement(playerId);
            return placement != null && placement.isWithinTopPercentage(percentage);
        });
    }
}
