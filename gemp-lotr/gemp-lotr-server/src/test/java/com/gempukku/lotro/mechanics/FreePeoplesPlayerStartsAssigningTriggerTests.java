package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The trigger fpStartedAssigning: the moment the Free Peoples player starts to assign minions, when both players
 * have passed the assignment actions and before he is asked for his assignments.  It comes once in the assignment
 * phase and once more in the fierce assignment phase, and not when the Shadow player assigns what is left over.
 *
 * The trigger was made in 2021 for Ulaire Nertea, Dark Horseman 69_38, whose burden was asked for at that
 * moment.  Since F-0308 her burden is added at the assignment itself and no card uses the trigger, so the old
 * test of it (at/TriggersAtTest.fpStartedAssigning, which read her question) is this one, with a card of the
 * tests' own: Test Drum 0_8022, "Each time the Free Peoples player starts to assign minions, add (1)."  The
 * engine's assignmentCost (Morgul Spawn 7_200) answers the same moment.
 */
public class FreePeoplesPlayerStartsAssigningTriggerTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Drum (the tests' own card): "Each time the Free Peoples player starts to assign minions, add (1)."
					put("drum", "0_8022");
					// Black Rider: fierce; strength 8, +1 for each companion; vitality 3
					put("rider", "12_161");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ComesWhenTheAssignmentActionsArePassedAndAgainInTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var drum = scn.GetShadowCard("drum");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToSupportArea(drum);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not at the start of the phase, while assignment actions may still be taken
		int pool = scn.GetTwilight();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(pool + 1, scn.GetTwilight());

		// Aragorn, strength 8, loses to the Rider's 10 and lives
		scn.FreepsAssignToMinions(aragorn, rider);
		assertEquals(pool + 1, scn.GetTwilight());
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		assertEquals(pool + 1, scn.GetTwilight());

		scn.PassFierceAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(pool + 2, scn.GetTwilight());
	}

	@Test
	public void DoesNotComeWhenTheShadowPlayerAssignsWhatIsLeftOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var drum = scn.GetShadowCard("drum");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToSupportArea(drum);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int pool = scn.GetTwilight();
		scn.PassCurrentPhaseActions();
		assertEquals(pool + 1, scn.GetTwilight());

		// The Free Peoples player assigns nobody, and the Shadow player is asked
		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(pool + 1, scn.GetTwilight());

		scn.ShadowAssignToMinions(aragorn, rider);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, rider));
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
