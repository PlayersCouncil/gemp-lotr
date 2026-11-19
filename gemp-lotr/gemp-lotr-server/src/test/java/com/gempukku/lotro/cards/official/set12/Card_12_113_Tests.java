package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_113_Tests
{

	// The Towers path: Eastemnet Gullies 4_325 (site 1, no terrain), Eastfold 4_331 (site 2, plains and no
	// battleground), Barrows of Edoras 4_337 (site 3, a sanctuary, no terrain), White Rocks 4_346 (site 4, a
	// battleground and no plains).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "12_113");
					put("warrior2", "12_113");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EoredWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Éored Warrior
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While this companion is at a battleground or plains site, he is strength +3.
		*/

		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");

		assertEquals("Éored Warrior", warrior.getBlueprint().getTitle());
		assertNull(warrior.getBlueprint().getSubtitle());
		assertFalse(warrior.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, warrior.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, warrior.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, warrior.getBlueprint().getCardType());
		assertEquals(Race.MAN, warrior.getBlueprint().getRace());
		assertEquals(2, warrior.getBlueprint().getTwilightCost());
		assertEquals(5, warrior.getBlueprint().getStrength());
		assertEquals(3, warrior.getBlueprint().getVitality());
		assertEquals(6, warrior.getBlueprint().getResistance());
	}

	@Test
	public void AtAPlainsEachEoredWarriorIsStrengthPlus3ForHimselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(warrior, warrior2, guard);

		scn.StartGame();

		// Eastemnet Gullies, site 1: neither a battleground nor a plains.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(5, scn.GetStrength(warrior));
		assertEquals(5, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(guard));

		// The move to Eastfold, site 2, a plains: each copy is +3 by its own text and not +6 by both; the other
		// [rohan] Man companion is not touched.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(8, scn.GetStrength(warrior));
		assertEquals(8, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtABattlegroundHeIsStrengthPlus3AndAtASanctuaryNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		scn.MoveCompanionsToTable(warrior);

		scn.StartGame();
		scn.SkipToSite(3);

		// Barrows of Edoras, site 3: a sanctuary, neither a battleground nor a plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(5, scn.GetStrength(warrior));

		// The move to White Rocks, site 4, a battleground and no plains.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(8, scn.GetStrength(warrior));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
