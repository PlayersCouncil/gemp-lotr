package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "103_125");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MusterofGorgorothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Muster of Gorgoroth
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 8
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 8K
		 * Game Text: Battleground. Each minion is strength +1 for each threat you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Muster of Gorgoroth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void MinionStrengthScalesWithThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();

		scn.SkipToSite(7);
		scn.MoveMinionsToTable(runner1, runner2);

		// At site 7: no bonus yet (not at Muster of Gorgoroth)
		// Goblin Runner base STR 5
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));

		scn.FreepsPass(); // move to site 8
		assertEquals(8, scn.GetCurrentSiteNumber());

		// At Muster of Gorgoroth with 0 threats: no bonus
		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));

		// Add 1 threat: each minion +1
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(6, scn.GetStrength(runner1));
		assertEquals(6, scn.GetStrength(runner2));

		// Add 2 more threats (3 total): each minion +3
		scn.AddThreats(2);
		assertEquals(3, scn.GetThreats());
		assertEquals(8, scn.GetStrength(runner1));
		assertEquals(8, scn.GetStrength(runner2));
	}
}
