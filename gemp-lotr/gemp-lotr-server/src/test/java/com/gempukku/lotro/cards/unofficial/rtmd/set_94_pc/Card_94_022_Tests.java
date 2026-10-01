package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.DeckValidationContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class Card_94_022_Tests {

	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario();
	}

	private DeckValidationContext metaSiteContext() {
		return new DeckValidationContext().setMinimumDeckSizeOverride(100);
	}

	@Test
	public void StatsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_22
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Your deck must have at least 100 cards.
		 */
		var bp = VirtualTableScenario.FindCard("94_22");
		assertEquals("Race Text 94_22", bp.getTitle());
		assertEquals(CardType.METASITE, bp.getCardType());
		assertEquals(3, bp.getIntensity());
	}

	@Test
	public void BlueprintHasDeckBuildingOverrides() throws CardNotFoundException {
		var bp = VirtualTableScenario.FindCard("94_22");
		var overrides = bp.getDeckBuildingOverrides();
		assertNotNull(overrides);
		assertEquals(100, (int) overrides.getMinimumDeckSizeOverride());
	}

	@Test
	public void SixtyCardsPassesWithoutContext() {
		// 60 is the format minimum
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 30, 30);

		var errors = dvs.validate(deck);
		assertFalse("60 cards should pass without context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void SixtyCardsFailsWithContext() {
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 30, 30);

		var errors = dvs.validate(deck, metaSiteContext());
		assertTrue("60 cards should fail with context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void NinetyNineCardsFailsWithContext() {
		// Right below the limit
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 49, 50);

		var errors = dvs.validate(deck, metaSiteContext());
		assertTrue("99 cards should fail with context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void HundredCardsPassesWithContext() {
		// Right at the limit
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 50, 50);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("100 cards should pass with context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void MergeKeepsTheMostRestrictiveMinimum() {
		// The league builds one context out of every meta-site a player carries; unlike the
		// same-name cap, a deck-size floor merges upwards.
		var low = new DeckValidationContext().setMinimumDeckSizeOverride(80);
		var high = new DeckValidationContext().setMinimumDeckSizeOverride(100);

		var mergedUp = new DeckValidationContext().setMinimumDeckSizeOverride(80);
		mergedUp.merge(high);
		assertEquals(100, (int) mergedUp.getMinimumDeckSizeOverride());

		var mergedDown = new DeckValidationContext().setMinimumDeckSizeOverride(100);
		mergedDown.merge(low);
		assertEquals(100, (int) mergedDown.getMinimumDeckSizeOverride());
	}

	@Test
	public void MergeIsIndependentOfTheSameNameChannel() {
		var deckSize = new DeckValidationContext().setMinimumDeckSizeOverride(100);
		var sameName = new DeckValidationContext().setMaximumSameNameOverride(6);

		var merged = new DeckValidationContext();
		merged.merge(deckSize);
		merged.merge(sameName);

		assertEquals(100, (int) merged.getMinimumDeckSizeOverride());
		assertEquals(6, (int) merged.getMaximumSameNameOverride());
	}

	@Test
	public void ContextWithoutTheOverrideFallsBackToTheFormatMinimum() {
		// A player who does not carry 94_22 keeps the format's own floor, even when another
		// meta-site gave them a context.
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 30, 30);
		var otherContext = new DeckValidationContext().setMaximumSameNameOverride(6);

		var errors = dvs.validate(deck, otherContext);
		assertFalse("60 cards should pass for a player without the deck-size override", dvs.hasMinimumDeckSizeError(errors));
	}
}
