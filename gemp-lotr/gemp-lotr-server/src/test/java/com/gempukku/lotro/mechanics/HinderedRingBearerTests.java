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
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
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
}
