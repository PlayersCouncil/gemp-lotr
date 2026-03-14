package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_129_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "103_129");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PinnacleofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pinnacle of Doom
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 9K
		 * Game Text: Mountain. At the start of each phase, the Shadow player may remove (1) to hinder a card (except companions or The One Ring).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Pinnacle of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void PinnacleofDoomCostsOneTwilightToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata added RemoveTwilight(1) as a cost to use the hinder trigger
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Move to site 9 (Pinnacle of Doom)
		scn.SkipToSite(8);
		scn.FreepsPass();

		// Verify we arrived at site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		// Set twilight to 3 (enough to use the trigger)
		scn.SetTwilight(3);
		int twilightBefore = scn.GetTwilight();

		// At the start of the shadow phase, the optional trigger fires
		// Skip through Fellowship phase
		scn.FreepsPassCurrentPhaseAction();

		// Shadow should have the optional trigger from Pinnacle of Doom
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

		// Accept the trigger - should cost (1) twilight
		scn.ShadowAcceptOptionalTrigger();

		// Choose the runner to hinder (it's not a companion or The One Ring)
		scn.ShadowChooseCard(runner);

		// The runner should now be hindered
		assertTrue(scn.IsHindered(runner));

		// Twilight should have been reduced by 1
		assertEquals(twilightBefore - 1, scn.GetTwilight());
	}
}
