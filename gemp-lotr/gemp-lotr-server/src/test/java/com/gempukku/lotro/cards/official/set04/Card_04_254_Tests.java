package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_254_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "4_254");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (2)</b>.
		*/

		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");

		assertEquals("Southron Soldier", soldier.getBlueprint().getTitle());
		assertNull(soldier.getBlueprint().getSubtitle());
		assertFalse(soldier.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, soldier.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, soldier.getBlueprint().getCulture());
		assertEquals(CardType.MINION, soldier.getBlueprint().getCardType());
		assertEquals(Race.MAN, soldier.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(soldier, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(soldier, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertEquals(2, soldier.getBlueprint().getTwilightCost());
		assertEquals(4, soldier.getBlueprint().getStrength());
		assertEquals(1, soldier.getBlueprint().getVitality());
		assertEquals(4, soldier.getBlueprint().getSiteNumber());
	}
}
