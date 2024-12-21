package com.gempukku.lotro.cards.build.field.effect.appender.resolver;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.GameUtils;

import java.util.Locale;

public class PlayerResolver {
    public static PlayerSource resolvePlayer(String type) throws InvalidCardDefinitionException {
        if (type.equalsIgnoreCase("you"))
            return ActionContext::getPerformingPlayer;
        if (type.equalsIgnoreCase("owner"))
            return (actionContext) -> actionContext.getSource().getOwner();
        else if (type.equalsIgnoreCase("shadowPlayer") || type.equalsIgnoreCase("shadow")
                || type.equalsIgnoreCase("s"))
            return (actionContext) -> GameUtils.getFirstShadowPlayer(actionContext.getGame());
        else if (type.equalsIgnoreCase("fp") || type.equalsIgnoreCase("freeps")
                || type.equalsIgnoreCase("free peoples") || type.equalsIgnoreCase("free people"))
            return ((actionContext) -> actionContext.getGame().getGameState().getCurrentPlayerId());
        else if (type.toLowerCase(Locale.ROOT).startsWith("ownerfrommemory(") && type.endsWith(")")) {
            String memory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            return (actionContext) -> {
                final PhysicalCard cardFromMemory = actionContext.getCardFromMemory(memory);
                if (cardFromMemory != null)
                    return cardFromMemory.getOwner();
                else
                    // Sensible default
                    return actionContext.getPerformingPlayer();
            };
        }
        else if (type.toLowerCase().startsWith("frommemory(") && type.endsWith(")")) {
            String memory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            return new PlayerSource() {
                @Override
                public String getPlayer(ActionContext actionContext) {
                    return actionContext.getValueFromMemory(memory);
                }

                @Override
                public boolean canPreEvaluate() {
                    return false;
                }
            };
        }
        throw new InvalidCardDefinitionException("Unable to resolve player resolver of type: " + type);
    }
}
