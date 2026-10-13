package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.TestConstants;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.RevealHandModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the readings of what a seat has been sent by the game.  FreepsHandIsRevealed,
 * ShadowHandIsRevealed and HandIsRevealed(player) answer whose hand the game stats tell every client is revealed;
 * FreepsSeesCardInHand, ShadowSeesCardInHand and PlayerSeesCardInHand(player, card) answer whether a seat has
 * been sent a card as a card in a hand and no removal of it since.  FreepsSeesCardInZone, FreepsSeesCardBorneBy
 * and FreepsSeesCardInSkirmish, with their Shadow and by-player twins, answer where a seat was last told a card
 * lies, which card it was told bears it, and whether it was told the card is in the skirmish being fought.  Each
 * is read from a channel the test table keeps for the seat from before the game's first step and from a client
 * joined to the seat as the reading is made, and is refused when the two were sent different things.
 *
 * One card has a player play with a revealed hand, Race Text 92_7 ("You must play with your hand revealed."), so
 * it is the card on the tables here that have a revealed hand; the cards in the hands are plain Decipher ones.
 */
public class SeatReadingTests implements TestConstants
{
	private static HashMap<String, String> Deck() {
		return new HashMap<>()
		{{
			put("gimli", "1_13");       // Gimli, Son of Gloin: companion, cost 2
			put("legolas", "1_50");     // Legolas, Greenleaf
			put("sword", "1_299");      // Hobbit Sword: "Bearer must be a Hobbit" and nothing else
			put("runner", "1_178");     // Goblin Runner: cost 1
			put("troop", "1_177");      // Goblin Patrol Troop: no text
		}};
	}

