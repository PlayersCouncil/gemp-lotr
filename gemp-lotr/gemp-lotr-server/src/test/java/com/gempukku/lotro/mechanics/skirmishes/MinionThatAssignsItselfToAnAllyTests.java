package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * "Assignment: Spot an ally to make that ally participate in skirmishes and assign this minion to skirmish that
 * ally": the ability is offered only when it could assign the minion to somebody, and only such allies may be
 * chosen for it.
 *
 * Ketura's rule (Q-0034): an optional action with no cost whose whole effect would do nothing is not offered; a
 * spot is no cost.  And on F-0283, of Dunlending Warrior 4_18 and Dunlending Ravager 4_15 (2026-10-09), reading A:
 * withhold the ability whenever it can assign this minion to nobody (no ally who is unassigned and not barred from
 * skirmishes, or this minion already assigned), and offer only such allies for the choice.  Both cards required
 * no more than an ally to spot, and let any ally be chosen: with the only ally already assigned, or the minion
 * itself already assigned, the ability could be used for nothing as often as the Shadow player liked.
 *
 * Both are now written with the filter word AssignableToSkirmishAgainstIgnoringAllyLocation(self): an ally who
 * could be assigned to skirmish this minion once a card has him participate in skirmishes.  Where his home is
 * does not count; everything else that stops an assignment does.  It is the question the assignment itself goes
 * on to ask, so the offer and the choice cannot disagree with what then happens.
 *
 * An ally a card bars from skirmishes (Streets of Edoras 4_340) is shown in AllyBarredFromTakingPartTests, on the
 * Towers path.  These tests are at Ettenmoors, site 2 of the Fellowship path, which bars nobody.
 */
public class MinionThatAssignsItselfToAnAllyTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior1", "4_18");    // Dunlending Warrior: strength 9, vitality 1; the ability is its whole text
					put("warrior2", "4_18");
					put("ravager", "4_15");     // Dunlending Ravager: strength 5, vitality 1; strength +2 while skirmishing a [rohan] Man, and the same ability
					put("rider", "12_161");     // Black Rider: fierce; strength +1 for each companion, nothing else

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("celeborn", "1_34");    // Celeborn: Elf ally, home site 6; a fellowship ability used only on purpose
					put("calaglin", "3_10");    // Calaglin: Elf ally, home site 6; a regroup ability used only on purpose
					put("bounder", "1_286");    // Bounder: Hobbit ally, home site 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheAbilityOffersAnAllyAtHomeOrAwayAndNoCompanionAndIsNotOfferedAgainOnceTheMinionIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		var warrior1 = scn.GetShadowCard("warrior1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn, bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(warrior1));
		scn.ShadowUseCardAction(warrior1);

		// An ally away from home and an ally at his home site; a companion is no ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(celeborn, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		scn.ShadowChooseCard(celeborn);

		assertTrue(scn.IsCharAssignedAgainst(celeborn, warrior1));
		assertFalse(scn.IsCharAssigned(bounder));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// This minion is assigned already: its ability could assign it to nobody, though the Bounder is free.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior1));
	}

	@Test
	public void WithNoAllyInPlayTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var warrior1 = scn.GetShadowCard("warrior1");
		var ravager = scn.GetShadowCard("ravager");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, ravager);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior1));
		assertFalse(scn.ShadowActionAvailable(ravager));
	}

	@Test
	public void WithTheOnlyAllyAlreadyAssignedTheAbilityIsNotOfferedAndAnAssignedAllyIsNotAmongThoseChosenFrom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var calaglin = scn.GetFreepsCard("calaglin");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, warrior2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// One ally it could be assigned to: he is taken without a choice.
		assertTrue(scn.ShadowActionAvailable(warrior1));
		assertTrue(scn.ShadowActionAvailable(warrior2));
		scn.ShadowUseCardAction(warrior1);
		assertTrue(scn.IsCharAssignedAgainst(celeborn, warrior1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The only ally is assigned: the other Warrior's ability could assign it to nobody.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior1));
		assertFalse(scn.ShadowActionAvailable(warrior2));

		// A second ally, unassigned: the ability is back, and he is the only one it takes.
		scn.MoveAlliesToTable(calaglin);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(warrior1));
		assertTrue(scn.ShadowActionAvailable(warrior2));
		scn.ShadowUseCardAction(warrior2);

		assertTrue(scn.IsCharAssignedAgainst(calaglin, warrior2));
		assertFalse(scn.IsCharAssignedAgainst(celeborn, warrior2));
		assertTrue(scn.IsCharAssignedAgainst(celeborn, warrior1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheRavagersAbilityIsOfferedAndWithheldAsTheWarriorsIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var warrior1 = scn.GetShadowCard("warrior1");
		var ravager = scn.GetShadowCard("ravager");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, ravager);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(ravager));
		scn.ShadowUseCardAction(warrior1);
		assertTrue(scn.IsCharAssignedAgainst(celeborn, warrior1));

		// The only ally is assigned to the Warrior: the Ravager could be assigned to nobody.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ravager));
		assertFalse(scn.IsCharAssigned(ravager));
	}

	@Test
	public void TheRavagerIsAssignedByItsOwnTextToAnAllyAwayFromHome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var ravager = scn.GetShadowCard("ravager");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(ravager));
		scn.ShadowUseCardAction(ravager);
		assertTrue(scn.IsCharAssignedAgainst(celeborn, ravager));
		assertFalse(scn.IsCharAssigned(aragorn));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ravager));
	}

	@Test
	public void InTheFierceRoundAMinionThatIsNotFierceCouldBeAssignedToNobodyAndItsAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var warrior1 = scn.GetShadowCard("warrior1");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, rider);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The first round: offered, and left unused.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.ShadowActionAvailable(warrior1));
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Nobody fought, and the Black Rider is fierce: a second assignment phase, for fierce minions only.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior1));
		assertFalse(scn.IsCharAssigned(celeborn));
	}
}
