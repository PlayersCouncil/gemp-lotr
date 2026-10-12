package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: SetFreepsDrawDeck and SetShadowDrawDeck, which make a player's draw deck exactly the cards
 * given, the first named on top.  The cards given come from wherever they lie; every other card of that deck is set
 * aside in no zone, where no pile counts it and no card finds it, and a cheat can still put it somewhere by the name
 * the table gave it; the other player's deck is left alone.  The use is a deck of a known size, or an empty one,
 * without a table small enough that every card in it can be named and moved.
 */
public class DrawDeckSettingTests
{
	// Hobbit Sword is a possession whose whole text is "Bearer must be a Hobbit": a card to hold and nothing else.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, cost 2
					for(int i = 1; i <= 6; i++) {
						put("sword" + i, "1_299");
					}
					put("runner", "1_178");     // Goblin Runner
					put("troop", "1_177");      // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Fails unless the rig refuses the call, saying each of the given things.
	private static void assertRefused(Runnable call, String... said) {
		String refusal = null;
		try {
			call.run();
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	@Test
	public void TheDrawDeckIsExactlyTheCardsGivenTopFirstAndItsOtherCardsAreInNoPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var sword4 = scn.GetFreepsCard("sword4");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(gimli);
		scn.MoveCardsToHand(sword1);

		scn.StartGame();
		assertInZone(Zone.DECK, sword2, sword3, sword4);
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.GetFreepsDeckCount() > 3);

		// One card from the hand, one from the discard pile, one from the table and one already in the deck.
		scn.SetFreepsDrawDeck(sword1, gimli, aragorn, sword2);

		assertEquals(4, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, sword1, gimli, aragorn, sword2);
		assertSame(sword1, scn.GetFreepsTopOfDeck());
		assertSame(gimli, scn.GetFromTopOfFreepsDeck(2));
		assertSame(aragorn, scn.GetFromTopOfFreepsDeck(3));
		assertSame(sword2, scn.GetFromTopOfFreepsDeck(4));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The cards that were not named are in no pile and not in play.
		assertNull(sword3.getZone());
		assertNull(sword4.getZone());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The other player's deck is as it was.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, runner);

		// The game draws from the deck as set.
		scn.FreepsDrawCard();
		assertEquals(List.of(sword1), scn.GetFreepsHand());
		assertSame(gimli, scn.GetFreepsTopOfDeck());

		// A card set aside is still found by its name, and a cheat puts it back.
		scn.MoveCardsToTopOfDeck(sword3);
		assertSame(sword3, scn.GetFreepsTopOfDeck());
		assertEquals(4, scn.GetFreepsDeckCount());
		scn.MoveCardsToHand(sword4);
		assertEquals(List.of(sword1, sword4), scn.GetFreepsHand());
	}

	@Test
	public void NoCardsGivenEmptiesTheShadowPlayersDrawDeckAndTheGameGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		int freepsDeck = scn.GetFreepsDeckCount();
		int discard = scn.GetShadowDiscardCount();

		scn.SetShadowDrawDeck();

		assertEquals(0, scn.GetShadowDeckCount());
		assertNull(runner.getZone());
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertEquals(List.of(troop), scn.GetShadowHand());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());

		// The rest of the turn, reconciles included: the Shadow player draws nothing from the empty deck, and the
		// game goes on to his turn.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetShadowDeckCount());
		assertInZone(Zone.HAND, troop);
	}

	@Test
	public void SetBeforeStartGameTheDeckKeepsTheCardsGivenAndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var gimli = scn.GetFreepsCard("gimli");

		scn.SetFreepsDrawDeck(sword1, sword2, sword3);
		scn.MoveCardsToHand(gimli);

		// The opening hand is dealt from the three and put back: they are the deck, in whatever order the deal's
		// shuffle left them.
		scn.StartGame();
		assertEquals(3, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, sword1, sword2, sword3);
		assertEquals(List.of(gimli), scn.GetFreepsHand());
	}

	@Test
	public void ACardOfTheOtherPlayersOrASiteIsRefusedAndNothingIsMoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword1 = scn.GetFreepsCard("sword1");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		assertRefused(() -> scn.SetFreepsDrawDeck(sword1, runner),
				"Goblin Runner, a card of Shadow Player's, cannot be put in the draw deck of Free Peoples Player");
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, sword1, runner);

		var site = scn.GetFreepsSite(2);
		assertRefused(() -> scn.SetFreepsDrawDeck(sword1, site), "cannot be put in the draw deck of Free Peoples Player",
				"and no site");
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
	}
}
