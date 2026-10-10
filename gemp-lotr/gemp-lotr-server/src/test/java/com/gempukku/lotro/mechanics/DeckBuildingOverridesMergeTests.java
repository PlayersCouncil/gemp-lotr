package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.DeckValidationContext;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * How the deck building overrides of several modifiers are put together for one player.  A league builds one
 * DeckValidationContext out of every meta-site a player carries (LeagueService.buildDeckValidationContext), by
 * merging each card's own overrides into a new context (DeckValidationContext.merge).  The floor on the size of
 * a draw deck keeps the highest number given, because a second "at least N cards" can only raise it, whichever
 * of the two is merged first; and a floor and a cap on copies of one title, from two different cards, both come
 * through.
 *
 * No rules document describes this: it is the server's reading of two Race to Mount Doom modifiers of deck
 * building held at once.  The cards are Race Text 94_22 ("Your draw deck must have at least 100 cards.") and Race
 * Text 92_1 ("Your draw deck may contain up to 6 copies of any card (instead of 4)."), each tested for its own
 * number in its own file.  A card's overrides are one object shared by every test of a run: they are merged INTO a
 * new context and never changed themselves.
 */
public class DeckBuildingOverridesMergeTests
{
	// The format, for what it makes of a deck. Its decks have Frodo 1_290 as Ring-bearer and filler of other titles.
	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario();
	}

	@Test
	public void MergeKeepsTheMostRestrictiveMinimum() throws CardNotFoundException {
		// The league builds one context out of every meta-site a player carries; unlike the
		// same-name cap, a deck-size floor merges upwards.
		var dvs = GetScenario();
		// No other modifier has a floor of its own: a lower one is made up for the merge
		var low = new DeckValidationContext().setMinimumDeckSizeOverride(80);
		// What Race Text 94_22 itself tells deck validation
		var high = VirtualTableScenario.FindCard("94_22").getDeckBuildingOverrides();

		var mergedUp = new DeckValidationContext().setMinimumDeckSizeOverride(80);
		mergedUp.merge(high);
		assertEquals(100, (int) mergedUp.getMinimumDeckSizeOverride());

		var mergedDown = new DeckValidationContext();
		mergedDown.merge(high);
		mergedDown.merge(low);
		assertEquals(100, (int) mergedDown.getMinimumDeckSizeOverride());

		// And the floor that comes out of either merge is the one a deck is held to: 99 cards are too few
		var deck = dvs.buildDeck(null, null, 49, 50);
		assertTrue("99 cards should fail with the floors merged upwards", dvs.hasMinimumDeckSizeError(dvs.validate(deck, mergedUp)));
		assertTrue("99 cards should fail with the floors merged downwards", dvs.hasMinimumDeckSizeError(dvs.validate(deck, mergedDown)));
	}

	@Test
	public void MergeIsIndependentOfTheSameNameChannel() throws CardNotFoundException {
		var dvs = GetScenario();
		// What Race Text 94_22 itself tells deck validation
		var deckSize = VirtualTableScenario.FindCard("94_22").getDeckBuildingOverrides();
		// Race Text 92_1: "Your draw deck may contain up to 6 copies of any card (instead of 4)."
		var sameName = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		var merged = new DeckValidationContext();
		merged.merge(deckSize);
		merged.merge(sameName);

		assertEquals(100, (int) merged.getMinimumDeckSizeOverride());
		assertEquals(6, (int) merged.getMaximumSameNameOverride());

		// A player who has both modifiers is still held to 100 cards
		var deck = dvs.buildDeck(null, null, 30, 30);
		assertTrue("60 cards should fail for a player with both modifiers", dvs.hasMinimumDeckSizeError(dvs.validate(deck, merged)));
	}
}
