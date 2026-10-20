package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_12");

					// Cards to hold and to place. None of them is played.
					put("lord", "1_21");          // Lord of Moria: Free Peoples condition
					put("armor", "1_8");          // Dwarven Armor: Free Peoples possession
					put("strike", "1_3");         // Axe Strike: Free Peoples event
					put("runner", "1_178");       // Goblin Runner: Shadow minion
					put("troop", "1_177");        // Goblin Patrol Troop: Shadow minion
					put("savage", "1_151");       // Uruk Savage: Shadow minion
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gimli, Dwarf of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Fellowship:</b> If the twilight pool has fewer than 2 twilight tokens, add (2) to place a card from hand
		 * 	beneath your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Dwarf of Erebor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForTwoWhichLeavesThePoolTooFullForHisAbilityUntilItIsBelowTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		// A card to place: only the pool stands in the ability's way.
		scn.MoveCardsToHand(gimli, lord);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetVitality(gimli));

		// Exactly 2 twilight tokens are not fewer than 2.
		assertInZone(Zone.HAND, lord);
		assertFalse(scn.FreepsActionAvailable(gimli));

		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtOneTwilightTokenAddsTwoAndPlacesTheChosenCardFromHandBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		var armor = scn.GetFreepsCard("armor");
		var strike = scn.GetFreepsCard("strike");
		var runner = scn.GetFreepsCard("runner");
		var troop = scn.GetFreepsCard("troop");
		var savage = scn.GetFreepsCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli);
		// In hand: a condition, a possession, an event and a Shadow minion. Not in hand: Aragorn in the discard
		// pile, Gimli in play, two minions in the draw deck.
		scn.MoveCardsToHand(lord, armor, strike, runner);
		scn.MoveCardsToDiscard(aragorn);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(troop);
		scn.MoveCardsToBottomOfDeck(savage);
		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetTwilight());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertSame(savage, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.FreepsActionAvailable(gimli));

		scn.FreepsUseCardAction(gimli);
		// The (2) is the cost: it is in the pool by the time the card is chosen.
		assertEquals(3, scn.GetTwilight());   // 1 + 2
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, armor, strike, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, troop, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(runner);
		assertEquals(3, scn.GetTwilight());
		// The Runner lies under the card that was the bottom one. Nothing is drawn: the top card is where it was.
		assertInZone(Zone.DECK, runner, savage, troop);
		assertSame(runner, scn.GetFreepsBottomOfDeck());
		assertSame(savage, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.HAND, lord, armor, strike);
		assertEquals(3, scn.GetFreepsHandCount());   // 4 - 1 placed
		assertEquals(3, scn.GetFreepsDeckCount());   // 2 + 1 placed
		assertInZone(Zone.DISCARD, aragorn);

		// 3 twilight tokens: the ability has shut itself, with cards still in hand to place.
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAnEmptyPoolTheOnlyCardInHandIsPlacedBeneathTheDrawDeckWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetFreepsCard("runner");
		var troop = scn.GetFreepsCard("troop");
		var savage = scn.GetFreepsCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(troop);
		scn.MoveCardsToBottomOfDeck(savage);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertSame(savage, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.FreepsActionAvailable(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DECK, runner);
		assertSame(runner, scn.GetFreepsBottomOfDeck());
		assertSame(savage, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck + 1, scn.GetFreepsDeckCount());

		assertFalse(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCardInHandTheTwoTwilightTokensAreAddedAndNothingIsPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetFreepsCard("troop");
		var savage = scn.GetFreepsCard("savage");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(troop);
		scn.MoveCardsToBottomOfDeck(savage);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		// The (2) can be paid, so the ability is on offer though there is no card to place.
		assertTrue(scn.FreepsActionAvailable(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertSame(savage, scn.GetFreepsBottomOfDeck());
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
