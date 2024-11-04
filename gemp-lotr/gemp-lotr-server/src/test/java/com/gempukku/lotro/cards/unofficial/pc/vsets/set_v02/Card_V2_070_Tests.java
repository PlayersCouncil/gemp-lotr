package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_070_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("haldir", "4_71");
					put("hillman", "15_82");
					put("mob", "4_205");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "102_66");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BreachedWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Breached Wall
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 6
		 * Type: Site
		 * Subtype: 
		 * Site Number: 5T
		 * Game Text: Battleground. The count of sites each Shadow player controls
		* 	 is +1. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Breached Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.BATTLEGROUND));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void BreachedWallBonusAffectsAllSiteControlMechanics() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wall = scn.GetShadowSite(5);
		var haldir = scn.GetFreepsCard("haldir");
		scn.FreepsMoveCharToTable(haldir);

		var hillman = scn.GetShadowCard("hillman");
		var mob = scn.GetShadowCard("mob");
		scn.ShadowMoveCardToHand(hillman, mob);

		scn.StartGame();

		scn.SkipToSite(3);
		scn.SetTwilight(12);
		scn.FreepsPassCurrentPhaseAction(); // we're now on 4, not quite Breached Wall

		//No opponent controls a site, so Haldir is strength +2
		assertEquals(7, scn.getStrength(haldir));

		assertEquals(15, scn.GetTwilight());
		scn.ShadowMoveCharToTable(hillman);
		//Is base 9 strength, not boosted or fierce from controlling a site
		assertEquals(9, scn.getStrength(hillman));
		assertFalse(scn.hasKeyword(hillman, Keyword.FIERCE));

		scn.ShadowPlayCard(mob);
		//15 - full undiscounted 5 -2 for roaming = 8
		assertEquals(8, scn.GetTwilight());

		scn.ShadowMoveCardToHand(hillman, mob);
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		assertEquals(wall, scn.GetCurrentSite());

		//Opponent site control count is +1, so Haldir loses bonus
		assertEquals(5, scn.getStrength(haldir));

		assertEquals(16, scn.GetTwilight());
		scn.ShadowMoveCharToTable(hillman);
		//Is base 9 strength +3 and fierce from controlling a site
		assertEquals(12, scn.getStrength(hillman));
		assertTrue(scn.hasKeyword(hillman, Keyword.FIERCE));

		scn.ShadowPlayCard(mob);
		//16 - 5 full cost + 1 discount for controlling 1 site = 12
		assertEquals(12, scn.GetTwilight());
	}
}
