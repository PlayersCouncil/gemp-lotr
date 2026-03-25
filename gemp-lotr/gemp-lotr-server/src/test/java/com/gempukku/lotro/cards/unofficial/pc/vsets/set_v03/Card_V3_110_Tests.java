package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
				}},
				VirtualTableScenario.FellowshipSites,
				"103_110",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoBereftofHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Frodo, Bereft of Hope
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: Ring-bearer (resistance 7).  Ring-bound.
		 * 	Frodo is strength +1 for each burden.
		 */

		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertEquals("Bereft of Hope", frodo.getBlueprint().getSubtitle());
		assertTrue(frodo.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, frodo.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, frodo.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, frodo.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, frodo.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(frodo, Keyword.RING_BOUND));
		assertEquals(0, frodo.getBlueprint().getTwilightCost());
		assertEquals(2, frodo.getBlueprint().getStrength());
		assertEquals(4, frodo.getBlueprint().getVitality());
		assertEquals(7, frodo.getBlueprint().getResistance());
		assertTrue(frodo.getBlueprint().canStartWithRing());
	}

	@Test
	public void FrodoBereftofHopeStrengthScalesWithBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		scn.StartGame();

		// Base strength: 2 (printed) + 1 (Ruling Ring) = 3, no burdens yet
		assertEquals(3, scn.GetStrength(frodo));

		// Add 4 burdens — strength should increase by 4
		scn.AddBurdens(4);
		assertEquals(7, scn.GetStrength(frodo));
	}
}
