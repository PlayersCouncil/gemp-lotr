
package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("late", "1_87");
					put("gandalf", "1_72");
					put("bb", "1_70");
					put("filler", "1_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWizardIsNeverLateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: A Wizard Is Never Late
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Fellowship: Play a [GANDALF] character from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("late");

		assertEquals("A Wizard Is Never Late", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAGandalfCompanionFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bb = scn.GetFreepsCard("bb");
		scn.MoveCardsToHand(late);
		scn.MoveCardsToTopOfDeck(gandalf, bb);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(late));
		assertEquals(Zone.DECK, gandalf.getZone());
		assertEquals(Zone.DECK, bb.getZone());

		scn.FreepsPlayCard(late);
		assertTrue(scn.FreepsDecisionAvailable("You may inspect the contents of your deck before retrieving cards"));
		scn.FreepsDismissRevealedCards();
        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(gandalf);

		assertEquals(Zone.FREE_CHARACTERS, gandalf.getZone());
		assertEquals(Zone.DECK, bb.getZone());
	}

	@Test
	public void PlaysAGandalfAllyFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bb = scn.GetFreepsCard("bb");
		scn.MoveCardsToHand(late);
		scn.MoveCardsToTopOfDeck(gandalf, bb);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(late));
		assertEquals(Zone.DECK, gandalf.getZone());
		assertEquals(Zone.DECK, bb.getZone());

		scn.FreepsPlayCard(late);
		assertTrue(scn.FreepsDecisionAvailable("You may inspect the contents of your deck before retrieving cards"));
		scn.FreepsDismissRevealedCards();
        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(bb);

		assertEquals(Zone.DECK, gandalf.getZone());
		assertEquals(Zone.SUPPORT, bb.getZone());
	}

	@Test
	public void CanPlayWithNoGandalfCharactersInTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bb = scn.GetFreepsCard("bb");
		scn.MoveCardsToHand(late, gandalf, bb);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(late));
		assertEquals(Zone.HAND, gandalf.getZone());
		assertEquals(Zone.HAND, bb.getZone());

		scn.FreepsPlayCard(late);
		scn.FreepsDismissRevealedCards();
		assertEquals(Zone.DISCARD, late.getZone());
	}
}
