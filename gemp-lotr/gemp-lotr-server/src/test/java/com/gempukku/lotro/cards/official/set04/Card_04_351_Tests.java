package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_351_Tests
{

	// The Towers path with Hornburg Parapet as site 5.  Site 4 is White Rocks (Shadow number +1 for each mounted
	// companion: there are none) and site 6 Caves of Aglarond (a maneuver ability used only on purpose); neither
	// touches an archery total.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer companion (fellowship archery total 1); his archery ability is not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no archer; a maneuver ability used only on purpose
					put("marksman1", "1_176");  // Goblin Marksman: an archer minion; Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site5", "4_351");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornburgParapetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hornburg Parapet
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 5T
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. The fellowship archery total is +1. The minion archery total is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Hornburg Parapet", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void AtTheParapetTheFellowshipArcheryTotalIsOneMoreAndTheMinionArcheryTotalOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable("marksman1", "marksman2");

		// At White Rocks, site 4: Legolas's 1 and the two Marksmen's 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		assertEquals("Hornburg Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		// 1 for Legolas + 1
		assertEquals(2, scn.GetFreepsArcheryTotal());
		// 2 for the Marksmen - 1
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromTheParapetBothTotalsAreTheirOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3");

		// At the Parapet, with no Free Peoples archer: 0 + 1, and 3 Marksmen - 1.
		assertEquals("Hornburg Parapet", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(2, scn.GetShadowArcheryTotal());

		// At Caves of Aglarond, site 6, the Parapet behind the fellowship.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
