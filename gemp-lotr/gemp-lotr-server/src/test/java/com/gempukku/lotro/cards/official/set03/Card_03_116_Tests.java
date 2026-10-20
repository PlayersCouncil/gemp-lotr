package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_116_Tests
{

	// The Fellowship sites, with Eregion Hills as site 4 in place of Balin's Tomb.  No minion is laid, so each
	// move goes from the Shadow phase straight to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "3_116");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EregionHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Eregion Hills
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: When the fellowship moves to Eregion Hills, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Eregion Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingToEregionHillsAddsABurdenAndMovingOnFromItAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());

		scn.FreepsPass();

		// The Shadow player lays his own Eregion Hills as the fellowship moves; the burden goes on the moving
		// fellowship's Ring-bearer, not on the site owner's.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(scn.GetShadowSite(4), scn.GetCurrentSite());
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
