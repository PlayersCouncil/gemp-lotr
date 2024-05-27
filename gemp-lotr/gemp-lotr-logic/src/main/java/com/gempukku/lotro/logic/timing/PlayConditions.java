package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;

import java.util.Collection;
import java.util.Collections;

public class PlayConditions {
    public static boolean canPayForShadowCard(LotroGame game, PhysicalCard self, Filterable validTargetFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty) {
        int minimumCost;
        if (validTargetFilter == null)
            minimumCost = game.getModifiersQuerying().getTwilightCostToPlay(game, self, null, twilightModifier, ignoreRoamingPenalty);
        else {
            minimumCost = 0;
            for (PhysicalCard potentialTarget : Filters.filterActive(game, validTargetFilter)) {
                minimumCost = Math.min(minimumCost, game.getModifiersQuerying().getTwilightCostToPlay(game, self, potentialTarget, twilightModifier, ignoreRoamingPenalty));
            }
        }

        return minimumCost <= game.getGameState().getTwilightPool() - withTwilightRemoved;
    }

    public static boolean isAhead(LotroGame game) {
        String currentPlayer = game.getGameState().getCurrentPlayerId();
        int currentPosition = game.getGameState().getCurrentSiteNumber();
        for (String player : game.getGameState().getPlayerOrder().getAllPlayers()) {
            if (!player.equals(currentPlayer))
                if (game.getGameState().getPlayerPosition(player) >= currentPosition)
                    return false;
        }
        return true;
    }

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


    public static boolean canDiscardFromHand(LotroGame game, String playerId, int count, Filterable... cardFilter) {
        return hasCardInHand(game, playerId, count, cardFilter);
    }

    public static boolean hasCardInHand(LotroGame game, String playerId, int count, Filterable... cardFilter) {
        return Filters.filter(game.getGameState().getHand(playerId), game, cardFilter).size() >= count;
    }

