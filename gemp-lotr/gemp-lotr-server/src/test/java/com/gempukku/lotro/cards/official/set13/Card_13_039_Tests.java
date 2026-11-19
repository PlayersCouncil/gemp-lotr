package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("return", "13_39");
					// Shadow cards to discard from hand, one of each kind: a minion, a condition and an event
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("weary", "4_212");      // Weary: an [isengard] condition
					put("bred", "1_121");       // Bred for Battle: an [isengard] Skirmish event
					// Defiance: an [elven] Skirmish event, a Free Peoples card in hand
					put("defiance", "1_37");
					// [gandalf] events to take back: a Fellowship one and a Skirmish one
					put("sleep", "1_84");       // Sleep, Caradhras: a [gandalf] Fellowship event
					put("mysterious", "1_78");  // Mysterious Wizard: a [gandalf] Skirmish event
					// The Nine Walkers: a [gandalf] condition, no event
					put("walkers", "1_79");
					// Elf-song: an [elven] Fellowship event
					put("elfsong", "1_39");
					// Servant of the Secret Fire: a [gandalf] event, laid in the other player's discard pile
					put("servant", "1_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReturntoUsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Return to Us
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Discard 2 Shadow cards from hand to take a [gandalf] event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("return");

		assertEquals("Return to Us", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsTwoShadowCardsFromHandToTakeAGandalfEventFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ret = scn.GetFreepsCard("return");
		var troop = scn.GetFreepsCard("troop");
		var weary = scn.GetFreepsCard("weary");
		var bred = scn.GetFreepsCard("bred");
		var defiance = scn.GetFreepsCard("defiance");
		var sleep = scn.GetFreepsCard("sleep");
		var mysterious = scn.GetFreepsCard("mysterious");
		var walkers = scn.GetFreepsCard("walkers");
		var elfsong = scn.GetFreepsCard("elfsong");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(ret, troop, weary, bred, defiance);
		scn.MoveCardsToDiscard(sleep, mysterious, walkers, elfsong, servant);

		scn.StartGame();

		assertInHand(ret, troop, weary, bred, defiance);
		assertInDiscard(sleep, mysterious, walkers, elfsong, servant);
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(ret));
		scn.FreepsPlayCard(ret);

		// Shadow cards of three kinds; not the Free Peoples card, nor the event being played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, weary, bred));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(defiance, ret));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(troop, weary);
		assertInDiscard(troop, weary);

		// Either [gandalf] event; not the [gandalf] condition, the [elven] event, the [gandalf] event in the other
		// player's discard pile, nor Return to Us itself, which is not yet in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, mysterious));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(walkers, elfsong, servant, ret));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(mysterious);

		assertInHand(mysterious, bred, defiance);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInDiscard(ret, sleep, walkers, elfsong, servant, troop, weary);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneShadowCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ret = scn.GetFreepsCard("return");
		var troop = scn.GetFreepsCard("troop");
		var defiance = scn.GetFreepsCard("defiance");
		var elfsong = scn.GetFreepsCard("elfsong");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToHand(ret, troop, defiance, elfsong);
		scn.MoveCardsToDiscard(sleep);

		scn.StartGame();

		// Two cards to discard besides the event, but only one of them a Shadow card.
		assertInHand(ret, troop, defiance, elfsong);
		assertInDiscard(sleep);
		assertFalse(scn.FreepsPlayAvailable(ret));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfEventInTheDiscardPileTheShadowCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ret = scn.GetFreepsCard("return");
		var troop = scn.GetFreepsCard("troop");
		var weary = scn.GetFreepsCard("weary");
		var walkers = scn.GetFreepsCard("walkers");
		var elfsong = scn.GetFreepsCard("elfsong");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(ret, troop, weary);
		scn.MoveCardsToDiscard(walkers, elfsong, servant);

		scn.StartGame();

		assertInHand(ret, troop, weary);
		assertInDiscard(walkers, elfsong, servant);
		assertEquals(3, scn.GetFreepsHandCount());
		// The take stands after "to": with nothing it could take, the event is still played.
		assertTrue(scn.FreepsPlayAvailable(ret));
		scn.FreepsPlayCard(ret);

		// Exactly two Shadow cards in hand: both are discarded unasked, and nothing comes back.
		assertInDiscard(ret, troop, weary, walkers, elfsong, servant);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
