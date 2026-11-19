package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "17_43");

					// Squad of Haradrim: a [men] Man of another title; its Regroup ability is used only on purpose
					put("squad", "11_99");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is no Man (a Hobbit), strength 3; his
					// abilities are used only on purpose
					put("ted", "18_75");
					// Southron Explorer: a [raider] Man, strength 5, vitality 3.  "Regroup: Exert this minion and
					// remove (5) to take control of a site."
					put("explorer", "4_250");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingSneakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Easterling Sneak
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you control 2 sites, Easterling Sneak is <b>fierce</b>.<br>While you control 4 sites, each [men] minion is strength +1 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var sneak = scn.GetFreepsCard("sneak");

		assertEquals("Easterling Sneak", sneak.getBlueprint().getTitle());
		assertNull(sneak.getBlueprint().getSubtitle());
		assertTrue(sneak.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sneak.getBlueprint().getSide());
		assertEquals(Culture.MEN, sneak.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sneak.getBlueprint().getCardType());
		assertEquals(Race.MAN, sneak.getBlueprint().getRace());
		assertEquals(6, sneak.getBlueprint().getTwilightCost());
		assertEquals(15, sneak.getBlueprint().getStrength());
		assertEquals(3, sneak.getBlueprint().getVitality());
		assertEquals(4, sneak.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceFromTwoSitesAndEveryMenMinionIsStrengthPlus1AndFierceFromFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var squad = scn.GetShadowCard("squad");
		var ted = scn.GetShadowCard("ted");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		// Both markers are on site 6: sites 1 to 5 are behind every marker.
		scn.SkipToSite(6);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		// One site of the other player's, which "you control" does not count, and one of the Shadow player's.
		scn.FreepsTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(scn.GetSite(2));

		// The move to site 7, with no minion in play: straight on to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(sneak, squad, ted, explorer);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertFalse(scn.HasKeyword(sneak, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(squad, Keyword.FIERCE));

		// A second site, by the Explorer: the Sneak is fierce, and the other [men] minion is not.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site3));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.HasKeyword(sneak, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(squad, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));

		// A third site, with the other player's one beside it (four controlled in all): no bonus yet for each [men] minion.
		scn.ShadowTakeControlOfSite(site4);
		assertEquals(3, scn.GetShadowControlledSiteCount());
		assertEquals(15, scn.GetStrength(sneak));
		assertEquals(9, scn.GetStrength(squad));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.HasKeyword(sneak, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(squad, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));

		// A fourth, by the Explorer: each [men] minion, the Hobbit among them, is strength +1 and fierce; the
		// [raider] Man is neither.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site5));
		assertEquals(4, scn.GetShadowControlledSiteCount());
		assertEquals(16, scn.GetStrength(sneak));
		assertEquals(10, scn.GetStrength(squad));
		assertEquals(4, scn.GetStrength(ted));
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.HasKeyword(sneak, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(squad, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(ted, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
