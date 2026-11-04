package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enemy", "13_106");
					put("enemy2", "13_106");
					put("prowling", "11_136");   // Prowling Orc: [orc] Orc lurker, cost 3, site 4; a skirmish ability only
					put("strategist", "12_97");  // Orc Strategist: [orc] Orc lurker, cost 4, site 4; a maneuver ability only
					// Horror of Harad: unique [orc] Half-troll lurker, cost 4, site 4: an [orc] lurker who is no Orc.
					// Ambush, damage +1, fierce, muster and toil 2 reach no test here.
					put("horror", "14_13");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc and no lurker; text only on its own play
					put("column", "11_72");      // Column of Easterlings: [men] Man lurker; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnemyUponEnemyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Enemy Upon Enemy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Spot an [orc] lurker minion to stack that minion here.<br><b>Shadow:</b> Remove a burden to play a lurker minion stacked here as if from hand. That minion is twilight cost -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enemy");

		assertEquals("Enemy Upon Enemy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityStacksTheChosenOrcLurkerMinionOnThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var enemy2 = scn.GetShadowCard("enemy2");
		var prowling = scn.GetShadowCard("prowling");
		var horror = scn.GetShadowCard("horror");
		var goblin = scn.GetShadowCard("goblin");
		var column = scn.GetShadowCard("column");
		scn.MoveCardsToSupportArea(enemy, enemy2);

		scn.StartGame();
		scn.MoveMinionsToTable(prowling, horror, goblin, column);

		// Nobody is assigned, so every minion is still in play in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// The Horror is fierce: the fierce assignment comes round, and is declined too.
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetStackedCards(enemy).size());
		assertEquals(0, scn.GetStackedCards(enemy2).size());
		assertTrue(scn.ShadowActionAvailable(enemy));
		scn.ShadowUseCardAction(enemy);

		assertEquals(1, scn.ShadowGetChoiceMin());
		// [orc] lurkers of two races: an Orc and a Half-troll.
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowling, horror));
		// An [orc] minion who is no lurker, and a lurker of another culture.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblin, column));
		scn.ShadowChooseCard(horror);

		assertInZone(Zone.STACKED, horror);
		assertTrue(scn.GetStackedCards(enemy).contains(horror));
		assertEquals(1, scn.GetStackedCards(enemy).size());
		assertEquals(0, scn.GetStackedCards(enemy2).size());
		assertInZone(Zone.SHADOW_CHARACTERS, prowling, goblin, column);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoOrcLurkerMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var goblin = scn.GetShadowCard("goblin");
		var column = scn.GetShadowCard("column");
		scn.MoveCardsToSupportArea(enemy);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, column);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, goblin, column);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enemy));
	}

	@Test
	public void ShadowAbilityRemovesABurdenToPlayALurkerStackedHereAtTwilightCostMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var enemy2 = scn.GetShadowCard("enemy2");
		var prowling = scn.GetShadowCard("prowling");
		var strategist = scn.GetShadowCard("strategist");
		var horror = scn.GetShadowCard("horror");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToSupportArea(enemy, enemy2);
		scn.StackCardsOn(enemy, strategist, horror);
		scn.StackCardsOn(enemy2, prowling);
		scn.MoveCardsToHand(goblin);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, prowling, strategist, horror);
		assertEquals(2, scn.GetStackedCards(enemy).size());
		assertInHand(goblin);
		scn.FreepsPass();
		// Shadow phase at Ettenmoors, site 2: every minion here is site 4, so roaming.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(enemy));
		scn.ShadowUseCardAction(enemy);
		assertEquals(0, scn.GetBurdens());

		assertEquals(1, scn.ShadowGetChoiceMin());
		// Lurkers of two races stacked here: an Orc and a Half-troll.
		assertTrue(scn.ShadowHasCardChoiceAvailable(strategist, horror));
		// The Prowling Orc is a lurker, but stacked on the other copy.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(prowling));
		scn.ShadowChooseCard(strategist);

		assertInZone(Zone.SHADOW_CHARACTERS, strategist);
		// 10 - (4 cost - 3 + 2 roaming)
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.STACKED, prowling, horror);
		assertTrue(scn.GetStackedCards(enemy).contains(horror));
		assertTrue(scn.GetStackedCards(enemy2).contains(prowling));

		// A minion played from hand afterwards pays its whole cost: 7 - (1 cost + 2 roaming).
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCardsToSupportArea(enemy);
		scn.StackCardsOn(enemy, prowling);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.STACKED, prowling);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enemy));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(enemy));
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolCanPayForTheStackedLurkerAtItsDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var strategist = scn.GetShadowCard("strategist");
		scn.MoveCardsToSupportArea(enemy);
		scn.StackCardsOn(enemy, strategist);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, strategist);
		scn.FreepsPass();

		// The Strategist costs 4 - 3 + 2 roaming = 3 at Ettenmoors.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enemy));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(enemy));
		scn.ShadowUseCardAction(enemy);

		assertInZone(Zone.SHADOW_CHARACTERS, strategist);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
