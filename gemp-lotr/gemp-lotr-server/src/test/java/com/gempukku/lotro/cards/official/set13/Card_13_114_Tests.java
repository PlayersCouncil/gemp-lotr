package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "13_114");
					put("runner2", "13_114");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc; text only on its own play
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment
					                             // trigger needs more companions than minions, which no board here has
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition (support area); nothing below 5 burdens
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a ranger, for Pathfinder
					put("pathfinder", "1_110");  // Pathfinder: Fellowship or Regroup event, spot a ranger to play the fellowship's next site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "13_114");
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] Half-troll
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcPlainsRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Orc Plains Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Discard this minion from play (or exert this minion if the fellowship's next site is an underground site) and spot another [orc] minion to return that minion to your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("runner");

		assertEquals("Orc Plains Runner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAnUndergroundSiteWhoseNextSiteIsNotTheRunnerDiscardsItselfToReturnAnotherOrcMinionToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(runner, runner2, goblin, halftroll, troop);

		// Nobody is assigned, so every minion is still in play in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		// The fellowship is at The Bridge of Khazad-dum (underground). Its next site, laid as it would be where the
		// other player had gone on ahead, is Galadriel's Glade, which is not.
		assertEquals(5, scn.GetCurrentSiteNumber());
		var glade = scn.GetFreepsSite(6);
		scn.MoveCardToAdventurePath(glade);
		assertInZone(Zone.ADVENTURE_PATH, glade);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, runner2, goblin, halftroll, troop);
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		// The discard is the only price: nothing asks which.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.ShadowGetChoiceMin());
		// [orc] minions of two races, the other copy among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner2, goblin, halftroll));
		// An Orc minion of another culture.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(halftroll);

		assertInHand(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, runner2, goblin, troop);
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheNextSiteIsBalinsTombTheRunnerExertsAndStaysToReturnAnotherOrcMinionToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var goblin = scn.GetShadowCard("goblin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(pathfinder);
		assertInHand(pathfinder);
		scn.MoveMinionsToTable(runner, runner2, goblin);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Regroup at Rivendell Valley, site 3. Pathfinder lays the fellowship's next site: Balin's Tomb, underground.
		assertEquals(3, scn.GetCurrentSiteNumber());
		var tomb = scn.GetFreepsSite(4);
		assertInHand(pathfinder);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPlayCard(pathfinder);
		assertInZone(Zone.ADVENTURE_PATH, tomb);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		// A conditional parenthesis is used whenever its condition is met (CR 4.2 "effect"; Q-0035):
		// the exertion is the price, and the player is not asked to discard instead.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner2));
		// "Another": the Runner that paid is no candidate.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner2, goblin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(goblin);

		assertInHand(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, runner, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheNextSiteIsTheDimholtTheRunnerExertsToReturnAnotherOrcMinionToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var runner = scn.GetShadowCard("runner");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, halftroll);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		// Regroup at King's Tent, site 2; the next site, laid ahead, is The Dimholt, an underground sanctuary.
		assertEquals(2, scn.GetCurrentSiteNumber());
		var dimholt = scn.GetFreepsSite(3);
		scn.MoveCardToAdventurePath(dimholt);
		assertInZone(Zone.ADVENTURE_PATH, dimholt);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// The Half-troll is the only other [orc] minion, and is returned unasked.
		assertInHand(halftroll);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheNextSiteIsUndergroundAndTheRunnerIsExhaustedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner, goblin);
		// Vitality 2 with 1 wound: exhausted, so it cannot exert.
		scn.AddWoundsToChar(runner, 1);
		assertEquals(1, scn.GetWoundsOn(runner));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		// Regroup at site 3, with Balin's Tomb laid ahead as the next site.
		assertEquals(3, scn.GetCurrentSiteNumber());
		var tomb = scn.GetFreepsSite(4);
		scn.MoveCardToAdventurePath(tomb);
		assertInZone(Zone.ADVENTURE_PATH, tomb);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// Ketura (Q-0389): with the condition met, the exertion is the only price (CR 4.2 "effect"); the discard
		// outside the parenthesis does not stand in for it, so the ability is not offered.
		assertFalse(scn.ShadowActionAvailable(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner, goblin);
	}

	@Test
	public void WithTheNextSiteNotYetLaidTheRunnerDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner, goblin);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		// Regroup at site 3. No site 4 lies on the path yet: Balin's Tomb is still in the adventure deck, and the
		// fellowship's next site is no underground site until one is laid.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, scn.GetFreepsSite(4));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		assertInZone(Zone.DISCARD, runner);
		assertInHand(goblin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoOtherOrcMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning);
		scn.MoveMinionsToTable(runner, troop);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		// An Orc of another culture and an [orc] card that is no minion beside the Runner itself.
		assertInZone(Zone.SHADOW_CHARACTERS, runner, troop);
		assertInZone(Zone.SUPPORT, beckoning);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(runner));
	}
}
