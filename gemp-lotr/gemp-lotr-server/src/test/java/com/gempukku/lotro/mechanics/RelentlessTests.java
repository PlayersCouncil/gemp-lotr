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
 * The keyword relentless.
 *
 * Comprehensive Rules 5.0, "relentless": "After all normal and fierce skirmishes have resolved, surviving minions
 * with the relentless keyword must be defended against a third time, with a new relentless assignment phase and
 * skirmish phase.
 *
 * Although the relentless skirmishes follow fierce skirmishes, a minion which is relentless is not automatically
 * fierce; for all three skirmish rounds to be performed, you must have minions with both the fierce and
 * relentless keywords (not necessarily the same minion).  Thus, if you have no fierce minions but you do have a
 * relentless minion, you will only perform two rounds of skirmishes: a regular skirmish and a relentless skirmish,
 * skipping the fierce skirmish."  And: "In all other ways, relentless skirmishes behave like the other two rounds
 * of skirmish."
 *
 * The rule's two examples about game text (text that names a fierce minion or a fierce skirmish does not reach a
 * relentless one) are their cards' own: Eowyn, Dernhelm and Filibert Bolger, Wily Rascal.
 */
public class RelentlessTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Úlairë Enquëa, Magnified through Suffering: the one printed minion that is fierce and
					// relentless; strength 12, vitality 4.  At the start of his skirmish with a wounded companion
					// he may exert to heal that companion and exert a companion: declined wherever it is offered.
					put("enquea", "103_74");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; neither keyword (its text is about being played)
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					// Savage Fury: "Maneuver: Discard a minion to make your [isengard] minion fierce or relentless
					// until the regroup phase."  How a minion comes to be relentless and not fierce.
					put("fury", "103_39");
					put("savage", "1_151");     // Uruk Savage: [isengard], strength 5, vitality 3; damage +1 is its whole text
					put("rider", "12_161");     // Black Rider: fierce; strength 8, +1 for each companion; vitality 3

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability, not used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AfterTheFierceRoundOnlyRelentlessMinionsAreAssignedAndFoughtAThirdTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, runner);

		assertTrue(scn.HasKeyword(enquea, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(enquea, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.RELENTLESS));

		// The first round: both minions are there to be defended against.
		scn.SkipToAssignments();
		assertFalse(scn.InFierceRound());
		assertFalse(scn.InRelentlessRound());
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(enquea));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { frodo, runner },
				new PhysicalCardImpl[] { aragorn, enquea }
		);

		// Frodo, 3 + 1 for the Ring, loses to the Runner's 5, so the Runner lives through its skirmish unwounded.
		// The Ring's response to his wound is declined.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(runner));

		// Aragorn, 8, loses to Enquea's 12.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(enquea));

		// The fierce round: an assignment phase again, and only the fierce minion in it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		assertFalse(scn.InRelentlessRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(enquea));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertTrue(scn.FreepsCanAssign(enquea));
		assertFalse(scn.FreepsCanAssign(runner));

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn is wounded now, and Enquea's own text makes its offer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(enquea));

		// The relentless round: a third assignment phase, after the fierce skirmishes are over, and again only
		// Enquea in it.  The Runner is alive and has no part in it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.InFierceRound());
		assertTrue(scn.InRelentlessRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(enquea));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertTrue(scn.FreepsCanAssign(enquea));
		assertFalse(scn.FreepsCanAssign(runner));

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();
		scn.PassSkirmishActions();

		// Three rounds and no fourth: one wound to Aragorn in each, and both minions still there.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.HasKeyword(enquea, Keyword.RELENTLESS));
		assertFalse(scn.InRelentlessRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARelentlessMinionThatIsNotFierceFightsTwoRoundsAndNoFierceOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var fury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, brigand, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Runner is discarded to pay; the Savage is the only [isengard] minion, so nothing more is asked
		// once the keyword is chosen.
		scn.ShadowUseCardAction(fury);
		scn.ShadowChooseCard(runner);
		scn.ShadowChoose("Relentless");
		assertTrue(scn.HasKeyword(savage, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(brigand, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));

		// The first round.  Aragorn, 8, beats the Savage's 5; Boromir, 7, loses to the Brigand's 8, so the
		// Brigand lives through its skirmish.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, savage },
				new PhysicalCardImpl[] { boromir, brigand }
		);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(brigand));

		// No minion is fierce: the round that follows is the relentless one, and no fierce round came between.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.InFierceRound());
		assertTrue(scn.InRelentlessRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(brigand));

		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Two rounds for the Savage, one for the Brigand, and then the regroup phase.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceMinionAndARelentlessMinionBringAllThreeRoundsEachFightingItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var fury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		var rider = scn.GetShadowCard("rider");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, rider, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(fury);
		scn.ShadowChooseCard(runner);
		scn.ShadowChoose("Relentless");
		// The Savage is relentless and not fierce; the Rider is fierce and not relentless.
		assertTrue(scn.HasKeyword(savage, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(rider, Keyword.RELENTLESS));

		// The first round: both.  Aragorn, 8, loses to the Rider's 11 (8 + 3 companions); Boromir, 7, beats the
		// Savage's 5.
		scn.SkipToAssignments();
		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, rider },
				new PhysicalCardImpl[] { boromir, savage }
		);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(rider));

		// The fierce round: the Rider alone.  A relentless minion is not fierce for being relentless.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		assertFalse(scn.InRelentlessRound());
		scn.PassFierceAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));

		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));

		// The relentless round: the Savage alone.  The Rider, fierce and alive, has no part in it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.InFierceRound());
		assertTrue(scn.InRelentlessRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(rider));

		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// The Rider fought twice and the Savage twice, and the regroup phase follows the third round.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
