package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Assigning defenders: who assigns, in what order, and to whom.
 *
 * Comprehensive Rules 4.2, "assignment phase": "When all players consecutively pass, proceed to assign defenders.
 * During your assignment phase, you may assign companions to defend against attacking minions.  A player cannot
 * assign more than one companion to the same minion. ...  All assignments of characters are on a one-to-one basis,
 * with the following two exceptions: Companions who have the defender +X keyword.  When you have informed the
 * Shadow players that you are done making assignments, they may assign any leftover unassigned minions to any
 * companions (even if those companions are already assigned)."  Its example: "Frodo and Aragorn face three
 * Uruk-hai.  The Free Peoples player assigns Aragorn to defend against one, and Frodo to another.  This leaves one
 * unassigned Uruk-hai, so the Shadow player assigns it".  And: "When the assignment phase is complete, each
 * companion being attacked will lead to a separate skirmish phase."
 *
 * The keyword's own exception is in DefenderTests; who may not be assigned at all is in UnhastyTests and
 * AllyBarredFromTakingPartTests; the second assignment of a turn is in FierceTests.
 */
public class AssignmentPhaseTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; no text but what it takes to play him
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
	public void TheFreePeoplesPlayerAssignsFirstAndTheShadowPlayerThenAssignsWhatIsLeftToAnyCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// While assignment actions may still be taken, and once they are passed, nobody is assigned yet.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(troop1));

		scn.PassAssignmentActions();

		// The Free Peoples player assigns first, and has every minion and every companion to assign, the
		// Ring-bearer too.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(guard));
		assertFalse(scn.IsCharAssigned(troop1));
		assertFalse(scn.IsCharAssigned(troop2));
		assertFalse(scn.IsCharAssigned(troop3));
		assertEquals(3, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop1));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop2));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop3));
		assertEquals(3, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(guard));

		// He assigns one companion to one minion and is done: that pair is assigned and nothing else.
		scn.FreepsAssignToMinions(aragorn, troop1);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(guard));
		assertFalse(scn.IsCharAssigned(troop2));
		assertFalse(scn.IsCharAssigned(troop3));

		// The Shadow player is then asked.  He has only the minions left over, and every companion: the one who
		// is already assigned as well as the two who are not.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.ShadowGetShadowAssignmentTargetCount());
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(troop1));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop2));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop3));
		assertEquals(3, scn.ShadowGetFreepsAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(guard));
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, troop2));
		assertTrue(scn.ShadowCanAssignAgainst(guard, troop3));

		// One leftover minion goes to the companion who already has a minion, the other to one who has none.
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{aragorn, troop2}, new PhysicalCardImpl[]{guard, troop3});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop2));
		assertTrue(scn.IsCharAssignedAgainst(guard, troop3));
		assertFalse(scn.IsCharAssignedAgainst(guard, troop2));
		assertFalse(scn.IsCharAssigned(frodo));

		// Each companion being attacked is a skirmish, in the order the Free Peoples player chooses: there are two
		// to choose from, and Aragorn's is against both of his minions at once.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop1));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertFalse(scn.IsCharSkirmishing(troop3));
		assertFalse(scn.IsCharSkirmishing(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
