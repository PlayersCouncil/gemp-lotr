package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_057_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Shadow phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-fellowship offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enemy", "32_57");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, Wise; a fellowship ability, a discard bar
					// that needs 2 [dwarven] followers, and a discount on Thorin
					put("gandalf", "30_26");
					// Elrond, Elven Lord: [elven] Elf ally, Wise; a start-of-turn heal of a wounded Wise ally (none
					// is wounded) and a regroup ability, not used
					put("elrond", "30_21");
					// Radagast, The Brown: [gandalf] Wizard ally, Wise; a fellowship ability, not used
					put("radagast", "31_17");
					// Thranduil, Elven King: [elven] Elf ally, not Wise; a maneuver ability that wants Thorin or a
					// [dwarven] artifact
					put("thranduil", "31_12");
					// Goblin Runner: [moria] Orc minion; a "when you play" trigger, not reached from a discard pile
					put("runner", "31_30");
					// Gangsquad Spies: [men] Man minion, fierce; a site-number modifier and a maneuver ability
					put("spies", "33_26");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text
					put("jail", "32_39");       // Jail: [sauron] condition, support area; a regroup ability, not used
					// Orkish Veteran: [sauron] Orc minion, laid in the FIRST player's discard pile; text for site 9
					// and a regroup ability, neither reached
					put("veteran", "32_43");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheGreatEnemyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: The Great Enemy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot X Wise characters to take into hand X minions from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enemy");

		assertEquals("The Great Enemy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingTwoOfThreeWiseCharactersTakesTwoMinionsIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var runner = scn.GetShadowCard("runner");
		var spies = scn.GetShadowCard("spies");
		var troop = scn.GetShadowCard("troop");
		var jail = scn.GetShadowCard("jail");
		var myVeteran = scn.GetFreepsCard("veteran");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(elrond, radagast, thranduil);
		scn.StartGame();
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToDiscard(runner, spies, troop, jail, myVeteran);
		assertInZone(Zone.DISCARD, runner, spies, troop, jail, myVeteran);
		assertInHand(enemy);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPlayCard(enemy);

		// Three Wise characters, a companion and two allies; Bilbo and Thranduil are not Wise. Filled in with all.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// Minions of two cultures and races; Jail is no minion, and the other player's Veteran lies in his own pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, spies, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail, myVeteran));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(runner, spies);

		assertInHand(runner, spies);
		assertInZone(Zone.DISCARD, troop, jail, myVeteran, enemy);
		// The event left the hand and two minions came into it.
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingThreeWithTwoMinionsInThePileTakesBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		var runner = scn.GetShadowCard("runner");
		var spies = scn.GetShadowCard("spies");
		var jail = scn.GetShadowCard("jail");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(elrond, radagast);
		scn.StartGame();
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToDiscard(runner, spies, jail);
		assertInZone(Zone.DISCARD, runner, spies, jail);
		assertInHand(enemy);
		scn.FreepsPass();

		scn.ShadowPlayCard(enemy);
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		// Three to take and two minions to take: both are taken, and Jail stays.
		assertInHand(runner, spies);
		assertInZone(Zone.DISCARD, jail, enemy);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMinionInThePileItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var gandalf = scn.GetFreepsCard("gandalf");
		var jail = scn.GetShadowCard("jail");
		var myVeteran = scn.GetFreepsCard("veteran");

		scn.MoveCompanionsToTable(gandalf);
		scn.StartGame();
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToDiscard(jail, myVeteran);
		assertInZone(Zone.DISCARD, jail, myVeteran);
		assertInHand(enemy);
		scn.FreepsPass();

		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPlayCard(enemy);
		// Gandalf, the one Wise character, is spotted, and there is no minion to take.
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertInZone(Zone.DISCARD, jail, myVeteran, enemy);
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayableInTheShadowPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gandalf);
		scn.StartGame();
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToDiscard(runner);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.DISCARD, runner);
		assertInHand(enemy);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(enemy));
		assertInHand(enemy);
	}
}
