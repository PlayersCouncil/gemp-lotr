package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("storm", "3_6");
					put("uruk", "1_151");       // Uruk Savage: a Shadow card of twilight cost 2
					// Three [dwarven] cards for the discard pile:
					put("axe", "1_9");          // Dwarven Axe
					put("armor", "1_8");        // Dwarven Armor
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("storm", "3_6");
					put("blow", "1_5");         // Cleaving Blow: a card of the Free Peoples player's own hand

					// [dwarven] cards of each kind for the discard pile, none with text that acts there
					put("gimli", "1_13");       // Gimli, Son of Glóin: companion
					put("grimir", "1_17");      // Grimir, Dwarven Elder: ally
					put("strike", "1_3");       // Axe Strike: event
					put("lord", "1_21");        // Lord of Moria: condition
					put("axe", "1_9");          // Dwarven Axe: possession
					put("fury", "1_4");         // Battle Fury: event
					put("enmity", "1_29");      // Ancient Enmity: an [elven] event, the card of the pile that is not [dwarven]

					// Shadow cards of different twilight costs for the Shadow hand; none has text that acts from a hand
					put("scimitar", "1_180");   // Goblin Scimitar: 0
					put("runner", "1_178");     // Goblin Runner: 1
					put("scout", "1_191");      // Moria Scout: 2
					put("marksman", "1_176");   // Goblin Marksman: 3
					put("chieftain", "1_163");  // Ancient Chieftain: 4
					put("archer", "1_172");     // Goblin Archer: 5
					put("troop", "1_177");      // Goblin Patrol Troop: 6, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StormofArgumentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Storm of Argument
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Reveal a card at random from an opponent's hand. Shuffle up to X [dwarven] cards from your discard pile into your draw deck, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("storm");

		assertEquals("Storm of Argument", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// A lone test, from engine pass 26 (F-0391): the event's first sentence is now written as its three siblings
	// write it (Friends of Old 3_16, His First Serious Check 3_33, Might of Númenor 3_43), and X is still the
	// twilight cost of the card revealed.  The card's own treatment is still to come.  With No Business of Ours
	// in play nothing is revealed: Card_02_044_Tests.
	@Test
	public void TheCardRevealedAtRandomIsShownToTheFreePeoplesPlayerAndItsTwilightCostIsX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetFreepsCard("storm");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(storm);
		scn.MoveCardsToDiscard(axe, armor, guard);

		// One card in the Shadow hand, so that the card revealed at random is not left to chance.  Its twilight
		// cost is 2, and the event's own is 1.
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(uruk);

		scn.StartGame();

		assertInZone(Zone.DISCARD, axe, armor, guard);
		assertInZone(Zone.HAND, storm, uruk);
		assertTrue(scn.FreepsPlayAvailable(storm));
		scn.FreepsPlayCard(storm);

		// The player of the event is shown the card; the owner of the hand is asked nothing.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// Up to 2 of the three [dwarven] cards, the cost of the card revealed.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard));
		scn.FreepsChooseCards(axe, armor);

		assertInZone(Zone.DECK, axe);
		assertInZone(Zone.DECK, armor);
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.DISCARD, storm);
		assertInZone(Zone.HAND, uruk);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtACostOf4UpTo4DwarvenCardsOfAnyKindAreShuffledInAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var storm = scn.GetFreepsCard("storm");
		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var strike = scn.GetFreepsCard("strike");
		var lord = scn.GetFreepsCard("lord");
		var axe = scn.GetFreepsCard("axe");
		var enmity = scn.GetFreepsCard("enmity");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(storm, blow, chieftain);
		scn.MoveCardsToDiscard(gimli, grimir, strike, lord, axe, enmity);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gimli, grimir, strike, lord, axe, enmity);
		assertInZone(Zone.HAND, blow, chieftain);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(storm);

		// The one card of the Shadow hand is shown, and not the Free Peoples player's own Cleaving Blow; the owner of
		// the hand is asked nothing.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// Ancient Chieftain costs 4: up to 4 of the five [dwarven] cards, a companion, an ally, an event, a condition
		// and a possession; not the [elven] event.
		assertEquals(5, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir, strike, lord, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(enmity));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli, grimir, strike, axe);

		assertInZone(Zone.DECK, gimli, grimir, strike, axe);
		assertInZone(Zone.DISCARD, lord, enmity, storm);
		assertEquals(deck + 4, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.HAND, chieftain, blow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XIsTheCostOfTheCardRevealedAtRandomAndNotOfAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var storm = scn.GetFreepsCard("storm");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var strike = scn.GetFreepsCard("strike");
		var lord = scn.GetFreepsCard("lord");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var runner = scn.GetShadowCard("runner");
		var scout = scn.GetShadowCard("scout");
		var marksman = scn.GetShadowCard("marksman");
		var chieftain = scn.GetShadowCard("chieftain");
		var archer = scn.GetShadowCard("archer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(storm);
		// Six different cards, of costs 1 to 6, and six [dwarven] cards, so that X is never capped by the pile.
		scn.MoveCardsToHand(runner, scout, marksman, chieftain, archer, troop);
		scn.MoveCardsToDiscard(gimli, grimir, strike, lord, axe, fury);

		scn.StartGame();

		assertEquals(6, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, gimli, grimir, strike, lord, axe, fury);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(storm);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var shown = new boolean[] {
				scn.FreepsHasCardChoiceAvailable(runner, false),
				scn.FreepsHasCardChoiceAvailable(scout, false),
				scn.FreepsHasCardChoiceAvailable(marksman, false),
				scn.FreepsHasCardChoiceAvailable(chieftain, false),
				scn.FreepsHasCardChoiceAvailable(archer, false),
				scn.FreepsHasCardChoiceAvailable(troop, false)
		};
		int expected = 0;
		int count = 0;
		for (int i = 0; i < shown.length; i++) {
			if (shown[i]) {
				// The six cards cost 1 to 6, in that order.
				expected = i + 1;
				count++;
			}
		}
		assertEquals(1, count);
		scn.FreepsDismissRevealedCards();

		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(expected, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();

		// None chosen: the pile is as it was, and the deck is shuffled all the same (Q-0102).
		assertInZone(Zone.DISCARD, gimli, grimir, strike, lord, axe, fury);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(6, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenTheCardRevealedCostsZeroNoCardIsShuffledInAndTheDeckIsShuffledAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var storm = scn.GetFreepsCard("storm");
		var gimli = scn.GetFreepsCard("gimli");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(storm, scimitar);
		scn.MoveCardsToDiscard(gimli);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.HAND, scimitar);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(storm);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, false));
		scn.FreepsDismissRevealedCards();

		// Goblin Scimitar costs 0: up to 0 cards, so none is asked for; a shuffle into a draw deck always shuffles
		// (Q-0102).
		assertInZone(Zone.DISCARD, gimli);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.HAND, scimitar);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenCardInTheDiscardPileTheEventIsPlayedAndTheDeckIsShuffledAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var storm = scn.GetFreepsCard("storm");
		var enmity = scn.GetFreepsCard("enmity");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(storm, scout);
		scn.MoveCardsToDiscard(enmity);

		scn.StartGame();

		assertInZone(Zone.DISCARD, enmity);
		assertInZone(Zone.HAND, storm, scout);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// The target is no requirement: the event is played with nothing in the pile to shuffle in.
		assertTrue(scn.FreepsPlayAvailable(storm));
		scn.FreepsPlayCard(storm);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, false));
		scn.FreepsDismissRevealedCards();

		// Moria Scout costs 2, but the one card of the pile is an [elven] event: none is asked for, and a shuffle into
		// a draw deck always shuffles (Q-0102).
		assertInZone(Zone.DISCARD, enmity, storm);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.HAND, scout);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheShadowHandEmptyTheEventIsPlayedAndNothingIsShuffledIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var storm = scn.GetFreepsCard("storm");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(storm);
		scn.MoveCardsToDiscard(gimli);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, gimli);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// The reveal is no requirement: the event is played with nothing to reveal.
		assertTrue(scn.FreepsPlayAvailable(storm));
		scn.MarkGameLog();
		scn.FreepsPlayCard(storm);

		// Nobody is shown anything, and the game log says the hand was empty (Q-0144).
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		// Nothing revealed, so X is 0; a shuffle into a draw deck always shuffles (Q-0102).
		assertInZone(Zone.DISCARD, gimli, storm);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var storm = scn.GetFreepsCard("storm");
		var gimli = scn.GetFreepsCard("gimli");
		var scout = scn.GetShadowCard("scout");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(storm, scout);
		scn.MoveCardsToDiscard(gimli);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(storm));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(storm));
		assertInZone(Zone.HAND, storm);
	}
}
