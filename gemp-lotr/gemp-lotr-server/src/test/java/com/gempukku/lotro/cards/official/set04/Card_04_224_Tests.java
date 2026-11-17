package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_224_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axeman", "4_224");
					put("axeman2", "4_224");
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
					put("axeman", "4_224");
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
	public void EasterlingAxemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Easterling Axeman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br>While you can spot 2 burdens, this minion is strength +3 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var axeman = scn.GetFreepsCard("axeman");

		assertEquals("Easterling Axeman", axeman.getBlueprint().getTitle());
		assertNull(axeman.getBlueprint().getSubtitle());
		assertFalse(axeman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, axeman.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, axeman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, axeman.getBlueprint().getCardType());
		assertEquals(Race.MAN, axeman.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(axeman, Keyword.EASTERLING));
		assertEquals(2, axeman.getBlueprint().getTwilightCost());
		assertEquals(6, axeman.getBlueprint().getStrength());
		assertEquals(2, axeman.getBlueprint().getVitality());
		assertEquals(4, axeman.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTwoBurdensEachCopyIsStrengthPlusThreeAndFierceAndOneShortItIsNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, axeman2, troop);
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(axeman));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(6, scn.GetStrength(axeman2));
		assertFalse(scn.HasKeyword(axeman2, Keyword.FIERCE));

		scn.AddBurdens(1);
		assertEquals(2, scn.GetBurdens());
		// Each copy gives itself +3 alone: 6 + 3, not 6 + 3 + 3.
		assertEquals(9, scn.GetStrength(axeman));
		assertTrue(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(axeman2));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));

		// A third burden changes nothing: the bonus does not grow with the count.
		scn.AddBurdens(1);
		assertEquals(3, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(axeman));
		assertTrue(scn.HasKeyword(axeman, Keyword.FIERCE));

		// The other minion is neither stronger nor fierce.
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenAddedByMovingToEregionHillsMakesItStrengthPlusThreeAndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEregionHillsScenario();

		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(axeman, troop);
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(axeman));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(13, scn.GetStrength(troop));

		// The fellowship moves to Eregion Hills, site 4, which adds the second burden.
		scn.FreepsPass();

		assertEquals("Eregion Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(axeman));
		assertTrue(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
