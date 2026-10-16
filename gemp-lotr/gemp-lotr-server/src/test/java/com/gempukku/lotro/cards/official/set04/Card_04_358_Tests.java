package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_358_Tests
{

	// Ring of Isengard is site 8 of the Towers sites.  Each test skips to site 7, lays the board there and moves on,
	// so that it acts at site 8.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dunlending Ravager: [dunland] Man, cost 1, site 3; an assignment ability that needs an ally
					put("ravager", "4_15");
					// Saruman, Rabble-rouser: [dunland] minion who is no Man (a Wizard), cost 4, site 4
					put("saruman", "4_33");
					// Constantly Threatening: a [dunland] condition, cost 1, for the support area
					put("threatening", "4_6");
					// Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; Damage +1 is its whole text
					put("savage", "1_151");
					// Goblin Patrol Troop: no text; laid on top of the Shadow player's draw deck to be drawn
					put("troop1", "1_177");
					put("troop2", "1_177");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ring of Isengard
		 * Unique: False
		 * Shadow Number: 7
		 * Type: Site
		 * Block: Towers
		 * Direction: Right
		 * Site Number: 8T
		 * Game Text: <b>Shadow:</b> Play a [dunland] minion to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Ring of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowActionPlaysADunlandMinionFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var saruman = scn.GetShadowCard("saruman");
		var threatening = scn.GetShadowCard("threatening");
		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(7);
		// The turns skipped have drawn cards: the hand and the top of the deck are laid now.  troop1 is the top card,
		// troop2 the one beneath it.
		scn.MoveCardsToHand(ravager, saruman, threatening, savage);
		scn.MoveCardsToTopOfDeck(troop2, troop1);
		scn.SetTwilight(20);
		assertInHand(ravager, saruman, threatening, savage);
		assertSame(troop1, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, troop1, troop2);
		scn.FreepsPass();

		var ring = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("Ring of Isengard", ring.getBlueprint().getTitle());
		assertInHand(ravager, saruman, threatening, savage);
		assertSame(troop1, scn.GetShadowTopOfDeck());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		// Each card in hand could be played: what keeps two of them out of the choice is the wording.
		assertTrue(scn.ShadowPlayAvailable(threatening));
		assertTrue(scn.ShadowPlayAvailable(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ring));

		scn.ShadowUseCardAction(ring);
		// [dunland] minions of two races; not a [dunland] condition, nor an [isengard] minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ravager, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(threatening, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ravager);

		// The minion played is the cost; the card on top of the deck is drawn.
		assertInZone(Zone.SHADOW_CHARACTERS, ravager);
		assertInHand(troop1);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(hand, scn.GetShadowHandCount());
		assertInHand(saruman, threatening, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No limit: Saruman is the one [dunland] minion left in hand, and is played unasked.
		assertTrue(scn.ShadowActionAvailable(ring));
		scn.ShadowUseCardAction(ring);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertInHand(troop2);
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(hand, scn.GetShadowHandCount());
		assertInHand(threatening, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoDunlandMinionInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var threatening = scn.GetShadowCard("threatening");
		var savage = scn.GetShadowCard("savage");
		var ravager = scn.GetShadowCard("ravager");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToHand(saruman, threatening, savage);
		scn.MoveCardsToDiscard(ravager);
		assertInHand(saruman, threatening, savage);
		assertInDiscard(ravager);
		scn.FreepsPass();

		var ring = scn.GetCurrentSite();
		assertEquals("Ring of Isengard", ring.getBlueprint().getTitle());
		// Saruman costs 4, one more than the pool; the Uruk Savage (2) and the condition (1) can be played.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertInHand(saruman, threatening, savage);
		assertInDiscard(ravager);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(saruman));
		assertTrue(scn.ShadowPlayAvailable(savage));
		assertTrue(scn.ShadowPlayAvailable(threatening));
		assertFalse(scn.ShadowActionAvailable(ring));

		// One more twilight, and Saruman can be played: the action is there.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(saruman));
		assertTrue(scn.ShadowActionAvailable(ring));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseAtRingOfIsengard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToHand(ravager);
		scn.SetTwilight(20);
		assertInHand(ravager);
		scn.FreepsPass();

		var ring = scn.GetCurrentSite();
		assertEquals("Ring of Isengard", ring.getBlueprint().getTitle());
		assertTrue(scn.ShadowActionAvailable(ring));
		// A minion on the table, so that there is a maneuver phase.
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInHand(ravager);
		// 20 set + 7 for the site + 1 companion: the Ravager's 1 could be paid.
		assertEquals(28, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ring));
	}
}
