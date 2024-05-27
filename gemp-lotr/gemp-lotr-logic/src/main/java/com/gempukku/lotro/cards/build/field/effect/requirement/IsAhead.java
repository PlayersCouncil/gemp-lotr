package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.state.LotroGame;
import org.json.simple.JSONObject;

public class IsAhead implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object);

        return actionContext -> {
            LotroGame game = actionContext.getGame();
            String currentPlayer = game.getGameState().getCurrentPlayerId();
            int currentPosition = game.getGameState().getCurrentSiteNumber();
            for (String player : game.getGameState().getPlayerOrder().getAllPlayers()) {
                if (!player.equals(currentPlayer))
                    if (game.getGameState().getPlayerPosition(player) >= currentPosition)
                        return false;
            }
            return true;
        };
    }
}
