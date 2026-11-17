package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_161_Tests
{

	// The Towers path with Uruk Camp as site 2: site 1 (Eastemnet Gullies) has no terrain, site 2 is a battleground
	// that is also plains, and site 3 (Barrows of Edoras) is a sanctuary.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunting", "15_161");
					put("hunting2", "15_161");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc of strength 13 with no text
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

	// The plain Towers path: White Rocks, site 4, is a battleground and no plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunting", "15_161");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc of strength 13 with no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HuntingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Hunting Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: While this minion is at a battleground site, it gains <b>hunter 1</b>  <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>.
		*/

		var scn = GetUrukCampScenario();

		var card = scn.GetFreepsCard("hunting");

		assertEquals("Hunting Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsHunter1OnlyWhileAtABattlegroundSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var hunting = scn.GetShadowCard("hunting");
		var hunting2 = scn.GetShadowCard("hunting2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(hunting, hunting2, troop);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetKeywordCount(hunting, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(hunting2, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));

		// The fellowship moves to Uruk Camp, and the minions with it.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(1, scn.GetKeywordCount(hunting, Keyword.HUNTER));
		// Each copy gains it for itself, and the two do not add up.
		assertEquals(1, scn.GetKeywordCount(hunting2, Keyword.HUNTER));
		// Another minion at the same battleground does not gain it.
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));

		// And on to Barrows of Edoras, a sanctuary and no battleground.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetKeywordCount(hunting, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(hunting2, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsHunter1AtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var hunting = scn.GetShadowCard("hunting");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(hunting, troop);

		assertEquals(0, scn.GetKeywordCount(hunting, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));

		// The fellowship moves to White Rocks: Battleground, and nothing else.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(1, scn.GetKeywordCount(hunting, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
