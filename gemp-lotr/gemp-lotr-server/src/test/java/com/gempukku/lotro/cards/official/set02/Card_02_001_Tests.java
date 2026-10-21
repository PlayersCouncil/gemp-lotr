package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mountains", "2_1");

					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no text but its own play requirement
					put("thorin", "30_19");     // Thorin, Oakenshield: Dwarf companion, the only bearer of Orcrist; his ability needs a [dwarven] follower
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Dwarf

					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon possession, cost 0, bearer must be a Dwarf
					put("battleaxe", "7_9");    // Gimli's Battle Axe, Trusted Weapon: unique [dwarven] hand weapon possession, cost 2, bearer must be a Dwarf
					put("orcrist", "30_17");    // Orcrist: the one [dwarven] weapon that is an artifact (The Hobbit), bearer must be Thorin
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
		 * Game Text: <b>Tale</b>. Plays to your support area.<br><b>Fellowship:</b> Discard the top 3 cards from your draw deck to play a [dwarven] weapon from your discard pile.
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
		assertEquals(filler1, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(filler2, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(filler3, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(filler4, scn.GetFromTopOfFreepsDeck(4));
		assertInZone(Zone.DECK, filler1, filler2, filler3, filler4);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(battleaxe);
		assertTrue(scn.FreepsActionAvailable(card));

		// The battle axe is the only weapon there and the guard the only Dwarf, so neither is asked.
		scn.FreepsUseCardAction(card);

		// Exactly the top 3 cards went, and the fourth is now on top.
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInDiscard(filler1, filler2, filler3);
		assertEquals(filler4, scn.GetFreepsTopOfDeck());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertAttachedTo(battleaxe, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WeaponAbilityOffersOnlyDwarvenWeaponsAlreadyInYourDiscardPileAndNotOneTheCostDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		var orcrist = scn.GetFreepsCard("orcrist");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		var handaxe = scn.GetShadowCard("handaxe");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard, thorin);
		// Armor could go on a Dwarf and the sword on Frodo: each is playable, and neither is a [dwarven] weapon.
		scn.MoveCardsToDiscard(axe, orcrist, armor, sword);
		// A [dwarven] weapon in the other player's discard pile.
		scn.MoveCardsToDiscard(handaxe);

		scn.StartGame();
		// The battle axe is the second card down, so the cost itself puts it in the discard pile.
		scn.MoveCardsToTopOfDeck(filler2, battleaxe, filler1);
		scn.FreepsRefreshDecision();

		assertInDiscard(axe, orcrist, armor, sword, handaxe);
		assertEquals(filler1, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(battleaxe, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(filler2, scn.GetFromTopOfFreepsDeck(3));
		assertInZone(Zone.DECK, filler1, battleaxe, filler2);
		scn.FreepsUseCardAction(card);

		// A possession and an artifact that were there before the action began; not the battle axe the cost has
		// just discarded (F-0548: A, CR 4.2 "effect": a card played from the discard pile must be there before the
		// action begins).
		assertInDiscard(filler1, battleaxe, filler2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, orcrist));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(battleaxe, armor, sword, handaxe));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCardBPFromSelection(orcrist);

		// Thorin is the one character who may bear Orcrist.
		assertAttachedTo(orcrist, thorin);
		assertInDiscard(axe, battleaxe, armor, sword);
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
		scn.MoveCardsToFreepsHand("thorin", "aragorn", "battleaxe", "orcrist", "handaxe", "armor", "sword", "filler1", "filler2");

		scn.StartGame();

		assertEquals(2, scn.GetFreepsDeckCount());
		assertInDiscard(axe);
		// The weapon and its bearer are there; only the cost cannot be paid in full.
		assertFalse(scn.FreepsActionAvailable(card));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WeaponAbilityIsNotOfferedWithoutADwarvenWeaponInDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		var handaxe = scn.GetShadowCard("handaxe");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(guard);
		// Playable from the discard pile, but not [dwarven] weapons.
		scn.MoveCardsToDiscard(armor, sword);
		// A [dwarven] weapon in the other player's discard pile, and one in hand.
		scn.MoveCardsToDiscard(handaxe);
		scn.MoveCardsToHand(axe);

		scn.StartGame();

		assertTrue(scn.GetFreepsDeckCount() >= 3);
		assertInDiscard(armor, sword, handaxe);
		assertInHand(axe);
		assertFalse(scn.FreepsActionAvailable(card));
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
		assertInDiscard(axe);
		assertFalse(scn.FreepsActionAvailable(card));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
