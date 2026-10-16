package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alone", "3_109");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in)
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion, cost 2; his abilities are used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability only
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit (to play, add a burden: cheated in); an
					// assignment ability only
					put("smeagol", "103_116");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a card for the Free Peoples player's own hand

					put("wreathed", "1_238");   // Wreathed in Shadow: Shadow condition, twilight cost 0
					put("runner", "1_178");     // Goblin Runner: minion, twilight cost 1
					put("wallcrawler", "1_184");// Goblin Wallcrawler: minion, twilight cost 2
					put("inquisitor", "1_268"); // Orc Inquisitor: minion, twilight cost 3
					put("lieutenant", "1_280"); // Tower Lieutenant: minion, twilight cost 4
					put("troop", "1_177");      // Goblin Patrol Troop: minion, twilight cost 6
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path, whose site 1 is Edoras Hall: "The twilight cost of each [rohan] possession is -1."
	protected VirtualTableScenario GetEdorasHallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alone", "3_109");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion
					// Sturdy Shield: a [rohan] possession of twilight cost 2, in the Shadow player's hand.  Its text
					// is about a bearer it never has here.
					put("shield", "15_141");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MeanttoBeAloneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Meant to Be Alone
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot a Hobbit companion (except the Ring-bearer) to reveal a card at random from an opponent's hand. Remove (X), where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alone");

		assertEquals("Meant to Be Alone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAHobbitCompanionWhoIsNotTheRingBearerCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetFreepsCard("alone");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(alone, sam);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		// The Ring-bearer (a Hobbit companion), a Hobbit ally and a [shire] companion who is no Hobbit, with a card in
		// the Shadow hand to reveal: none of them is the Hobbit to spot.
		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.SUPPORT, bounder);
		assertInZone(Zone.HAND, alone);
		assertInZone(Zone.HAND, runner);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(alone));

		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(alone));
	}

	@Test
	public void SpottingAGondorHobbitItRevealsACardAtRandomToItsPlayerAndRemovesItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetFreepsCard("alone");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(alone, gimli);
		scn.MoveCardsToHand(wallcrawler);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, wallcrawler);
		assertInZone(Zone.HAND, gimli);
		assertTrue(scn.FreepsPlayAvailable(alone));

		scn.FreepsPlayCard(alone);
		// The Shadow player's one card is shown to the Free Peoples player, his own hand is not, and the hand's owner
		// is shown nothing.  The event's own cost is in the pool already: 5 + 1.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(wallcrawler, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(gimli, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(6, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();

		// The Wallcrawler costs 2: 6 - 2.
		assertEquals(4, scn.GetTwilight());
		// The card revealed stays in the hand it was shown from.
		assertInZone(Zone.HAND, wallcrawler);
		assertInZone(Zone.HAND, gimli);
		assertInZone(Zone.DISCARD, alone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardOfCostOneRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetFreepsCard("alone");
		var pippin = scn.GetFreepsCard("pippin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(alone);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, runner);

		scn.FreepsPlayCard(alone);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertEquals(6, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();

		// The Runner costs 1: 6 - 1.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardOfCostZeroRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetFreepsCard("alone");
		var pippin = scn.GetFreepsCard("pippin");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(alone);
		scn.MoveCardsToHand(wreathed);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, wreathed);

		scn.FreepsPlayCard(alone);
		assertTrue(scn.FreepsHasCardChoiceAvailable(wreathed, false));
		scn.FreepsDismissRevealedCards();

		// X is 0: 5 + 1 for the event, and nothing removed.
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.DISCARD, alone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheTwilightRemovedIsTheCostOfTheCardShownAndNotOfAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetFreepsCard("alone");
		var pippin = scn.GetFreepsCard("pippin");
		var wreathed = scn.GetShadowCard("wreathed");
		var runner = scn.GetShadowCard("runner");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var lieutenant = scn.GetShadowCard("lieutenant");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(alone);
		// Six cards of six different costs: 0, 1, 2, 3, 4 and 6.
		scn.MoveCardsToHand(wreathed, runner, wallcrawler, inquisitor, lieutenant, troop);

		scn.StartGame();
		scn.SetTwilight(7);

		assertEquals(7, scn.GetTwilight());
		assertEquals(6, scn.GetShadowHandCount());

		scn.FreepsPlayCard(alone);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was, and that it was one of them only.
		var shown = 0;
		var cost = 0;
		if (scn.FreepsHasCardChoiceAvailable(wreathed, false)) { shown++; cost = 0; }
		if (scn.FreepsHasCardChoiceAvailable(runner, false)) { shown++; cost = 1; }
		if (scn.FreepsHasCardChoiceAvailable(wallcrawler, false)) { shown++; cost = 2; }
		if (scn.FreepsHasCardChoiceAvailable(inquisitor, false)) { shown++; cost = 3; }
		if (scn.FreepsHasCardChoiceAvailable(lieutenant, false)) { shown++; cost = 4; }
		if (scn.FreepsHasCardChoiceAvailable(troop, false)) { shown++; cost = 6; }
		assertEquals(1, shown);
		scn.FreepsDismissRevealedCards();

		// 7 + 1 for the event, less the cost of the card shown.
		assertEquals(8 - cost, scn.GetTwilight());
		assertEquals(6, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyHandNothingIsRevealedAndNothingIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetFreepsCard("alone");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(alone);

		scn.StartGame();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		// An event may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(alone));

		scn.MarkGameLog();
		scn.FreepsPlayCard(alone);

		// Nobody is shown anything, the log says why, and only the event's own cost goes to the pool: 3 + 1.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, alone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XIsTheCostPrintedOnTheCardThoughASiteMakesItCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEdorasHallScenario();

		var alone = scn.GetFreepsCard("alone");
		var pippin = scn.GetFreepsCard("pippin");
		var shield = scn.GetShadowCard("shield");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(alone);
		scn.MoveCardsToHand(shield);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		// At Edoras Hall the Shield, printed 2, costs 1 to play.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilightCost(shield));

		scn.FreepsPlayCard(alone);
		assertTrue(scn.FreepsHasCardChoiceAvailable(shield, false));
		scn.FreepsDismissRevealedCards();

		// X is 2: a card in play changes another card's cost only as that card is played.  5 + 1 - 2.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
