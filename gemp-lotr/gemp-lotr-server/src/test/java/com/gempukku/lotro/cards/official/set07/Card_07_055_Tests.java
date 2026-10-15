package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_55");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("days", "7_55");
					// Three Sméagols of three blueprints, so that a choice from the pile tells them apart.  Each
					// plays for a burden; nothing else of their text is reached.
					put("helps", "7_71");      // Sméagol, Always Helps: for the hand
					put("noser", "5_28");      // Sméagol, Old Noser: for the discard pile
					put("slinker", "5_29");    // Sméagol, Slinker: for the discard pile
					// Two more cards for the hand, which is discarded whole
					put("guard", "1_7");       // Dwarf Guard: [dwarven] companion
					put("armor", "1_8");       // Dwarven Armor: [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DaysGrowingDarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Days Growing Dark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Discard 3 cards from hand to play Sméagol from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Days Growing Dark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheSmeagolPlayedIsNotOneOfTheCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var days = scn.GetFreepsCard("days");
		var helps = scn.GetFreepsCard("helps");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(helps, guard, armor);
		scn.MoveCardsToDiscard(noser, slinker);

		scn.StartGame();

		assertInHand(helps, guard, armor);
		assertInDiscard(noser, slinker);
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(days));
		scn.FreepsUseCardAction(days);
		// Three cards in hand pay the discard of 3 without a choice
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(helps, guard, armor);

		// The two Sméagols that lay in the discard pile before the cost was paid, and not the one that lies there
		// now because it paid it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.FreepsHasCardChoiceAvailable(noser, slinker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(helps));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(noser);

		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(helps, slinker, guard, armor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DaysGrowingDarkTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
