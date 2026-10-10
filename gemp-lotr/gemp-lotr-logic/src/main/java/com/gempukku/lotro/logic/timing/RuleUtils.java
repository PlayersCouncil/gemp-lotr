package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.CantBeOverwhelmedModifier;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleUtils {
    public static int calculateArcheryTotal(LotroGame game, Side side) {
        if (side == Side.FREE_PEOPLE)
            return calculateFellowshipArcheryTotal(game);
        else
            return calculateShadowArcheryTotal(game);
    }

    public static int calculateFellowshipArcheryTotal(LotroGame game) {
        int normalArcheryTotal = Filters.countActive(game,
                Filters.or(
                        CardType.COMPANION,
                        Filters.and(
                                CardType.ALLY,
                                Filters.or(
                                        Filters.and(
                                                Filters.allyAtHome,
                                                new Filter() {
                                                    @Override
                                                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                                        return !game.getModifiersQuerying().isAllyPreventedFromParticipatingInArcheryFire(game, physicalCard);
                                                    }
                                                }),
                                        new Filter() {
                                            @Override
                                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                                return game.getModifiersQuerying().isAllyAllowedToParticipateInArcheryFire(game, physicalCard);
                                            }
                                        })
                        )
                ),
                Keyword.ARCHER,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().addsToArcheryTotal(game, physicalCard);
                    }
                });

        return game.getModifiersQuerying().getArcheryTotal(game, Side.FREE_PEOPLE, normalArcheryTotal);
    }

    public static int calculateShadowArcheryTotal(LotroGame game) {
        int normalArcheryTotal = Filters.countActive(game,
                CardType.MINION,
                Keyword.ARCHER,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().addsToArcheryTotal(game, physicalCard);
                    }
                });

        return game.getModifiersQuerying().getArcheryTotal(game, Side.SHADOW, normalArcheryTotal);
    }

    public static int calculateMoveLimit(LotroGame game) {
        return game.getModifiersQuerying().getMoveLimit(game, 2);
    }

    /**
     * Whether the fellowship of the player whose turn it is has a companion who is not hindered.  Only that
     * player's Free Peoples cards are active, so another player's companions are not counted.
     */
    public static boolean fellowshipHasUnhinderedCompanion(LotroGame game) {
        return Filters.hasActive(game, CardType.COMPANION);
    }

    /**
     * Whether the Free Peoples player may choose to move the fellowship again this turn: no card forbids the move
     * (CANT_MOVE), the move limit has not been reached, and the fellowship has a companion who is not hindered.
     * Comprehensive Rules 5.0, "hinder", 17: "During the Regroup phase, if the fellowship has no unhindered
     * companions, the Free Peoples player cannot choose to move."  A card that would make that player choose to
     * move (HAS_TO_MOVE_IF_POSSIBLE) is read after this, and cannot make a player do what the player may not.
     */
    public static boolean canChooseToMoveAgain(LotroGame game) {
        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
                && game.getGameState().getMoveCount() < calculateMoveLimit(game)
                && fellowshipHasUnhinderedCompanion(game);
    }

    /**
     * Whether a player is barred from taking this card into hand from a draw deck or discard pile
     * (CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK: "You may not ... take into hand cards from your discard pile or
     * draw deck").  The bar is on the player doing the taking, and it reaches that player's own piles.  A card that
     * another player's effect returns to its owner's hand is not taken by its owner (A New Light 17_15: "search a
     * Shadow player's discard pile and choose a minion, then return that minion to its owner's hand"), and a card
     * the barred player takes out of another player's pile is not from "your" pile.
     * @param takingPlayer the player carrying the effect out: the one the text has take the card, whoever's card
     *                     the text is on
     */
    public static boolean isBarredFromTakingIntoHand(LotroGame game, String takingPlayer, PhysicalCard card) {
        return takingPlayer != null && takingPlayer.equals(card.getOwner())
                && game.getModifiersQuerying().hasFlagActive(game,
                        ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, takingPlayer);
    }

    /**
     * Whether a player is barred from drawing.  A card drawn is a card taken into hand from the draw deck, so the
     * same bar stops the player's draws.  The draws of a reconcile are the exception such a text names ("except
     * when reconciling"): DrawOneCardEffect does not ask this of them.  The opening hand and the mulligan are
     * dealt in set-up, by no effect, and are not asked either.
     */
    public static boolean isBarredFromDrawing(LotroGame game, String drawingPlayer) {
        return game.getModifiersQuerying().hasFlagActive(game,
                ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, drawingPlayer);
    }

    /**
     * How many characters of the other side a character is assigned to skirmish, by the assignments still waiting
     * for their skirmish: the minions a companion or ally has, and 1 for a minion that is assigned.
     */
    public static int countAssignedAgainst(LotroGame game, PhysicalCard character) {
        for (Assignment assignment : game.getGameState().getAssignments()) {
            if (assignment.getFellowshipCharacter() == character)
                return assignment.getShadowCharacters().size();
            if (assignment.getShadowCharacters().contains(character))
                return 1;
        }
        return 0;
    }

    /**
     * Whether a character has room for this many more of the other side where assignments are one to one and
     * defender is counted, which is the Free Peoples player's own assigning.  Comprehensive Rules 4.2, "defender
     * +X": "if your assigned companion has the keyword defender +1, you may assign that character to one
     * additional unassigned minion."  The minions the character already has are counted, whoever assigned them.
     * The one reading of that count: the answer to the Free Peoples player's assignment decision
     * (FreePeoplePlayerAssignsMinionsGameProcess) and Filters.assignableToSkirmish, where it is not told to
     * ignore defender, both ask it.  An assignment a card makes does not: it needs both characters unassigned.
     * @param more how many more characters of the other side are to be assigned to the character
     */
    public static boolean hasRoomForAssignments(LotroGame game, PhysicalCard character, int more) {
        return countAssignedAgainst(game, character) + more
                <= 1 + game.getModifiersQuerying().getKeywordCount(game, character, Keyword.DEFENDER);
    }

    /**
     * The site the current player's fellowship has to survive in order to win the game.  Normally the last site of the
     * adventure path, site 9; MUST_SURVIVE_SITE_10, scoped to that player, pushes it back to site 10.
     */
    public static int getFellowshipWinSiteNumber(LotroGame game) {
        return getFellowshipWinSiteNumber(game, game.getGameState().getCurrentPlayerId());
    }

    public static int getFellowshipWinSiteNumber(LotroGame game, String playerId) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.MUST_SURVIVE_SITE_10, playerId))
            return 10;
        return 9;
    }

    public static int getFellowshipSkirmishStrength(LotroGame game) {
        final Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish == null)
            return 0;

        PhysicalCard fpChar = skirmish.getFellowshipCharacter();
        if (fpChar == null)
            return 0;

        final Evaluator fpStrengthOverrideEvaluator = game.getModifiersQuerying().getFPStrengthOverrideEvaluator(game, fpChar);
        if (fpStrengthOverrideEvaluator != null)
            return fpStrengthOverrideEvaluator.evaluateExpression(game, fpChar);

        final Evaluator overrideEvaluator = skirmish.getFpStrengthOverrideEvaluator();
        if (overrideEvaluator != null)
            return overrideEvaluator.evaluateExpression(game, fpChar);

        return game.getModifiersQuerying().getStrength(game, fpChar);
    }

    public static int getShadowSkirmishStrength(LotroGame game) {
        final Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish == null)
            return 0;

        int total = 0;
        for (PhysicalCard minion : skirmish.getShadowCharacters()) {
            total += getShadowSkirmishStrength(game, minion);
        }

        return total;
    }

    /**
     * What one minion adds to the Shadow side's total in the current skirmish.
     */
    public static int getShadowSkirmishStrength(LotroGame game, PhysicalCard minion) {
        final Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish == null)
            return 0;

        final Evaluator modifierOverrideEvaluator = game.getModifiersQuerying().getShadowStrengthOverrideEvaluator(game, minion);
        if (modifierOverrideEvaluator != null)
            return modifierOverrideEvaluator.evaluateExpression(game, minion);

        final Evaluator overrideEvaluator = skirmish.getShadowStrengthOverrideEvaluator();
        if (overrideEvaluator != null)
            return overrideEvaluator.evaluateExpression(game, minion);

        return game.getModifiersQuerying().getStrength(game, minion);
    }

    /**
     * The minions who would be overwhelmed if the current skirmish were resolved as it stands.  Overwhelm is decided
     * minion by minion (Ketura on Q-0044).  Nobody is overwhelmed unless the Shadow side loses to at least double its
     * total strength.  When it does, every minion with no protection is overwhelmed, and a minion who cannot be
     * overwhelmed "unless its strength is tripled" is overwhelmed only if the winning strength is at least that
     * multiple of its own strength; otherwise it has merely lost the skirmish.  With one minion on the side its
     * strength is the side's total, and this is the plain rule.
     */
    public static Set<PhysicalCard> getMinionsFacingOverwhelm(LotroGame game) {
        final Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish == null || skirmish.getFellowshipCharacter() == null || skirmish.getShadowCharacters().isEmpty())
            return Collections.emptySet();

        int fpStrength = getFellowshipSkirmishStrength(game);
        int shadowStrength = getShadowSkirmishStrength(game);
        if (fpStrength <= shadowStrength || fpStrength < 2L * shadowStrength)
            return Collections.emptySet();

        Set<PhysicalCard> overwhelmed = new HashSet<>();
        for (PhysicalCard minion : skirmish.getShadowCharacters()) {
            int multiplier = game.getModifiersQuerying().getOverwhelmMultiplier(game, minion);
            if (multiplier <= 2
                    || (multiplier < CantBeOverwhelmedModifier.ImmuneToOverwhelmThreshold
                    && fpStrength >= (long) multiplier * getShadowSkirmishStrength(game, minion)))
                overwhelmed.add(minion);
        }
        return overwhelmed;
    }

    public static int getFellowshipSkirmishDamageBonus(LotroGame game) {
        if (game.getGameState().getSkirmish() == null)
            return 0;

        PhysicalCard fpChar = game.getGameState().getSkirmish().getFellowshipCharacter();
        if (fpChar == null)
            return 0;

        return game.getModifiersQuerying().getKeywordCount(game, fpChar, Keyword.DAMAGE);
    }

    public static int getShadowSkirmishDamageBonus(LotroGame game) {
        if (game.getGameState().getSkirmish() == null)
            return 0;

        int totalBonus = 0;

        for (PhysicalCard physicalCard : game.getGameState().getSkirmish().getShadowCharacters())
            totalBonus += game.getModifiersQuerying().getKeywordCount(game, physicalCard, Keyword.DAMAGE);

        return totalBonus;
    }

    /**
     * The cards that a card's effect may transfer the given card to, "another eligible bearer" (Comprehensive
     * Rules 4.2, "transfer"): a bearer that the card's own text and the rules on what a character may bear allow
     * (getFullValidTargetFilter), other than the card that bears it now, and none that a "cannot bear" keeps it
     * from.  Whether the card may be transferred at all (ModifiersQuerying.canBeTransferred) is not asked here.
     * The Transfer effect with checkTarget and Filters.transferableTo both read this; the Free Peoples player's
     * own transfer in the fellowship phase has rules of its own about allies (TransferItemRule).
     */
    public static Filter getTransferTargetFilter(final LotroGame game, final PhysicalCard card) {
        return Filters.and(
                getFullValidTargetFilter(card.getOwner(), game, card),
                (Filter) (g, bearer) -> bearer != card.getAttachedTo()
                        && g.getModifiersQuerying().canHaveTransferredOn(g, card, bearer));
    }

    public static Filter getFullValidTargetFilter(String playerId, final LotroGame game, final PhysicalCard self) {
        final LotroCardBlueprint blueprint = self.getBlueprint();
        final Filterable validTargetFilter = blueprint.getValidTargetFilter(playerId, game, self);
        // A card with no bearer definition (e.g. a support-area condition that only ever attaches itself through its
        // own conditional transfer, like Lost in the Woods or Black Breath) has no "eligible bearer" for other effects
        // to transfer it to. Previously the null filter crashed the game (#1025).
        if (validTargetFilter == null)
            return Filters.none;
        return Filters.and(validTargetFilter,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        final CardType thisType = blueprint.getCardType();
                        if(game.getGameState().isHindered(physicalCard))
                            return false;

                        if (thisType == CardType.POSSESSION || thisType == CardType.ARTIFACT) {
                            var types = game.getModifiersQuerying().getCardTypes(game, physicalCard);

                            for(var type : types) {
                                if(type == CardType.COMPANION || type == CardType.ALLY || type == CardType.MINION)
                                    return true;
                            }
                            return false;
                        }
                        return true;
                    }
                },
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard attachedTo) {
                        Set<PossessionClass> possessionClasses = blueprint.getPossessionClasses();
                        if (possessionClasses != null) {
                            for (PossessionClass itemClass : possessionClasses) {
                                List<PhysicalCard> attachedCards = game.getGameState().getAttachedCards(attachedTo);

                                int allowedItemsOfClass = game.getModifiersQuerying().bearableItemsOfClass(game, attachedTo, itemClass);

                                var matchingClassItems = Filters.filter(game, attachedCards, Filters.item, itemClass);
                                if (matchingClassItems.size() > allowedItemsOfClass)
                                    return false;

                                boolean extraPossessionClass = self.getBlueprint().isExtraPossessionClass(game, self, attachedTo);
                                if (!extraPossessionClass && matchingClassItems.size() == allowedItemsOfClass) {
                                    boolean anyExtraClasses = false;
                                    for(var item : matchingClassItems) {
                                        if(item.getBlueprint().isExtraPossessionClass(game, item, attachedTo)) {
                                            anyExtraClasses = true;
                                        }
                                    }
                                    if(!anyExtraClasses)
                                        return false;
                                }
                            }
                        }
                        return true;
                    }
                });
    }

    public static boolean isAllyAtHome(PhysicalCard ally, int siteNumber, SitesBlock siteBlock) {
        for(var home : ally.getBlueprint().getAllyHomes()) {
            if(home.block() == siteBlock && home.siteNum() == siteNumber)
                return true;
        }

        return false;
    }

    public static boolean isAllyInRegion(PhysicalCard ally, int regionNumber, SitesBlock siteBlock) {
        for(var home : ally.getBlueprint().getAllyHomes()) {
            if(home.block() == siteBlock && GameUtils.getRegion(home.siteNum()) == regionNumber)
                return true;
        }

        return false;
    }

}
