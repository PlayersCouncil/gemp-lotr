package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import org.junit.Test;

import static org.junit.Assert.*;

public class Card_V2_075_Tests
{
	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario(DeckValidationScenario.PCMovie);
	}

	@Test
	public void RoadToTheTowersStatsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: V2
		 * Name: Road to the Towers
		 * Type: Map
		 * Game Text: Your adventure deck must only contain cards from the Towers site path.
		 */

		var bp = VirtualTableScenario.FindCard("102_75");
		assertEquals("Road to the Towers", bp.getTitle());
		assertFalse(bp.isUnique());
		assertEquals(CardType.MAP, bp.getCardType());
	}

	@Test
	public void TowersSitesPassWithTowersMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.TowersSites);

		var errors = dvs.validate(deck);
		assertFalse("Towers sites should pass with Towers map", dvs.hasMapError(errors));
	}

	@Test
	public void FellowshipSitesFailWithTowersMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.FellowshipSites);

		var errors = dvs.validate(deck);
		assertTrue("Fellowship sites should fail with Towers map", dvs.hasMapError(errors));
	}

	@Test
	public void KingSitesFailWithTowersMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.KingSites);

		var errors = dvs.validate(deck);
		assertTrue("King sites should fail with Towers map", dvs.hasMapError(errors));
	}
}
