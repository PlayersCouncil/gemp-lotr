package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * The placeholder {stored} in a definition's text, and the definition word storeRace.
 *
 * A card that names something as it is played ("name a race") keeps the name on itself for as long as it stays
 * where it is.  A placeholder such as {chosenRace} reads the memory of the action that is asking, and the action
 * that named the race is long over when a later one asks: the question read "... to a NONE?" (F-0307, Ulaire
 * Nertea 69_38).  {stored} reads the card instead, in the words its displayableInformation shows, so any text of
 * the card can name what it stored.  storeRace stores the race a memory holds with its printed word beside it
 * ("Uruk-Hai", where storeWhileInZone keeps only "URUK_HAI"), as storeCulture and storeKeyword do for theirs.
 *
 * Shown with a card of the tests' own, Test Herald 0_8020: "When you play this condition, name a race.
 * Fellowship: You may add (1) for the named race."
 */
public class StoredValueInATextTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Herald (the tests' own card): names a race when played; its Fellowship ability asks
					// "Would you like to add (1) for the named race ({stored})?"
					put("herald", "0_8020");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AQuestionOfALaterActionNamesTheRaceTheCardStoredInItsPrintedWords() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herald = scn.GetFreepsCard("herald");
		scn.MoveCardsToHand(herald);

		scn.StartGame();

		scn.FreepsPlayCard(herald);
		assertTrue(scn.FreepsDecisionAvailable("Choose a race"));
		scn.FreepsChoose("Uruk-Hai");

		assertInZone(Zone.SUPPORT, herald);
		assertEquals("Named race is Uruk-Hai", scn.GetDisplayableInformation(herald));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(herald));

		// Another action than the one that named the race: its question reads the card
		scn.FreepsUseCardAction(herald);

		assertTrue(scn.FreepsDecisionAvailable("for the named race (Uruk-Hai)?"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardThatHasStoredNothingNamesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Put in play without being played, so no race was named
		var herald = scn.GetFreepsCard("herald");
		scn.MoveCardsToSupportArea(herald);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(herald));
		scn.FreepsUseCardAction(herald);

		assertTrue(scn.FreepsDecisionAvailable("for the named race (nothing)?"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
