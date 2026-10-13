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
 * Tests of the rig itself: SetFreepsHand and SetShadowHand, which make a player's hand exactly the cards given.
 * The cards given come from wherever they lie and stand in the hand in the order given; every other card of that
 * hand goes to the top of that player's draw deck, the front of the hand uppermost; the other player's hand is
 * left alone.  The use is a hand of a known size late in a game, after the reconciles of a turn-skipper have
 * filled it with whatever the draw deck held.
 */
public class HandSettingTests
{
	// Hobbit Sword is a possession whose whole text is "Bearer must be a Hobbit": a card to hold and nothing else.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, cost 2
					for(int i = 1; i <= 12; i++) {
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
	public void TheHandIsExactlyTheCardsGivenInTheirOrderAndItsOtherCardsGoOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.MoveCardsToHand(sword1, sword2, sword3);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		assertEquals(List.of(sword1, sword2, sword3), scn.GetFreepsHand());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, sword4);
		int deck = scn.GetFreepsDeckCount();

		// One card from the draw deck, one already in the hand, one from the discard pile and one from the table.
		// Swords 1 and 3 are in the hand and are not named.
		scn.SetFreepsHand(sword4, sword2, gimli, aragorn);

		assertEquals(List.of(sword4, sword2, gimli, aragorn), scn.GetFreepsHand());
		assertInZone(Zone.HAND, sword4, sword2, gimli, aragorn);
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The two that were not named are the top two cards of the draw deck, the front of the old hand on top.
		assertInZone(Zone.DECK, sword1, sword3);
		assertSame(sword1, scn.GetFreepsTopOfDeck());
		assertSame(sword3, scn.GetFromTopOfFreepsDeck(2));
		// Two went on and sword 4 came off.
		assertEquals(deck + 1, scn.GetFreepsDeckCount());

		// The other player's hand is as it was.
		assertEquals(List.of(runner), scn.GetShadowHand());

		// The game goes on from the hand as set: once the offer is worked out again, Gimli can be played from it.
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(gimli));
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(List.of(sword4, sword2, aragorn), scn.GetFreepsHand());
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void TheShadowPlayersHandIsSetTheSameWayAndNoCardsGivenEmptiesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var sword1 = scn.GetShadowCard("sword1");
		var freepsSword = scn.GetFreepsCard("sword1");
		scn.MoveCardsToHand(runner, sword1);
		scn.MoveCardsToHand(freepsSword);

		scn.StartGame();
		assertEquals(List.of(runner, sword1), scn.GetShadowHand());
		int deck = scn.GetShadowDeckCount();

		scn.SetShadowHand(troop);
		assertEquals(List.of(troop), scn.GetShadowHand());
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertSame(sword1, scn.GetFromTopOfShadowDeck(2));
		// Two went on and the Troop came off.
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(List.of(freepsSword), scn.GetFreepsHand());

		// No cards given: an empty hand.
		scn.SetShadowHand();
		assertEquals(0, scn.GetShadowHandCount());
		assertSame(troop, scn.GetShadowTopOfDeck());
		assertEquals(deck + 2, scn.GetShadowDeckCount());
		assertEquals(List.of(freepsSword), scn.GetFreepsHand());
	}

	@Test
	public void AHandThatReconcilesHaveFilledIsCutToTheCardsGiven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		assertEquals(0, scn.GetFreepsHandCount());

		// The rest of this turn and the whole of the other player's: each reconcile draws the hand up to 8.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(8, scn.GetFreepsHandCount());
		int held = scn.GetFreepsHandCount() + scn.GetFreepsDeckCount() + scn.GetFreepsDiscardCount();

		scn.SetFreepsHand(sword1, sword2, sword3, gimli);
		assertEquals(List.of(sword1, sword2, sword3, gimli), scn.GetFreepsHand());
		// Nothing has left the player's piles: what the hand lost, the draw deck has.
		assertEquals(held, scn.GetFreepsHandCount() + scn.GetFreepsDeckCount() + scn.GetFreepsDiscardCount());

		// With four cards in hand the Free Peoples player has initiative, as the game reads it at its next step.
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(gimli);
		assertFalse(scn.FreepsHasInitiative());
	}

	@Test
	public void ACardOfTheOtherPlayersIsRefusedAndNothingIsMoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(sword1);
		scn.MoveCardsToHand(troop);

		scn.StartGame();

		assertRefused(() -> scn.SetFreepsHand(sword2, runner),
				"Goblin Runner, a card of Shadow Player's, cannot be put in the hand of Free Peoples Player");
		assertEquals(List.of(sword1), scn.GetFreepsHand());
		assertInZone(Zone.DECK, sword2, runner);

		assertRefused(() -> scn.SetShadowHand(sword1),
				"Hobbit Sword, a card of Free Peoples Player's, cannot be put in the hand of Shadow Player");
		assertEquals(List.of(troop), scn.GetShadowHand());
		assertEquals(List.of(sword1), scn.GetFreepsHand());
	}
}
