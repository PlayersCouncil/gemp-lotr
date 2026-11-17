package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_103_Tests
{

	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Eastfold) is a plains and nothing more,
	// site 3 (Westemnet Plains) a plains and a sanctuary, site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "11_103");
					put("warrior2", "11_103");
					put("soldier", "4_254");    // Southron Soldier: [raider] Man; "Southron. Ambush (2)." is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
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
	public void WarriorofDunlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Warrior of Dunland
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Ambush (1)</b>.<br>While this minion is at a plains site, it gains an additional ambush (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Warrior of Dunland", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GainsAnAdditionalAmbush1OnlyOnceTheFellowshipIsAtAPlainsSiteAndNoOtherMinionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(warrior, warrior2, soldier);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(warrior2, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));

		// The fellowship moves to Eastfold.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		// 1 printed + 1 at a plains, each copy for itself only.
		assertEquals(2, scn.GetKeywordCount(warrior, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(warrior2, Keyword.AMBUSH));
		// Another ambush minion at the same plains gains nothing.
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GainsItAtAPlainsThatIsASanctuaryAndNotAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(warrior);

		// Westemnet Plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(2, scn.GetKeywordCount(warrior, Keyword.AMBUSH));

		// The fellowship moves to White Rocks, a battleground and no plains.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerAtAPlainsItsOwnerMayAddTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		// Eastfold.
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		int pool = scn.GetTwilight();
		scn.FreepsAssignToMinions(aragorn, warrior);

		// The printed ambush and the one gained are offered as one, for their sum (F-0010).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		assertEquals(pool, scn.GetTwilight());
		scn.ShadowAcceptOptionalTrigger();

		// 1 printed + 1 gained.
		assertEquals(pool + 2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
