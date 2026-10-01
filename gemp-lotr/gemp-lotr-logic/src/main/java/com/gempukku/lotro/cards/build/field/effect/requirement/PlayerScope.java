package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.GameUtils;

import java.util.function.BiPredicate;

/**
 * Builds a {@link Requirement} that tests one player, named by a {@code player} field.
 * Every value {@link PlayerResolver} understands is accepted, plus "any", which is true when the
 * test holds for at least one player in the game.
 */
class PlayerScope {

    private PlayerScope() {
    }

    static Requirement requirement(String player, BiPredicate<ActionContext, String> test) throws InvalidCardDefinitionException {
        if (player.equalsIgnoreCase("any")) {
            return (actionContext) -> {
                if (actionContext.getGame() == null)
                    return false;

                for (String playerId : GameUtils.getAllPlayers(actionContext.getGame())) {
                    if (test.test(actionContext, playerId))
                        return true;
                }
                return false;
            };
        }

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        return (actionContext) -> {
            if (actionContext.getGame() == null)
                return false;

            final String playerId = playerSource.getPlayer(actionContext);
            return playerId != null && test.test(actionContext, playerId);
        };
    }
}
