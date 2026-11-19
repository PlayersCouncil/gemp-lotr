package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_325_Tests
{

	// The Fellowship path with Shire Lookout Point as its site 1, so the first fellowship phase is played on it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cart", "1_73");        // Gandalf's Cart: unique [gandalf] possession, plays to the support area; a fellowship ability used only on purpose
					put("cart2", "1_73");       // a second Gandalf's Cart, kept in the discard pile
					put("pipe", "1_74");        // Gandalf's Pipe: a [gandalf] possession whose title only begins with "Gandalf's"; bearer must be Gandalf
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a standing strength bonus only
					put("greywizard", "1_364"); // Gandalf, The Grey Wizard: a Gandalf of another subtitle; a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a standing strength bonus only
				}},
				new HashMap<>()
				{{
					put("site1", "1_325");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShireLookoutPointStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Shire Lookout Point
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Spot Gandalf to play Gandalf's Cart from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Shire Lookout Point", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionSpotsGandalfToPlayGandalfsCartFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var cart2 = scn.GetFreepsCard("cart2");
		var pipe = scn.GetFreepsCard("pipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveCardsToDiscard(cart2);
		scn.FreepsRefreshDecision();

		var lookout = scn.GetCurrentSite();
		assertEquals("Shire Lookout Point", lookout.getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DECK, cart, pipe);
		assertInZone(Zone.DISCARD, cart2);
		assertInZone(Zone.DECK, scn.GetShadowCard("cart"));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(lookout));

		scn.FreepsUseCardAction(lookout);

		// His own draw deck is shown to him, and no other.
		assertEquals(scn.GetFreepsDeckCount(), scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Gandalf's Cart may be chosen; Gandalf's Pipe, which Gandalf could bear, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(cart));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe));
		// Looking through a draw deck never forces a card to be taken (Q-0019, F-0049).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(cart);

		// The Cart plays to the support area; the Cart in the discard pile and the Pipe stay where they were.
		assertInZone(Zone.SUPPORT, cart);
		assertInZone(Zone.DISCARD, cart2);
		assertInZone(Zone.DECK, pipe);
		// One shuffle as the card leaves the deck that was looked through, one when the look is over.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		// The Shadow player's deck was not looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, scn.GetShadowCard("cart"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfOfAnotherSubtitleIsGandalfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var cart2 = scn.GetFreepsCard("cart2");
		var greywizard = scn.GetFreepsCard("greywizard");
		scn.MoveCompanionsToTable(greywizard);

		scn.StartGame();
		// One Cart in the deck, so that the one chosen is known.
		scn.MoveCardsToDiscard(cart2);
		scn.FreepsRefreshDecision();

		var lookout = scn.GetCurrentSite();
		assertInZone(Zone.FREE_CHARACTERS, greywizard);
		assertInZone(Zone.DECK, cart);
		assertInZone(Zone.DISCARD, cart2);
		assertTrue(scn.FreepsActionAvailable(lookout));

		scn.FreepsUseCardAction(lookout);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(cart);

		assertInZone(Zone.SUPPORT, cart);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilGandalfIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		var lookout = scn.GetCurrentSite();
		// A [gandalf] companion who is no Gandalf, and Gandalf in hand, are no Gandalf to spot.
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertInHand(gandalf);
		assertInZone(Zone.DECK, cart);
		assertFalse(scn.FreepsActionAvailable(lookout));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsActionAvailable(lookout));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCartInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var cart2 = scn.GetFreepsCard("cart2");
		var pipe = scn.GetFreepsCard("pipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveCardsToDiscard(cart, cart2);
		scn.FreepsRefreshDecision();

		var lookout = scn.GetCurrentSite();
		assertInZone(Zone.DISCARD, cart, cart2);
		assertInZone(Zone.DECK, pipe);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(lookout));
		scn.FreepsUseCardAction(lookout);

		// The deck is shown, with nothing in it that may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, pipe);
		assertInZone(Zone.DISCARD, cart, cart2);
		// The one shuffle when the look is over.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
