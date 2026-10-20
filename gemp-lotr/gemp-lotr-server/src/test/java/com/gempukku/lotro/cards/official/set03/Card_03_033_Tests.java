package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("check", "3_33");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf

					put("runner", "1_178");     // Goblin Runner: minion, cost 1, site 4
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("sauron", "9_48");      // Sauron, The Lord of the Rings: minion, cost 18; 1 less for each burden, threat and site controlled
					put("wreathed", "1_238");   // Wreathed in Shadow: Shadow condition, cost 0
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Weatherhills as site 2: "Each Nazgul's twilight cost is -1."
	protected VirtualTableScenario GetWeatherhillsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("check", "3_33");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard
					put("runner", "1_178");     // Goblin Runner: a minion for the table
					put("lemenya", "1_232");    // Ulaire Lemenya: Nazgul, cost 4, site 3; her text is about [wraith] conditions
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_335");      // Weatherhills
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisFirstSeriousCheckStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: His First Serious Check
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot Gandalf to reveal a card at random from an opponent's hand. You may add (X) to discard that card, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("check");

		assertEquals("His First Serious Check", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HisFirstSeriousCheckSpotsGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var avenger = scn.GetFreepsCard("avenger");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(check);
		// A [gandalf] companion, and no Gandalf.
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToShadowHand("sauron", "check", "gandalf");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(check));
		assertEquals(3, scn.GetShadowHandCount());
	}

	@Test
	public void HisFirstSeriousCheckRevealsACardAtRandom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(check, avenger);
		scn.MoveCompanionsToTable(gandalf);
		// A minion and two Free Peoples cards of the Shadow player's own: any card of his hand may be revealed.
		scn.MoveCardsToShadowHand("sauron", "check", "gandalf");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(check));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(check);
		// One card of the three is shown to the player who played the event, and nothing to the hand's owner.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add twilight to discard"));
		scn.FreepsChooseNo();

		// Declined: nothing is added beyond the event's own cost and nothing is discarded.
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, check);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, avenger);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void HisFirstSeriousCheckCanAddRevealedCardsTwilightToDiscardIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(check));
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPlayCard(check);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sauron, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.HAND, sauron);
		assertEquals(1, scn.GetTwilight()); // the event's own cost
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add twilight to discard"));
		scn.FreepsChooseYes();

		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, sauron);
		// 1 for the event + 18, the cost printed on Sauron (there is no burden, threat or controlled site).
		assertEquals(19, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsOnlyTheCardRevealedAndAddsTheCostOfARoamingMinionWithoutItsPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionsToTable(gandalf);
		// Two copies of one card, so that whichever is revealed the readings are the same.
		scn.MoveCardsToHand(runner2, runner3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard(check);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner2, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetTwilight());
		scn.FreepsChooseYes();

		// 1 for the event + 1, the Runner's cost: at site 2 a Runner (site 4) would cost 3 to play, roaming.
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsTheCardRevealedAndNotAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var shadowCheck = scn.GetShadowCard("check");
		var shadowGandalf = scn.GetShadowCard("gandalf");
		var shadowAvenger = scn.GetShadowCard("avenger");
		var runner2 = scn.GetShadowCard("runner2");
		var sauron = scn.GetShadowCard("sauron");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionsToTable(gandalf);
		// Six different cards, so that a card other than the one revealed is most often a different card.
		scn.MoveCardsToShadowHand("check", "gandalf", "avenger", "runner2", "sauron", "wreathed");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard(check);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var checkShown = scn.FreepsHasCardChoiceAvailable(shadowCheck, false);
		var gandalfShown = scn.FreepsHasCardChoiceAvailable(shadowGandalf, false);
		var avengerShown = scn.FreepsHasCardChoiceAvailable(shadowAvenger, false);
		var runnerShown = scn.FreepsHasCardChoiceAvailable(runner2, false);
		var sauronShown = scn.FreepsHasCardChoiceAvailable(sauron, false);
		var wreathedShown = scn.FreepsHasCardChoiceAvailable(wreathed, false);
		scn.FreepsDismissRevealedCards();

		scn.FreepsChooseYes();

		// The card discarded is the card that was shown, whichever it was, and no other.
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(5, scn.GetShadowHandCount());
		if (checkShown) assertInDiscard(shadowCheck); else assertInHand(shadowCheck);
		if (gandalfShown) assertInDiscard(shadowGandalf); else assertInHand(shadowGandalf);
		if (avengerShown) assertInDiscard(shadowAvenger); else assertInHand(shadowAvenger);
		if (runnerShown) assertInDiscard(runner2); else assertInHand(runner2);
		if (sauronShown) assertInDiscard(sauron); else assertInHand(sauron);
		if (wreathedShown) assertInDiscard(wreathed); else assertInHand(wreathed);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACardOfCostZeroIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, wreathed);

		scn.FreepsPlayCard(check);
		assertTrue(scn.FreepsHasCardChoiceAvailable(wreathed, false));
		scn.FreepsDismissRevealedCards();

		// X is 0, and the discard is still his to make.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add twilight to discard"));
		scn.FreepsChooseYes();

		assertInZone(Zone.DISCARD, wreathed);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void XIsTheCostPrintedOnTheCardThoughASiteMakesItCheaperToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeatherhillsScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertEquals(4, scn.GetTwilightCost(lemenya));
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		// At Weatherhills a Nazgul costs 1 less to play.
		assertEquals(3, scn.GetTwilightCost(lemenya));

		scn.FreepsPlayCard(check);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetTwilight());
		scn.FreepsChooseYes();

		// 1 for the event + 4: a card in play changes another card's cost only as that card is played.
		assertInZone(Zone.DISCARD, lemenya);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAnEmptyHandNothingIsRevealedAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetShadowHandCount());
		// An event may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(check));

		scn.MarkGameLog();
		scn.FreepsPlayCard(check);

		// Nobody is shown anything, the log says why, and the player is not asked about a discard.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, check);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
