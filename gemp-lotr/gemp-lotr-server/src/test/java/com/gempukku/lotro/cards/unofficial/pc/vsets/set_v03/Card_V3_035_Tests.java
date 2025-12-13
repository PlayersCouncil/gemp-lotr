package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");
					put("aragorn", "1_89");
					put("boromir", "1_97"); // Second unbound Man for choice tests

					// Gondor cards for deck reveal testing
					put("gondor1", "1_94");
					put("gondor2", "1_95");
					put("gondor3", "1_96");
					// Rohan cards for deck reveal testing
					put("rohan1", "4_267");
					put("rohan2", "4_279");
					// Non-Gondor/Rohan cards
					put("elven1", "1_36");
					put("elven2", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarbeaconStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: War-beacon
		 * Unique: 3
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: <b>Beacon</b>. To play, spot an unbound Man.
		 * 		Fellowship: Add (1) per beacon you can spot to play a beacon from the discard pile.
		 * 		Fellowship: Exert a Man and hinder a beacon to reveal the top 5 cards of your draw deck.
		 * 		You may take any [gondor] or [rohan] cards revealed into hand. Replace the rest in any order.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beacon1");

		assertEquals("War-beacon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(3, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}
// War-beacon (103_35) Tests



//
// Extra Cost tests - requires spotting unbound Man
//

	@Test
	public void WarBeaconRequiresUnboundManToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(beacon1, aragorn);

		scn.StartGame();

		// With only Frodo (ring-bearer, bound), cannot play beacon
		assertFalse(scn.FreepsPlayAvailable(beacon1));

		// Play Aragorn (unbound Man)
		scn.FreepsPlayCard(aragorn);

		// Now beacon is playable
		assertTrue(scn.FreepsPlayAvailable(beacon1));
	}

//
// Ability 1 tests - Play beacon from discard pile
//

	@Test
	public void WarBeaconCannotPlayBeaconFromDiscardIfNoBeaconInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable("Play a beacon"));
	}

	@Test
	public void WarBeaconPlayBeaconFromDiscardAddsTwilightPerBeacon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);
		scn.MoveCardsToDiscard(beacon2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, beacon2);

		// With 1 beacon in play, should add 1 twilight + 1 for played beacon's cost = 2
		assertTrue(scn.FreepsActionAvailable("Play a beacon"));
		scn.FreepsUseCardAction(beacon1);
		// Only 1 beacon in discard, so no choice needed

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon2);
	}

	@Test
	public void WarBeaconPlayFromDiscardAddsTwilightForMultipleBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2);
		scn.MoveCardsToDiscard(beacon3);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		// With 2 beacons in play, should add 2 twilight + 1 for played beacon's cost = 3
		scn.FreepsUseCardAction(beacon1);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon3);
	}

	@Test
	public void WarBeaconDoesNotCountHinderedBeaconsForTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2);
		scn.MoveCardsToDiscard(beacon3);
		scn.HinderCard(beacon2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsHindered(beacon2));

		// With only 1 non-hindered beacon in play, should add 1 twilight + 1 for played beacon = 2
		scn.FreepsUseCardAction(beacon1);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, beacon3);
	}

//
// Ability 2 tests - Exert Man + Hinder beacon to reveal and take cards
//

	@Test
	public void WarBeaconRevealAbilityExertsManAndHindersBeacon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gondor1 = scn.GetFreepsCard("gondor1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);
		scn.MoveCardsToTopOfDeck(gondor1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(beacon1));

		assertTrue(scn.FreepsActionAvailable("Reveal cards"));
		scn.FreepsUseCardAction(beacon1);
		// Only 1 Man available, only 1 beacon available - both auto-selected

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(beacon1));
	}

	@Test
	public void WarBeaconRevealAbilityAllowsChoosingManAndBeacon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gondor1 = scn.GetFreepsCard("gondor1");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(beacon1, beacon2);
		scn.MoveCardsToTopOfDeck(gondor1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.IsHindered(beacon1));
		assertFalse(scn.IsHindered(beacon2));

		scn.FreepsUseCardAction(beacon1);
		scn.FreepsChooseCard(boromir); // Choose Boromir to exert
		scn.FreepsChooseCard(beacon2); // Choose beacon2 to hinder

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
	}

	@Test
	public void WarBeaconRevealTakesGondorAndRohanCardsIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gondor1 = scn.GetFreepsCard("gondor1");
		var rohan1 = scn.GetFreepsCard("rohan1");
		var rohan2 = scn.GetFreepsCard("rohan2");
		var elven1 = scn.GetFreepsCard("elven1");
		var elven2 = scn.GetFreepsCard("elven2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elven1, rohan1, gondor1, rohan2, elven2);

		scn.FreepsUseCardAction(beacon1);
		// Man and beacon auto-selected

		scn.DismissRevealedCards();

		scn.FreepsChooseCards(gondor1, rohan1);
		//Putting the rest back in any order
		scn.FreepsChooseAny();
		scn.FreepsChooseAny();

		assertInZone(Zone.HAND, gondor1);
		assertInZone(Zone.HAND, rohan1);
		assertInZone(Zone.DECK, elven1);
	}

	@Test
	public void WarBeaconRevealCannotTakeNonGondorNonRohanCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gondor1 = scn.GetFreepsCard("gondor1");
		var elven1 = scn.GetFreepsCard("elven1");
		var elven2 = scn.GetFreepsCard("elven2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(elven2, elven1, gondor1);

		scn.FreepsUseCardAction(beacon1);

		scn.DismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoicesNotAvailable(elven1, elven2));
		assertTrue(scn.FreepsHasCardChoicesAvailable(gondor1));
	}

	@Test
	public void WarBeaconRevealCanTakeZeroCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gondor1 = scn.GetFreepsCard("gondor1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);
		scn.MoveCardsToTopOfDeck(gondor1);

		scn.StartGame();

		scn.FreepsUseCardAction(beacon1);

		scn.DismissRevealedCards();

		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsChoose("");

		assertInZone(Zone.DECK, gondor1);
	}

	@Test
	public void WarBeaconRevealLimitedToFourCardsByRuleOf4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gondor1 = scn.GetFreepsCard("gondor1");
		var gondor2 = scn.GetFreepsCard("gondor2");
		var gondor3 = scn.GetFreepsCard("gondor3");
		var rohan1 = scn.GetFreepsCard("rohan1");
		var rohan2 = scn.GetFreepsCard("rohan2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(rohan2, rohan1, gondor3, gondor2, gondor1);

		scn.FreepsUseCardAction(beacon1);

		scn.DismissRevealedCards();

		// All 5 cards should be valid choices
		assertEquals(5, scn.FreepsGetCardChoiceCount());
		// But max selection should be 4 due to Rule of 4
		assertEquals(4, scn.FreepsGetChoiceMax());
	}
}
