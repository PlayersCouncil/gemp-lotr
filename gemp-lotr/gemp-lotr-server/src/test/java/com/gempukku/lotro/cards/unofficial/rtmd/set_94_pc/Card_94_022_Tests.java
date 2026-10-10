package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
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
		 * Game Text: Your draw deck must have at least 100 cards.
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
