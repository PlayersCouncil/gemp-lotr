package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ottar", "1_80");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: companion, cost 4
					put("barliman", "1_70");      // Barliman Butterbur, Prancing Pony Proprietor: [gandalf] Man ally who is not Gandalf; a fellowship ability used only on purpose

					// Cards held in hand: a companion, a possession, an event and two Shadow minions.
					put("legolas", "1_50");       // Legolas, Greenleaf
					put("bow", "1_41");           // Elven Bow
					put("pathfinder", "1_110");   // Pathfinder
					put("troop", "1_177");        // Goblin Patrol Troop
					put("runner", "1_178");       // Goblin Runner

					// Cards put on top of the draw deck, to be read by name once drawn.
					put("aragorn", "1_89");       // Aragorn, Ranger of the North
					put("gimli", "1_13");         // Gimli, Son of Gloin
					put("savage", "1_151");       // Uruk Savage
					put("marksman", "1_176");     // Goblin Marksman
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OttarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ottar, Man of Laketown
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot Gandalf.
		 * 	<b>Fellowship:</b> Exert Ottar to discard up to 3 cards from hand and draw an equal number of cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ottar");

		assertEquals("Ottar", card.getBlueprint().getTitle());
		assertEquals("Man of Laketown", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpottedThenIsPlayedForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(ottar, gandalf);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();

		// Barliman is a [gandalf] card, but he is not Gandalf.
		assertFalse(scn.FreepsPlayAvailable(ottar));
		assertTrue(scn.FreepsPlayAvailable(gandalf));

		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(ottar));

		scn.FreepsPlayCard(ottar);
		assertEquals(5, scn.GetTwilight()); // 4 for Gandalf + 1 for Ottar
		assertInZone(Zone.SUPPORT, ottar);
		assertEquals(2, scn.GetStrength(ottar));
		assertEquals(2, scn.GetVitality(ottar));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsOttarToDiscardThreeChosenCardsFromHandAndDrawThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var barliman = scn.GetFreepsCard("barliman");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var troop = scn.GetFreepsCard("troop");
		var runner = scn.GetFreepsCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetFreepsCard("savage");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveAlliesToTable(ottar, barliman);
		scn.MoveCardsToHand(legolas, bow, pathfinder, troop, runner);
		scn.MoveCardsToShadowHand("legolas", "troop");

		scn.StartGame();
		// The Goblin Marksman is the top card of the deck, then the Uruk Savage, Gimli and Aragorn.
		scn.MoveCardsToTopOfDeck(aragorn, gimli, savage, marksman);

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(ottar));

		scn.FreepsUseCardAction(ottar);
		// Ottar exerts, and Barliman, the other ally, does not.
		assertEquals(1, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetWoundsOn(barliman));

		// Any card in hand may go, of either side and of any type: none of them need, and no more than 3 may.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, bow, pathfinder, troop, runner));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, pathfinder, troop);

		// 3 discarded and 3 drawn: the three cards from the top of the deck, and not the fourth.
		assertInDiscard(legolas, pathfinder, troop);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInHand(bow, runner, marksman, savage, gimli);
		assertEquals(5, scn.GetFreepsHandCount()); // 5 - 3 discarded + 3 drawn
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertSame(aragorn, scn.GetFreepsTopOfDeck());

		// The hand and the deck are the player's own.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsOneChosenCardAndDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var troop = scn.GetFreepsCard("troop");
		var savage = scn.GetFreepsCard("savage");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToHand(legolas, bow, pathfinder, troop);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(savage, marksman);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(ottar);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(bow);

		assertEquals(1, scn.GetWoundsOn(ottar));
		assertInDiscard(bow);
		assertEquals(1, scn.GetFreepsDiscardCount());
		// One card drawn for the one discarded: the top card, and the next stays on the deck.
		assertInHand(legolas, pathfinder, troop, marksman);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(savage, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChoosingNoCardStillExertsOttarAndNothingIsDiscardedOrDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToHand(legolas, bow);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(marksman);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(ottar));

		scn.FreepsUseCardAction(ottar);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, bow));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		// "Up to 3" may be none. The exertion was the price, and is paid all the same.
		assertEquals(1, scn.GetWoundsOn(ottar));
		assertInHand(legolas, bow);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertSame(marksman, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyHandOttarStillExertsAndNothingIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveAlliesToTable(ottar);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 0);
		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetFreepsHandCount());
		// The exertion can be paid, so the ability is there to be used, for nothing.
		assertTrue(scn.FreepsActionAvailable(ottar));

		scn.FreepsUseCardAction(ottar);
		assertEquals(1, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileOttarIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToHand(legolas, bow);

		scn.StartGame();
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(ottar, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.GetFreepsDeckCount() > 0);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ottar));

		scn.RemoveWoundsFromChar(ottar, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(ottar));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
