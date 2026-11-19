package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attacker", "18_124");
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
					put("attacker", "18_124");
					put("attacker2", "18_124");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil, strength 14
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is played here
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandAttackerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Attacker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While at a battleground, this minion is strength +2.
		*/

		var scn = GetScenario();

		var attacker = scn.GetFreepsCard("attacker");

		// Decipher also gave this title to White Hand Attacker 17_120; the two are ruled separate titles (F-0004),
		// and the "(2)" is never shown to players.
		assertEquals("White Hand Attacker(2)", attacker.getBlueprint().getTitle());
		assertNull(attacker.getBlueprint().getSubtitle());
		assertFalse(attacker.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, attacker.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, attacker.getBlueprint().getCulture());
		assertEquals(CardType.MINION, attacker.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, attacker.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(attacker, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(attacker, Keyword.DAMAGE));
		assertEquals(3, attacker.getBlueprint().getTwilightCost());
		assertEquals(8, attacker.getBlueprint().getStrength());
		assertEquals(2, attacker.getBlueprint().getVitality());
		assertEquals(5, attacker.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2OnceTheFellowshipIsAtABattlegroundAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var attacker = scn.GetShadowCard("attacker");
		var attacker2 = scn.GetShadowCard("attacker2");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(attacker, attacker2, legion);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(8, scn.GetStrength(attacker));
		assertEquals(8, scn.GetStrength(attacker2));
		assertEquals(14, scn.GetStrength(legion));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		// 8 + 2 on each copy, for itself alone (not 8 + 4); another minion at the same battleground is not strengthened.
		assertEquals(10, scn.GetStrength(attacker));
		assertEquals(10, scn.GetStrength(attacker2));
		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var attacker = scn.GetShadowCard("attacker");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(attacker, legion);

		// White Rocks.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(10, scn.GetStrength(attacker));
		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
