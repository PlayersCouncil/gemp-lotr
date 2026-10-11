package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card that answers the play of an event may place "that event" on top of, or beneath, its owner's draw deck.
 *
 * Comprehensive Rules 4.2, "playing a card": an event is played, the responses to its play are made, its effects
 * are carried out, and it then goes to the discard pile "or where it is instructed to go".  The effects
 * PutPlayedEventOnTopOfDrawDeck and PutPlayedEventOnBottomOfDrawDeck are that instruction.  They were built on
 * the card whose text they are written in, which is right for an event that places itself ("You may place this
 * event on top of your draw deck", Still Needed 7_29) and wrong for another card's Response to the play: there
 * the card is the one answering, which is in play, so the cost was paid and the event was discarded all the same
 * (F-0287, Shrouded Elf 13_23 and 63_23).
 *
 * Both now take a field "filter": "self" (the default) is the card they are written on, and "played" is the card
 * whose play is being answered, as PutPlayedEventIntoHand and RemovePlayedEventFromTheGame already had.  Any
 * other word is refused when the card is loaded (definitions/PlacingTheEventPlayedOnADrawDeckDefinitionTests: no
 * game is played there).
 *
 * The two test cards (testCards.hjson) are Free Peoples conditions: "Response: If you play an event, add (1) to
 * place that event on top of (beneath) your draw deck."  With a cost, such a response is offered again for the
 * same play for as long as the cost can be paid (CR 4.2 "response": "You may respond more than once to the same
 * situation"; Q-0034), so a test that uses one then declines that second offer.
 */
public class PlacingTheEventPlayedOnADrawDeckTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test cards (testCards.hjson), Free Peoples conditions in the support area:
					// "Response: If you play an event, add (1) to place that event on top of your draw deck."
					put("shelf", "0_8016");
					// "Response: If you play an event, add (1) to place that event beneath your draw deck."
					put("cellar", "0_8017");

					put("sorry", "1_312");      // Sorry About Everything: Fellowship event, cost 0: exert a Hobbit companion to remove a burden
					put("tossme", "6_11");      // Toss Me: Fellowship event, cost 1: place this card or another [dwarven] card from hand on top of or beneath your draw deck
					put("tossme2", "6_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnotherCardsResponseToThePlayPlacesTheEventOnTopOfItsOwnersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var shelf = scn.GetFreepsCard("shelf");
		var sorry = scn.GetFreepsCard("sorry");
		scn.MoveCardsToSupportArea(shelf);
		scn.MoveCardsToHand(sorry);

		scn.StartGame();
		scn.AddBurdens(1);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());

		scn.MarkGameLog();
		scn.FreepsPlayCard(sorry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shelf));
		scn.FreepsAcceptOptionalTrigger();
		// The response has a cost that can be paid again, so it is offered again for the same play.
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		// The event played is what moved: it is the top card of the draw deck and not the bottom one...
		assertInZone(Zone.DECK, sorry);
		assertSame(sorry, scn.GetFreepsTopOfDeck());
		assertNotSame(sorry, scn.GetFreepsBottomOfDeck());
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// ...and the card that answered is where it was.
		assertInZone(Zone.SUPPORT, shelf);
		// It went there from its play, not by way of the hand.
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GameLogSinceMarkSays("on top of their deck"));
		assertFalse(scn.GameLogSinceMarkSays("back into hand"));
		// The event still does what it says.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherCardsResponseToThePlayPlacesTheEventBeneathItsOwnersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var cellar = scn.GetFreepsCard("cellar");
		var sorry = scn.GetFreepsCard("sorry");
		scn.MoveCardsToSupportArea(cellar);
		scn.MoveCardsToHand(sorry);

		scn.StartGame();
		scn.AddBurdens(1);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);
		assertEquals(0, scn.GetTwilight());

		scn.MarkGameLog();
		scn.FreepsPlayCard(sorry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cellar));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DECK, sorry);
		assertSame(sorry, scn.GetFreepsBottomOfDeck());
		assertNotSame(sorry, scn.GetFreepsTopOfDeck());
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, cellar);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GameLogSinceMarkSays("on bottom of their deck"));
		assertFalse(scn.GameLogSinceMarkSays("back into hand"));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheResponsesDeclinedTheEventGoesToTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelf = scn.GetFreepsCard("shelf");
		var cellar = scn.GetFreepsCard("cellar");
		var sorry = scn.GetFreepsCard("sorry");
		scn.MoveCardsToSupportArea(shelf, cellar);
		scn.MoveCardsToHand(sorry);

		scn.StartGame();
		scn.AddBurdens(1);

		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(sorry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shelf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cellar));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertInDiscard(sorry);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEventThatPlacesItselfStillDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var top = scn.GetFreepsCard("tossme");
		var beneath = scn.GetFreepsCard("tossme2");
		scn.MoveCardsToHand(top, beneath);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		// With no filter written the card is the one the effect is written on: the event itself.
		scn.FreepsPlayCard(top);
		// With another [dwarven] card in hand Toss Me first asks which of the two is to be placed.
		scn.FreepsChoose("Place this card on top of or beneath");
		scn.FreepsChoose("Place this card on top of your draw deck");

		assertInZone(Zone.DECK, top);
		assertSame(top, scn.GetFreepsTopOfDeck());
		assertEquals(deck + 1, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(beneath);
		scn.FreepsChoose("Place this card beneath your draw deck");

		assertInZone(Zone.DECK, beneath);
		assertSame(beneath, scn.GetFreepsBottomOfDeck());
		assertSame(top, scn.GetFreepsTopOfDeck());
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
