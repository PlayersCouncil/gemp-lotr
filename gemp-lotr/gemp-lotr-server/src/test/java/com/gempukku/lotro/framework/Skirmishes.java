package com.gempukku.lotro.framework;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.Skirmish;

import java.util.List;

public interface Skirmishes extends TestBase, Decisions, GameProcedures {

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

	default boolean IsCharSkirmishing(PhysicalCardImpl card) {
		var skirmish = gameState().getSkirmish();
		if(skirmish == null)
			return false;

		return skirmish.getFellowshipCharacter() == card ||
				skirmish.getShadowCharacters().stream().anyMatch(x -> x == card);
	}

	default Boolean CanBeAssignedViaAction(PhysicalCardImpl card)
	{
		return CanBeAssignedViaAction(card, Side.SHADOW) || CanBeAssignedViaAction(card, Side.FREE_PEOPLE);
	}

	default Boolean FreepsCanAssign(PhysicalCardImpl card)
	{
		return CanBeAssignedNormally(card, Side.FREE_PEOPLE);
	}

	default Boolean ShadowCanAssign(PhysicalCardImpl card)
	{
		return CanBeAssignedViaAction(card, Side.FREE_PEOPLE);
	}

	default Boolean CanBeAssignedViaAction(PhysicalCardImpl card, Side side)
	{
		return Filters.assignableToSkirmish(side, false, true, false).accepts(game(), card);
	}

	default Boolean CanBeAssignedNormally(PhysicalCardImpl card, Side side)
	{
		return Filters.assignableToSkirmish(side, side == Side.SHADOW, side == Side.SHADOW, false).accepts(game(), card);
	}

	default Skirmish GetActiveSkirmish() { return gameState().getSkirmish(); }

	default int FreepsGetFreepsAssignmentTargetCount() { return FreepsGetFreepsAssignmentTargets().size(); }
	default int FreepsGetShadowAssignmentTargetCount() { return FreepsGetShadowAssignmentTargets().size(); }
	default int ShadowGetFreepsAssignmentTargetCount() { return ShadowGetFreepsAssignmentTargets().size(); }
	default int ShadowGetShadowAssignmentTargetCount() { return ShadowGetShadowAssignmentTargets().size(); }



	default void FreepsDeclineAssignments() { FreepsPassCurrentPhaseAction(); }
	default void ShadowDeclineAssignments() { ShadowPassCurrentPhaseAction(); }

	default void FreepsAssignAndResolve(PhysicalCardImpl comp, PhysicalCardImpl...minions) {
		AssignToMinions(P1, comp, minions);

		if(ShadowDecisionAvailable("Assign minions to companions or allies at home")) {
			ShadowDeclineAssignments();
		}

		FreepsResolveSkirmish(comp);
	}

	default void FreepsAssignToMinions(PhysicalCardImpl comp, PhysicalCardImpl...minions) { AssignToMinions(P1, comp, minions); }
	default void ShadowAssignToMinions(PhysicalCardImpl comp, PhysicalCardImpl...minions) { AssignToMinions(P2, comp, minions); }
	default void AssignToMinions(String player, PhysicalCardImpl comp, PhysicalCardImpl...minions) {
		String result = comp.getCardId() + "";

		for (PhysicalCardImpl minion : minions) {
			result += " " + minion.getCardId();
		}

		PlayerDecided(player, result);
	}

	default void FreepsAssignToMinions(PhysicalCardImpl[]...groups) { AssignToMinions(P1, groups); }
	default void ShadowAssignToMinions(PhysicalCardImpl[]...groups) { AssignToMinions(P2, groups); }
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
