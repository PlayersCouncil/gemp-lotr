package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "1_131");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrthancAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orthanc Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Archer</b>. <b>Damage +1</b>.
		*/

		var scn = GetScenario();

		var assassin = scn.GetFreepsCard("assassin");

		assertEquals("Orthanc Assassin", assassin.getBlueprint().getTitle());
		assertNull(assassin.getBlueprint().getSubtitle());
		assertTrue(assassin.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, assassin.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, assassin.getBlueprint().getCulture());
		assertEquals(CardType.MINION, assassin.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, assassin.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(assassin, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(assassin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(assassin, Keyword.DAMAGE));
		assertEquals(5, assassin.getBlueprint().getTwilightCost());
		assertEquals(10, assassin.getBlueprint().getStrength());
		assertEquals(3, assassin.getBlueprint().getVitality());
		assertEquals(5, assassin.getBlueprint().getSiteNumber());
	}
}
