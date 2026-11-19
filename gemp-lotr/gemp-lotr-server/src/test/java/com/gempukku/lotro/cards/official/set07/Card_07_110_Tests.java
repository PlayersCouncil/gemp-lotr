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

public class Card_07_110_Tests
{

	// The Fellowship path. The skirmishes are at site 2 (Ettenmoors), where a minion roams if its site number is 3
	// or more.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madril", "7_110");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only

					// Hand cards of every kind, for the cost's choice (held in hand, where their text does nothing):
					// Aragorn above (a companion), and the Free Peoples player's own Goblin Patrol Troop (a Shadow minion).
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession

					// Site number above 2: roaming at site 2.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, site 4; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, site 5; Damage +1 only
					// Site number 2: not roaming at site 2.
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: vitality 3; nothing else below 2 burdens
					put("fighter1", "7_286");   // Mordor Fighter: while a threat and another [sauron] Orc can be spotted,
					put("fighter2", "7_286");   //   the Shadow has initiative whatever the Free Peoples hand holds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MadrilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Madril, Faramir's Aide
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
		 * 	<b>Skirmish:</b> If you have initiative, discard 2 cards from hand to wound a roaming minion Madril is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("madril");

		assertEquals("Madril", card.getBlueprint().getTitle());
		assertEquals("Faramir's Aide", card.getBlueprint().getSubtitle());
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
	public void DiscardingTwoCardsWoundsARoamingMinionHeIsSkirmishingAndFromThreeCardsItIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(madril);
		var aragorn = scn.GetFreepsCard("aragorn");
		var handTroop = scn.GetFreepsCard("troop");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var deckCard = scn.GetFreepsCard("savage");
		scn.MoveCardsToHand(aragorn, handTroop, deftness, sword, armor);
		assertInHand(aragorn, handTroop, deftness, sword, armor);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, nelya);
		// The Uruk Savage, vitality 3, is exhausted.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(madril, troop);
		// The minions left over are put on Madril by the Shadow player.
		scn.ShadowAssignToMinions(madril, savage, nelya);
		scn.FreepsResolveSkirmish(madril);

		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(madril));

		scn.FreepsUseCardAction(madril);
		// Any two cards of the hand, of either side and any type; not Madril in play or a card of the deck.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, handTroop, deftness, sword, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(madril, deckCard));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, handTroop);
		assertInDiscard(aragorn, handTroop);
		assertInHand(deftness, sword, armor);
		assertEquals(3, scn.GetFreepsHandCount());

		// The roaming minions he is skirmishing, of two cultures and races, the exhausted one among them; not
		// Nelya, who is not roaming.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nelya, madril, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Three cards left: two could still be discarded, but the Shadow has initiative.
		scn.ShadowPass();
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(madril));
	}

	@Test
	public void NotOfferedFromThreeCardsInHandAndFromExactlyFourItDiscardsTwoAndStillWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(madril);
		var aragorn = scn.GetFreepsCard("aragorn");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(aragorn, deftness, sword);
		assertInHand(aragorn, deftness, sword);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(madril, savage);
		scn.FreepsResolveSkirmish(madril);

		// Three cards: no initiative, and the ability is not on offer.
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(madril));

		// A fourth card: initiative, and the ability is offered.
		scn.MoveCardsToHand(armor);
		assertInHand(armor);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(madril));

		// The initiative is checked before the cost: the discard leaves two cards and no initiative, and the
		// one roaming minion he is skirmishing is wounded all the same.
		scn.FreepsUseCardAction(madril);
		scn.FreepsChooseCards(aragorn, deftness);
		assertInDiscard(aragorn, deftness);
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheShadowHasInitiativeWhateverTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(madril);
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("troop"), scn.GetFreepsCard("deftness"),
				scn.GetFreepsCard("sword"), scn.GetFreepsCard("armor"));

		var savage = scn.GetShadowCard("savage");
		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, fighter1, fighter2);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(madril, savage);
		// The Mordor Fighters stay out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(madril);

		// Five cards in hand, enough to pay; but each Mordor Fighter spots a threat and the other [sauron] Orc.
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(madril));

		// With the threat gone the Fighters no longer take the initiative, and the ability is offered.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(madril));
	}

	@Test
	public void InAnotherCompanionsSkirmishTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(madril, aragorn);
		var handTroop = scn.GetFreepsCard("troop");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(handTroop, deftness, sword, armor);
		assertInHand(handTroop, deftness, sword, armor);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{madril, savage});
		// Aragorn's skirmish first: the Savage, assigned to Madril, waits for his.
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertTrue(scn.IsCharAssigned(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(madril));

		scn.FreepsUseCardAction(madril);
		scn.FreepsChooseCards(handTroop, deftness);
		assertInDiscard(handTroop, deftness);
		assertEquals(2, scn.GetFreepsHandCount());
		// No minion Madril is skirmishing: neither the one in this skirmish nor the one still to fight him.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
