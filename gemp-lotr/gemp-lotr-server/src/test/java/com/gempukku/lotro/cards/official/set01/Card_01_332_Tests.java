package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_332_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_332");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Midgewater Marshes is site 2; the rest is the usual Fellowship path.
	protected VirtualTableScenario GetMarshesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a second Hobbit; his text needs a weapon
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_332");
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
	public void MidgewaterMarshesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Midgewater Marshes
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 1
		 * Type: Site
		 * Subtype: 
		 * Site Number: 2
		 * Game Text: <b>Marsh</b>. Each time a Hobbit moves to Midgewater Marshes, that Hobbit must exert.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(2);
		var card = scn.GetFreepsCard("card");

		assertEquals("Midgewater Marshes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachHobbitMovingToMidgewaterMarshesExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshesScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals("Midgewater Marshes", scn.GetCurrentSite().getBlueprint().getTitle());
		// Each Hobbit has exerted once, and the Man not at all.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
