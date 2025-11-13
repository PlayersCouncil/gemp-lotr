package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;

import java.util.Map;

public class PlayConditions {
    public static boolean canLiberateASite(LotroGame game, String performingPlayer, PhysicalCard source, String controlledByPlayerId) {
        PhysicalCard siteToLiberate = getSiteToLiberate(game, controlledByPlayerId);
        return siteToLiberate != null && game.getModifiersQuerying().canBeLiberated(game, performingPlayer, siteToLiberate, source);
    }

    private static PhysicalCard getSiteToLiberate(LotroGame game, String controlledByPlayerId) {
        int maxUnoccupiedSite = Integer.MAX_VALUE;
        for (String playerId : game.getGameState().getPlayerOrder().getAllPlayers())
            maxUnoccupiedSite = Math.min(maxUnoccupiedSite, game.getGameState().getPlayerPosition(playerId) - 1);

        for (int i = maxUnoccupiedSite; i >= 1; i--) {
            PhysicalCard site = game.getGameState().getSite(i);
            if (controlledByPlayerId == null) {
                if (site != null && site.getCardController() != null
                        && !site.getCardController().equals(game.getGameState().getCurrentPlayerId()))
                    return site;
            } else {
                if (site != null && site.getCardController() != null && site.getCardController().equals(controlledByPlayerId))
                    return site;
            }
        }

        return null;
    }

    public static boolean isPhase(LotroGame game, Phase phase) {
        return (game.getGameState().getCurrentPhase() == phase);
    }

    public static boolean isActive(LotroGame game, Filterable... filters) {
        return isActive(game, 1, filters);
    }

    public static boolean isActive(LotroGame game, int count, Filterable... filters) {
        return Filters.countActive(game, filters) >= count;
    }

    public static boolean canSpot(LotroGame game, int count, Filterable... filters) {
        return Filters.canSpot(game, count, filters);
    }

    public static boolean canSpot(LotroGame game, int count, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        return Filters.canSpot(game, count, spotOverrides, filters);
    }

    public static boolean hasInitiative(LotroGame game, Side side) {
        return game.getModifiersQuerying().hasInitiative(game) == side;
    }

    public static boolean canAddThreat(LotroGame game, PhysicalCard card, int count) {
        return getMaxAddThreatCount(game) >= count;
    }

    public static int getMaxAddThreatCount(LotroGame game) {
        int companionCount = Filters.countActive(game, SpotOverride.INCLUDE_HINDERED, CardType.COMPANION);
        int threatLimit = game.getModifiersQuerying().getThreatLimit(game, companionCount);
        return threatLimit - game.getGameState().getThreats();
    }

    public static boolean canPlayFromDiscard(String playerId, LotroGame game, int modifier, Filterable... filters) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return false;
        return Filters.acceptsAny(game, game.getGameState().getDiscard(playerId), Filters.and(filters, Filters.playable(modifier)));
    }

    public static boolean canPlayFromDrawDeck(String playerId, LotroGame game, int modifier, Filterable... filters) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return false;
        return Filters.acceptsAny(game, game.getGameState().getDeck(playerId), Filters.and(filters, Filters.playable(modifier)));
    }

    public static boolean canPlayFromHand(String playerId, LotroGame game, int modifier, Filterable... filters) {
        return Filters.acceptsAny(game, game.getGameState().getHand(playerId), Filters.and(filters, Filters.playable(modifier)));
    }

    public static boolean canPlayFromStacked(String playerId, LotroGame game, int modifier, Filterable... filters) {
        return Filters.acceptsAny(game, game.getGameState().getStacked(playerId), Filters.and(filters, Filters.playable(modifier)));
    }

    public static boolean canDiscardFromPlay(final PhysicalCard source, LotroGame game, int count, final Filterable... filters) {
        return Filters.countActive(game, Filters.and(filters,
                (Filter) (game1, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(game, source.getOwner(), physicalCard, source))) >= count;
    }

    public static boolean checkTurnLimit(LotroGame game, PhysicalCard card, int max) {
        return game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(card).getUsedLimit() < max;
    }

    public static boolean checkPhaseLimit(LotroGame game, PhysicalCard card, Phase phase, int max) {
        return checkPhaseLimit(game, card, phase, "", max);
    }

    public static boolean checkPhaseLimit(LotroGame game, PhysicalCard card, Phase phase, String prefix, int max) {
        Phase usePhase = (phase != null) ? phase : game.getGameState().getCurrentPhase();
        return game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(card, prefix, usePhase).getUsedLimit() < max;
    }
}
