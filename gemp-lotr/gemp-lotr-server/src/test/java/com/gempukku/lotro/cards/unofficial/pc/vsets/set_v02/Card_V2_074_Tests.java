package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import org.junit.Test;

import static org.junit.Assert.*;

public class Card_V2_074_Tests
{
	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario(DeckValidationScenario.PCMovie);
	}

	@Test
	public void PathOfTheFellowshipStatsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: V2
		 * Name: Path of the Fellowship
		 * Type: Map
		 * Game Text: Your adventure deck must only contain cards from the Fellowship site path.
		 */

		var bp = VirtualTableScenario.FindCard("102_74");
		assertEquals("Path of the Fellowship", bp.getTitle());
		assertFalse(bp.isUnique());
		assertEquals(CardType.MAP, bp.getCardType());
	}

	@Test
	public void FellowshipSitesPassWithFellowshipMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.FellowshipSites);

		var errors = dvs.validate(deck);
		assertFalse("Fellowship sites should pass with Fellowship map", dvs.hasMapError(errors));
	}

	@Test
	public void TowersSitesFailWithFellowshipMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.TowersSites);

		var errors = dvs.validate(deck);
		assertTrue("Towers sites should fail with Fellowship map", dvs.hasMapError(errors));
	}

	@Test
	public void KingSitesFailWithFellowshipMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.KingSites);

		var errors = dvs.validate(deck);
		assertTrue("King sites should fail with Fellowship map", dvs.hasMapError(errors));
	}
}
