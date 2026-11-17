package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "10_81");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirith Ungol Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>.
		*/

		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");

		assertEquals("Cirith Ungol Guard", guard.getBlueprint().getTitle());
		assertNull(guard.getBlueprint().getSubtitle());
		assertFalse(guard.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, guard.getBlueprint().getSide());
		assertEquals(Culture.SAURON, guard.getBlueprint().getCulture());
		assertEquals(CardType.MINION, guard.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, guard.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(3, guard.getBlueprint().getTwilightCost());
		assertEquals(9, guard.getBlueprint().getStrength());
		assertEquals(2, guard.getBlueprint().getVitality());
		assertEquals(6, guard.getBlueprint().getSiteNumber());
	}
}
