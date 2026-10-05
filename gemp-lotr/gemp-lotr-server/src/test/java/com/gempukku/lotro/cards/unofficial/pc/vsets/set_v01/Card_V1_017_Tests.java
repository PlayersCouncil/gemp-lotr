package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_017_Tests
{
	// Frodo, Son of Drogo (vitality 4) is the Ring-bearer, with The Ruling Ring, which leaves vitality alone.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twoeyes", "101_17");

					// Gandalf, Friend of the Shirefolk: companion, cost 4, vitality 4.  His text only changes his
					// own strength.
					put("gandalf", "1_72");
					// Gandalf's Cart: a [gandalf] possession for the support area, with Gandalf's name in its
					// title.  Its fellowship ability needs an artifact or possession in hand, and there is none.
					put("cart", "1_73");

					// Dwarf Guard: cards to draw.  No text but a requirement to be played.
					put("filler1", "1_7");
					put("filler2", "1_7");
					put("filler3", "1_7");
					put("filler4", "1_7");
					put("filler5", "1_7");
					put("filler6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same Frodo bearing The One Ring, Isildur's Bane, which makes its bearer vitality +1.
	protected VirtualTableScenario GetIsildursBaneScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twoeyes", "101_17");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4

					// Dwarf Guard: cards to draw.
					put("filler1", "1_7");
					put("filler2", "1_7");
					put("filler3", "1_7");
					put("filler4", "1_7");
					put("filler5", "1_7");
					put("filler6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.IsildursBaneRing
		);
	}

	// Gimli, Bearer of Grudges (vitality 3) is the Ring-bearer, so that no Frodo starts in play.
	protected VirtualTableScenario GetGimliRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twoeyes", "101_17");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: cost 4, vitality 4

					// Frodo, Frenzied Fighter: a Frodo with no Ring-bearer keyword, who can be played beside
					// another Ring-bearer.  Cost 2, vitality 4.  A skirmish ability, never used here.
					put("frodo", "13_149");

					// Dwarf Guard: cards to draw.
					put("filler1", "1_7");
					put("filler2", "1_7");
					put("filler3", "1_7");
					put("filler4", "1_7");
					put("filler5", "1_7");
					put("filler6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TwoEyesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Two Eyes, as Often as I Can Spare Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Gandalf and Frodo to draw X cards, where X is Frodo's vitality.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("twoeyes");

		assertEquals("Two Eyes, as Often as I Can Spare Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithFrodoAloneAndDrawsFourCardsForAnUnwoundedFrodoOnceGandalfIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twoeyes = scn.GetFreepsCard("twoeyes");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var cart = scn.GetFreepsCard("cart");
		// A [gandalf] card in play that is not Gandalf, though it has his name in its title.
		scn.MoveCardsToSupportArea(cart);
		scn.MoveCardsToHand(twoeyes, gandalf);

		scn.StartGame();

		assertInPlay(frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(twoeyes));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsPlayAvailable(twoeyes));

		// Gandalf's vitality is made 3, so that it is Frodo's 4 that is counted.
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(3, scn.GetVitality(gandalf));
		assertEquals(4, scn.GetVitality(frodo));

		int deckCount = scn.GetFreepsDeckCount();
		assertTrue(deckCount > 4);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetTwilight());     // Gandalf's cost

		scn.FreepsPlayCard(twoeyes);
		assertEquals(7, scn.GetTwilight());     // 4 for Gandalf + 3 for the event
		assertInDiscard(twoeyes);
		// The event has left the hand and 4 cards have come into it.
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deckCount - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithGandalfAloneAndCountsTheVitalityOfAFrodoWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var twoeyes = scn.GetFreepsCard("twoeyes");
		var gimli = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetFreepsCard("frodo");
		scn.MoveCardsToHand(twoeyes, gandalf, frodo);

		scn.StartGame();

		// Neither is in play.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(twoeyes));

		// Gandalf in play, Frodo still in hand.
		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInHand(frodo);
		assertFalse(scn.FreepsPlayAvailable(twoeyes));

		scn.FreepsPlayCard(frodo);
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertTrue(scn.FreepsPlayAvailable(twoeyes));

		// Frodo's vitality is made 3, and Gandalf's and the Ring-bearer's 2.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(3, scn.GetVitality(frodo));
		assertEquals(2, scn.GetVitality(gandalf));
		assertEquals(2, scn.GetVitality(gimli));

		int deckCount = scn.GetFreepsDeckCount();
		assertTrue(deckCount > 3);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(6, scn.GetTwilight());     // 4 for Gandalf + 2 for Frodo

		scn.FreepsPlayCard(twoeyes);
		assertEquals(9, scn.GetTwilight());     // 6 + 3 for the event
		assertInDiscard(twoeyes);
		// The event has left the hand and 3 cards have come into it.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deckCount - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XIsFrodosVitalityAsItStandsWithABonusAddedAndHisWoundsTakenOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIsildursBaneScenario();

		var twoeyes = scn.GetFreepsCard("twoeyes");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(twoeyes);
		// Printed 4, +1 for the Ring he bears, less 3 wounds: 2.  His printed vitality is 4, and his printed
		// vitality less his wounds is 1.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();

		assertEquals(4, frodo.getBlueprint().getVitality());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetVitality(frodo));
		assertEquals(4, scn.GetVitality(gandalf));

		int deckCount = scn.GetFreepsDeckCount();
		assertTrue(deckCount > 4);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(twoeyes));

		scn.FreepsPlayCard(twoeyes);
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(twoeyes);
		// The event has left the hand and 2 cards have come into it.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deckCount - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
