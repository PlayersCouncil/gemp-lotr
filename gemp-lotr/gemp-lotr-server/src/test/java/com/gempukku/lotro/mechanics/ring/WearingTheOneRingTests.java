package com.gempukku.lotro.mechanics.ring;

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
 * Wearing The One Ring.
 *
 * Every card that has the Ring-bearer put the Ring on says "until the regroup phase", each version of The One
 * Ring included, and the game takes it off for all of them as the regroup phase starts.  Comprehensive Rules 4.2,
 * "The One Ring", has the one case that leaves open: "If your Ring-bearer puts on The One Ring (any version)
 * during the regroup phase, he then takes it off at the end of that regroup phase."
 *
 * "Ring-bearer": "While wearing The One Ring, your Ring-bearer can perform all normal actions such as moving and
 * skirmishing.  He may defend against attacking minions as usual."
 *
 * "Overwhelmed": "When the Ring-bearer is overwhelmed, he is killed, regardless of whether he wears the Ring."
 *
 * "Losing the game": "When a new character becomes your Ring-bearer, he is not wearing The One Ring, even if the
 * old Ring-bearer was wearing it when he was killed."
 *
 * What a Ring does while it is worn is that Ring's own text, and is in its own file.  What else a new
 * Ring-bearer takes over is in RingBearerSuccessionTests.
 */
public class WearingTheOneRingTests
{
	protected VirtualTableScenario GetScenario(String ring) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					// Sam, Son of Hamfast: strength 3, vitality 4.  "Response: If Frodo dies, make Sam the
					// Ring-bearer (resistance 5)."  His fellowship ability is not used here.
					put("sam", "1_311");
					put("sword", "1_299");      // Hobbit Sword: strength +2 and no text

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					// Ted Sandyman, Chief's Men's Ally: strength 3, vitality 4.  His maneuver ability needs a site
					// the Shadow player controls and his regroup ability a [men] Man.
					put("ted", "18_75");
					// Gollum, Dark as Darkness: strength 5, vitality 4.  "Regroup: Exert Gollum twice to wound the
					// Ring-bearer."  His other abilities are not used here.
					put("gollum", "9_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				ring
		);
	}

	// The One Ring, The Great Ring: "While wearing The One Ring, each time bearer is about to take a wound, add
	// a burden instead.  Maneuver: Wear The One Ring until the regroup phase.  Skirmish: Add a burden to make
	// the Ring-bearer strength +3."  It is put on at no cost, and gives Frodo, Son of Drogo, no strength: he is 3.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.GreatRing);
	}

	// The One Ring, The Ring of Rings: "Response: If the Ring-bearer is about to take a wound, he or she wears
	// The One Ring until the regroup phase.  While the Ring-bearer is wearing The One Ring, each time he or she
	// is about to take a wound, add a burden instead."  A wound in any phase, the regroup phase included.
	protected VirtualTableScenario GetRingOfRingsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario("11_1");
	}

	@Test
	public void ARingPutOnInTheManeuverPhaseIsWornThroughTheSkirmishesAndIsOffAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		// Worn in the archery phase, in the assignment phase ...
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.RBWearingOneRing());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.RBWearingOneRing());

		// ... and in a skirmish, his own or another's.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.RBWearingOneRing());

		// Aragorn, 8, kills the Runner, 5 and vitality 1, and the skirmishes are over.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void ARingPutOnInTheRegroupPhaseIsWornUntilThatPhaseEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingOfRingsScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());

		// Gollum wounds the Ring-bearer in the regroup phase, and the Ring is put on there.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// It is not taken off at once, though the regroup phase has long started: it is still on when the
		// Free Peoples player is asked whether to move again.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.RBWearingOneRing());

		// He stays.  The regroup phase and the turn end, and the Ring is off.
		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void AFellowshipWhoseRingBearerWearsTheRingMayMoveAgainAndTheRingIsOffOnceThatRegroupPhaseIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingOfRingsScenario();

		var ring = scn.GetRing();
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		// "All normal actions such as moving": wearing the Ring, the fellowship is asked whether to move
		// again like any other, and moves.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// The regroup phase it was put on in is over: the Ring is off in the Shadow phase at the next site.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void WhileWearingTheRingTheRingBearerIsAssignedAndFightsHisSkirmishAsUsual() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		// He is among those the Free Peoples player may assign, and is assigned.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		scn.FreepsAssignToMinions(frodo, ted);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, ted));
		// 3 + 2 for the sword; the Ring gives him nothing
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(ted));

		// The skirmish is resolved by strength like any other: 5 against 3, Ted loses and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRingBearerOverwhelmedWhileWearingTheRingIsKilledAndTheGameIsLost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(ring);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.GameIsFinished());

		// 3 against 13.  The Ring he wears turns wounds into burdens, and an overwhelmed character takes no
		// wound: he is killed, with no burden added.
		scn.MarkGameLog();
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is dead"));
		assertTrue(scn.FreepsHasLost());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ANewRingBearerIsNotWearingTheRingThoughTheOldOneWasWhenHeWasKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(ring);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, troop}, new PhysicalCardImpl[]{sam, runner});

		// Frodo, wearing the Ring, is overwhelmed and killed, and Sam takes the Ring up.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.DEAD, frodo);
		assertSame(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);

		// The skirmishes are not over, so no regroup phase has taken the Ring off: the new Ring-bearer is
		// simply not wearing it.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.GameIsFinished());

		// So the wound he takes for losing, 3 against the Runner's 5, is a wound: worn, this Ring would
		// have made a burden of it.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
