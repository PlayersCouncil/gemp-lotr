package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_056_Tests
{

	// The Hobbit table: the Hobbit path, with Smaug's Den as site 8, Bilbo, Reliable Companion 30_46 as the
	// Ring-bearer (a Hobbit, so nothing lowers Smaug's cost) and no card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Smaug, The Golden: the one Smaug in print, a [smaug] Dragon minion of cost 16 (-1 for each Dwarf,
					// Man and Elf spotted), discarded if not at a mountain.  Two copies, for the two piles.
					put("smaug", "30_66");
					put("smaug2", "30_66");
					// Goblin Patrol Troop: [moria] Orc minion, cost 6, no text: a minion of another title the pool can pay for.
					put("troop", "1_177");
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SmaugsDenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Smaug's Den
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>Underground</b>. <b>Shadow:</b> Play Smaug from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Smaug's Den", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void ShadowActionPlaysSmaugFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		// The turns skipped have drawn cards: the piles are laid now.  A Smaug in each pile, and the Troop in the deck.
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(smaug, troop);
		scn.MoveCardsToDiscard(smaug2);
		assertInZone(Zone.DECK, smaug, troop);
		assertInZone(Zone.DISCARD, smaug2);
		scn.FreepsPass();

		var den = scn.GetCurrentSite();
		assertEquals("30_56", den.getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(den));

		scn.ShadowUseCardAction(den);
		// Smaug lies in both piles: the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.DismissRevealedCards();

		// Smaug is offered; the Troop, a minion the pool could pay for, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(smaug));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCardBPFromSelection(smaug);

		// At a mountain, so Smaug stays.
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertInZone(Zone.DISCARD, smaug2);
		assertInZone(Zone.DECK, troop);
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSmaugInBothPilesTheDiscardPileMayBeChosenAndNoDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(smaug2, troop);
		scn.MoveCardsToDiscard(smaug);
		assertInZone(Zone.DECK, smaug2, troop);
		assertInZone(Zone.DISCARD, smaug);
		scn.FreepsPass();

		var den = scn.GetCurrentSite();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(den));

		scn.ShadowUseCardAction(den);
		scn.ShadowChoose("discard pile");

		// The one Smaug in the discard pile is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertInZone(Zone.DECK, smaug2, troop);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionPlaysSmaugFromTheDiscardPileAndShufflesNoDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		// Both Smaugs and the Troop in the discard pile; no Smaug in the deck.
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(smaug, smaug2, troop);
		assertInZone(Zone.DISCARD, smaug, smaug2, troop);
		scn.FreepsPass();

		var den = scn.GetCurrentSite();
		assertEquals("30_56", den.getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(den));

		scn.ShadowUseCardAction(den);

		// With nothing to play in the draw deck, the discard pile is the one pile left and no pile is asked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(smaug, smaug2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(smaug);

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertInZone(Zone.DISCARD, smaug2, troop);
		// No deck was looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithNoSmaugInEitherPileThatThePoolCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var troop = scn.GetShadowCard("troop");
		var smaug2 = scn.GetShadowCard("smaug2");
		var freepsSmaug = scn.GetFreepsCard("smaug");

		scn.StartGame();
		scn.SkipToSite(7);
		// This player's Smaugs in his hand, the other player's in that player's own discard pile, and the Troop
		// alone in this player's draw deck.
		scn.SetShadowHand(smaug, smaug2);
		scn.SetShadowDrawDeck(troop);
		scn.MoveCardsToDiscard(freepsSmaug);
		assertInHand(smaug, smaug2);
		assertInZone(Zone.DECK, troop);
		assertInZone(Zone.DISCARD, freepsSmaug);
		scn.FreepsPass();

		var den = scn.GetCurrentSite();
		assertEquals("30_56", den.getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(den));

		// A Smaug in the discard pile, with one twilight too few: 16, nothing spotted that lowers it.
		scn.MoveCardsToDiscard(smaug);
		scn.SetTwilight(15);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, smaug);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(den));

		scn.SetTwilight(16);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(den));
	}
}
