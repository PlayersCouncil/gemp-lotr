package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_001_Tests
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
				"92_1", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_1
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Your draw deck may contain up to 6 copies of any card (instead of 4).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_1", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void BlueprintHasDeckBuildingOverrides() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var bp = scn.GetFreepsCard("mod").getBlueprint();
		var overrides = bp.getDeckBuildingOverrides();
		assertNotNull(overrides);
		assertEquals(6, (int) overrides.getMaximumSameNameOverride());
	}

	@Test
	public void FiveCopiesFailsWithoutContext() {
		// 5 copies of a shadow card (Forces of Mordor) should fail normal validation (limit 4): this is the deck
		// of a player who has no such modifier
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 5, false);

		var errors = dvs.validate(deck);
		assertTrue("5 copies should fail without context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void FiveCopiesPassesWithContext() throws CardNotFoundException {
		// With 92_1's context (max 6), 5 copies should pass
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 5, false);
		// What the modifier itself tells deck validation, as a league hands it over for the player who has it
		var context = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		var errors = dvs.validate(deck, context);
		assertFalse("5 copies should pass with context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void SixCopiesPassesWithContext() throws CardNotFoundException {
		// 6 copies should pass with context (right at the limit)
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 6, false);
		// What the modifier itself tells deck validation, as a league hands it over for the player who has it
		var context = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		var errors = dvs.validate(deck, context);
		assertFalse("6 copies should pass with context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void SevenCopiesFailsEvenWithContext() throws CardNotFoundException {
		// 7 copies should fail even with context (exceeds 6)
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 7, false);
		// What the modifier itself tells deck validation, as a league hands it over for the player who has it
		var context = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		var errors = dvs.validate(deck, context);
		assertTrue("7 copies should fail even with context", dvs.hasMaxSameNameError(errors));
	}

	@Test
	public void FourCopiesPassesBothWays() throws CardNotFoundException {
		// 4 copies should pass with or without context
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithCopies("1_248", 4, false);
		// What the modifier itself tells deck validation, as a league hands it over for the player who has it
		var context = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		var errorsWithout = dvs.validate(deck);
		var errorsWith = dvs.validate(deck, context);

		assertFalse("4 copies should pass without context", dvs.hasMaxSameNameError(errorsWithout));
		assertFalse("4 copies should pass with context", dvs.hasMaxSameNameError(errorsWith));
	}

	@Test
	public void SixCopiesOfAFreePeoplesCardPassAndSevenFail() throws CardNotFoundException {
		// "Any card": the other tests use a Shadow event; Gimli, Son of Glóin 1_13 is a Free Peoples companion
		var dvs = GetScenario();
		var context = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		var six = dvs.buildDeckWithCopies("1_13", 6, true);
		assertTrue("6 copies should fail without context", dvs.hasMaxSameNameError(dvs.validate(six)));
		assertFalse("6 copies should pass with context", dvs.hasMaxSameNameError(dvs.validate(six, context)));

		var seven = dvs.buildDeckWithCopies("1_13", 7, true);
		assertTrue("7 copies should fail even with context", dvs.hasMaxSameNameError(dvs.validate(seven, context)));
	}

	@Test
	public void TheRingBearerIsOneOfTheSixCopies() throws CardNotFoundException {
		// The Ring-bearer is a copy of his card that is always in the starting fellowship, so the rules allow
		// one fewer of him in the draw deck: 3 where the limit is 4, and so 5 where it is 6.
		var dvs = GetScenario();
		var context = VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides();

		// Frodo 1_290 is the Ring-bearer of every deck this scenario builds
		var five = dvs.buildDeckWithCopies("1_290", 5, true);
		assertTrue("5 copies beside the Ring-bearer should fail without context", dvs.hasMaxSameNameError(dvs.validate(five)));
		assertFalse("5 copies beside the Ring-bearer should pass with context", dvs.hasMaxSameNameError(dvs.validate(five, context)));

		var six = dvs.buildDeckWithCopies("1_290", 6, true);
		assertTrue("6 copies beside the Ring-bearer should fail even with context", dvs.hasMaxSameNameError(dvs.validate(six, context)));
	}
}
