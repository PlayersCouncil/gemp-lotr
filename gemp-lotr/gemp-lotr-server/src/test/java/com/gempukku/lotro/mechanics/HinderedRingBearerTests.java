package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * What the rules themselves forbid about the Ring-bearer holds while he is hindered.
 *
 * Comprehensive Rules 4.2, "Ring-bearer": "The Ring-bearer cannot be discarded or returned to your hand".
 * Comprehensive Rules 5.0, "hinder": the game text of a hindered card does not apply, nor do its title, culture,
 * type and race, but "the side/alignment of a hindered card continues to apply", so an effect that names "a Free
 * Peoples card" and nothing more reaches a hindered companion (Ketura on Q-0098).  It does not reach the
 * Ring-bearer: his protection is the rules' and not a card's, and being hindered takes away only what cards give.
 *
 * The game used to ask what protects a card the same way it asks what applies to it, and nothing applies to a
 * hindered card: so a hindered Ring-bearer was discarded, and the game lost, by any discard that reaches hindered
 * cards (F-0174; met at Doom Causeway 103_127).
 *
 * The rule on hindering has two sentences of its own about him.  On his burdens, as the exception to "Tokens on
 * hindered cards cannot be spotted, counted, reinforced, removed, or added to": "If the Ring-bearer is hindered,
 * burdens may be spotted and counted, but they still cannot be added or removed."  And on his resistance, which
 * like every number on a hindered card does not apply: "While the Ring-bearer is hindered, they cannot be
 * corrupted until restored."  (Burdens are "on the Ring-bearer" whatever his state: Ketura on Q-0186.)
 */
