package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
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
 * phase."  There is one fierce round, not one for as long as a fierce minion lives.  "Skirmish Phase(s) (Fierce)":
 * "Then each defending companion fights in a separate skirmish phase, just as during the regular skirmish phases, in
 * an order decided by the Free Peoples player."  Only a SURVIVING fierce minion is defended against again.
 *
 * Fierce is read as a keyword (CR 4.2 "fierce"); the stats test of a card printed fierce reads it, and this class
 * is what lets it stop there (Ketura on Q-0279, 2026-10-12).  An ally at home in the fierce round is in
 * AllyHomeTests; a hindered companion in it, HinderedCharacterCannotSkirmishTests.
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
					// Uruk Pursuer: fierce, strength 5, vitality 3.  "Tracker.  Fierce.  Unbound Hobbits may not be
					// discarded."  Its response answers an unbound Hobbit killed, and none is.
					put("pursuer", "4_190");
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

	@Test
	public void AFierceMinionKilledInTheFirstRoundIsNotDefendedAgainstInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var rider = scn.GetShadowCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(rider, pursuer);
		scn.AddWoundsToChar(pursuer, 2);
		scn.SkipToAssignments();

		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));

		// Boromir, 7, beats the Pursuer, 5, and its last vitality goes; the Rider beats Aragorn and lives.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider}, new PhysicalCardImpl[]{boromir, pursuer});
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, pursuer);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(rider));
	}

	@Test
	public void WithNoFierceMinionLeftAfterTheFirstRoundThereIsNoFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.AddWoundsToChar(pursuer, 2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, pursuer);
		assertFalse(scn.InFierceRound());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFierceRoundWaitsForEveryNormalSkirmishAndItsSkirmishesAreFoughtOneByOneInTheOrderTheFreePeoplesPlayerChooses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var rider = scn.GetShadowCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(rider, pursuer);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider}, new PhysicalCardImpl[]{boromir, pursuer});

		// The first fierce minion's skirmish is over, and the other normal skirmish is still to be fought first.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// Both survived; both are defended against again, each by the other companion.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pursuer}, new PhysicalCardImpl[]{boromir, rider});

		// The Free Peoples player picks which of the two is fought first, and the other waits for it.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, rider));
		scn.PassFierceSkirmishActions();

		// Boromir, 7, loses to the Rider's 11 (8 + 3 companions), which is under double: one wound.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		scn.PassFierceSkirmishActions();

		// Aragorn, 8, beats the Pursuer, 5: its second wound.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
