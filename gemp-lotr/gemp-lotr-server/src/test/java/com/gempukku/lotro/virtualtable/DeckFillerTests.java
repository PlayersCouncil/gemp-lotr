package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the filler DeckValidationScenario puts round the cards a test names is never what a
 * format objects to, at any size.  It used to be at its 37th card a side: Boromir 1_97 follows Boromir 1_96 in the
 * Free Peoples range, five of one title, and the Shadow range reaches Forces of Mordor 1_248, which Multipath
 * X-lists and Expanded R-lists.  A deck-size test of 99 or 100 cards could then read only the one objection it was
 * about, never that nothing else was said.
 */
public class DeckFillerTests
{
	// Every line of what a format said against a deck: one objection may hold several lines.
	private static List<String> lines(List<String> errors) {
		return errors.stream()
				.flatMap(error -> Arrays.stream(error.split("\n")))
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.sorted()
				.collect(Collectors.toList());
	}

	@Test
	public void InAFormatThatTakesTheSitesADeckOfFillerAloneDrawsNoObjectionAtAnySize() {
		var dvs = new DeckValidationScenario(VirtualTableScenario.Fellowship);

		for (int aSide : new int[] { 30, 36, 37, 50, 100 }) {
			var deck = dvs.buildDeck(null, null, aSide, aSide);
			assertEquals(aSide * 2, deck.getDrawDeckCards().size());
			assertEquals("At " + aSide + " cards a side", List.of(), dvs.validate(deck));
		}
	}

	@Test
	public void InMultipathTheOnlyObjectionsAreToTheTwoSitesItXLists() {
		var dvs = new DeckValidationScenario();

		for (int aSide : new int[] { 30, 37, 50, 100 }) {
			var errors = dvs.validate(dvs.buildDeck(null, null, aSide, aSide));
			// Emyn Muil 1_360 and Ettenmoors 1_331 of the rig's FellowshipSites; nothing against the filler,
			// Forces of Mordor 1_248 included.
			assertEquals("At " + aSide + " cards a side",
					List.of("Card is X-listed: Emyn Muil", "Card is X-listed: Ettenmoors"), lines(errors));
			assertFalse(dvs.hasMaxSameNameError(errors));
		}
	}

	@Test
	public void InExpandedTheFillerHoldsNoCardOfWhichFewerThanFourCopiesAreAllowed() {
		var dvs = new DeckValidationScenario(VirtualTableScenario.Shadows);

		for (int aSide : new int[] { 30, 50, 100 }) {
			var errors = dvs.validate(dvs.buildDeck(null, null, aSide, aSide));
			// Expanded has its own word on the Fellowship sites of these decks, and none on the filler: Forces of
			// Mordor 1_248 and No Stranger to the Shadows 1_108 are on its R-list, one copy to a deck.
			assertFalse("At " + aSide + " cards a side: " + errors, lines(errors).stream().anyMatch(line -> line.contains("R-listed")));
			assertFalse(dvs.hasMaxSameNameError(errors));
			assertTrue(lines(errors).stream().allMatch(line -> line.startsWith("Site does not use supported block")
					|| line.contains("Adventure deck") || line.contains("site")));
		}
	}
}