	// No hand is revealed at this table.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(Deck(), VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo, VirtualTableScenario.RulingRing);
	}

	// Player 1 plays with his hand revealed.
	protected VirtualTableScenario GetFreepsRevealedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(Deck(), VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo, VirtualTableScenario.RulingRing, "92_7", null);
	}

	// Player 2 plays with his hand revealed.
	protected VirtualTableScenario GetShadowRevealedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(Deck(), VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo, VirtualTableScenario.RulingRing, null, "92_7");
	}

	// A table that keeps no channel for its seats, as a table with a person at it keeps none.
	private static class TableWithoutSeatChannels extends VirtualTableScenario {
		TableWithoutSeatChannels() throws CardNotFoundException, DecisionResultInvalidException {
			super(Deck(), VirtualTableScenario.FellowshipSites, VirtualTableScenario.FOTRFrodo, VirtualTableScenario.RulingRing);
		}
		@Override
		protected void BeforeGameStarts() { }
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
	public void WithNoHandRevealedEachSeatSeesTheCardsOfItsOwnHandAndNoneOfTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(gimli, runner);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(legolas);

		assertFalse(scn.FreepsHandIsRevealed());
		assertFalse(scn.ShadowHandIsRevealed());
		assertFalse(scn.HandIsRevealed(P1));
		assertFalse(scn.HandIsRevealed(P2));

		// His own hand and not the other player's.
		assertTrue(scn.FreepsSeesCardInHand(gimli));
		assertFalse(scn.ShadowSeesCardInHand(gimli));
		assertTrue(scn.ShadowSeesCardInHand(runner));
		assertFalse(scn.FreepsSeesCardInHand(runner));
		assertTrue(scn.PlayerSeesCardInHand(P1, gimli));
		assertFalse(scn.PlayerSeesCardInHand(P2, gimli));

		// A card on a draw deck is in nobody's sight; drawn, it is in its owner's.
		assertFalse(scn.FreepsSeesCardInHand(legolas));
		assertFalse(scn.ShadowSeesCardInHand(legolas));
		scn.FreepsDrawCards(1);
		assertInZone(Zone.HAND, legolas);
		assertTrue(scn.FreepsSeesCardInHand(legolas));
		assertFalse(scn.ShadowSeesCardInHand(legolas));

		// Played, a card is no longer a card in a hand to its owner either.
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.FreepsSeesCardInHand(gimli));
		assertFalse(scn.ShadowSeesCardInHand(gimli));
	}

	@Test
	public void ARevealedHandIsReportedAndItsCardsAreSeenFromTheOtherSeatAsTheyComeAndGo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsRevealedScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetFreepsCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(gimli, sword, troop, runner);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(legolas);

		assertTrue(scn.FreepsHandIsRevealed());
		assertFalse(scn.ShadowHandIsRevealed());
		assertTrue(scn.HandIsRevealed(P1));
		assertFalse(scn.HandIsRevealed(P2));

		// The revealed hand is before both seats; the other hand before its owner only.
		assertTrue(scn.FreepsSeesCardInHand(gimli));
		assertTrue(scn.ShadowSeesCardInHand(gimli));
		assertTrue(scn.ShadowSeesCardInHand(sword));
		assertTrue(scn.ShadowSeesCardInHand(troop));
		assertTrue(scn.ShadowSeesCardInHand(runner));
		assertFalse(scn.FreepsSeesCardInHand(runner));

		// A card is seen from the other seat as it arrives in the revealed hand, and not before.
		assertFalse(scn.ShadowSeesCardInHand(legolas));
		scn.FreepsDrawCards(1);
		assertInZone(Zone.HAND, legolas);
		assertTrue(scn.ShadowSeesCardInHand(legolas));

		// And it goes from the other seat's sight as it leaves: played, discarded, put back on the draw deck.
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.ShadowSeesCardInHand(gimli));
		assertFalse(scn.FreepsSeesCardInHand(gimli));

		scn.MoveCardsToDiscard(sword);
		assertFalse(scn.ShadowSeesCardInHand(sword));

		scn.MoveCardsToTopOfDeck(troop);
		assertFalse(scn.ShadowSeesCardInHand(troop));

		// The cards that stayed are still seen.
		assertTrue(scn.ShadowSeesCardInHand(legolas));
		assertTrue(scn.FreepsHandIsRevealed());
	}

	@Test
	public void TheReadingsGoByThePlayerAndNotByTheSeatOrTheSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowRevealedScenario();

		var freepsGimli = scn.GetFreepsCard("gimli");
		var shadowGimli = scn.GetShadowCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(freepsGimli, shadowGimli, troop);

		scn.StartGame();

		assertTrue(scn.ShadowHandIsRevealed());
		assertFalse(scn.FreepsHandIsRevealed());
		assertTrue(scn.FreepsSeesCardInHand(shadowGimli));
		assertTrue(scn.FreepsSeesCardInHand(troop));
		assertFalse(scn.ShadowSeesCardInHand(freepsGimli));

		// On the next turn player 2 is the Free Peoples player, and his hand is the revealed one still.
		scn.SkipToOtherPlayersTurn();
		assertEquals(P2, scn.GetCurrentPlayer());
		assertTrue(scn.ShadowHandIsRevealed());
		assertFalse(scn.FreepsHandIsRevealed());
		assertInZone(Zone.HAND, shadowGimli, troop);
		assertTrue(scn.FreepsSeesCardInHand(shadowGimli));
		assertTrue(scn.FreepsSeesCardInHand(troop));
		assertFalse(scn.ShadowSeesCardInHand(freepsGimli));

		// A card he plays goes from the other seat's sight, and the rest of his hand stays in it.
		scn.ShadowPlayCard(shadowGimli);
		assertInZone(Zone.FREE_CHARACTERS, shadowGimli);
		assertFalse(scn.FreepsSeesCardInHand(shadowGimli));
		assertTrue(scn.FreepsSeesCardInHand(troop));
	}

	@Test
	public void EachSeatSeesTheZoneACardLiesInAndTheCardThatBearsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(gimli, sword, runner);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(legolas);

		// A card in a hand is before its owner there, and nowhere before the other player.
		assertTrue(scn.FreepsSeesCardInZone(gimli, Zone.HAND));
		assertFalse(scn.ShadowSeesCardInZone(gimli, Zone.HAND));
		assertTrue(scn.ShadowSeesCardInZone(runner, Zone.HAND));
		assertFalse(scn.FreepsSeesCardInZone(runner, Zone.HAND));
		assertTrue(scn.PlayerSeesCardInZone(P2, runner, Zone.HAND));
		assertFalse(scn.PlayerSeesCardInZone(P1, runner, Zone.HAND));

		// A card on a draw deck is before nobody: in no zone, and on no card nor on none.
		assertInZone(Zone.DECK, legolas);
		assertFalse(scn.FreepsSeesCardInZone(legolas, Zone.DECK));
		assertFalse(scn.ShadowSeesCardInZone(legolas, Zone.DECK));
		assertFalse(scn.FreepsSeesCardBorneBy(legolas, null));
		assertFalse(scn.FreepsSeesCardBorneBy(legolas, frodo));

		// A character in play is before both seats, on no card; what he bears is before both as borne by him.
		assertTrue(scn.FreepsSeesCardInZone(frodo, Zone.FREE_CHARACTERS));
		assertTrue(scn.ShadowSeesCardInZone(frodo, Zone.FREE_CHARACTERS));
		assertTrue(scn.FreepsSeesCardBorneBy(frodo, null));
		assertTrue(scn.ShadowSeesCardBorneBy(frodo, null));
		assertTrue(scn.FreepsSeesCardInZone(ring, Zone.ATTACHED));
		assertTrue(scn.ShadowSeesCardInZone(ring, Zone.ATTACHED));
		assertFalse(scn.FreepsSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.FreepsSeesCardBorneBy(ring, frodo));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, frodo));
		assertTrue(scn.PlayerSeesCardBorneBy(P2, ring, frodo));
		assertFalse(scn.FreepsSeesCardBorneBy(ring, null));
		assertFalse(scn.ShadowSeesCardBorneBy(ring, gimli));

		// Played, a card is where it was played to, for both seats, and in a hand for neither.
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsSeesCardInZone(gimli, Zone.FREE_CHARACTERS));
		assertTrue(scn.ShadowSeesCardInZone(gimli, Zone.FREE_CHARACTERS));
		assertFalse(scn.FreepsSeesCardInZone(gimli, Zone.HAND));
		assertTrue(scn.ShadowSeesCardBorneBy(gimli, null));

		// "Bearer must be a Hobbit": Frodo is the only one, so the sword goes on him with nothing asked.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.FreepsSeesCardInZone(sword, Zone.ATTACHED));
		assertTrue(scn.ShadowSeesCardInZone(sword, Zone.ATTACHED));
		assertTrue(scn.FreepsSeesCardBorneBy(sword, frodo));
		assertTrue(scn.ShadowSeesCardBorneBy(sword, frodo));
		assertFalse(scn.ShadowSeesCardBorneBy(sword, gimli));

		// In its owner's discard pile it is before him there, borne by no card any longer.
		scn.MoveCardsToDiscard(sword);
		assertTrue(scn.FreepsSeesCardInZone(sword, Zone.DISCARD));
		assertFalse(scn.FreepsSeesCardInZone(sword, Zone.ATTACHED));
		assertFalse(scn.FreepsSeesCardBorneBy(sword, frodo));
		assertTrue(scn.FreepsSeesCardBorneBy(sword, null));
	}

	@Test
	public void EachSeatSeesWhoIsInTheSkirmishWhileItIsFoughtAndNobodyOnceItIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		assertFalse(scn.FreepsSeesCardInSkirmish(gimli));
		assertFalse(scn.ShadowSeesCardInSkirmish(runner));

		scn.FreepsAssignAndResolve(gimli, runner);

		// Both seats are told who fights, and of nobody else.
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner));
		assertTrue(scn.FreepsSeesCardInSkirmish(gimli));
		assertTrue(scn.ShadowSeesCardInSkirmish(gimli));
		assertTrue(scn.FreepsSeesCardInSkirmish(runner));
		assertTrue(scn.ShadowSeesCardInSkirmish(runner));
		assertTrue(scn.PlayerSeesCardInSkirmish(P1, runner));
		assertTrue(scn.PlayerSeesCardInSkirmish(P2, gimli));
		assertFalse(scn.FreepsSeesCardInSkirmish(frodo));
		assertFalse(scn.ShadowSeesCardInSkirmish(frodo));

		// 6 against 5: the Goblin Runner loses and dies, and the skirmish is over.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsSeesCardInSkirmish(gimli));
		assertFalse(scn.ShadowSeesCardInSkirmish(gimli));
		assertFalse(scn.FreepsSeesCardInSkirmish(runner));
		assertFalse(scn.ShadowSeesCardInSkirmish(runner));
	}

	@Test
	public void AReadingIsRefusedWhereTheSeatAtTheTableAndAClientJoiningItWereSentDifferentThings() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(legolas);
		assertFalse(scn.ShadowSeesCardInHand(gimli));

		// No card reveals a hand part of the way through a game.  Done by a cheat, the game sends the cards
		// already in that hand to nobody at the table, while a client who joins is sent the whole hand: the seat
		// at the table and the client joining it differ, and the rig says so in place of an answer.
		scn.ApplyAdHocModifier(new RevealHandModifier(null, null, P1));

		assertRefused(() -> scn.ShadowSeesCardInHand(gimli),
				"Shadow Player has not been sent Gimli", "a client joining that seat now is sent it as one");
		assertRefused(() -> scn.ShadowSeesCardInZone(gimli, Zone.HAND),
				"Shadow Player was last sent Gimli", "nowhere", "a client joining that seat now is sent it as a card in HAND");

		// A card that arrives after the hand was revealed is sent to both, and is read.
		scn.FreepsDrawCards(1);
		assertTrue(scn.ShadowSeesCardInHand(legolas));
		// The owner's own seat was sent his whole hand either way.
		assertTrue(scn.FreepsSeesCardInHand(gimli));
	}

	@Test
	public void TheReadingsAreRefusedForNoCardAndAtATableThatKeepsNoSeatChannels() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.StartGame();

		assertRefused(() -> scn.ShadowSeesCardInHand(null), "No card was given", "Shadow Player");
		assertRefused(() -> scn.ShadowSeesCardInZone(null, Zone.HAND), "No card was given", "Shadow Player");
		assertRefused(() -> scn.FreepsSeesCardInZone(scn.GetRing(), null), "No zone was given", "Free Peoples Player");
		assertRefused(() -> scn.FreepsSeesCardBorneBy(null, scn.GetRingBearer()), "No card was given", "Free Peoples Player");
		assertRefused(() -> scn.ShadowSeesCardInSkirmish(null), "No card was given", "Shadow Player");
		// The table keeps nothing of how a site is sent as it is laid on the adventure path.
		assertRefused(() -> scn.FreepsSeesCardInZone(scn.GetCurrentSite(), Zone.ADVENTURE_PATH),
				"Free Peoples Player was last sent", "nowhere", "a client joining that seat now is sent it as a card in ADVENTURE_PATH");

		var bare = new TableWithoutSeatChannels();
		var gimli = bare.GetFreepsCard("gimli");
		bare.MoveCardsToHand(gimli);
		bare.StartGame();

		assertRefused(() -> bare.FreepsHandIsRevealed(), "keeps no channel for Free Peoples Player");
		assertRefused(() -> bare.FreepsSeesCardInHand(gimli), "keeps no channel for Free Peoples Player");
		assertRefused(() -> bare.ShadowSeesCardInHand(gimli), "keeps no channel for Shadow Player");
		assertRefused(() -> bare.FreepsSeesCardInZone(gimli, Zone.HAND), "keeps no channel for Free Peoples Player");
		assertRefused(() -> bare.ShadowSeesCardBorneBy(bare.GetRing(), bare.GetRingBearer()), "keeps no channel for Shadow Player");
		assertRefused(() -> bare.FreepsSeesCardInSkirmish(gimli), "keeps no channel for Free Peoples Player");
	}
}
