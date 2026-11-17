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

public class Card_07_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("targon", "7_124");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only

					// Hand cards of every kind, for the cost's choice (held in hand, where their text does nothing):
					// the Free Peoples player's own Household Guard (a companion) and Goblin Patrol Troop (a Shadow minion).
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant only
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession

					// Men of two cultures, and an Orc.
					put("explorer1", "4_250");  // Southron Explorer: [raider] Man, vitality 3; a regroup ability only
					put("explorer2", "4_250");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, vitality 1; no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3; no text
					put("fighter1", "7_286");   // Mordor Fighter: while a threat and another [sauron] Orc can be spotted,
					put("fighter2", "7_286");   //   the Shadow has initiative whatever the Free Peoples hand holds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TargonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Targon
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.
		 * 	<b>Skirmish:</b> If you have initiative, discard 2 cards from hand to wound a Man Targon is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("targon");

		assertEquals("Targon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void DiscardingTwoCardsWoundsAManHeIsSkirmishingAndFromThreeCardsItIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targon = scn.GetFreepsCard("targon");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(targon);
		var guard = scn.GetFreepsCard("guard");
		var handTroop = scn.GetFreepsCard("troop");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var deckCard = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(guard, handTroop, deftness, sword, armor);
		assertInHand(guard, handTroop, deftness, sword, armor);

		var explorer = scn.GetShadowCard("explorer1");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, brigand, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(targon, explorer);
		// The minions left over are put on Targon by the Shadow player.
		scn.ShadowAssignToMinions(targon, brigand, troop);
		scn.FreepsResolveSkirmish(targon);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(targon));

		scn.FreepsUseCardAction(targon);
		// Any two cards of the hand, of either side and any type; not Targon in play or a card of the deck.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, handTroop, deftness, sword, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(targon, deckCard));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard, handTroop);
		assertInDiscard(guard, handTroop);
		assertInHand(deftness, sword, armor);
		assertEquals(3, scn.GetFreepsHandCount());

		// The Men he is skirmishing, of two cultures; not the Orc beside them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(explorer, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, targon, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Three cards left: two could still be discarded, but the Shadow has initiative.
		scn.ShadowPass();
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(targon));
	}

	@Test
	public void AnExhaustedManCanBeChosenAndIsKilledByTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targon = scn.GetFreepsCard("targon");
		scn.MoveCompanionsToTable(targon);
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(deftness, sword, armor, guard);
		assertInHand(deftness, sword, armor, guard);

		var explorer = scn.GetShadowCard("explorer1");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(targon, explorer);
		scn.ShadowAssignToMinions(targon, brigand);
		scn.FreepsResolveSkirmish(targon);

		// The Brigand has 1 vitality: he cannot exert, and one wound kills him.
		assertEquals(1, scn.GetVitality(brigand));
		assertTrue(scn.FreepsActionAvailable(targon));
		scn.FreepsUseCardAction(targon);
		scn.FreepsChooseCards(deftness, sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(explorer, brigand));
		scn.FreepsChooseCard(brigand);

		assertInDiscard(brigand);
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedFromThreeCardsInHandAndFromExactlyFourItDiscardsTwoAndStillWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targon = scn.GetFreepsCard("targon");
		scn.MoveCompanionsToTable(targon);
		var guard = scn.GetFreepsCard("guard");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(guard, deftness, sword);
		assertInHand(guard, deftness, sword);

		var explorer = scn.GetShadowCard("explorer1");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(targon, explorer);
		scn.FreepsResolveSkirmish(targon);

		// Three cards: no initiative, and the ability is not on offer.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(targon));

		// A fourth card: initiative, and the ability is offered.
		scn.MoveCardsToHand(armor);
		assertInHand(armor);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.FreepsActionAvailable(targon));

		// The initiative is checked before the cost: the discard leaves two cards and no initiative, and the
		// one Man he is skirmishing is wounded all the same.
		scn.FreepsUseCardAction(targon);
		scn.FreepsChooseCards(guard, deftness);
		assertInDiscard(guard, deftness);
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheShadowHasInitiativeWhateverTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targon = scn.GetFreepsCard("targon");
		scn.MoveCompanionsToTable(targon);
		scn.MoveCardsToHand(scn.GetFreepsCard("guard"), scn.GetFreepsCard("troop"), scn.GetFreepsCard("deftness"),
				scn.GetFreepsCard("sword"), scn.GetFreepsCard("armor"));
		assertEquals(5, scn.GetFreepsHandCount());

		var explorer = scn.GetShadowCard("explorer1");
		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, fighter1, fighter2);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(targon, explorer);
		// The Mordor Fighters stay out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(targon);

		// Five cards in hand, enough to pay; but each Mordor Fighter spots a threat and the other [sauron] Orc.
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(targon));

		// With the threat gone the Fighters no longer take the initiative, and the ability is offered.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(targon));
	}

	@Test
	public void InAnotherCompanionsSkirmishTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targon = scn.GetFreepsCard("targon");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(targon, aragorn);
		var handTroop = scn.GetFreepsCard("troop");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(handTroop, deftness, sword, armor);
		assertInHand(handTroop, deftness, sword, armor);

		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer1, explorer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer1}, new PhysicalCardImpl[]{targon, explorer2});
		// Aragorn's skirmish first: the second Explorer, assigned to Targon, waits for his.
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(explorer1));
		assertFalse(scn.IsCharSkirmishing(explorer2));
		assertTrue(scn.IsCharAssigned(explorer2));
		assertEquals(0, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetWoundsOn(explorer2));
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(targon));

		scn.FreepsUseCardAction(targon);
		scn.FreepsChooseCards(handTroop, deftness);
		assertInDiscard(handTroop, deftness);
		assertEquals(2, scn.GetFreepsHandCount());
		// No Man Targon is skirmishing: neither the one in this skirmish nor the one still to fight him.
		assertEquals(0, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetWoundsOn(explorer2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
