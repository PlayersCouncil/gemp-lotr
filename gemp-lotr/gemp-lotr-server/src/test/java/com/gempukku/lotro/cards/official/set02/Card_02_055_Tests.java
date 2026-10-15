package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_55");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The default format, multipath, on the Towers sites, with Fellowship sites put in each player's adventure deck
	// as well (those of VirtualTableScenario.FellowshipSites, with Midgewater Marshes 1_332, a marsh, as site 2);
	// the test makes the first player's adventure deck Towers sites and the second player's the Fellowship sites (in
	// multipath all of a player's sites are of one block).
	protected VirtualTableScenario GetTowersPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");
					put("bagend", "1_319");       // neither marsh nor underground
					put("marshes", "1_332");      // Midgewater Marshes: marsh; "each time a Hobbit moves to" it, which no test does
					put("rivendell", "1_341");
					put("balinstomb", "1_343");
					put("bridge", "1_349");
					put("glade", "1_351");
					put("confluence", "1_353");
					put("banks", "1_356");
					put("emynmuil", "1_360");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	// The default format, multipath, on the King sites: both players' sites are King sites.  Site 3 is The Dimholt
	// 8_117, underground.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	// A Shadows-block path, whose sites have no number until they are played.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DarkPlacesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Dark Places
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each unique [moria] minion is strength +1.<br><b>Shadow:</b> Spot an opponent's site to replace it with your marsh or underground site of the same number.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dark Places", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DarkPlacesTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void InTheMultipathFormatItReplacesATowersSiteWithHisFellowshipMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersPathScenario();

		var dark = scn.GetShadowCard("dark");
		var towersSite1 = scn.GetFreepsSite("site1");
		var towersSite2 = scn.GetFreepsSite("site2");
		var bagEnd = scn.GetShadowCard("bagend");
		var marshes = scn.GetShadowCard("marshes");

		// Each adventure deck holds two site 1s: the first player starts on his Towers site 1.
		scn.StartGame(towersSite1);
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SetShadowAdventureDeck(bagEnd, marshes, scn.GetShadowCard("rivendell"), scn.GetShadowCard("balinstomb"),
				scn.GetShadowCard("bridge"), scn.GetShadowCard("glade"), scn.GetShadowCard("confluence"),
				scn.GetShadowCard("banks"), scn.GetShadowCard("emynmuil"));
		// The first player lays his own site 2 himself (as a card that plays the next site would).
		scn.MoveCardToAdventurePath(towersSite2);
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(towersSite1, scn.GetSite(1));
		assertSame(towersSite2, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(dark));

		// Both sites are the opponent's; only site 2 is one he has a Fellowship-block marsh or underground site of
		// the same number for (his site 1, Bag End, is neither), and it is chosen unasked.
		scn.ShadowUseCardAction(dark);

		// His Fellowship-block marsh takes the place of the Towers-block site the fellowship is at (Q-0327).
		assertSame(marshes, scn.GetSite(2));
		assertSame(marshes, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, towersSite2, bagEnd);
		assertSame(towersSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhereHisUndergroundSiteOfThatNumberIsAKingSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite1 = scn.GetFreepsSite(1);
		var freepsSite3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.MoveCardsToSupportArea(dark);
		// The move lays the Shadow player's own site 2.
		scn.FreepsPass();
		// The first player's site 3, The Dimholt, lies ahead, as where he played it himself.
		scn.MoveCardToAdventurePath(freepsSite3);
		scn.ShadowRefreshDecision();

		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertTrue(scn.HasKeyword(shadowSite3, Keyword.UNDERGROUND));

		// Two of the opponent's sites are there to spot, and the Shadow player has an underground site 3, but of
		// the King block: there is no Fellowship-block site to lay, and the ability is not offered (Q-0327).
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dark));
	}

	@Test
	public void NotOfferedOnAShadowsBlockPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite1 = scn.GetFreepsSite("site1");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame(freepsSite1);
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowSite2);

		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(shadowSite2, scn.GetSite(2));

		// The opponent's site 1 is there to spot, but the Shadow player's adventure deck holds Shadows-block sites,
		// none of them a Fellowship-block site: there is no site to lay, and the ability is not offered (Q-0327).
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dark));
	}
}
