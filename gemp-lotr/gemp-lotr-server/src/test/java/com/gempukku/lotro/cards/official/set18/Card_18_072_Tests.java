package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruffian1", "18_72");
					put("ruffian2", "18_72");
					put("brigand", "4_10");     // Dunlending Brigand: a minion with no text, strength 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RuffianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ruffian
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 2
		 * Game Text: While the fellowship is in region 1, this minion is strength +4.
		 * While the fellowship is in region 2, this minion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruffian1");

		assertEquals("Ruffian", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus4WhileTheFellowshipIsAtSite3AndPlus2OnceItMovesToSite4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian1 = scn.GetShadowCard("ruffian1");
		var ruffian2 = scn.GetShadowCard("ruffian2");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(3);
		// Laid in the fellowship phase at site 3, the last site of region 1, to be read before and after the move.
		scn.MoveMinionsToTable(ruffian1, ruffian2, brigand);

		assertEquals(3, scn.GetCurrentSiteNumber());
		// 8 + 4: each copy gives the +4 to itself alone, so neither reads 16.
		assertEquals(12, scn.GetStrength(ruffian1));
		assertEquals(12, scn.GetStrength(ruffian2));
		// Another minion in region 1 is not strengthened.
		assertEquals(8, scn.GetStrength(brigand));

		// The fellowship moves to site 4, the first site of region 2.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 8 + 2, the region 1 bonus gone.
		assertEquals(10, scn.GetStrength(ruffian1));
		assertEquals(10, scn.GetStrength(ruffian2));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus2WhileTheFellowshipIsAtSite6AndPrintedOnceItMovesToSite7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian1 = scn.GetShadowCard("ruffian1");
		var ruffian2 = scn.GetShadowCard("ruffian2");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(6);
		// Laid in the fellowship phase at site 6, the last site of region 2.
		scn.MoveMinionsToTable(ruffian1, ruffian2, brigand);

		assertEquals(6, scn.GetCurrentSiteNumber());
		// 8 + 2 for each copy, and nothing for the other minion.
		assertEquals(10, scn.GetStrength(ruffian1));
		assertEquals(10, scn.GetStrength(ruffian2));
		assertEquals(8, scn.GetStrength(brigand));

		// The fellowship moves to site 7, in region 3.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(ruffian1));
		assertEquals(8, scn.GetStrength(ruffian2));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
