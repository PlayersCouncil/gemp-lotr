package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_345_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_345");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Mithril Mine as its site 4; a Nazgul, and a weapon of his in the discard pile.
	protected VirtualTableScenario GetMineScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "1_232");    // Ulaire Lemenya: Nazgul; her text is about [wraith] conditions on companions
					put("blade", "1_216");      // Morgul Blade: [wraith] hand weapon, cost 1, bearer must be a Nazgul
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_345");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MithrilMineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Mithril Mine
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: 4
		 * Game Text: <b>Underground</b>. <b>Shadow:</b> Remove (1) to play a Shadow weapon from your discard pile.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(4);
		var card = scn.GetFreepsCard("card");

		assertEquals("Mithril Mine", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "Remove (1) to play a Shadow weapon from your discard pile": a play from a pile as an effect is treated as
	// a cost, so the pool must pay for the removal and the weapon together (F-0046, F-0141).  The definition did
	// not count the (1) it removes: with exactly the weapon's cost in the pool the action was offered, took the
	// token and played nothing.
	// F-0450.
	@Test
	public void ThePoolMustPayForTheRemovalAndTheWeaponTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMineScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var blade = scn.GetShadowCard("blade");
		scn.MoveCardsToDiscard(blade);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		var mine = scn.GetCurrentSite();
		scn.MoveMinionsToTable(lemenya);

		// (1) in the pool: enough for the removal or for the Blade, not for both.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mine));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(mine));
		scn.ShadowUseCardAction(mine);

		assertEquals(0, scn.GetTwilight());
		assertAttachedTo(blade, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void MithrilMineTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
