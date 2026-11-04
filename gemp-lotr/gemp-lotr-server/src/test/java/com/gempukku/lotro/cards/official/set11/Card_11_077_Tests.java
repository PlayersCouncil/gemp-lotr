package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_077_Tests
{

	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Eastfold) is a plains and nothing more,
	// site 3 (Westemnet Plains) a plains and a sanctuary, site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elder", "11_77");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					// Westemnet Plains: Plains. Sanctuary. Its bar on [dunland] Men's archery wounds reaches no test.
					put("site3", "4_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElderofDunlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Elder of Dunland
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While this minion is at a plains site, it is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elder");

		assertEquals("Elder of Dunland", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus1OnlyOnceTheFellowshipIsAtAPlainsSiteAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elder = scn.GetShadowCard("elder");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(elder, legion);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(0, scn.GetKeywordCount(elder, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		// The fellowship moves to Eastfold.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(1, scn.GetKeywordCount(elder, Keyword.DAMAGE));
		// Another [men] Man at the same plains is not made damage +1.
		assertEquals(0, scn.GetKeywordCount(legion, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDamagePlus1AtAPlainsThatIsASanctuaryAndNotAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elder = scn.GetShadowCard("elder");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(elder, legion);

		// Westemnet Plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(1, scn.GetKeywordCount(elder, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		// The fellowship moves to White Rocks, a battleground and no plains.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(0, scn.GetKeywordCount(elder, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
