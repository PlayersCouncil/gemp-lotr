package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;


public class Card_01_018_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halls", "1_18");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; his fellowship ability is used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, cost 1, vitality 2; to play, spot a Dwarf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf, vitality 4

					put("runner", "1_178");     // Goblin Runner: a [moria] minion
					put("fear", "1_278");       // Strength Born of Fear: a [sauron] condition
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession
					put("farin", "1_11");       // Farin, Dwarven Emissary: a Free Peoples companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A table of five cards, so that each draw deck can be brought under three cards by moving a few to the
	// discard pile.  With the event in hand and Gimli in play the Free Peoples deck is a Goblin Runner, a
	// Dwarven Armor and a Strength Born of Fear; the Shadow deck is all five.
	protected VirtualTableScenario GetSmallDecksScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halls", "1_18");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("runner", "1_178");     // Goblin Runner: a [moria] minion
					put("fear", "1_278");       // Strength Born of Fear: a [sauron] condition
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HallsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Halls of My Home
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Fellowship: Exert a Dwarf to reveal the top 3 cards of any draw deck. You may discard 1 Shadow card revealed.
		 * Return the rest in any order.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("halls");

		assertEquals("Halls of My Home", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfToRevealTheTopThreeCardsOfTheOpponentsDeckAndDiscardsAShadowCardRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var armor = scn.GetShadowCard("armor");
		var fear = scn.GetShadowCard("fear");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// The Shadow player's deck, from the top: a Goblin Runner, his copy of Dwarven Armor, Strength Born of
		// Fear, and fourth an Uruk Savage.
		scn.MoveCardsToTopOfDeck(savage, fear, armor, runner);
		var shadowDeck = scn.GetShadowDeckCount();
		var freepsDeck = scn.GetFreepsDeckCount();
		var shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(halls));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(halls);
		// A Dwarf companion and a Dwarf ally may pay; Aragorn, who could exert, is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// Each deck has three cards to show, so the player says which.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("opponent's deck");

		// Both players are shown the top three, and not the fourth.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, false));
		assertFalse(scn.FreepsHasBPChoice(savage));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(fear, false));
		assertFalse(scn.ShadowHasBPChoice(savage));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("discard 1 Shadow card revealed"));
		scn.FreepsChooseYes();
		// A minion and a condition, of two cultures: either may go, and one only.  The Free Peoples card that
		// was revealed may not, nor may the Shadow card lying fourth.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, fear));
		assertFalse(scn.FreepsHasBPChoice(armor));
		assertFalse(scn.FreepsHasBPChoice(savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(fear);
		assertInZone(Zone.DISCARD, fear);
		assertEquals(1, scn.GetShadowDiscardCount());

		// The other two go back in the order the player of the event gives: the Runner first, so that the
		// Armor, which came off beneath it, ends on top.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, armor));
		scn.FreepsChooseCardBPFromSelection(runner);

		assertSame(armor, scn.GetShadowTopOfDeck());
		assertSame(runner, scn.GetFromTopOfShadowDeck(2));
		assertSame(savage, scn.GetFromTopOfShadowDeck(3));
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// The other deck is as it was, and the event is the one card in the other discard pile.
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, halls);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyMayPayAndAShadowCardRevealedFromThePlayersOwnDeckGoesToHisOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var runner = scn.GetFreepsCard("runner");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetFreepsCard("savage");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// The Free Peoples player's own deck, from the top: a Goblin Runner, a Dwarven Armor, Strength Born of
		// Fear, and fourth an Uruk Savage.
		scn.MoveCardsToTopOfDeck(savage, fear, armor, runner);
		var freepsDeck = scn.GetFreepsDeckCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();
		var shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsChooseOption("your deck");
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		scn.FreepsChooseYes();
		// Shadow cards of his own, a minion and a condition; the Free Peoples card that was revealed may not
		// go, nor the Shadow card lying fourth, which was not revealed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, fear));
		assertFalse(scn.FreepsHasBPChoice(armor));
		assertFalse(scn.FreepsHasBPChoice(savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(fear);
		assertInZone(Zone.DISCARD, fear);

		// The Armor goes back first, and the Runner on top of it.
		scn.FreepsChooseCardBPFromSelection(armor);
		assertSame(runner, scn.GetFreepsTopOfDeck());
		assertSame(armor, scn.GetFromTopOfFreepsDeck(2));
		assertSame(savage, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(freepsDeck - 1, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// The condition and the event
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningToDiscardReturnsAllThreeInTheOrderChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var armor = scn.GetShadowCard("armor");
		var fear = scn.GetShadowCard("fear");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// From the top: a Goblin Runner, a Dwarven Armor, Strength Born of Fear, and fourth an Uruk Savage.
		scn.MoveCardsToTopOfDeck(savage, fear, armor, runner);
		var shadowDeck = scn.GetShadowDeckCount();
		var shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChooseOption("opponent's deck");
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("discard 1 Shadow card revealed"));
		scn.FreepsChooseNo();
		assertInZone(Zone.DECK, runner, armor, fear);
		assertEquals(0, scn.GetShadowDiscardCount());

		// All three go back: the Armor first, then the Runner, and the condition, which came off last, on top.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, armor, fear));
		scn.FreepsChooseCardBPFromSelection(armor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, fear));
		assertFalse(scn.FreepsHasBPChoice(armor));
		scn.FreepsChooseCardBPFromSelection(runner);

		assertSame(fear, scn.GetShadowTopOfDeck());
		assertSame(runner, scn.GetFromTopOfShadowDeck(2));
		assertSame(armor, scn.GetFromTopOfShadowDeck(3));
		assertSame(savage, scn.GetFromTopOfShadowDeck(4));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShadowCardAmongTheThreeRevealedFromHisOwnDeckNoDiscardIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var farin = scn.GetFreepsCard("farin");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// From the top: a Dwarven Armor, a Dwarf Guard and Farin, and fourth, just out of reach, a Goblin Runner.
		scn.MoveCardsToTopOfDeck(runner, farin, guard, armor);
		var freepsDeck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChooseOption("your deck");
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// There is no Shadow card to discard, so the game goes straight on to putting the three back.
		assertFalse(scn.FreepsDecisionAvailable("discard 1 Shadow card revealed"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, guard, farin));
		scn.FreepsChooseCardBPFromSelection(farin);
		scn.FreepsChooseCardBPFromSelection(armor);

		assertSame(guard, scn.GetFreepsTopOfDeck());
		assertSame(armor, scn.GetFromTopOfFreepsDeck(2));
		assertSame(farin, scn.GetFromTopOfFreepsDeck(3));
		assertSame(runner, scn.GetFromTopOfFreepsDeck(4));
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		// The event alone
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShadowCardAmongTheThreeRevealedFromTheOpponentsDeckNoDiscardIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetShadowCard("armor");
		var guard = scn.GetShadowCard("guard");
		var farin = scn.GetShadowCard("farin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// The Shadow player's deck, from the top: his copies of Dwarven Armor, a Dwarf Guard and Farin, and
		// fourth, just out of reach, a Goblin Runner.
		scn.MoveCardsToTopOfDeck(runner, farin, guard, armor);
		var shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(halls);
		scn.FreepsChooseOption("opponent's deck");
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// There is no Shadow card to discard, so the game goes straight on to putting the three back.
		assertFalse(scn.FreepsDecisionAvailable("discard 1 Shadow card revealed"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, guard, farin));
		scn.FreepsChooseCardBPFromSelection(guard);
		scn.FreepsChooseCardBPFromSelection(farin);

		assertSame(armor, scn.GetShadowTopOfDeck());
		assertSame(farin, scn.GetFromTopOfShadowDeck(2));
		assertSame(guard, scn.GetFromTopOfShadowDeck(3));
		assertSame(runner, scn.GetFromTopOfShadowDeck(4));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoCardsInTheOpponentsDeckThePlayersOwnDeckIsRevealedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmallDecksScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetFreepsCard("runner");
		var armor = scn.GetFreepsCard("armor");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// The Shadow player's deck is left with a Goblin Runner and a Dwarven Armor: three cannot be revealed
		// from it.  The Free Peoples player's own, from the top: a Goblin Runner, a Dwarven Armor, Strength
		// Born of Fear.
		scn.MoveCardsToDiscard(scn.GetShadowCard("halls"), scn.GetShadowCard("gimli"), scn.GetShadowCard("fear"));
		scn.MoveCardsToTopOfDeck(fear, armor, runner);
		var shadowTop = scn.GetShadowTopOfDeck();
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(halls);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// A player may only choose what can be performed in full: his own deck it is, unasked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// He keeps both of his Shadow cards, and all three go back on his own deck: the condition first, then
		// the Runner, and the Armor on top.
		assertTrue(scn.FreepsDecisionAvailable("discard 1 Shadow card revealed"));
		scn.FreepsChooseNo();
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, armor, fear));
		scn.FreepsChooseCardBPFromSelection(fear);
		scn.FreepsChooseCardBPFromSelection(runner);

		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertSame(runner, scn.GetFromTopOfFreepsDeck(2));
		assertSame(fear, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(3, scn.GetFreepsDeckCount());
		// The event alone
		assertEquals(1, scn.GetFreepsDiscardCount());
		// The other deck was not touched; its owner's three were set aside before the event was played.
		assertSame(shadowTop, scn.GetShadowTopOfDeck());
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFewerThanThreeCardsInEachDeckBothAreOfferedAndWhatIsThereIsRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmallDecksScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var shadowFear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// Two cards in the Free Peoples deck (a Goblin Runner and a Dwarven Armor), one in the Shadow deck
		// (Strength Born of Fear).
		scn.MoveCardsToDiscard(scn.GetFreepsCard("fear"));
		scn.MoveCardsToDiscard(scn.GetShadowCard("halls"), scn.GetShadowCard("gimli"), scn.GetShadowCard("runner"), scn.GetShadowCard("armor"));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetShadowDeckCount());

		scn.FreepsPlayCard(halls);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Neither deck can show three, so the player has both to choose from and does what he can.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("opponent's deck");

		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowFear, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// The one card shown is a Shadow card: it is the one discarded, and there is no rest to put back.
		assertTrue(scn.FreepsDecisionAvailable("discard 1 Shadow card revealed"));
		scn.FreepsChooseYes();
		assertInZone(Zone.DISCARD, shadowFear);
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfIsExhaustedUntilADwarfWhoCanExertIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		// Gimli has 1 vitality left; Aragorn beside him could exert, and is no Dwarf.
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(halls, guard);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(halls));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsPlayAvailable(halls));

		// The Guard is the one Dwarf who can exert, and so he does.
		scn.FreepsPlayCard(halls);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
	}
}
