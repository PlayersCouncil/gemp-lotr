package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_062_Tests
{

	// The Fellowship path with Rivendell Waterfall 1_342 (Forest, Sanctuary; "While you can spot a ranger at
	// Rivendell Waterfall, the move limit is +1 for this turn") as site 3.  Site 1, Bag End, is no forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madril", "11_62");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Derndingle 4_330 (Forest, no sanctuary; its text needs an unbound Hobbit) as site 2.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madril", "11_62");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MadrilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Madril, Ranger of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Ranger</b>.
		 * <b>Fellowship:</b> If Madril is at a forest site, exert him to play the fellowship's next site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("madril");

		assertEquals("Madril", card.getBlueprint().getTitle());
		assertEquals("Ranger of Ithilien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void AtAForestMadrilExertsToPlayTheFellowshipsNextSiteUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(madril);
		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.FreepsRefreshDecision();
		// Bag End is no forest.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.FreepsActionAvailable(madril));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(3);
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4, shadowSite4);
		assertEquals(0, scn.GetWoundsOn(madril));
		assertTrue(scn.FreepsActionAvailable(madril));

		scn.FreepsUseCardAction(madril);

		// His own site 4 is laid; the Shadow player's stays in his adventure deck, and the fellowship does not move.
		assertEquals(1, scn.GetWoundsOn(madril));
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With his own site already next the ability has a cost, so it is offered: Madril exerts and nothing is
		// played.
		assertTrue(scn.FreepsActionAvailable(madril));
		scn.FreepsUseCardAction(madril);
		assertEquals(2, scn.GetWoundsOn(madril));
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		assertFalse(scn.FreepsActionAvailable(madril));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAForestThatIsNoSanctuaryItPlaysTheNextSiteToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(madril);
		var site3 = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Derndingle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertNull(scn.GetSite(3));
		assertEquals(0, scn.GetWoundsOn(madril));

		assertTrue(scn.FreepsActionAvailable(madril));
		scn.FreepsUseCardAction(madril);

		assertEquals(1, scn.GetWoundsOn(madril));
		assertSame(site3, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItReplacesTheOpponentsSiteLyingNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(madril);
		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(3);

		// The other player's Balin's Tomb lies at site 4, as it does where he has played his next site himself.
		// Both players hold the same nine sites here, so the site that replaces it is its twin.
		scn.MoveCardToAdventurePath(shadowSite4);
		scn.FreepsRefreshDecision();
		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertEquals(0, scn.GetWoundsOn(madril));

		scn.FreepsUseCardAction(madril);

		assertEquals(1, scn.GetWoundsOn(madril));
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
