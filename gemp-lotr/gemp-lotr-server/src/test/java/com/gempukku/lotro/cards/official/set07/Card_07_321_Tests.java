package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_321_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_321");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North
					put("rider", "4_286");        // Rider of Rohan: [rohan] Man
					put("spear", "4_288");        // Rider's Spear: [rohan] possession, bearer must be a [rohan] Man
					put("armor", "1_92");         // Armor: [gondor] possession, bearer must be a Man
					put("hall", "11_149");        // Protecting the Hall: [rohan] condition, bearer must be a [rohan] Man (toil 2)
					put("troop", "1_177");        // Goblin Patrol Troop
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPossessionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_321");
					put("guard", "5_83");         // Household Guard: [rohan] Man companion, Valiant is his whole text
					put("knight", "5_35");        // Gondorian Knight: [gondor] Man companion, Knight is his whole text
					// [rohan] possessions of three item classes: a hand weapon and a shield for a [rohan] Man,
					// and armor any Man may bear
					put("spear", "4_288");        // Rider's Spear: damage +1 only while bearer is mounted
					put("shield", "5_91");        // Rohirrim Shield: minion archery total -1; a regroup ability
					put("chain", "4_278");        // Heavy Chain: bearer must be a Man; an overwhelm bar
					// [rohan] and no possession: a condition borne by a [rohan] Man (toil 2)
					put("hall", "11_149");        // Protecting the Hall
					// A possession a Man may bear, of another culture
					put("armor", "1_92");         // Armor: [gondor] possession, armor, bearer must be a Man
					// A [rohan] possession of a title that only the Shadow player's discard pile holds
					put("sword", "4_291");        // Sword of Rohan: bearer must be a [rohan] Man
					put("runner", "1_178");       // Goblin Runner: strength 5; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreeSkirmishesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_321");
					put("guard", "5_83");         // Household Guard: [rohan] Man companion, strength 6
					put("knight", "5_35");        // Gondorian Knight: [gondor] Man companion, strength 6
					put("spear", "4_288");        // Rider's Spear: [rohan] possession, bearer must be a [rohan] Man
					put("runner1", "1_178");      // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUnplayableScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_321");
					put("knight", "5_35");        // Gondorian Knight: [gondor] Man companion, Knight is his whole text
					put("spear", "4_288");        // Rider's Spear: bearer must be a [rohan] Man
					put("merrysarmor", "7_241");  // Merry's Armor: [rohan] possession, bearer must be Merry
					put("guard", "5_83");         // Household Guard: [rohan] Man companion
					put("runner", "1_178");       // Goblin Runner: a minion for the Knight's skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSupportAreaScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_321");
					put("knight", "5_35");        // Gondorian Knight: [gondor] Man companion, Knight is his whole text
					// A [rohan] possession that has no bearer: it goes to the support area, and to play it two
					// beacons are hindered. Its own ability is a maneuver one.
					put("flame", "103_86");       // Northern Signal-fire, Flame of Calenhad
					// Two beacons for it to hinder, neither with a text that a skirmish reaches
					put("warbeacon", "103_35");   // War-beacon: its abilities are fellowship ones
					put("amondin", "103_28");     // Northern Signal-fire, Flame of Amon Din: a response to a wound on an unbound Man
					put("runner", "1_178");       // Goblin Runner: a minion for the Knight's skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Merry, Swordthain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Skirmish:</b> If Merry is not assigned to a skirmish, return him to your hand to play up to 2 [rohan] possessions from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Swordthain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void SkirmishActionIsNotOfferedUnlessARohanPossessionInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var spear = scn.GetFreepsCard("spear");
		var armor = scn.GetFreepsCard("armor");
		var hall = scn.GetFreepsCard("hall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(card, aragorn, rider);
		//A possession that could be played, but is not a [rohan] one, and a [rohan] card that could be played,
		//but is not a possession (the Hall goes on the Rider)
		scn.MoveCardsToDiscard(armor, hall);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		//Playing from the discard pile is treated as a cost: with nothing to play, the ability is withheld
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(card));

		//The Spear can go on the Rider
		scn.MoveCardsToDiscard(spear);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(card));
	}

	@Test
	public void SkirmishActionReturnsMerryToHandToPlayUpToTwoRohanPossessionsFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPossessionsScenario();

		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var spear = scn.GetFreepsCard("spear");
		var shield = scn.GetFreepsCard("shield");
		var chain = scn.GetFreepsCard("chain");
		var hall = scn.GetFreepsCard("hall");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetShadowCard("sword");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry, guard, knight);
		scn.MoveCardsToDiscard(spear, shield, chain, hall, armor);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner);
		scn.FreepsResolveSkirmish(knight);

		// Merry is assigned to nothing; the skirmish being fought is the Knight's.
		assertFalse(scn.IsCharAssigned(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.DISCARD, spear, shield, chain, hall, armor, sword);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		// The cost: Merry is back in hand before anything is played.
		assertInZone(Zone.HAND, merry);
		// Each [rohan] possession is offered, whatever its item class or the Man it needs. Not the [rohan]
		// condition, the [gondor] possession, or the [rohan] possession in the Shadow player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, shield, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hall, armor, sword));
		// "Up to 2": none need be played.
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCardBPFromSelection(chain);
		// Heavy Chain goes on any Man: the [rohan] Guard and the [gondor] Knight are both offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, knight));
		scn.FreepsChooseCard(knight);
		assertEquals(knight, chain.getAttachedTo());

		// The second play, a choice of its own: again only a [rohan] possession of the Free Peoples player's
		// own pile, and again none need be played. The Spear's only [rohan] Man is the Guard.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, shield));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hall, armor, sword));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(spear);
		assertEquals(guard, spear.getAttachedTo());

		// Two have been played: the Shield stays in the discard pile and nothing more is asked.
		assertInZone(Zone.DISCARD, shield, hall, armor, sword);
		assertInZone(Zone.HAND, merry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileMerryIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeSkirmishesScenario();

		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(merry, guard, knight);
		// The Spear can be played on the Guard throughout.
		scn.MoveCardsToDiscard(spear);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, runner1},
				new PhysicalCardImpl[]{merry, runner2},
				new PhysicalCardImpl[]{guard, runner3});

		// The Knight's skirmish: Merry is assigned to one still to come.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharAssigned(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();

		// His own skirmish: not offered either. Merry, strength 3, loses to his Runner, strength 5, and takes
		// one wound.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();

		// The Guard's skirmish: Merry's is over and he is assigned to nothing any more.
		scn.FreepsResolveSkirmish(guard);
		assertFalse(scn.IsCharAssigned(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void SkirmishActionIsNotOfferedWhenNoRohanPossessionInTheDiscardPileCanBePlayedOnceMerryIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUnplayableScenario();

		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var spear = scn.GetFreepsCard("spear");
		var merrysarmor = scn.GetFreepsCard("merrysarmor");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry, knight);
		// Two [rohan] possessions: the Spear wants a [rohan] Man and there is none; Merry's Armor wants Merry, who
		// is in play now but goes back to hand as the cost, before anything is played.
		scn.MoveCardsToDiscard(spear, merrysarmor);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner);
		scn.FreepsResolveSkirmish(knight);

		assertFalse(scn.IsCharAssigned(merry));
		assertInZone(Zone.DISCARD, spear, merrysarmor);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));

		// A [rohan] Man arrives, and the Spear can be played on him.
		scn.MoveCompanionsToTable(guard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void SkirmishActionIsOfferedForARohanPossessionOfTheSupportAreaOnlyWhileItsOwnCostCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSupportAreaScenario();

		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var flame = scn.GetFreepsCard("flame");
		var warbeacon = scn.GetFreepsCard("warbeacon");
		var amondin = scn.GetFreepsCard("amondin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry, knight);
		scn.MoveCardsToSupportArea(warbeacon);
		scn.MoveCardsToDiscard(flame);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner);
		scn.FreepsResolveSkirmish(knight);

		// The Flame needs two beacons to hinder and one is in play: it cannot be played, so no ability.
		assertInZone(Zone.DISCARD, flame);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));

		// A second beacon: the Flame can be played, with no bearer to name.
		scn.MoveCardsToSupportArea(amondin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));
		assertFalse(scn.IsHindered(warbeacon));
		assertFalse(scn.IsHindered(amondin));

		scn.FreepsUseCardAction(merry);
		assertInZone(Zone.HAND, merry);
		scn.FreepsChooseCardBPFromSelection(flame);
		assertInZone(Zone.SUPPORT, flame);
		assertTrue(scn.IsHindered(warbeacon));
		assertTrue(scn.IsHindered(amondin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
