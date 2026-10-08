package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Skirmishes extends TableBase, Decisions, GameProcedures {

	default void SkipToShadowAssignments() {
		SkipToAssignments();
		FreepsDeclineAssignments();
	}

	default void SkipPastAllAssignments() {
		SkipToAssignments();
		if(FreepsDecisionAvailable("Assign minions to companions or allies at home")) {
			FreepsDeclineAssignments();
		}
		if(ShadowDecisionAvailable("Assign minions to companions or allies at home")) {
			ShadowDeclineAssignments();
		}
		if(FreepsDecisionAvailable("Assignment action")) {
			PassFierceAssignmentActions();
			FreepsDeclineAssignments();
			ShadowDeclineAssignments();
		}
	}

	default boolean IsCharAssigned(PhysicalCardImpl card) {
		List<Assignment> assigns = gameState().getAssignments();
		return assigns.stream().anyMatch(x -> x.getFellowshipCharacter() == card || x.getShadowCharacters().contains(card));
	}

	default boolean IsCharAssignedAgainst(PhysicalCardImpl skirmisher, PhysicalCardImpl opponent) {
		List<Assignment> assigns = gameState().getAssignments();
		return assigns.stream().anyMatch(x -> {
			if(x.getFellowshipCharacter() == skirmisher) {
				return x.getShadowCharacters().contains(opponent);
			}

			if(x.getShadowCharacters().contains(skirmisher)) {
				return x.getFellowshipCharacter() == opponent;
			}

			return false;
		});

	}

	default boolean IsCharSkirmishing(PhysicalCardImpl card) {
		var skirmish = gameState().getSkirmish();
		if(skirmish == null)
			return false;

		return skirmish.getFellowshipCharacter() == card ||
				skirmish.getShadowCharacters().stream().anyMatch(x -> x == card);
	}

	default boolean IsCharSkirmishingAgainst(PhysicalCardImpl skirmisher, PhysicalCardImpl opponent) {
		var skirmish = gameState().getSkirmish();
		if(skirmish == null)
			return false;

		if(skirmish.getFellowshipCharacter() == skirmisher) {
			return skirmish.getShadowCharacters().contains(opponent);
		}

		if(skirmish.getShadowCharacters().contains(skirmisher)) {
			return skirmish.getFellowshipCharacter() == opponent;
		}

		return false;
	}

	default Boolean CanBeAssignedViaAction(PhysicalCardImpl card)
	{
		return CanBeAssignedViaAction(card, Side.SHADOW) || CanBeAssignedViaAction(card, Side.FREE_PEOPLE);
	}

	/**
	 * Asks the game's own rule for who may be assigned, not the decision in front of the player.
	 * @param card A companion, ally or minion.
	 * @return True if the Free Peoples player could assign that character in the ordinary assignment of the
	 * assignment phase.
	 */
	default Boolean FreepsCanAssign(PhysicalCardImpl card)
	{
		return CanBeAssignedNormally(card, Side.FREE_PEOPLE);
	}

	/**
	 * Asks the game's own rule for who may be assigned, not the decision in front of the player.
	 * @param card A companion, ally or minion.
	 * @return True if the Shadow player could assign that character in the ordinary assignment of the assignment
	 * phase, which is of whatever the Free Peoples player left unassigned.
	 */
	default Boolean ShadowCanAssign(PhysicalCardImpl card)
	{
		return CanBeAssignedNormally(card, Side.SHADOW);
	}

	default Boolean CanBeAssignedViaAction(PhysicalCardImpl card, Side side)
	{
		return Filters.assignableToSkirmish(side, false, true, false).accepts(game(), card);
	}

	default Boolean CanBeAssignedNormally(PhysicalCardImpl card, Side side)
	{
		return Filters.assignableToSkirmish(side, side == Side.SHADOW, side == Side.SHADOW, false).accepts(game(), card);
	}

	/**
	 * Asks the game's own rule whether one character may be assigned to one minion, which is what the game checks
	 * when an assignment is answered.  A restriction on a pair ("may not assign a character to skirmish this
	 * minion", "cannot be assigned to skirmish companions of strength less than 6") leaves both cards in the
	 * assignment decision's lists, so neither FreepsCanAssign nor FreepsHasAssignmentTargetAvailable shows it.
	 * @param character A companion or ally.
	 * @param minion A minion.
	 * @return True if the Free Peoples player could assign that character to that minion in the ordinary
	 * assignment of the assignment phase, as the board stands.
	 */
	default Boolean FreepsCanAssignAgainst(PhysicalCardImpl character, PhysicalCardImpl minion)
	{
		return CanBeAssignedAgainst(character, minion, Side.FREE_PEOPLE);
	}

	/**
	 * Asks the game's own rule whether one character may be assigned to one minion, which is what the game checks
	 * when an assignment is answered.
	 * @param character A companion or ally.
	 * @param minion A minion.
	 * @return True if the Shadow player could assign that minion to that character in the ordinary assignment of
	 * the assignment phase, as the board stands.  The Shadow player may add a minion to a character who already
	 * has one; the minion itself must still be unassigned.
	 */
	default Boolean ShadowCanAssignAgainst(PhysicalCardImpl character, PhysicalCardImpl minion)
	{
		return CanBeAssignedAgainst(character, minion, Side.SHADOW);
	}

	/**
	 * Whether the player of the given side could assign the one character to the one minion in the ordinary
	 * assignment of the assignment phase: each of the two may be assigned by that side at all (as
	 * CanBeAssignedNormally reads it for the character), and no restriction on the pair forbids it.  Fails unless
	 * the first card is a companion or ally and the second a minion.
	 * @param character A companion or ally.
	 * @param minion A minion.
	 * @param side The side doing the assigning.
	 */
	default Boolean CanBeAssignedAgainst(PhysicalCardImpl character, PhysicalCardImpl minion, Side side)
	{
		Verify(Filters.or(CardType.COMPANION, CardType.ALLY).accepts(game(), character) && Filters.and(CardType.MINION).accepts(game(), minion),
				"CanBeAssignedAgainst takes a companion or ally and then a minion; it was given "
						+ GameUtils.getFullName(character) + " and " + GameUtils.getFullName(minion) + ".");
		if(!CanBeAssignedNormally(character, side))
			return false;
		// A minion is listed for either player only while it is unassigned.
		if(!Filters.assignableToSkirmish(side, false, false, false).accepts(game(), minion))
			return false;
		Map<PhysicalCard, Set<PhysicalCard>> pair = Collections.singletonMap(character, Collections.singleton(minion));
		return game().getModifiersQuerying().isValidAssignments(game(), side, pair);
	}

	default Skirmish GetActiveSkirmish() { return gameState().getSkirmish(); }

	default int FreepsGetFreepsAssignmentTargetCount() { return FreepsGetFreepsAssignmentTargets().size(); }
	default int FreepsGetShadowAssignmentTargetCount() { return FreepsGetShadowAssignmentTargets().size(); }
	default int ShadowGetFreepsAssignmentTargetCount() { return ShadowGetFreepsAssignmentTargets().size(); }
	default int ShadowGetShadowAssignmentTargetCount() { return ShadowGetShadowAssignmentTargets().size(); }



	default void FreepsDeclineAssignments() { FreepsPass(); }
	default void ShadowDeclineAssignments() { ShadowPass(); }

	default void FreepsAssignAndResolve(PhysicalCardImpl comp, PhysicalCardImpl...minions) {
		AssignToMinions(P1(), comp, minions);

		if(ShadowDecisionAvailable("Assign minions to companions or allies at home")) {
			ShadowDeclineAssignments();
		}

		FreepsResolveSkirmish(comp);
	}

	default void FreepsAssignToMinions(PhysicalCardImpl comp, PhysicalCardImpl...minions) { AssignToMinions(P1(), comp, minions); }
	default void ShadowAssignToMinions(PhysicalCardImpl comp, PhysicalCardImpl...minions) { AssignToMinions(P2(), comp, minions); }
	default void AssignToMinions(String player, PhysicalCardImpl comp, PhysicalCardImpl...minions) {
		String result = comp.getCardId() + "";

		for (PhysicalCardImpl minion : minions) {
			result += " " + minion.getCardId();
		}

		PlayerDecided(player, result);
	}

	default void FreepsAssignToMinions(PhysicalCardImpl[]...groups) { AssignToMinions(P1(), groups); }
	default void ShadowAssignToMinions(PhysicalCardImpl[]...groups) { AssignToMinions(P2(), groups); }
	default void AssignToMinions(String player, PhysicalCardImpl[]...groups) {
		String result = "";

		for (PhysicalCardImpl[] group : groups) {
			result += group[0].getCardId();
			for(int i = 1; i < group.length; i++)
			{
				result += " " + group[i].getCardId();
			}
			result += ",";
		}

		PlayerDecided(player, result);
	}
}
