package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.DeckValidationContext;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_022_Tests
{
	// The format, for what it makes of a deck. Its decks have Frodo 1_290 as Ring-bearer and filler of other titles.
	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario();
	}

	// A table whose player 1 has the modifier, for the card itself
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// No card but the Ring-bearer is needed
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_22", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_22
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Your deck must have at least 100 cards.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_22", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getIntensity());
	}

	@Test
	public void BlueprintHasDeckBuildingOverrides() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var bp = scn.GetFreepsCard("mod").getBlueprint();
		var overrides = bp.getDeckBuildingOverrides();
		assertNotNull(overrides);
		assertEquals(100, (int) overrides.getMinimumDeckSizeOverride());
	}

	@Test
	public void SixtyCardsPassesWithoutContext() {
		// 60 is the format minimum: this is the deck of a player who has no such modifier
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 30, 30);

		var errors = dvs.validate(deck);
		assertFalse("60 cards should pass without context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void SixtyCardsFailsWithContext() throws CardNotFoundException {
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 30, 30);
		// What the modifier itself tells deck validation, as a league hands it over for the player who has it
		var context = VirtualTableScenario.FindCard("94_22").getDeckBuildingOverrides();

		var errors = dvs.validate(deck, context);
		assertTrue("60 cards should fail with context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void NinetyNineCardsFailsWithContext() throws CardNotFoundException {
		// Right below the limit
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 49, 50);
		// What the modifier itself tells deck validation, as a league hands it over for the player who has it
		var context = VirtualTableScenario.FindCard("94_22").getDeckBuildingOverrides();

		// The same deck is large enough for a player who has no such modifier
		assertFalse("99 cards should pass without context", dvs.hasMinimumDeckSizeError(dvs.validate(deck)));

		var errors = dvs.validate(deck, context);
		assertTrue("99 cards should fail with context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void HundredCardsPassesWithContext() throws CardNotFoundException {
		// Right at the limit
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 50, 50);
		// What the modifier itself tells deck validation, as a league hands it over for the player who has it
		var context = VirtualTableScenario.FindCard("94_22").getDeckBuildingOverrides();

		var errors = dvs.validate(deck, context);
		assertFalse("100 cards should pass with context", dvs.hasMinimumDeckSizeError(errors));
	}

	@Test
	public void MergeKeepsTheMostRestrictiveMinimum() throws CardNotFoundException {
		// The league builds one context out of every meta-site a player carries; unlike the
		// same-name cap, a deck-size floor merges upwards.
		var dvs = GetScenario();
		// No other modifier has a floor of its own: a lower one is made up for the merge
		var low = new DeckValidationContext().setMinimumDeckSizeOverride(80);
		// What the modifier itself tells deck validation
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
		// What the modifier itself tells deck validation
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

	@Test
	public void ContextWithoutTheOverrideFallsBackToTheFormatMinimum() throws CardNotFoundException {
		// A player who does not carry 94_22 keeps the format's own floor, even when another
		// meta-site gave them a context.
		var dvs = GetScenario();
		var deck = dvs.buildDeck(null, null, 30, 30);
		// Race Text 92_1: "Your draw deck may contain up to 6 copies of any card (instead of 4)."
		var otherContext = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		var errors = dvs.validate(deck, otherContext);
		assertFalse("60 cards should pass for a player without the deck-size override", dvs.hasMinimumDeckSizeError(errors));
	}
}
