package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_336_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, not printed fierce; her text needs a
					// companion or ally bearing a [wraith] condition
					put("lemenya", "1_232");
					// Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, not printed fierce; her text answers
					// her own play only
					put("nertea", "1_234");
					// Morgul Creeper: a [wraith] Orc, no Nazgûl and not fierce; its text needs 3 burdens
					put("creeper", "8_75");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_336");     // Weathertop in place of Ettenmoors
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
	public void WeathertopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Weathertop
		 * Unique: False
		 * Shadow Number: 3
		 * Type: Site
		 * Block: Fellowship
		 * Direction: Left
		 * Site Number: 2
		 * Game Text: Each Nazgûl at Weathertop is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Weathertop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachNazgulAtWeathertopIsFierceAndAMinionWhoIsNoNazgulIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nertea, creeper);

		// At site 1, Bag End: nobody is fierce.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(creeper, Keyword.FIERCE));

		// The fellowship moves to Weathertop.
		scn.FreepsPass();

		assertEquals("Weathertop", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(creeper, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromWeathertopANazgulIsNoLongerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(lemenya);

		assertEquals("Weathertop", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));

		// The fellowship moves on to site 3, Weathertop still on the path behind it.
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Weathertop", scn.GetSite(2).getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
