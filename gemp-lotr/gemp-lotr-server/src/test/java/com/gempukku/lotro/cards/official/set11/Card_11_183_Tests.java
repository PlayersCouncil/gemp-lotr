package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_183_Tests
{

	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Uruk Camp) is a battleground and a plains,
	// site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("feral", "11_183");
					put("ruthless", "11_200");  // Ruthless Uruk: an [uruk-hai] Uruk-hai, strength 10, no damage bonus;
					                            // its text answers only its own assignment
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
	public void FeralUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Feral Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: While this minion is at a battleground site, it is strength +1 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("feral");

		assertEquals("Feral Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus1AndDamagePlus1OnlyOnceTheFellowshipIsAtABattlegroundSiteAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feral = scn.GetShadowCard("feral");
		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(feral, ruthless);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(9, scn.GetStrength(feral));
		assertEquals(0, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		assertEquals(10, scn.GetStrength(ruthless));
		assertEquals(0, scn.GetKeywordCount(ruthless, Keyword.DAMAGE));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(10, scn.GetStrength(feral));  // 9 printed + 1
		assertEquals(1, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		// Another Uruk-hai minion at the same battleground gains neither.
		assertEquals(10, scn.GetStrength(ruthless));
		assertEquals(0, scn.GetKeywordCount(ruthless, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus1AndDamagePlus1AtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feral = scn.GetShadowCard("feral");
		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(feral, ruthless);

		// White Rocks.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(10, scn.GetStrength(feral));
		assertEquals(1, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		assertEquals(10, scn.GetStrength(ruthless));
		assertEquals(0, scn.GetKeywordCount(ruthless, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