    public static boolean hasCardsStacked(LotroGame game, Filterable stackedOn, int count, Filterable... cardFilter) {
        final Collection<PhysicalCard> matchingStackedOn = Filters.filterActive(game, stackedOn);
        for (PhysicalCard stackedOnCard : matchingStackedOn) {
            if (Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, cardFilter).size() >= count)
                return true;
        }
        return false;
    }

    public static boolean hasCardInAdventureDeck(LotroGame game, String playerId, int count, Filterable... cardFilters) {
        return Filters.filter(game.getGameState().getAdventureDeck(playerId), game, cardFilters).size() >= count;
    }

    public static boolean hasCardInDiscard(LotroGame game, String playerId, int count, Filterable... cardFilters) {
        return Filters.filter(game.getGameState().getDiscard(playerId), game, cardFilters).size() >= count;
    }

    public static boolean hasCardInRemoved(LotroGame game, String playerId, int count, Filterable... cardFilters) {
        return Filters.filter(game.getGameState().getRemoved(playerId), game, cardFilters).size() >= count;
    }

    public static boolean isPhase(LotroGame game, Phase phase) {
        return (game.getGameState().getCurrentPhase() == phase);
    }

    public static boolean location(LotroGame game, Filterable... filters) {
        return Filters.and(filters).accepts(game, game.getGameState().getCurrentSite());
    }

    public static boolean checkUniqueness(LotroGame game, PhysicalCard self, boolean ignoreCheckingDeadPile) {
        LotroCardBlueprint blueprint = self.getBlueprint();
        if (!blueprint.isUnique())
            return true;

        final int activeCount = Filters.countActive(game, Filters.name(blueprint.getSanitizedTitle()));
        return activeCount == 0
                && (ignoreCheckingDeadPile || (Filters.filter(game.getGameState().getDeadPile(self.getOwner()), game, Filters.name(blueprint.getSanitizedTitle())).size() == 0));
    }

    private static int getTotalCompanions(String playerId, LotroGame game) {
        return Filters.countActive(game, CardType.COMPANION)
                + Filters.filter(game.getGameState().getDeadPile(playerId), game, CardType.COMPANION).size();
    }

    public static boolean checkRuleOfNine(LotroGame game, PhysicalCard self) {
        if (self.getZone() == Zone.DEAD)
            return (getTotalCompanions(self.getOwner(), game) <= 9);
        else
            return (getTotalCompanions(self.getOwner(), game) < 9);
    }

    public static boolean checkPlayRingBearer(LotroGame game, PhysicalCard self) {
        // If a character other than Frodo is your Ringbearer,
        // you cannot play any version of Frodo
        // with the Ring-bearer keyword during the game
        PhysicalCard ringBearer = game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId());
        boolean ringBearerIsNotFrodo = ringBearer != null && !ringBearer.getBlueprint().getTitle().equals("Frodo");
        if (ringBearerIsNotFrodo) {
            boolean isRingBearerFrodo = self.getBlueprint().getTitle().equals("Frodo") && self.getBlueprint().hasKeyword(Keyword.CAN_START_WITH_RING);
            return !isRingBearerFrodo;
        }
        return true;
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

    public static boolean canSpotThreat(LotroGame game, int count) {
        return game.getGameState().getThreats() >= count;
    }

    public static boolean canSpotBurdens(LotroGame game, int count) {
        return game.getGameState().getBurdens() >= count;
    }

    // "If you can spot X [elven] tokens on conditions..."
    public static boolean canSpotCultureTokensOnCards(LotroGame game, Token token, int count, Filterable... filters) {
        return GameUtils.getSpottableCultureTokensOfType(game, token, filters) >= count;
    }

    // "If you can spot X culture tokens on conditions..."
    // Strictly speaking this would only be needed if there was a card that could add culture tokens to cards
    // that did not match their own native culture.
    public static boolean canSpotAllCultureTokensOnCards(LotroGame game, int count, Filterable... filters) {
        return GameUtils.getAllSpottableCultureTokens(game, filters) >= count;
    }

    public static boolean hasInitiative(LotroGame game, Side side) {
        return game.getModifiersQuerying().hasInitiative(game) == side;
    }

    public static boolean canAddThreat(LotroGame game, PhysicalCard card, int count) {
        return Filters.countActive(game, CardType.COMPANION) - game.getGameState().getThreats() >= count;
    }

    public static boolean canRemoveThreat(LotroGame game, PhysicalCard card, int count) {
        return game.getGameState().getThreats() >= count && game.getModifiersQuerying().canRemoveThreat(game, card);
    }

    public static boolean canAddBurdens(LotroGame game, String performingPlayer, PhysicalCard card) {
        return game.getModifiersQuerying().canAddBurden(game, performingPlayer, card);
    }

    public static boolean canWound(final PhysicalCard source, final LotroGame game, final int times, final int count, Filterable... filters) {
        final Filter filter = Filters.and(filters, Filters.character);
        return Filters.countActive(game, filter,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().getVitality(game, physicalCard) >= times
                                && game.getModifiersQuerying().canTakeWounds(game, (source != null) ? Collections.singleton(source) : Collections.emptySet(), physicalCard, times);
                    }
                }) >= count;
    }

    public static boolean canPlayFromDiscard(String playerId, LotroGame game, Filterable... filters) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return false;
        return Filters.filter(game.getGameState().getDiscard(playerId), game, Filters.and(filters, Filters.playable(game))).size() > 0;
    }

    public static boolean canPlayFromDiscard(String playerId, LotroGame game, int modifier, Filterable... filters) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return false;
        return Filters.filter(game.getGameState().getDiscard(playerId), game, Filters.and(filters, Filters.playable(game, modifier))).size() > 0;
    }

    public static boolean canDiscardFromPlay(final PhysicalCard source, LotroGame game, int count, final Filterable... filters) {
        return Filters.countActive(game, Filters.and(filters,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().canBeDiscardedFromPlay(game, source.getOwner(), physicalCard, source);
                    }
                })) >= count;
    }

    public static boolean controlsSite(LotroGame game, String playerId) {
        return Filters.findFirstActive(game, Filters.siteControlled(playerId)) != null;
    }

    public static boolean canMove(LotroGame game) {
        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
                && game.getGameState().getMoveCount() < game.getModifiersQuerying().getMoveLimit(game, 2);
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
