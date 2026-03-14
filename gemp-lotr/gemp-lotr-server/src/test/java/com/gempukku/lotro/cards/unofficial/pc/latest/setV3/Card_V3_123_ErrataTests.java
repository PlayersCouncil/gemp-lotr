package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_123_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lance", "103_91");
					put("eomer", "4_267");
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
					put("site8", "103_123");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothHighwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gorgoroth Highway
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 7
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 8K
		 * Game Text: At the start of the maneuver phase, hinder all weapons. All characters gain <b>enduring</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Gorgoroth Highway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void GorgorothHighwayHindersAllWeaponsAtStartOfManeuver() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata changed from cancelStrengthBonus modifier to hinder-all-weapons trigger at maneuver start
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var eomer = scn.GetFreepsCard("eomer");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, lance);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Move to site 8 (Gorgoroth Highway)
		scn.SkipToSite(7);
		scn.FreepsPass();

		// Verify we are at site 8
		assertEquals(8, scn.GetCurrentSiteNumber());

		// The lance should not be hindered yet (we haven't reached maneuver)
		assertFalse(scn.IsHindered(lance));

		// Skip to maneuver phase - the trigger should hinder all weapons
		scn.SkipToPhase(Phase.MANEUVER);

		// After the start-of-maneuver trigger fires, the lance should be hindered
		assertTrue(scn.IsHindered(lance));
	}
}
