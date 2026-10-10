package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A draw deck that a player has looked through is reshuffled when the look is over, and for a card played out of
 * it the look is over as the card leaves the deck.
 *
 * Comprehensive Rules 4.2, "draw deck": "When you finish looking through it, reshuffle it."  Ketura on Q-0013:
 * that sentence is the timing, wherever a card prints its own "reshuffle".
 *
 * A card played from a draw deck onto a bearer, and an event played from one, have always shuffled the deck as
 * they left it.  A card played to the table or the support area (PlayPermanentAction) did not: its deck waited
 * for the shuffle the definition writes, which is carried out after the play and everything the play sets off.
 * So the card's own "when you play this" text was answered with the deck still in the order its owner had just
 * seen: Old Toby's "you may draw a card" drew the card the player knew to be on top (F-0313).
 *
 * PlayPermanentAction now shuffles as the card leaves the deck when it has been told that the deck was looked
 * through, and PlayCardFromDrawDeck tells it so where the definition writes `showAll: true`, which is the look.
 * It is told nothing where no one looked: a card revealed from the top of a deck and then played leaves the rest
 * of the deck as it lay, and a starting fellowship's deck is shuffled once, when the fellowships are down.
 *
 * The deck that is reshuffled is the deck that was looked through.  A search of another player's draw deck that
 * writes its own shuffle (`deck:` names the other player, with `shuffle: true`) shuffles the deck that `deck:`
 * names and leaves the searcher's own alone (the two tests at the end).  It used to shuffle the deck of the
 * player who searched and leave the deck he had looked through as he saw it (F-0369).  The two real cards of
 * that shape, Grond 18_82 and 68_82, write `shuffle: false` and a ShuffleDeck of the deck named (F-0298), which
 * plays the same, so the shape is laid here with two test cards.
 */
public class DrawDeckShuffledWhenTheLookIsOverTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf", "17_110");      // Southfarthing Leaf: [shire] condition, support area. "Regroup: Discard this pipeweed from play to play a pipeweed from your draw deck. Reshuffle your draw deck."
					put("toby", "1_305");       // Old Toby: [shire] possession, pipeweed, cost 1, to the support area. "When you play this possession, you may draw a card."

					put("squealer", "8_77");    // Morgul Squealer: [wraith] Orc. "Shadow: Spot 6 companions and discard this minion to play up to 2 [wraith] minions from your draw deck; their twilight costs are each -1."
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, cost 4, site 3; nothing on being played
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, cost 3, site 4; archery +2 only at 3 burdens

					put("deep", "12_38");       // From Deep in Shadow: [gollum] Shadow event, cost 0. "Spot X [gollum] cards to reveal the top X cards of your draw deck. You may play a revealed minion. Its twilight cost is -2."
					put("poison1", "8_28");     // Spider Poison: [gollum] possession with the Support Area keyword; its texts need Shelob or a bearer
					put("poison2", "8_28");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, cost 6, site 4, no text

					// Test cards (testCards.hjson), Shadow conditions in the support area, each a search of the
					// other player's deck that writes `shuffle: true`:
					// "Shadow: Remove (1) to search the Free Peoples player's draw deck and discard a Free Peoples card found there."
					put("dredge", "0_8027");
					// "Shadow: Remove (1) to search the Free Peoples player's draw deck and remove a Free Peoples card found there from the game."
					put("trawl", "0_8028");

					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("legolas", "1_50");
					put("boromir", "1_96");
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACardPlayedToTheSupportAreaFromADeckLookedThroughFindsItShuffledWhenItsOwnTextIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(leaf);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int hand = scn.GetFreepsHandCount();
		assertInZone(Zone.DECK, toby);

		assertTrue(scn.FreepsActionAvailable(leaf));
		scn.FreepsUseCardAction(leaf);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(toby);

		// Old Toby is down and his own text is being asked: the look ended when he was taken out of the deck,
		// and the deck has been shuffled since
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.DISCARD, leaf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetFreepsHandCount());

		// The definition's own "Reshuffle your draw deck" follows the play, as it did
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// The Shadow player is next to act in the regroup phase
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AMinionPlayedFromADeckLookedThroughLeavesItShuffledBeforeTheDeckIsLookedThroughAgain() throws DecisionResultInvalidException, CardNotFoundException {
		// Morgul Squealer's definition looks through the deck twice and writes one shuffle after both plays:
		// neither look writes `shuffle: true`.  It is the look that shuffles, not that word.
		var scn = GetScenario();

		var squealer = scn.GetShadowCard("squealer");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas", "boromir", "merry");

		scn.StartGame();
		scn.MoveMinionsToTable(squealer);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DECK, lemenya, creeper);

		scn.ShadowUseCardAction(squealer);
		assertInZone(Zone.DISCARD, squealer);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCard(lemenya);

		// The first minion is down, and the deck is being shown for the second: it was shuffled in between
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.ShadowGetTotalRevealedCardCount() > 0);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());

		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCard(creeper);

		// Once more as the second minion left, and then the definition's own shuffle
		assertInZone(Zone.SHADOW_CHARACTERS, creeper);
		assertEquals(shuffles + 3, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardRevealedFromTheTopOfADeckAndPlayedLeavesTheRestOfTheDeckAsItLay() throws DecisionResultInvalidException, CardNotFoundException {
		// Nobody looked through this deck: its top two cards were revealed, and one of them was played.
		var scn = GetScenario();

		var deep = scn.GetShadowCard("deep");
		var troop = scn.GetShadowCard("troop");
		var creeper = scn.GetShadowCard("creeper");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveCardsToSupportArea(scn.GetShadowCard("poison1"), scn.GetShadowCard("poison2"));
		scn.MoveCardsToHand(deep);
		// From the top: the Troop, the Creeper, Lemenya
		scn.MoveCardsToTopOfDeck(lemenya, creeper, troop);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		int shuffles = scn.GetShadowDeckShuffleCount();
		int deck = scn.GetShadowDeckCount();
		assertEquals(troop, scn.GetShadowTopOfDeck());

		scn.ShadowPlayCard(deep);
		scn.ShadowChoose("2");
		scn.DismissRevealedCards();
		scn.ShadowChooseCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		// The other card revealed is on top, and the card that lay under it, which nobody saw, is still under it
		assertEquals(creeper, scn.GetShadowTopOfDeck());
		scn.MoveCardsToHand(creeper);
		assertEquals(lemenya, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionPlayedIntoAStartingFellowshipDoesNotShuffleTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// The player is still choosing from the deck: it is shuffled once, when the fellowships are down
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DECK, aragorn);

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		// The game has gone on to the other player's starting fellowship
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void ASearchOfTheOtherPlayersDeckThatDiscardsFromItShufflesTheDeckSearched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dredge = scn.GetShadowCard("dredge");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(dredge);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int twilight = scn.GetTwilight();
		assertInZone(Zone.DECK, aragorn);

		assertTrue(scn.ShadowActionAvailable(dredge));
		scn.ShadowUseCardAction(dredge);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(aragorn);

		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, aragorn);
		// The Free Peoples deck is the one that was looked through, so it is the one reshuffled; nobody looked
		// at the Shadow player's own
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASearchOfTheOtherPlayersDeckThatRemovesFromTheGameShufflesTheDeckSearched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trawl = scn.GetShadowCard("trawl");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(trawl);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int twilight = scn.GetTwilight();
		assertInZone(Zone.DECK, aragorn);

		assertTrue(scn.ShadowActionAvailable(trawl));
		scn.ShadowUseCardAction(trawl);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(aragorn);

		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.REMOVED, aragorn);
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
