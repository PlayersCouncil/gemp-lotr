package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_252_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "4_252");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 3
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (2)</b>.
		*/

		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");

		assertEquals("Southron Scout", scout.getBlueprint().getTitle());
		assertNull(scout.getBlueprint().getSubtitle());
		assertFalse(scout.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, scout.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, scout.getBlueprint().getCulture());
		assertEquals(CardType.MINION, scout.getBlueprint().getCardType());
		assertEquals(Race.MAN, scout.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(scout, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(scout, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(scout, Keyword.AMBUSH));
		assertEquals(1, scout.getBlueprint().getTwilightCost());
		assertEquals(3, scout.getBlueprint().getStrength());
		assertEquals(1, scout.getBlueprint().getVitality());
		assertEquals(4, scout.getBlueprint().getSiteNumber());
	}
}
