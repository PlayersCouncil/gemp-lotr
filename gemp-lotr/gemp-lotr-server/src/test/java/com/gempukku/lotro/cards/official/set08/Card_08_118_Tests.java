package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_118_Tests
{

	// The King path with City of the Dead as its site 4. The tests move there from The Dimholt (site 3) and act in
	// the Shadow phase that follows.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Slaughterer: [sauron] Orc, enduring, cost 4, site 6 (4 + 2 roaming at site 4); a strength bonus only
					put("slaughterer", "10_95");
					// Ulaire Otsea, Thrall of The One: [wraith] Nazgul, enduring and fierce, cost 4, site 3; a regroup ability only
					put("otsea", "8_81");
					// Dead Faces: [wraith] Wraith, enduring, cost 2, site 3; answers only his own assignment
					put("deadfaces", "16_4");
					// Ulaire Nertea, Thrall of The One: [wraith] Nazgul, enduring; laid only in the other player's discard pile
					put("nertea", "8_80");
					// Orc Soldier: [sauron] Orc, NOT enduring, cost 2, site 6; a skirmish ability only
					put("soldier", "1_271");
					put("soldier2", "1_271");
					// Sleepless Dead: an enduring [gondor] Wraith that is a companion, not a minion
					put("sleepless", "8_45");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.KingSites);
					put("site4", "8_118");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CityoftheDeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: City of the Dead
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 4
		 * Shadow Number: 4
		 * Direction: Right
		 * Game Text: <b>Underground</b>. <b>Shadow:</b> Remove 2 burdens to play an enduring minion from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("City of the Dead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void RemovesTwoBurdensToPlayAnEnduringMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slaughterer = scn.GetShadowCard("slaughterer");
		var otsea = scn.GetShadowCard("otsea");
		var deadfaces = scn.GetShadowCard("deadfaces");
		var soldier = scn.GetShadowCard("soldier");
		var sleepless = scn.GetShadowCard("sleepless");
		var freepsNertea = scn.GetFreepsCard("nertea");

		scn.StartGame();
		scn.SkipToSite(3);
		// Otsea in the draw deck, so that both piles have an enduring minion. (The turns skipped have drawn cards:
		// every pile is laid now.) A pile's choice is read by blueprint, so the other player's enduring minion in
		// his own discard pile is of a title that lies in none of this player's piles: this player's Nertea is in hand.
		scn.MoveCardsToDiscard(slaughterer, deadfaces, soldier, sleepless, freepsNertea);
		scn.MoveCardsToHand(scn.GetShadowCard("nertea"));
		scn.MoveCardsToTopOfDeck(otsea);
		scn.AddBurdens(3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var city = scn.GetCurrentSite();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals("City of the Dead", city.getBlueprint().getTitle());
		assertEquals(3, scn.FreepsGetBurdens());
		assertInZone(Zone.DECK, otsea);
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(city));

		scn.ShadowUseCardAction(city);
		// Two burdens, not all of them.
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");

		// Enduring minions of two cultures and races.
		assertTrue(scn.ShadowHasCardChoiceAvailable(slaughterer, deadfaces));
		// A minion that is not enduring, an enduring companion, the other player's enduring minion in his own
		// discard pile, and the enduring minion in the other pile.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, sleepless, freepsNertea, otsea));
		scn.ShadowChooseCard(deadfaces);

		assertInZone(Zone.SHADOW_CHARACTERS, deadfaces);
		assertInZone(Zone.DISCARD, slaughterer, soldier, sleepless, freepsNertea);
		assertInZone(Zone.DECK, otsea);
		// The draw deck was not looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesTwoBurdensToPlayAnEnduringMinionFromTheDrawDeckAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slaughterer = scn.GetShadowCard("slaughterer");
		var otsea = scn.GetShadowCard("otsea");
		var deadfaces = scn.GetShadowCard("deadfaces");
		var soldier = scn.GetShadowCard("soldier");
		var sleepless = scn.GetShadowCard("sleepless");

		scn.StartGame();
		scn.SkipToSite(3);
		// Dead Faces in the discard pile, so that both piles have an enduring minion; the rest in the draw deck.
		scn.MoveCardsToDiscard(deadfaces);
		scn.MoveCardsToTopOfDeck(slaughterer, otsea, soldier, sleepless);
		scn.AddBurdens(3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var city = scn.GetCurrentSite();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.FreepsGetBurdens());
		assertInZone(Zone.DECK, slaughterer, otsea, soldier, sleepless);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowUseCardAction(city);
		assertEquals(1, scn.FreepsGetBurdens());
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.ShadowHasCardChoiceAvailable(slaughterer, otsea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, sleepless, deadfaces));
		scn.ShadowChooseCardBPFromSelection(slaughterer);

		assertInZone(Zone.SHADOW_CHARACTERS, slaughterer);
		assertInZone(Zone.DECK, otsea, soldier, sleepless);
		assertInZone(Zone.DISCARD, deadfaces);
		// Looked through, the deck is reshuffled; the other player's is not.
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEnduringMinionFoundInTheDrawDeckNeedNotBePlayedAndTheBurdensStayRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slaughterer = scn.GetShadowCard("slaughterer");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		scn.SkipToSite(3);
		// Dead Faces in hand: the discard pile has no enduring minion, so no pile is asked.
		scn.MoveCardsToHand(scn.GetShadowCard("deadfaces"));
		scn.MoveCardsToTopOfDeck(slaughterer, otsea);
		scn.AddBurdens(3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var city = scn.GetCurrentSite();
		assertEquals(3, scn.FreepsGetBurdens());
		assertInZone(Zone.DECK, slaughterer, otsea);
		int deckBefore = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(city);
		scn.ShadowDismissRevealedCards();

		// "There is no penalty if you don't find (or choose not to play) a card you are looking for in your draw deck."
		assertTrue(scn.ShadowHasCardChoiceAvailable(slaughterer, otsea));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowDeclineChoosing();

		// Nothing is played; the burdens, the cost, are gone all the same, and the deck is reshuffled.
		assertInZone(Zone.DECK, slaughterer, otsea);
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneBurdenAndIsOnceThereAreTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slaughterer = scn.GetShadowCard("slaughterer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToDiscard(slaughterer);
		scn.AddBurdens(1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var city = scn.GetCurrentSite();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.FreepsGetBurdens());
		assertInZone(Zone.DISCARD, slaughterer);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(city));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(city));
	}

	@Test
	public void IsNotOfferedWithNoEnduringMinionInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var sleepless = scn.GetShadowCard("sleepless");

		scn.StartGame();
		scn.SkipToSite(3);
		// Every enduring minion is in hand; a minion that is not enduring lies in each pile, and an enduring
		// companion in the discard pile.
		scn.MoveCardsToHand(scn.GetShadowCard("slaughterer"), scn.GetShadowCard("otsea"), scn.GetShadowCard("deadfaces"));
		scn.MoveCardsToDiscard(soldier, sleepless);
		scn.MoveCardsToTopOfDeck(soldier2);
		scn.AddBurdens(3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var city = scn.GetCurrentSite();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.FreepsGetBurdens());
		assertInZone(Zone.DECK, soldier2);
		assertInZone(Zone.DISCARD, soldier, sleepless);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(city));
	}
}
