package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggressor", "18_85");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Uruk Camp) is a battleground and a plains,
	// site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggressor", "18_85");
					put("aggressor2", "18_85");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil, strength 14
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishAggressorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Aggressor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While at a battleground, this minion is strength +2.
		*/

		var scn = GetScenario();

		var aggressor = scn.GetFreepsCard("aggressor");

		assertEquals("Orkish Aggressor", aggressor.getBlueprint().getTitle());
		assertNull(aggressor.getBlueprint().getSubtitle());
		assertFalse(aggressor.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, aggressor.getBlueprint().getSide());
		assertEquals(Culture.ORC, aggressor.getBlueprint().getCulture());
		assertEquals(CardType.MINION, aggressor.getBlueprint().getCardType());
		assertEquals(Race.ORC, aggressor.getBlueprint().getRace());
		assertEquals(3, aggressor.getBlueprint().getTwilightCost());
		assertEquals(8, aggressor.getBlueprint().getStrength());
		assertEquals(2, aggressor.getBlueprint().getVitality());
		assertEquals(4, aggressor.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2OnceTheFellowshipIsAtABattlegroundAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(aggressor, aggressor2, legion);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(8, scn.GetStrength(aggressor));
		assertEquals(8, scn.GetStrength(aggressor2));
		assertEquals(14, scn.GetStrength(legion));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		// 8 + 2 on each copy, for itself alone (not 8 + 4); another minion at the same battleground is not strengthened.
		assertEquals(10, scn.GetStrength(aggressor));
		assertEquals(10, scn.GetStrength(aggressor2));
		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var aggressor = scn.GetShadowCard("aggressor");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(aggressor, legion);

		// White Rocks.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(10, scn.GetStrength(aggressor));
		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
