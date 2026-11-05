package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_218_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legion", "4_218");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Eregion Hills 3_116 as site 4: "When the fellowship moves to Eregion Hills, add a burden."
	protected VirtualTableScenario GetEregionHillsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legion", "4_218");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "3_116");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertLegionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Desert Legion
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (1)</b>.<br>While you can spot 4 burdens, this minion is strength +5.<br>While you can spot 5 burdens, this minion is <b>fierce</b>.<br>While you can spot 6 burdens, this minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var legion = scn.GetFreepsCard("legion");

		assertEquals("Desert Legion", legion.getBlueprint().getTitle());
		assertNull(legion.getBlueprint().getSubtitle());
		assertTrue(legion.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, legion.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, legion.getBlueprint().getCulture());
		assertEquals(CardType.MINION, legion.getBlueprint().getCardType());
		assertEquals(Race.MAN, legion.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(legion, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(legion, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.AMBUSH));
		assertEquals(6, legion.getBlueprint().getTwilightCost());
		assertEquals(13, legion.getBlueprint().getStrength());
		assertEquals(3, legion.getBlueprint().getVitality());
		assertEquals(4, legion.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthFierceAndDamageComeAtFourFiveAndSixBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(legion, troop);
		scn.AddBurdens(3);

		assertEquals(3, scn.GetBurdens());
		assertEquals(13, scn.GetStrength(legion));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		scn.AddBurdens(1);
		assertEquals(4, scn.GetBurdens());
		assertEquals(18, scn.GetStrength(legion));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		scn.AddBurdens(1);
		assertEquals(5, scn.GetBurdens());
		assertEquals(18, scn.GetStrength(legion));
		assertTrue(scn.HasKeyword(legion, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		scn.AddBurdens(1);
		assertEquals(6, scn.GetBurdens());
		assertEquals(18, scn.GetStrength(legion));
		assertTrue(scn.HasKeyword(legion, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		// The other minion is neither stronger, fierce nor damage +1.
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenAddedByMovingToEregionHillsMakesItStrengthPlusFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEregionHillsScenario();

		var legion = scn.GetShadowCard("legion");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(legion, troop);
		scn.AddBurdens(3);

		assertEquals(3, scn.GetBurdens());
		assertEquals(13, scn.GetStrength(legion));
		assertEquals(13, scn.GetStrength(troop));

		// The fellowship moves to Eregion Hills, site 4, which adds the fourth burden.
		scn.FreepsPass();

		assertEquals("Eregion Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetBurdens());
		assertEquals(18, scn.GetStrength(legion));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
