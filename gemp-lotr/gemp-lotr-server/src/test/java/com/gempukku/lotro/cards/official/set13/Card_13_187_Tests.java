package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_187_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cards for the draw deck only, never played: the draw is all the site does.
					put("cloak1", "1_42");          // Elven Cloak
					put("cloak2", "1_42");
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("ruins", "18_138");         // Sirannon Ruins: River, and no other text
					put("city", "13_187");
					// Never laid in these tests.
					put("site4", "11_234");
					put("site5", "12_187");
					put("site6", "13_185");
					put("site7", "12_185");
					put("site8", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void CityofKingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: City of Kings
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. When the fellowship moves to this site during the regroup phase, the Free Peoples player may draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("city");

		assertEquals("City of Kings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void MovingToTheCityInTheRegroupPhaseOffersTheFreePeoplesPlayerACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak1 = scn.GetFreepsCard("cloak1");
		var cloak2 = scn.GetFreepsCard("cloak2");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.SetDrawDeck(VirtualTableScenario.P1, cloak1, cloak2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertSame(cloak1, scn.GetFreepsTopOfDeck());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("city"));

		// The Shadow player laid the City, his own copy: the offer is the Free Peoples player's, not the site's owner's.
		var city = scn.GetCurrentSite();
		assertEquals("City of Kings", city.getBlueprint().getTitle());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(city));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(city));

		scn.FreepsAcceptOptionalTrigger();
		// One card, the top one.
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(cloak1);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertSame(cloak2, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDrawDeckAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak1 = scn.GetFreepsCard("cloak1");
		var cloak2 = scn.GetFreepsCard("cloak2");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.SetDrawDeck(VirtualTableScenario.P1, cloak1, cloak2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("city"));

		var city = scn.GetCurrentSite();
		assertEquals("City of Kings", city.getBlueprint().getTitle());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(city));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertSame(cloak1, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheCityInTheFellowshipPhaseOrFromItInTheRegroupPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak1 = scn.GetFreepsCard("cloak1");
		var cloak2 = scn.GetFreepsCard("cloak2");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.SetDrawDeck(VirtualTableScenario.P1, cloak1, cloak2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("city"));

		var city = scn.GetCurrentSite();
		assertEquals("City of Kings", city.getBlueprint().getTitle());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(city));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());

		// The regroup move away from the City: the fellowship moves, in the regroup phase, but not to this site.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));

		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(city));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheDrawIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame(scn.GetFreepsSite("site1"));
		// The opening hand is dealt from this table's two cards: set both deck and hand empty.
		scn.SetFreepsHand();
		scn.SetDrawDeck(VirtualTableScenario.P1);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("city"));

		// An optional draw is asked with nothing to draw (Q-0029).
		var city = scn.GetCurrentSite();
		assertEquals("City of Kings", city.getBlueprint().getTitle());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(city));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
