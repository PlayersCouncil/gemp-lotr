package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The keyword fierce.
 *
 * Comprehensive Rules 4.2, "fierce": "During a turn, after all normal skirmishes are resolved, surviving minions
 * with the keyword fierce must be defended against a second time.  Players perform another assignment phase and
 * then complete a separate skirmish phase for each fierce skirmish. ...  The Free Peoples player then assigns
 * defenders just as during the regular assignment phase, and then Shadow players assign any fierce minions that
 * remain unassigned."  Its example: "Aragorn is assigned to defend against a fierce Uruk-hai. ...  During the
 * following fierce skirmish phase, the Free Peoples player may once more assign a companion to defend against the
 * Uruk-hai.  This companion may be Aragorn or may be a different companion."
 *
 * "Skirmish phase(s)": "When all skirmishes (both normal and fierce) have been resolved, proceed to the regroup
 * phase."  There is one fierce round, not one for as long as a fierce minion lives.
 */
public class FierceTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
					// Black Rider: fierce; strength 8, +1 for each companion; vitality 3
					put("rider", "12_161");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and no fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AfterTheFirstRoundOnlyTheFierceMinionsAreAssignedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var rider = scn.GetShadowCard("rider");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(rider, savage);

		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.SkipToAssignments();
		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));

		// The first round.  Aragorn, 8, loses to the Rider's 11 (8 + 3 companions) and takes a wound; Boromir, 7,
		// beats the Savage's 5, which takes one.  Both minions live.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider}, new PhysicalCardImpl[]{boromir, savage});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(savage));

		// Another assignment phase, with its own assignment actions.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(rider));
		assertFalse(scn.IsCharAssigned(aragorn));

		scn.PassFierceAssignmentActions();

		// Only the fierce minion is there to be defended against; every companion may defend, the one who has
		// just fought it too.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertEquals(3, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(boromir));

		scn.FreepsAssignToMinions(aragorn, rider);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, rider));
		assertFalse(scn.IsCharAssigned(savage));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		// Aragorn loses again.  The Rider is alive and still fierce, and there is no third round.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceMinionThatNobodyDefendedAgainstIsStillToBeAssignedInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var rider = scn.GetShadowCard("rider");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(rider, savage);
		scn.SkipToAssignments();

		// Neither player assigns anybody, so no skirmish is fought.
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(2, scn.ShadowGetShadowAssignmentTargetCount());
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertEquals(3, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(boromir));

		// Left unassigned by the Free Peoples player again, the fierce minion alone is the Shadow player's to assign.
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(rider));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(savage));
		scn.ShadowDeclineAssignments();

		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
