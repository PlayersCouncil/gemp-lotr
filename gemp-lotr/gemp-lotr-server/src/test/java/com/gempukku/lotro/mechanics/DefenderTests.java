package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The keyword defender +X.
 *
 * Comprehensive Rules 4.2, "defender +X": "During the assignment phase, if your assigned companion has the keyword
 * defender +1, you may assign that character to one additional unassigned minion."  Its example: "Frodo and
 * Aragorn face two Uruk-hai.  The Free Peoples player could assign Aragorn to one and Frodo to the other.  However,
 * this version of Aragorn has defender +1, so he may be assigned to defend against both minions".
 *
 * "Assignment phase": "All assignments of characters are on a one-to-one basis, with the following two exceptions:
 * Companions who have the defender +X keyword", and the Shadow player's assigning of leftover minions, which is in
 * AssignmentPhaseTests.
 *
 * The Aragorn of the rules' example is Aragorn, Ranger of the North 1_89, who makes himself defender +1 with his
 * own maneuver ability; that is how the keyword comes onto the table here.
 */
public class DefenderTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("aragorn", "1_89");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion with no defender; a skirmish ability, not used here
					// Unexpected Visitor (bearer must be a [gondor] companion) and Reckless Pride (a [dwarven] one).
					// Each: 'Each minion gains this ability: "Assignment: Assign this minion to bearer of <this card>."'
					// Their regroup abilities are not used here.
					put("visitor", "7_126");
					put("pride", "7_13");
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text at all
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACompanionWithDefenderPlusOneIsAssignedTwoMinionsByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));

		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(3, scn.FreepsGetShadowAssignmentTargetCount());
		assertFalse(scn.IsCharAssigned(aragorn));

		// Two minions on the one companion, in the one answer.
		scn.FreepsAssignToMinions(aragorn, troop1, troop2);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop2));
		assertFalse(scn.IsCharAssigned(troop3));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(frodo));

		// waits: G-0041 the two refusals beside this: the same answer with Gimli (no defender) in Aragorn's place,
		// and all three minions on Aragorn (defender +1 is one more, not any number).  The game refuses each when
		// the assignment is answered, and the rig reads one character against one minion only.

		// The third minion is left over, and is the Shadow player's to assign.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop3));
		scn.ShadowAssignToMinions(gimli, troop3);

		// Aragorn's two minions are one skirmish.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop1));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertFalse(scn.IsCharSkirmishing(troop3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionWhoAlreadyHasAMinionIsOfferedForAnotherOnlyIfHeHasDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var visitor = scn.GetFreepsCard("visitor");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, visitor);
		scn.AttachCardsTo(gimli, pride);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// With assignment actions the Shadow player puts one minion on each of the two companions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();
		scn.ShadowUseCardAction(troop1, "Unexpected Visitor");
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		scn.FreepsPass();
		scn.ShadowUseCardAction(troop2, "Reckless Pride");
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop2));
		scn.FreepsPass();
		scn.ShadowPass();

		// Each has one minion.  The Free Peoples player's assigning is one to one, so Gimli is not his to assign
		// again; Aragorn, with defender +1, is, to one additional unassigned minion.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop3));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gimli));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));

		scn.FreepsAssignToMinions(aragorn, troop3);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop3));
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop2));
		assertFalse(scn.IsCharAssignedAgainst(gimli, troop3));
		assertFalse(scn.IsCharAssigned(frodo));
		// No minion is left over, so the Shadow player is asked nothing.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
