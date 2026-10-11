package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Cards stacked on a card.
 *
 * Comprehensive Rules 4.2, "stack": "Stacking a card is not playing a card.  Stacked cards are placed face up and
 * may be looked at by any player at any time.
 *
 * Stacked cards are not in play and are not active.  You cannot spot them.  They do not count for uniqueness.  A
 * stacked unique card may be in play elsewhere.  Multiple copies of the same unique card may be stacked together."
 *
 * The cards that stack are plain ones.  The Shards of Narsil stacks a [gondor] card from its owner's hand in the
 * fellowship phase at no cost, and gives one back for (1); Teeth of Mordor stacks a [sauron] Orc from play in the
 * regroup phase.  What a card says may be done with the cards stacked on it (played as if from hand, counted,
 * discarded to pay for something) is that card's own text and is not gone over here.
 *
 * What becomes of stacked cards when the card under them leaves play is LeavingPlayTests (CR 4.2 "leaving play");
 * when a controlled site is liberated or replaced, ControlOfSitesTests ("liberate", "control"); when the card
 * under them is hindered, the classes of the hinder rules.  "May be looked at by any player" has no test: the rig
 * reads what a seat is shown of a hand, and has no reading of what it is shown of a stack.
 */
public class StackingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Shards of Narsil: unique [gondor] artifact in the support area.  "Fellowship: Stack a
					// [gondor] card from hand here.  Fellowship: Add (1) to take a card stacked here into hand."
					put("shards", "3_44");
					// Aragorn, Ranger of the North: unique, cost 4, a ranger; a maneuver ability, not used
					put("aragorn1", "1_89");
					put("aragorn2", "1_89");
					put("aragorn3", "1_89");
					// Ranger's Sword: cost 1.  "Bearer must be Aragorn.  He is damage +1."
					put("sword", "1_112");
					// Pathfinder: [gondor] event, cost 0.  "Fellowship or Regroup: Spot a ranger to play the
					// fellowship's next site (replacing opponent's site if necessary)."
					put("pathfinder", "1_110");

					// Teeth of Mordor: [sauron] condition in the support area.  "Regroup: Spot a [sauron] Orc
					// and remove (3) to stack that minion here."  (Its Shadow ability plays one stacked here.)
					put("teeth", "5_110");
					// Grishnákh, Orc Captain: unique, site 6.  "The site number of each [sauron] Orc is -3."
					// His Shadow ability is not used.
					put("grishnakh", "5_100");
					// Orc Chieftain: [sauron] Orc, site 6; his text answers a weapon played on him
					put("chieftain", "1_266");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StackingACardIsNotPlayingItNothingIsPaidAndNothingItNeedsToBePlayedIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var aragorn = scn.GetFreepsCard("aragorn1");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(aragorn, sword);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		// The Sword cannot be played: there is no Aragorn to bear it.
		assertFalse(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.FreepsPlayAvailable(aragorn));

		// It can be stacked all the same, and its cost of 1 is not paid.
		scn.FreepsUseCardAction(shards, "Stack");
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(sword);
		assertInZone(Zone.STACKED, sword);
		assertEquals(1, scn.GetStackedCards(shards).size());
		assertSame(sword, scn.GetStackedCards(shards).get(0));
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());

		// Aragorn costs 4 to play and nothing to stack.  He is the only [gondor] card left in hand.
		scn.FreepsUseCardAction(shards, "Stack");
		assertInZone(Zone.STACKED, aragorn);
		assertNotInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetStackedCards(shards).size());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AStackedCardCannotBeSpottedUntilItIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var aragorn = scn.GetFreepsCard("aragorn1");
		var sword = scn.GetFreepsCard("sword");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(aragorn, sword, pathfinder);

		scn.StartGame();

		scn.FreepsUseCardAction(shards, "Stack");
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.STACKED, aragorn);

		// A ranger named Aragorn lies stacked on the Shards.  No ranger can be spotted, and there is no
		// Aragorn to bear the Sword.
		assertFalse(scn.FreepsPlayAvailable(pathfinder));
		assertFalse(scn.FreepsPlayAvailable(sword));

		// The Shards hand him back for (1), and he is played for 4.
		scn.FreepsUseCardAction(shards, "take");
		assertInZone(Zone.HAND, aragorn);
		assertEquals(0, scn.GetStackedCards(shards).size());
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(pathfinder));
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(5, scn.GetTwilight());

		// In play he is a ranger to spot and an Aragorn to bear the Sword.
		assertTrue(scn.FreepsPlayAvailable(pathfinder));
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AStackedCompanionIsNoCompanionOfTheFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var aragorn = scn.GetFreepsCard("aragorn1");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		scn.FreepsUseCardAction(shards, "Stack");
		assertInZone(Zone.STACKED, aragorn);
		assertEquals(0, scn.GetTwilight());

		// The fellowship moves to Ettenmoors, Shadow number 2.  One token more is added for each companion,
		// and Frodo is the only one: the companion on the Shards is not in play.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.STACKED, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AStackedUniqueCardDoesNotBarAnotherOfItsTitleAndCopiesOfItMayBeStackedTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var stackedFirst = scn.GetFreepsCard("aragorn1");
		var played = scn.GetFreepsCard("aragorn2");
		var stackedSecond = scn.GetFreepsCard("aragorn3");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(stackedFirst, played, stackedSecond);

		scn.StartGame();

		scn.FreepsUseCardAction(shards, "Stack");
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(stackedFirst);
		assertInZone(Zone.STACKED, stackedFirst);

		// "They do not count for uniqueness": with one Aragorn stacked, another is played.
		assertTrue(scn.FreepsPlayAvailable(played));
		scn.FreepsPlayCard(played);
		assertInZone(Zone.FREE_CHARACTERS, played);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.STACKED, stackedFirst);

		// The Aragorn in play does bar a third from being played ...
		assertFalse(scn.FreepsPlayAvailable(stackedSecond));
		// ... and not from being stacked beside the first (he is the only [gondor] card left in hand): "a
		// stacked unique card may be in play elsewhere", and "multiple copies of the same unique card may be
		// stacked together."
		scn.FreepsUseCardAction(shards, "Stack");
		assertInZone(Zone.STACKED, stackedFirst, stackedSecond);
		assertEquals(2, scn.GetStackedCards(shards).size());
		assertInZone(Zone.FREE_CHARACTERS, played);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AStackedMinionsTextIsNotInForceAndItIsNotDiscardedWithTheMinionsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var teeth = scn.GetShadowCard("teeth");
		var grishnakh = scn.GetShadowCard("grishnakh");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(teeth);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);

		// "The site number of each [sauron] Orc is -3"
		assertEquals(3, scn.GetMinionSiteNumber(chieftain));

		scn.FreepsPass();
		scn.ShadowUseCardAction(teeth);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(grishnakh);
		assertInZone(Zone.STACKED, grishnakh);
		assertNotInZone(Zone.SHADOW_CHARACTERS, grishnakh);
		assertSame(grishnakh, scn.GetStackedCards(teeth).get(0));
		assertEquals(0, scn.GetTwilight());

		// Grishnákh is not active on the stack: the other Orc has its printed site number again.
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The turn ends.  "The Shadow players discard all minions in play": the Orc in play goes, and the
		// one on the stack is not in play and stays where it is.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseToStay();
		assertInZone(Zone.DISCARD, chieftain);
		assertInZone(Zone.STACKED, grishnakh);
		assertEquals(1, scn.GetStackedCards(teeth).size());
		assertInZone(Zone.SUPPORT, teeth);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
