package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.DeckValidationContext;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import org.junit.Test;

import static org.junit.Assert.*;

public class Card_92_001_Tests
{
	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario();
	}

	@Test
	public void StatsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_1
		 * Type: MetaSite
		 * Game Text: Your draw deck may contain up to 6 copies of any card (instead of 4).
		 */

		var bp = VirtualTableScenario.FindCard("92_1");
		assertEquals("Race Text 92_1", bp.getTitle());
		assertEquals(CardType.METASITE, bp.getCardType());
	}

	@Test
	public void BlueprintHasDeckBuildingOverrides() throws CardNotFoundException {
		var bp = VirtualTableScenario.FindCard("92_1");
		var overrides = bp.getDeckBuildingOverrides();
		assertNotNull(overrides);
		assertEquals(6, (int) overrides.getMaximumSameNameOverride());
	}

	@Test
	public void FiveCopiesFailsWithoutContext() {
		// 5 copies of a shadow card should fail normal validation (limit 4)
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 5, false);

		var errors = dvs.validate(deck);
		assertTrue("5 copies should fail without context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void FiveCopiesPassesWithContext() {
		// With 92_1's context (max 6), 5 copies should pass
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 5, false);
		var context = new DeckValidationContext().setMaximumSameNameOverride(6);

		var errors = dvs.validate(deck, context);
		assertFalse("5 copies should pass with context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void SixCopiesPassesWithContext() {
		// 6 copies should pass with context (right at the limit)
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 6, false);
		var context = new DeckValidationContext().setMaximumSameNameOverride(6);

		var errors = dvs.validate(deck, context);
		assertFalse("6 copies should pass with context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void SevenCopiesFailsEvenWithContext() {
		// 7 copies should fail even with context (exceeds 6)
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 7, false);
		var context = new DeckValidationContext().setMaximumSameNameOverride(6);

		var errors = dvs.validate(deck, context);
		assertTrue("7 copies should fail even with context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void FourCopiesPassesBothWays() {
		// 4 copies should pass with or without context
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 4, false);
		var context = new DeckValidationContext().setMaximumSameNameOverride(6);

		var errorsWithout = dvs.validate(deck);
		var errorsWith = dvs.validate(deck, context);

		assertFalse("4 copies should pass without context", dvs.hasMaxSameNameError(errorsWithout));
		assertFalse("4 copies should pass with context", dvs.hasMaxSameNameError(errorsWith));
	}
}
