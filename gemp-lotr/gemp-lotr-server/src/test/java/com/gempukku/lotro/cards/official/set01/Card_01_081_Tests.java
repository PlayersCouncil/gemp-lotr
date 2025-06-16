package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_01_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("questions", "1_81");
					put("gandalf", "1_364");

					put("chaff1", "1_91");
					put("chaff2", "1_92");
					put("chaff3", "1_93");
					put("chaff4", "1_94");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void QuestionsThatNeedAnsweringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Questions That Need Answering
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> If the twilight pool has fewer than 3 twilight tokens, spot Gandalf to look at the top 4 cards of your draw deck. Take 2 of those cards into hand and discard the rest.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("questions");

		assertEquals("Questions That Need Answering", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void QuestionsThatNeedAnsweringTakes2CardsIntoHandAndDiscardsTheRest() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var questions = scn.GetFreepsCard("questions");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(questions);
		scn.MoveCompanionToTable(gandalf);

		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		var chaff4 = scn.GetFreepsCard("chaff4");
		scn.MoveCardsToTopOfDeck(chaff1, chaff2, chaff3, chaff4);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(questions));
		scn.FreepsPlayCard(questions);
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, chaff1, chaff2, chaff3, chaff4);
		assertTrue(scn.FreepsHasCardChoicesAvailable(chaff1, chaff2, chaff3,  chaff4));

		scn.FreepsChooseCards(chaff3, chaff4);
		assertInZone(Zone.HAND, chaff3, chaff4);
		assertInZone(Zone.DISCARD, chaff1, chaff2);

	}
}
