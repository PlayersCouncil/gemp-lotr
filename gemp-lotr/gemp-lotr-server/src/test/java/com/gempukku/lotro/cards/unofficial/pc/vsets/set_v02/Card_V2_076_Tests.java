package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import org.junit.Test;

import static org.junit.Assert.*;

public class Card_V2_076_Tests
{
	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario(DeckValidationScenario.PCMovie);
	}

	@Test
	public void JourneyOfTheKingStatsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: V2
		 * Name: Journey of the King
		 * Type: Map
		 * Game Text: Your adventure deck must only contain cards from the King site path.
		 */

		var bp = VirtualTableScenario.FindCard("102_76");
		assertEquals("Journey of the King", bp.getTitle());
		assertFalse(bp.isUnique());
		assertEquals(CardType.MAP, bp.getCardType());
	}

	@Test
	public void KingSitesPassWithKingMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.KingSites);

		var errors = dvs.validate(deck);
		assertFalse("King sites should pass with King map", dvs.hasMapError(errors));
	}

	@Test
	public void FellowshipSitesFailWithKingMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.FellowshipSites);

		var errors = dvs.validate(deck);
		assertTrue("Fellowship sites should fail with King map", dvs.hasMapError(errors));
	}

	@Test
	public void TowersSitesFailWithKingMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.TowersSites);

		var errors = dvs.validate(deck);
		assertTrue("Towers sites should fail with King map", dvs.hasMapError(errors));
	}
}
