package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_001_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mountains", "52_1");

					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin: Dwarf ally, a maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Dwarf

					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon, cost 0, bearer must be a Dwarf
					put("battleaxe", "7_9");    // Gimli's Battle Axe, Trusted Weapon: unique [dwarven] hand weapon, cost 2, bearer must be a Dwarf
					put("handaxe", "2_10");     // Hand Axe: [dwarven] hand weapon, cost 0, bearer must be a Dwarf
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession that is not a weapon, cost 0, bearer must be a Dwarf
					put("sword", "1_299");      // Hobbit Sword: [shire] hand weapon, cost 1, bearer must be a Hobbit (Frodo)

					// Draw deck fillers: no text at all.
					put("filler1", "1_177");    // Goblin Patrol Troop
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeneaththeMountainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Beneath the Mountains
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Tale.
		 * Fellowship: Discard the top 3 cards from your draw deck to play a [dwarven] weapon from your discard pile.
		 * Fellowship: Spot a Dwarf to discard the top 3 cards from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");

		assertEquals("Beneath the Mountains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardingTheTopThreeCardsPlaysADwarvenWeaponFromDiscardPileOnADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToDiscard(battleaxe);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(filler4, filler3, filler2, filler1);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(card, "play a"));

		// The battle axe is the only weapon there and the guard the only Dwarf, so neither is asked.
		scn.FreepsUseCardAction(card, "play a");

		// Exactly the top 3 cards went, and the fourth is now on top.
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInDiscard(filler1, filler2, filler3);
		assertEquals(filler4, scn.GetFreepsTopOfDeck());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertAttachedTo(battleaxe, guard);
		// The weapon was played, so its cost of 2 was paid.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WeaponAbilityOffersOnlyDwarvenWeaponsInYourDiscardPileIncludingOneTheCostDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		var handaxe = scn.GetShadowCard("handaxe");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);
		// Armor could go on the guard and the sword on Frodo: each is playable, and neither is a [dwarven] weapon.
		scn.MoveCardsToDiscard(axe, armor, sword);
		// A [dwarven] weapon in the other player's discard pile.
		scn.MoveCardsToDiscard(handaxe);

		scn.StartGame();
		// The battle axe is the second card down, so the cost itself puts it in the discard pile.
		scn.MoveCardsToTopOfDeck(filler2, battleaxe, filler1);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(card, "play a");

		assertInDiscard(filler1, battleaxe, filler2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, battleaxe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, sword, handaxe));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCardBPFromSelection(battleaxe);

		assertAttachedTo(battleaxe, guard);
		assertInDiscard(axe, armor, sword);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WeaponAbilityIsNotOfferedWithOnlyTwoCardsInDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToDiscard(axe);
		// Everything else but two fillers out of the draw deck.
		scn.MoveCardsToFreepsHand("thrarin", "aragorn", "battleaxe", "handaxe", "armor", "sword", "filler1", "filler2");

		scn.StartGame();

		assertEquals(2, scn.GetFreepsDeckCount());
		// The weapon and its bearer are there; only the cost cannot be paid.
		assertFalse(scn.FreepsActionAvailable(card, "play a"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WeaponAbilityIsNotOfferedWithoutADwarvenWeaponInDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);
		// Playable from the discard pile, but not [dwarven] weapons.
		scn.MoveCardsToFreepsDiscard("armor", "sword");
		// A [dwarven] weapon in the other player's discard pile, and one in hand.
		scn.MoveCardsToShadowDiscard("handaxe");
		scn.MoveCardsToFreepsHand("axe");

		scn.StartGame();

		assertTrue(scn.GetFreepsDeckCount() >= 3);
		assertFalse(scn.FreepsActionAvailable(card, "play a"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WeaponAbilityIsNotOfferedWhenNoDwarfCanBearTheWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(axe);

		scn.StartGame();

		assertTrue(scn.GetFreepsDeckCount() >= 3);
		assertFalse(scn.FreepsActionAvailable(card, "play a"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarfAbilityAsksHowManyTimesAndDiscardsThreeCardsForEachFromTheTopOfDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		// From the top: filler1 to aragorn, then thrarin, then the armor, which no discard in this test reaches.
		scn.MoveCardsToTopOfDeck(armor, thrarin, aragorn, filler4, filler3, filler2, filler1);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(card, "no effect"));

		scn.FreepsUseCardAction(card, "no effect");

		// The number of times is asked, from none up; nothing has been discarded yet.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChoose("2");

		// Twice 3: the top 6 cards, in order, and the seventh is now on top.
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertInDiscard(filler1, filler2, filler3, filler4, aragorn, thrarin);
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Used again, one time: 3 more.
		assertTrue(scn.FreepsActionAvailable(card, "no effect"));
		scn.FreepsUseCardAction(card, "no effect");
		scn.FreepsChoose("1");

		assertEquals(deck - 9, scn.GetFreepsDeckCount());
		assertEquals(9, scn.GetFreepsDiscardCount());
		assertInDiscard(armor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarfAbilityDiscardsAsManyCardsAsThereAreWhenThereAreFewerThanThreeForEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);
		// Everything but the four fillers out of the draw deck.
		scn.MoveCardsToFreepsHand("thrarin", "aragorn", "axe", "battleaxe", "handaxe", "armor", "sword");

		scn.StartGame();

		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(card, "no effect"));

		scn.FreepsUseCardAction(card, "no effect");
		assertEquals(0, scn.FreepsGetChoiceMin());
		// The number has no cap, whatever the deck holds.
		assertEquals(Integer.MAX_VALUE, scn.FreepsGetChoiceMax());
		// 5 times 3 is 15 cards, and the deck has 4.
		scn.FreepsChoose("5");

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertInDiscard(filler1, filler2, filler3, filler4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarfAbilityIsNotOfferedWithoutADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(card);
		// A companion who is not a Dwarf, beside this [dwarven] card.
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertTrue(scn.GetFreepsDeckCount() >= 3);
		assertFalse(scn.FreepsActionAvailable(card, "no effect"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarfAbilityIsOfferedWhenTheOnlyDwarfIsAnAllyAndOneCardIsLeftInDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCardsToSupportArea(card);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToFreepsHand("guard", "aragorn", "axe", "battleaxe", "handaxe", "armor", "sword", "filler1", "filler2", "filler3");

		scn.StartGame();

		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsActionAvailable(card, "no effect"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarfAbilityIsNotOfferedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToFreepsHand("thrarin", "aragorn", "axe", "battleaxe", "handaxe", "armor", "sword", "filler1", "filler2", "filler3", "filler4");

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsActionAvailable(card, "no effect"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
