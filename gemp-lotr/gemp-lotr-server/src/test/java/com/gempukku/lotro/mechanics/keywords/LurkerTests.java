package com.gempukku.lotro.mechanics.keywords;

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
 * The keyword lurker.
 *
 * Comprehensive Rules 4.2, "lurker": "During the skirmish phase(s), the Free Peoples player must resolve all
 * skirmishes involving minions who do not have the lurker keyword before he may choose to resolve any skirmishes
 * involving one or more minions who do have it."
 *
 * "Skirmish phase(s)": "In an order decided by the Free Peoples player, skirmishes are resolved one at a time ...
 * Once a skirmish phase has finished, the Free Peoples player must select another defending companion (one who
 * is still assigned to a minion), and perform another skirmish phase."  Lurker narrows that choice and nothing
 * else: among the skirmishes it leaves him he still chooses, and when only lurkers' skirmishes are left he
 * chooses among those.  "One or more minions who do have it": one lurker among a companion's minions makes that
 * skirmish wait.  "Skirmish phase(s)" takes in the fierce skirmishes.
 */
public class LurkerTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, damage +1; a skirmish ability, not used here

					// Orc Skulker: lurker; strength 5, vitality 2.  "Each skirmish event you play is twilight
					// cost -1", and no event is played here.
					put("skulker", "12_95");
					// Prowling Orc: lurker; strength 6, vitality 2.  "Skirmish: Remove (2) to make an [orc]
					// minion strength +1", not used here.
					put("prowler", "11_136");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and no lurker
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; no lurker; its text is about being played

					// Saruman, Of Many Colours: damage +1, fierce and lurker; strength 8, vitality 4.  The
					// rest of his text acts only when he is played, and he is not.
					put("saruman", "12_54");
					put("rider", "12_161");     // Black Rider: fierce and no lurker; strength 8, +1 for each companion; vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ASkirmishInvolvingALurkerIsNotOfferedUntilEverySkirmishWithoutOneIsResolved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var skulker = scn.GetShadowCard("skulker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(skulker, savage, runner);
		scn.SkipToAssignments();

		assertTrue(scn.HasKeyword(skulker, Keyword.LURKER));
		assertFalse(scn.HasKeyword(savage, Keyword.LURKER));
		assertFalse(scn.HasKeyword(runner, Keyword.LURKER));

		// Assigning is not touched by the keyword: the lurker is assigned like any minion.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(skulker));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, skulker}, new PhysicalCardImpl[]{boromir, savage},
				new PhysicalCardImpl[]{gimli, runner});
		assertTrue(scn.IsCharAssignedAgainst(aragorn, skulker));

		// Three skirmishes wait.  The two with no lurker in them are the Free Peoples player's to order;
		// Aragorn's, against the lurker, is not offered.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));

		// Gimli, 6, beats the Runner, 5, and kills it.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// One skirmish without a lurker is left, and the lurker's still waits behind it.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, skulker));

		// Boromir, 7, beats the Savage, 5.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// Now the lurker's skirmish may be chosen, and is fought like any other: Aragorn, 8, beats the Skulker, 5.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, skulker));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(skulker));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishInvolvingALurkerAndAMinionWhoIsNoneWaitsAsALurkersDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var skulker = scn.GetShadowCard("skulker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(skulker, savage, runner);
		scn.SkipToAssignments();

		// Aragorn takes the Savage and Boromir the Runner; the Shadow player adds the lurker, left over, to
		// Aragorn's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{boromir, runner});
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(aragorn, skulker);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, skulker));

		// "Involving one or more minions who do have it": Aragorn's skirmish has a minion who is no lurker in
		// it and waits all the same.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		// Boromir, 7, beats the Runner, 5, and kills it.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, skulker));

		// Aragorn, 8, loses to 5 + 5 and takes a wound, and one more for the Savage's damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenEverySkirmishInvolvesALurkerTheFreePeoplesPlayerChoosesAmongThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var skulker = scn.GetShadowCard("skulker");
		var prowler = scn.GetShadowCard("prowler");

		scn.StartGame();
		scn.MoveMinionsToTable(skulker, prowler);
		scn.SkipToAssignments();

		assertTrue(scn.HasKeyword(skulker, Keyword.LURKER));
		assertTrue(scn.HasKeyword(prowler, Keyword.LURKER));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, skulker}, new PhysicalCardImpl[]{boromir, prowler});

		// No skirmish without a lurker is left to resolve first, so both are on offer, in either order.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));

		// Boromir, 7, beats the Prowling Orc, 6; then Aragorn, 8, beats the Skulker, 5.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, prowler));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(prowler));

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(skulker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheFierceSkirmishesTooTheLurkersSkirmishWaits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var saruman = scn.GetShadowCard("saruman");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, rider);
		scn.SkipToAssignments();

		assertTrue(scn.HasKeyword(saruman, Keyword.LURKER));
		assertTrue(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(rider, Keyword.LURKER));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));

		// Nobody is assigned in the first round, so the fierce round's skirmishes are the only ones fought.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, saruman}, new PhysicalCardImpl[]{boromir, rider});

		// The fierce skirmish against the lurker waits behind the one against the Rider.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		// Boromir, 7, loses to the Rider, 8 + 3 companions, and takes a wound.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, saruman));

		// 8 against 8: the Shadow wins a tie, and Aragorn takes a wound and one more for Saruman's damage +1.
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
