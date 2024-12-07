package com.gempukku.lotro.logic;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.AttachPermanentAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.actions.PlayPermanentAction;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class PlayUtils {
    private static Zone getPlayToZone(PhysicalCard card) {
        final CardType cardType = card.getBlueprint().getCardType();
        return switch (cardType) {
            case COMPANION -> Zone.FREE_CHARACTERS;
            case MINION -> Zone.SHADOW_CHARACTERS;
            default -> Zone.SUPPORT;
        };
    }

    public static Map<Phase, Timeword> PhaseKeywordMap = ImmutableMap.copyOf(new HashMap<>() {{
        put(Phase.FELLOWSHIP, Timeword.FELLOWSHIP);
        put(Phase.SHADOW, Timeword.SHADOW);
        put(Phase.MANEUVER, Timeword.MANEUVER);
        put(Phase.ARCHERY, Timeword.ARCHERY);
        put(Phase.ASSIGNMENT, Timeword.ASSIGNMENT);
        put(Phase.SKIRMISH, Timeword.SKIRMISH);
        put(Phase.REGROUP, Timeword.REGROUP);
    }});

    private static Filter getFullAttachValidTargetFilter(final LotroGame game, final PhysicalCard card, int twilightModifier, int withTwilightRemoved) {
        return Filters.and(RuleUtils.getFullValidTargetFilter(card.getOwner(), game, card),
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().canHavePlayedOn(game, card, physicalCard);
                    }
                },
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        if (card.getBlueprint().getSide() == Side.SHADOW) {
                            final int twilightCostOnTarget = game.getModifiersQuerying().getTwilightCostToPlay(game, card, physicalCard, twilightModifier, false);
                            int potentialDiscount = game.getModifiersQuerying().getPotentialDiscount(game, card);
                            return twilightCostOnTarget - potentialDiscount <= game.getGameState().getTwilightPool() - withTwilightRemoved;
                        } else {
                            return true;
                        }
                    }
                });
    }


    public static CostToEffectAction getPlayCardAction(LotroGame game, PhysicalCard card, int twilightModifier, Filterable additionalAttachmentFilter, boolean ignoreRoamingPenalty) {
        final LotroCardBlueprint blueprint = card.getBlueprint();

        if (blueprint.getCardType() != CardType.EVENT) {
            final Filterable validTargetFilter = blueprint.getValidTargetFilter(card.getOwner(), game, card);
            if (validTargetFilter == null) {
                PlayPermanentAction action = new PlayPermanentAction(card, getPlayToZone(card), twilightModifier, ignoreRoamingPenalty);

                game.getModifiersQuerying().appendExtraCosts(game, action, card);
                game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);

                return action;
            } else {
                final AttachPermanentAction action = new AttachPermanentAction(game, card, Filters.and(getFullAttachValidTargetFilter(game, card, twilightModifier, 0), additionalAttachmentFilter), twilightModifier);

                game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);
                game.getModifiersQuerying().appendExtraCosts(game, action, card);

                return action;
            }
        } else {
            final PlayEventAction action = blueprint.getPlayEventCardAction(card.getOwner(), game, card);

            game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);
            game.getModifiersQuerying().appendExtraCosts(game, action, card);

            return action;
        }
    }

    public static boolean checkPlayRequirements(LotroGame game, PhysicalCard card, Filterable additionalAttachmentFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, boolean ignoreResponseEvents) {
        final LotroCardBlueprint blueprint = card.getBlueprint();

        // Check if card's own play requirements are met
        if (!blueprint.checkPlayRequirements(game, card))
            return false;

        twilightModifier -= game.getModifiersQuerying().getPotentialDiscount(game, card);

        // Check if there exists a legal target (if needed)
        final Filterable validTargetFilter = blueprint.getValidTargetFilter(card.getOwner(), game, card);
        Filterable finalTargetFilter = null;
        if (validTargetFilter != null) {
            finalTargetFilter = Filters.and(getFullAttachValidTargetFilter(game, card, twilightModifier, withTwilightRemoved), additionalAttachmentFilter);
            if (Filters.countActive(game, finalTargetFilter) == 0)
                return false;
        }

        // Check if can play extra costs
        if (!game.getModifiersQuerying().canPayExtraCostsToPlay(game, card))
            return false;

        if (!game.getModifiersQuerying().canPlayCard(game, card.getOwner(), card))
            return false;

        // Check uniqueness
        if (!blueprint.skipUniquenessCheck() && !checkUniqueness(game, card, ignoreCheckingDeadPile))
            return false;

        if (blueprint.getCardType() == CardType.COMPANION
                && !(checkRuleOfNine(game, card) && checkPlayRingBearer(game, card)))
            return false;

        final Timeword timeword = PhaseKeywordMap.get(game.getGameState().getCurrentPhase());

        if (blueprint.getCardType() == CardType.EVENT) {
            //Some events are dual-timeword, such as Eye of Barad-dur, and so should not be automatically disqualified
            if (ignoreResponseEvents && blueprint.hasTimeword(Timeword.RESPONSE) && !blueprint.hasTimeword(timeword)) {
                return false;
            }

            if (!blueprint.hasTimeword(Timeword.RESPONSE) && timeword != null && !card.getBlueprint().hasTimeword(timeword))
                return false;
        }

        return (blueprint.getSide() != Side.SHADOW || canPayForShadowCard(game, card, finalTargetFilter, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty));
    }

    private static boolean canPayForShadowCard(LotroGame game, PhysicalCard self, Filterable validTargetFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty) {
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

    private static boolean checkUniqueness(LotroGame game, PhysicalCard self, boolean ignoreCheckingDeadPile) {
        LotroCardBlueprint blueprint = self.getBlueprint();
        if (!blueprint.isUnique())
            return true;

        final int activeCount = Filters.countActive(game, Filters.name(blueprint.getSanitizedTitle()));
        return activeCount == 0
                && (ignoreCheckingDeadPile || (Filters.filter(game, game.getGameState().getDeadPile(self.getOwner()), Filters.name(blueprint.getSanitizedTitle())).size() == 0));
    }

    private static boolean checkRuleOfNine(LotroGame game, PhysicalCard self) {
        if (self.getZone() == Zone.DEAD)
            return (getTotalCompanions(self.getOwner(), game) <= 9);
        else
            return (getTotalCompanions(self.getOwner(), game) < 9);
    }

    private static int getTotalCompanions(String playerId, LotroGame game) {
        return Filters.countActive(game, CardType.COMPANION)
                + Filters.filter(game, game.getGameState().getDeadPile(playerId), CardType.COMPANION).size();
    }

    private static boolean checkPlayRingBearer(LotroGame game, PhysicalCard self) {
        // If a character other than Frodo is your Ringbearer,
        // you cannot play any version of Frodo
        // with the Ring-bearer keyword during the game
        PhysicalCard ringBearer = game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId());
        boolean ringBearerIsNotFrodo = ringBearer != null && !ringBearer.getBlueprint().getTitle().equals("Frodo");
        if (ringBearerIsNotFrodo) {
            boolean isRingBearerFrodo = self.getBlueprint().getTitle().equals("Frodo") && self.getBlueprint().canStartWithRing();
            return !isRingBearerFrodo;
        }
        return true;
    }
}
