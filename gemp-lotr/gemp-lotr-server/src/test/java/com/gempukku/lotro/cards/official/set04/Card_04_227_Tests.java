package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_227_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("infantry", "4_227");
					put("infantry2", "4_227");
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
					put("infantry", "4_227");
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
	public void EasterlingInfantryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Easterling Infantry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br>While you can spot 3 burdens, this minion is <b>fierce</b> and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var infantry = scn.GetFreepsCard("infantry");

		assertEquals("Easterling Infantry", infantry.getBlueprint().getTitle());
		assertNull(infantry.getBlueprint().getSubtitle());
		assertFalse(infantry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, infantry.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, infantry.getBlueprint().getCulture());
		assertEquals(CardType.MINION, infantry.getBlueprint().getCardType());
		assertEquals(Race.MAN, infantry.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(infantry, Keyword.EASTERLING));
		assertEquals(3, infantry.getBlueprint().getTwilightCost());
		assertEquals(8, infantry.getBlueprint().getStrength());
		assertEquals(2, infantry.getBlueprint().getVitality());
		assertEquals(4, infantry.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtThreeBurdensEachCopyIsFierceAndDamagePlusOneAndOneShortItIsNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var infantry2 = scn.GetShadowCard("infantry2");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, infantry2, troop);
		scn.AddBurdens(2);
		// A wound on the Ring-bearer beside the burdens: a token that nearly counts.
		scn.AddWoundsToChar(frodo, 1);

		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(infantry, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(infantry, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(infantry2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(infantry2, Keyword.DAMAGE));

		scn.AddBurdens(1);
		assertEquals(3, scn.GetBurdens());
		// Each copy gives itself damage +1 alone: 1 each, not 2.
		assertTrue(scn.HasKeyword(infantry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(infantry, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(infantry2, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(infantry2, Keyword.DAMAGE));

		// A fourth burden changes nothing: the bonus does not grow with the count.
		scn.AddBurdens(1);
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.HasKeyword(infantry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(infantry, Keyword.DAMAGE));

		// The other minion is neither fierce nor damage +1.
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenAddedByMovingToEregionHillsMakesItFierceAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEregionHillsScenario();

		var infantry = scn.GetShadowCard("infantry");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(infantry, troop);
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.HasKeyword(infantry, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(infantry, Keyword.DAMAGE));

		// The fellowship moves to Eregion Hills, site 4, which adds the third burden.
		scn.FreepsPass();

		assertEquals("Eregion Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.HasKeyword(infantry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(infantry, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
