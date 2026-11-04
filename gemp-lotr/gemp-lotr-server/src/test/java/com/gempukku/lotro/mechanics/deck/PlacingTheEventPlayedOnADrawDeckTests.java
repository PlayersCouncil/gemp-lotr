package com.gempukku.lotro.mechanics.deck;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * An event may place itself on top of, or beneath, its owner's draw deck in place of going to the discard pile.
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
 * What is shown here is the default, on a printed event that places itself either way (Toss Me 6_11).  Another
 * card's Response that places the event played on top of a draw deck is Shrouded Elf's, and is shown in that
 * card's own tests (Card_13_023_ErrataTests).  No printed card places another card's event BENEATH a draw deck:
 * "filter: played" on PutPlayedEventOnBottomOfDrawDeck is built and is not tested (Ketura, 2026-10-11: a test
 * shows what a card that exists does).
 */
public class PlacingTheEventPlayedOnADrawDeckTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tossme", "6_11");      // Toss Me: Fellowship event, cost 1: place this card or another [dwarven] card from hand on top of or beneath your draw deck
					put("tossme2", "6_11");
					// Cards for the draw deck, so that its top and its bottom are two cards.
					put("sorry1", "1_312");     // Sorry About Everything: a [shire] Fellowship event, never played
					put("sorry2", "1_312");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
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
