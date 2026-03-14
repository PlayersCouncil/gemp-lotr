package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_126_ErrataTests
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
					put("site9", "103_126");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CracksofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cracks of Doom
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 9K
		 * Game Text: Mountain.  Underground. Game text on Free Peoples items and The One Ring does not apply. Each minion is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Cracks of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void CracksofDoomGivesMinionsPlusOneStrength() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata adds ModifyStrength +1 for all minions
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Goblin Runner is normally STR 5
		assertEquals(5, scn.GetStrength(runner));

		// Move to site 9 (Cracks of Doom)
		scn.SkipToSite(8);
		scn.FreepsPass();

		// Verify we arrived at site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		// At Cracks of Doom, each minion is strength +1
		// Runner should now be STR 5 + 1 = 6
		assertEquals(6, scn.GetStrength(runner));
	}
}
