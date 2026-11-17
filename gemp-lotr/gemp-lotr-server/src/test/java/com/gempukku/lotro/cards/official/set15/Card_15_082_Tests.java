package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman1", "15_82");
					put("hillman2", "15_82");    // a second copy: not unique
					put("explorer", "4_250");    // Southron Explorer: Regroup: exert him and remove (5) to take control of a site
					put("corps", "11_73");       // Corps of Harad: another [men] Man minion, fierce only at a battleground (none on this path)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrousingHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Grousing Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you control a site, this minion is strength +3 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hillman1");

		assertEquals("Grousing Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileItsOwnerControlsASiteThisMinionIsStrengthPlus3AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman1 = scn.GetShadowCard("hillman1");
		var hillman2 = scn.GetShadowCard("hillman2");
		var explorer = scn.GetShadowCard("explorer");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		// Both markers on site 3: site 1 is behind them, and the Free Peoples player holds it, as he may have
		// taken it on a turn of his own as the Shadow player.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		scn.MoveMinionsToTable(hillman1, hillman2, explorer, corps);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);

		// Only the other player controls a site: no bonus.
		assertEquals(4, scn.FreepsGetSiteNumber());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsControls(site1));
		assertEquals(9, scn.GetStrength(hillman1));
		assertEquals(9, scn.GetStrength(hillman2));
		assertEquals(10, scn.GetStrength(corps));
		assertFalse(scn.HasKeyword(hillman1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(hillman2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(corps, Keyword.FIERCE));

		scn.FreepsPass();
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(explorer);

		// The Explorer takes site 2 for its owner: each Hillman is 9 + 3 and fierce, the other copy adding
		// nothing to it; the Corps of Harad is not touched.
		assertTrue(scn.ShadowControls(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(12, scn.GetStrength(hillman1));
		assertEquals(12, scn.GetStrength(hillman2));
		assertEquals(10, scn.GetStrength(corps));
		assertTrue(scn.HasKeyword(hillman1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(hillman2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(corps, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
