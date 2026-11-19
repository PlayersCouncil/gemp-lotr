package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elder1", "6_2");
					put("elder2", "6_2");
					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man, strength 11, with no text
					// Southron Explorer: a [raider] Man, vitality 3.  "Regroup: Exert this minion and remove (5) to
					// take control of a site": the legal way to a second and a third site.
					put("explorer", "4_250");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingElderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Dunlending Elder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: While you control 2 sites, this minion is strength +5, <b>fierce</b> and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elder1");

		assertEquals("Dunlending Elder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileItsOwnerControlsTwoSitesEachElderIsStrengthPlusFiveFierceAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elder1 = scn.GetShadowCard("elder1");
		var elder2 = scn.GetShadowCard("elder2");
		var savage = scn.GetShadowCard("savage");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		// Both markers are on site 5: sites 1 to 4 are behind every marker.
		scn.SkipToSite(5);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		// One site of the other player's, and one of the Shadow player's: two controlled, one of them his.
		scn.FreepsTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(scn.GetSite(2));

		// The move to site 6, with no minion in play: straight on to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(elder1, elder2, savage, explorer);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(7, scn.GetStrength(elder1));
		assertEquals(7, scn.GetStrength(elder2));
		assertFalse(scn.HasKeyword(elder1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(elder2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(elder1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(elder2, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(explorer));

		// A second site of his own, by the Explorer: each Elder is 7 + 5, fierce and damage +1 (once each,
		// not once for every Elder), and the Savage, another [dunland] Man, is as he was.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowControls(site3));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(12, scn.GetStrength(elder1));
		assertEquals(12, scn.GetStrength(elder2));
		assertTrue(scn.HasKeyword(elder1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(elder2, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(elder1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(elder2, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// A third: two sites or more, and no more than the one bonus.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site4));
		assertEquals(3, scn.GetShadowControlledSiteCount());
		assertEquals(12, scn.GetStrength(elder1));
		assertTrue(scn.HasKeyword(elder1, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(elder1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
