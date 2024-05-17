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
import com.gempukku.lotro.logic.timing.PlayConditions;
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

    public static Map<Phase, Keyword> PhaseKeywordMap = ImmutableMap.copyOf(new HashMap<>() {{
        put(Phase.FELLOWSHIP, Keyword.FELLOWSHIP);
        put(Phase.SHADOW, Keyword.SHADOW);
        put(Phase.MANEUVER, Keyword.MANEUVER);
        put(Phase.ARCHERY, Keyword.ARCHERY);
        put(Phase.ASSIGNMENT, Keyword.ASSIGNMENT);
        put(Phase.SKIRMISH, Keyword.SKIRMISH);
        put(Phase.REGROUP, Keyword.REGROUP);
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

    public static boolean checkPlayRequirements(LotroGame game, PhysicalCard card, Filterable additionalAttachmentFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return checkPlayRequirements(game, card, additionalAttachmentFilter, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, false);
    }

    public static boolean checkPlayRequirements(LotroGame game, PhysicalCard card, Filterable additionalAttachmentFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, boolean ignoreResponseEvents) {
        final LotroCardBlueprint blueprint = card.getBlueprint();

        // Check if card's own play requirements are met
        if (!card.getBlueprint().checkPlayRequirements(game, card))
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
        if (!blueprint.skipUniquenessCheck() && !PlayConditions.checkUniqueness(game, card, ignoreCheckingDeadPile))
            return false;

        if (blueprint.getCardType() == CardType.COMPANION
            && !(PlayConditions.checkRuleOfNine(game, card) && PlayConditions.checkPlayRingBearer(game, card)))
            return false;

        if(blueprint.getCardType() == CardType.EVENT)
        {
            if(game.getModifiersQuerying().hasKeyword(game, card, Keyword.RESPONSE)) {
                if (ignoreResponseEvents)
                    return false;
            }
            else {
                final Keyword phaseKeyword = PhaseKeywordMap.get(game.getGameState().getCurrentPhase());
                if (phaseKeyword != null && !game.getModifiersQuerying().hasKeyword(game, card, phaseKeyword))
                    return false;
            }
        }

        return (blueprint.getSide() != Side.SHADOW || PlayConditions.canPayForShadowCard(game, card, finalTargetFilter, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty));
    }
}
