package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_151_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSavageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Savage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <helper>(When this minion wins a skirmish, add 1 extra wound to the defender.)</helper>
		*/

		var scn = GetScenario();

		var savage = scn.GetFreepsCard("savage");

		assertEquals("Uruk Savage", savage.getBlueprint().getTitle());
		assertNull(savage.getBlueprint().getSubtitle());
		assertFalse(savage.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, savage.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, savage.getBlueprint().getCulture());
		assertEquals(CardType.MINION, savage.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, savage.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(2, savage.getBlueprint().getTwilightCost());
		assertEquals(5, savage.getBlueprint().getStrength());
		assertEquals(3, savage.getBlueprint().getVitality());
		assertEquals(5, savage.getBlueprint().getSiteNumber());
	}
}
