package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_046_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("emhr", "54_46");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin: Dwarf ally, a maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion: a companion who is no Dwarf
					put("mountains", "52_1");   // Beneath the Mountains: another [dwarven] support condition, abilities used only on purpose
					// Cards to stack: no text that acts from a stack.
					put("guard2", "1_7");       // Dwarf Guard: a [dwarven] companion
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] possession
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion, no text
					// Draw deck fillers: no text at all.
					put("filler1", "1_177");    // Goblin Patrol Troop
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
					put("filler7", "1_177");
					put("filler8", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EverMyHeartRisesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Ever My Heart Rises
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Tale. When you play this condition, reveal the top 6 cards of your draw deck and stack them here.
		 * Fellowship: Spot a Dwarf and discard the top card of your draw deck to take a Free Peoples card stacked here into hand.
		 * Fellowship: Spot a Dwarf to discard the top 5 cards of your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("emhr");

		assertEquals("Ever My Heart Rises", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandForThreeAndStacksTheTopSixCardsOfYourDrawDeckHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var mountains = scn.GetFreepsCard("mountains");
		var f1 = scn.GetFreepsCard("filler1");
		var f2 = scn.GetFreepsCard("filler2");
		var f3 = scn.GetFreepsCard("filler3");
		var f4 = scn.GetFreepsCard("filler4");
		var f5 = scn.GetFreepsCard("filler5");
		var f6 = scn.GetFreepsCard("filler6");
		var f7 = scn.GetFreepsCard("filler7");
		scn.MoveCardsToHand(emhr, mountains);

		scn.StartGame();
		// f1 on top, f7 seventh.
		scn.MoveCardsToTopOfDeck(f7, f6, f5, f4, f3, f2, f1);
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(emhr));
		scn.FreepsPlayCard(emhr);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, emhr);
		// Exactly the top 6 of your own deck, stacked here; the seventh is now on top.
		assertEquals(6, scn.GetStackedCards(emhr).size());
		assertTrue(scn.GetStackedCards(emhr).containsAll(java.util.List.of(f1, f2, f3, f4, f5, f6)));
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertEquals(f7, scn.GetFreepsTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Playing another condition with this one in play stacks nothing more.
		scn.FreepsPlayCard(mountains);
		assertInZone(Zone.SUPPORT, mountains);
		assertEquals(6, scn.GetStackedCards(emhr).size());
		assertEquals(0, scn.GetStackedCards(mountains).size());
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakeAbilityDiscardsTheTopCardToTakeAFreePeoplesCardStackedHereIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var guard = scn.GetFreepsCard("guard");
		var mountains = scn.GetFreepsCard("mountains");
		var guard2 = scn.GetFreepsCard("guard2");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetFreepsCard("troop");
		var axe = scn.GetFreepsCard("axe");
		var f1 = scn.GetFreepsCard("filler1");
		var f2 = scn.GetFreepsCard("filler2");
		scn.MoveCardsToSupportArea(emhr, mountains);
		scn.MoveCompanionsToTable(guard);
		// Two Free Peoples cards of different culture and type and a Shadow card stacked here; a Free Peoples card
		// stacked on another card.
		scn.StackCardsOn(emhr, guard2, sword, troop);
		scn.StackCardsOn(mountains, axe);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(f2, f1);
		int deck = scn.GetFreepsDeckCount();
		int hand = scn.GetFreepsHandCount();

		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(emhr, "take"));
		scn.FreepsUseCardAction(emhr, "take");

		// The cost: the top card only.
		assertInDiscard(f1);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(f2, scn.GetFreepsTopOfDeck());

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, axe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		assertInHand(sword);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetStackedCards(emhr).size());
		assertTrue(scn.GetStackedCards(emhr).containsAll(java.util.List.of(guard2, troop)));
		assertTrue(scn.GetStackedCards(mountains).contains(axe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakeAbilityIsOfferedWithADwarfAllyAndDiscardsTheTopCardEvenWithOnlyAShadowCardStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop = scn.GetFreepsCard("troop");
		var f1 = scn.GetFreepsCard("filler1");
		scn.MoveCardsToSupportArea(emhr);
		scn.MoveAlliesToTable(thrarin);
		scn.StackCardsOn(emhr, troop);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(f1);
		int deck = scn.GetFreepsDeckCount();
		int hand = scn.GetFreepsHandCount();

		// The ally is the only Dwarf. The discard is a cost, so the ability is offered with nothing to take.
		assertTrue(scn.FreepsActionAvailable(emhr, "take"));
		scn.FreepsUseCardAction(emhr, "take");

		assertInDiscard(f1);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.GetStackedCards(emhr).contains(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NeitherAbilityIsOfferedWithNoDwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(emhr);
		// A companion who is no Dwarf, beside this condition, itself a [dwarven] card that is no Dwarf.
		scn.MoveCompanionsToTable(aragorn);
		scn.StackCardsOn(emhr, sword);

		scn.StartGame();

		assertTrue(scn.GetFreepsDeckCount() >= 5);
		assertFalse(scn.FreepsActionAvailable(emhr, "take"));
		assertFalse(scn.FreepsActionAvailable(emhr, "no effect"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakeAbilityIsNotOfferedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(emhr);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToFreepsHand("thrarin", "aragorn", "mountains", "guard2", "axe", "troop",
				"filler1", "filler2", "filler3", "filler4", "filler5", "filler6", "filler7", "filler8");
		scn.StackCardsOn(emhr, sword);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsActionAvailable(emhr, "take"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardAbilityAsksHowManyTimesAndDiscardsFiveCardsForEachFromTheTopOfDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var guard = scn.GetFreepsCard("guard");
		var f1 = scn.GetFreepsCard("filler1");
		var f2 = scn.GetFreepsCard("filler2");
		var f3 = scn.GetFreepsCard("filler3");
		var f4 = scn.GetFreepsCard("filler4");
		var f5 = scn.GetFreepsCard("filler5");
		var f6 = scn.GetFreepsCard("filler6");
		var f7 = scn.GetFreepsCard("filler7");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var mountains = scn.GetFreepsCard("mountains");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(emhr);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		// From the top: f1 to f7, then aragorn, thrarin, mountains, and the sword eleventh.
		scn.MoveCardsToTopOfDeck(sword, mountains, thrarin, aragorn, f7, f6, f5, f4, f3, f2, f1);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 15);
		int stacked = scn.GetStackedCards(emhr).size();

		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(emhr, "no effect"));
		scn.FreepsUseCardAction(emhr, "no effect");

		// The number of times is asked, from none up; nothing has been discarded yet.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChoose("2");

		// Twice 5: the top 10, in order, and the eleventh is now on top.
		assertEquals(deck - 10, scn.GetFreepsDeckCount());
		assertEquals(10, scn.GetFreepsDiscardCount());
		assertInDiscard(f1, f2, f3, f4, f5, f6, f7, aragorn, thrarin, mountains);
		assertEquals(sword, scn.GetFreepsTopOfDeck());
		assertEquals(stacked, scn.GetStackedCards(emhr).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Used again, one time: 5 more.
		assertTrue(scn.FreepsActionAvailable(emhr, "no effect"));
		scn.FreepsUseCardAction(emhr, "no effect");
		scn.FreepsChoose("1");
		assertEquals(deck - 15, scn.GetFreepsDeckCount());
		assertEquals(15, scn.GetFreepsDiscardCount());
		assertInDiscard(sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardAbilityWithADwarfAllyDiscardsAsManyCardsAsThereAreWhenThereAreFewer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCardsToSupportArea(emhr);
		scn.MoveAlliesToTable(thrarin);
		// Seven fillers left in the draw deck.
		scn.MoveCardsToFreepsHand("guard", "aragorn", "mountains", "guard2", "sword", "axe", "troop", "filler8");

		scn.StartGame();
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The ally is the only Dwarf.
		assertTrue(scn.FreepsActionAvailable(emhr, "no effect"));
		scn.FreepsUseCardAction(emhr, "no effect");
		// Twice 5 is 10 cards, and the deck has 7.
		scn.FreepsChoose("2");
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(7, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardAbilityIsNotOfferedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emhr = scn.GetFreepsCard("emhr");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(emhr);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToFreepsHand("thrarin", "aragorn", "mountains", "guard2", "sword", "axe", "troop",
				"filler1", "filler2", "filler3", "filler4", "filler5", "filler6", "filler7", "filler8");

		scn.StartGame();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsActionAvailable(emhr, "no effect"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