public class HinderedRingBearerTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test cards (testCards.hjson), Shadow conditions in the support area:
					// "Maneuver: Remove (1) to discard a Free Peoples card."
					put("undertow", "0_8006");
					// "Maneuver: Remove (1) to return a Free Peoples card to its owner's hand."
					put("ebbtide", "0_8007");
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability that is never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability that is never used here
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase

					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit; a skirmish ability only
					put("pippin", "1_306");     // Pippin, Friend to Frodo: an unbound Hobbit; his text is about tales
					// Uruk Follower: "Unbound Hobbits may not be discarded."  Its response answers a killed Hobbit.
					put("follower", "4_186");

					// Smeagol, Slinker: cost 0.  "Ring-bound.  To play, add a burden."  A skirmish ability
					// that is never used here.
					put("smeagol", "5_29");
					// Gollum, Stinker: a minion of strength 5.  "Gollum is strength +1 for each burden."  His
					// other text answers a skirmish he wins.
					put("stinker", "5_25");
					// Ulaire Enquea, Lieutenant of Morgul: "Fierce.  Maneuver: Spot 6 companions (or 5 burdens)
					// and exert Ulaire Enquea to wound a companion (except the Ring-bearer)."
					put("enquea", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Ring-bearer whom her own text keeps from corruption: Galadriel, Bearer of Wisdom, resistance 3.  "While
	// Galadriel bears an artifact or The One Ring, she is resistance +1 for each [elven] companion you can spot."
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Lorien Elf: an [elven] companion, strength 4, vitality 2; "To play, spot an Elf."
					put("elf", "1_53");
					// Was He Dear to Thee?: [sauron] Maneuver event, cost 3.  "Exert a unique [sauron] or
					// [ringwraith] minion and hinder the Ring-bearer to make each minion strength +2 until
					// the regroup phase."
					put("dear", "103_104");
					// Ulaire Lemenya, Lieutenant of Morgul: a unique [ringwraith] minion, strength 9,
					// vitality 2, not fierce.  Her text is about companions who bear a [ringwraith] condition.
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ADiscardThatReachesHinderedCardsCannotTakeAHinderedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var undertow = scn.GetShadowCard("undertow");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(undertow);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(frodo, sam);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(sam));
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(undertow));

		scn.ShadowUseCardAction(undertow);
		assertEquals(twilight - 1, scn.GetTwilight());
		// "A Free Peoples card": Sam, hindered, is offered beside Aragorn.  The hindered Ring-bearer is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(sam);
		assertInZone(Zone.DISCARD, sam);
		assertInZone(Zone.FREE_CHARACTERS, frodo, aragorn);

		// Aragorn is now the only card that can be discarded, and is taken without a question
		scn.FreepsPass();
		scn.ShadowUseCardAction(undertow);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// The hindered Ring-bearer is the only Free Peoples card left: the cost is paid and nothing is discarded
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(undertow));
		scn.ShadowUseCardAction(undertow);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertTrue(scn.IsHindered(frodo));
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AReturnToHandThatReachesHinderedCardsCannotTakeAHinderedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ebbtide = scn.GetShadowCard("ebbtide");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(ebbtide);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(frodo, sam);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(sam));
		int twilight = scn.GetTwilight();
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.ShadowActionAvailable(ebbtide));

		scn.ShadowUseCardAction(ebbtide);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(sam);
		assertInZone(Zone.HAND, sam);
		assertFalse(scn.IsHindered(sam));
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertInZone(Zone.FREE_CHARACTERS, frodo, aragorn);

		scn.FreepsPass();
		scn.ShadowUseCardAction(ebbtide);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.HAND, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// The hindered Ring-bearer is the only Free Peoples card left in play: the cost is paid and he stays
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(ebbtide));
		scn.ShadowUseCardAction(ebbtide);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertTrue(scn.IsHindered(frodo));
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhatACardForbidsAboutOtherCardsDoesNotReachAHinderedOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var undertow = scn.GetShadowCard("undertow");
		var follower = scn.GetShadowCard("follower");

		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveCardsToSupportArea(undertow);

		scn.StartGame();
		scn.MoveMinionsToTable(follower);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(merry);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(pippin));

		scn.ShadowUseCardAction(undertow);
		// Uruk Follower keeps Pippin, an unbound Hobbit, from the discard.  Merry, hindered, is no Hobbit to any
		// card (CR 5.0) and has nothing from it; no rule protects him as the rules protect the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pippin, frodo));
		scn.ShadowChooseCard(merry);
		assertInZone(Zone.DISCARD, merry);
		assertInZone(Zone.FREE_CHARACTERS, frodo, pippin, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ABurdenCannotBePaidAsACostWhileTheRingBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.IsHindered(frodo));
		assertFalse(scn.FreepsPlayAvailable(smeagol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RestoreCard(frodo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoBurdenIsRemovedWhileTheRingBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(2);

		// "Fellowship: Exert Sam to remove a burden."
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(sam));

		// The ability has a cost, so it is still offered: Sam exerts, and the burden stays where it is
		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RestoreCard(frodo);
		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(sam);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BurdensAreStillSpottedAndCountedWhileTheRingBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stinker = scn.GetShadowCard("stinker");
		var enquea = scn.GetShadowCard("enquea");
		// Three companions: Enquea's "6 companions" is far off, and his ability rests on the burdens
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(stinker, enquea);
		scn.AddBurdens(4);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// One burden short of the five Enquea spots
		assertEquals(4, scn.GetBurdens());
		// 5 + 4
		assertEquals(9, scn.GetStrength(stinker));
		assertFalse(scn.ShadowActionAvailable(enquea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		// 5 + 5
		assertEquals(10, scn.GetStrength(stinker));
		assertTrue(scn.ShadowActionAvailable(enquea));

		// Both readings are the same as before the hindering, and that is the check: were his burdens tokens
		// like any other on a hindered card, Gollum would be back at 5 and Enquea would have nothing to spot
		scn.HinderCard(frodo);
		scn.ShadowRefreshDecision();
		assertTrue(scn.IsHindered(frodo));
		assertEquals(5, scn.GetBurdens());
		assertEquals(10, scn.GetStrength(stinker));
		assertTrue(scn.ShadowActionAvailable(enquea));

		assertEquals(0, scn.GetWoundsOn(enquea));
		scn.ShadowUseCardAction(enquea);
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHinderedRingBearerIsNotCorruptedThoughHisResistanceIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var elf = scn.GetFreepsCard("elf");
		var lemenya = scn.GetShadowCard("lemenya");
		var dear = scn.GetShadowCard("dear");
		scn.MoveCompanionsToTable(elf);
		// A card for the Free Peoples player's hand, so that his reconcile stops to ask for a discard
		scn.MoveCardsToHand(scn.GetFreepsCard("dear"), dear);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddBurdens(4);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 3 printed + 1 for herself + 1 for the Lorien Elf, less the 4 burdens: only her own text stands
		// between her and corruption
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetResistance(galadriel));
		assertFalse(scn.GameIsFinished());
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(dear));

		// Lemenya is the one minion that can pay and Galadriel the one Ring-bearer: nothing is asked
		scn.ShadowPlayCard(dear);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.IsHindered(galadriel));
		// 9 + 2
		assertEquals(11, scn.GetStrength(lemenya));

		// Her text does not apply now, nor does her resistance; the burdens are all still there
		assertEquals(4, scn.GetBurdens());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Nobody is assigned.  The Elf lives, so she is restored at her player's reconcile to what she had
		// before, and the game goes on.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.IsHindered(galadriel));
		assertFalse(scn.GameIsFinished());
		scn.PassRegroupActions();
		scn.FreepsChooseToStay();

		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(galadriel));
		assertInZone(Zone.FREE_CHARACTERS, elf);
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetResistance(galadriel));
		assertFalse(scn.GameIsFinished());
	}

	// "Until restored."  The Lorien Elf is killed while she is hindered, so the text that comes back with her is
	// worth one less: 3 printed + 1 for herself, less the 4 burdens.
	@Test
	public void AHinderedRingBearerWhoseResistanceIsSpentIsCorruptedWhenRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var elf = scn.GetFreepsCard("elf");
		var lemenya = scn.GetShadowCard("lemenya");
		var dear = scn.GetShadowCard("dear");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(dear);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddBurdens(4);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetResistance(galadriel));
		scn.ShadowPlayCard(dear);
		assertTrue(scn.IsHindered(galadriel));

		// The Elf, strength 4, against Lemenya at 9 + 2: overwhelmed and killed
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, lemenya);
		scn.FreepsResolveSkirmish(elf);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, elf);
		assertTrue(scn.IsHindered(galadriel));
		assertEquals(4, scn.GetBurdens());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// No unhindered companion: no second move is offered, and the Free Peoples player reconciles.  That
		// restores her.
		scn.MarkGameLog();
		scn.PassRegroupActions();

		assertFalse(scn.IsHindered(galadriel));
		assertEquals(0, scn.GetResistance(galadriel));
		assertTrue(scn.GameIsFinished());
		// The game's own words for it; a game that ended for any other reason would not say them
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
	}
}
