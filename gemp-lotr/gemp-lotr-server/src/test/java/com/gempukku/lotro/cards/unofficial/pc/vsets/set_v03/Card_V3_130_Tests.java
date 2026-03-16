package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");
					put("witchking", "1_237");
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
					put("site9", "103_130");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThresholdofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Threshold of Doom
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 9K
		 * Game Text: Mountain. Shadow: Remove a burden or a threat to play a minion from your discard pile. If it costs (5) or more, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Threshold of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void RequiresBurdenOrThreatToUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site = scn.GetFreepsSite(9);
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToDiscard(savage);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.FreepsPass(); // move to site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		// No burdens and no threats — action unavailable even with minion in discard
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(site));
	}

	@Test
	public void PlaysMinionsFromDiscardByRemovingBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site = scn.GetShadowSite(9);
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToDiscard(savage);

		scn.StartGame();

		scn.AddBurdens(1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetBurdens());

		scn.SkipToSite(8);
		scn.FreepsPass(); // move to site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		int twilightBefore = scn.GetTwilight();

		// Shadow uses site action, chooses to remove a burden
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);
		scn.ShadowChoose("Remove a burden");

		// Play savage (cost 2) from discard — only minion, auto-chosen
		// Savage cost 2 < 5, no extra twilight
		assertEquals(Zone.SHADOW_CHARACTERS, savage.getZone());
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertEquals(twilightBefore - 2, scn.GetTwilight());
	}

	@Test
	public void PlaysMinionsFromDiscardByRemovingThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site = scn.GetShadowSite(9);
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToDiscard(savage);

		scn.StartGame();

		scn.AddBurdens(1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetBurdens());

		scn.SkipToSite(8);
		scn.FreepsPass(); // move to site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		int twilightBefore = scn.GetTwilight();

		// Shadow uses site action, chooses to remove a burden
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);
		scn.ShadowChoose("threat");

		// Play savage (cost 2) from discard — only minion, auto-chosen
		// Savage cost 2 < 5, no extra twilight
		assertEquals(Zone.SHADOW_CHARACTERS, savage.getZone());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(twilightBefore - 2, scn.GetTwilight());
	}

	@Test
	public void AddsOneTwilightForExpensiveMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site = scn.GetShadowSite(9);
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCardsToDiscard(witchking);

		scn.StartGame();

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		scn.SkipToSite(8);
		scn.FreepsPass(); // move to site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		int twilightBefore = scn.GetTwilight();

		// Shadow uses site action, chooses to remove a threat
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);

		// Play Witch-king (cost 8) from discard — only minion, auto-chosen
		// Witch-king cost 8 >= 5, so add (1) twilight
		assertEquals(Zone.SHADOW_CHARACTERS, witchking.getZone());
		assertEquals(0, scn.GetThreats());
		// twilight: started at X, spent 8 on Witch-king, added 1 for expensive = X - 8 + 1
		assertEquals(twilightBefore - 8 + 1, scn.GetTwilight());
	}
}
