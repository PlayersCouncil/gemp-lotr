package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_213_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("discover", "4_213");
					// Two [isengard] trackers of different titles: Uruk Chaser and Uruk Runner, fierce Uruk-hai whose
					// texts read only in a fierce skirmish
					put("chaser", "4_181");
					put("runner", "4_193");
					// Uruk Plains Runner: a third [isengard] tracker, of its own title, to be discarded from hand
					put("plainsrunner", "4_189");
					// Uruk Foot Soldier: an [isengard] tracker of a fourth title, in the Free Peoples player's discard pile
					put("footsoldier", "4_187");
					// Orc Scout: a [sauron] tracker; its roaming discount and response read nothing in a discard pile
					put("scout", "2_89");
					// Uruk Savage: an [isengard] Uruk-hai who is no tracker; damage +1 only
					put("savage", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, for the hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples card for the hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 3 is Rivendell Waterfall: "While you can spot a ranger at Rivendell Waterfall, the move limit is +1 for
	// this turn."  With Aragorn, a ranger, in the fellowship, it moves twice in the regroup phase of one turn.
	protected VirtualTableScenario GetThreeMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("discover", "4_213");
					put("chaser", "4_181");     // Uruk Chaser: an [isengard] tracker
					put("runner", "4_193");     // Uruk Runner: an [isengard] tracker
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, for the hand
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, damage +1 only, for the hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the ranger; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a Free Peoples card for the hand
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhatDidYouDiscoverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: What Did You Discover?
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Search. Plays to your support area.
		 * Each time the fellowship moves during the regroup phase, you may discard 2 cards from hand to take an [isengard] tracker from your discard pile into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("discover");

		assertEquals("What Did You Discover?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveOffersToDiscard2CardsToTakeAnIsengardTrackerFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discover = scn.GetShadowCard("discover");
		var chaser = scn.GetShadowCard("chaser");
		var runner = scn.GetShadowCard("runner");
		var scout = scn.GetShadowCard("scout");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var plainsrunner = scn.GetShadowCard("plainsrunner");
		var freepsFootsoldier = scn.GetFreepsCard("footsoldier");

		scn.StartGame();
		scn.MoveCardsToSupportArea(discover);
		scn.MoveCardsToDiscard(chaser, runner, scout, savage, freepsFootsoldier);
		scn.MoveCardsToHand(troop, shadowAragorn, plainsrunner);
		// An empty draw deck, so that the Shadow player's reconcile draws nothing (and no Foot Soldier is his).
		scn.SetShadowDrawDeck();
		assertInDiscard(chaser, runner, scout, savage, freepsFootsoldier);
		assertEquals(3, scn.GetShadowHandCount());
		assertInHand(troop, shadowAragorn, plainsrunner);

		// The fellowship's move at the end of the fellowship phase is no regroup move.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(discover));

		// No minion: the Shadow phase is followed by the regroup phase, and the second move is a regroup move.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(discover));
		scn.ShadowAcceptOptionalTrigger();

		// Any 2 cards of the hand, and exactly 2.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, shadowAragorn, plainsrunner));
		scn.ShadowChooseCards(troop, shadowAragorn);
		assertInDiscard(troop, shadowAragorn);
		assertEquals(1, scn.GetShadowHandCount());

		// Each [isengard] tracker of his discard pile; not a [sauron] tracker, an [isengard] minion who is no
		// tracker, or the Free Peoples player's [isengard] tracker in his own pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chaser, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scout, savage, freepsFootsoldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chaser);

		assertInHand(chaser, plainsrunner);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(runner, scout, savage, freepsFootsoldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTrackerJustDiscardedForTheCostCannotBeTakenBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discover = scn.GetShadowCard("discover");
		var chaser = scn.GetShadowCard("chaser");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var plainsrunner = scn.GetShadowCard("plainsrunner");

		scn.StartGame();
		scn.MoveCardsToSupportArea(discover);
		scn.MoveCardsToDiscard(chaser, runner);
		scn.MoveCardsToHand(troop, shadowAragorn, plainsrunner);
		scn.SetShadowDrawDeck();
		assertInDiscard(chaser, runner);
		assertInHand(troop, shadowAragorn, plainsrunner);

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(discover));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(plainsrunner, troop);
		assertInDiscard(plainsrunner, troop);

		// Q-0192: "discarding a card from hand to retrieve a card is not allowed to pull the card just discarded."
		assertTrue(scn.ShadowHasCardChoiceAvailable(chaser, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(plainsrunner));
		scn.ShadowChooseCard(chaser);
		assertInHand(chaser, shadowAragorn);
		assertInDiscard(plainsrunner, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesHandAndDiscardPileAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discover = scn.GetShadowCard("discover");
		var chaser = scn.GetShadowCard("chaser");
		var troop = scn.GetShadowCard("troop");
		var shadowAragorn = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.MoveCardsToSupportArea(discover);
		scn.MoveCardsToDiscard(chaser);
		scn.MoveCardsToHand(troop, shadowAragorn);
		scn.SetShadowDrawDeck();
		assertInDiscard(chaser);
		assertInHand(troop, shadowAragorn);

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(discover));
		scn.ShadowDeclineOptionalTrigger();

		assertInHand(troop, shadowAragorn);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(chaser);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discover = scn.GetShadowCard("discover");
		var chaser = scn.GetShadowCard("chaser");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToSupportArea(discover);
		scn.MoveCardsToDiscard(chaser);
		scn.MoveCardsToHand(troop);
		scn.SetShadowDrawDeck();
		assertInDiscard(chaser);
		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(troop);

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(discover));
		assertInHand(troop);
		assertInDiscard(chaser);
	}

	@Test
	public void IsOfferedAtEachRegroupMoveOfATurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeMoveScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var discover = scn.GetShadowCard("discover");
		var chaser = scn.GetShadowCard("chaser");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var shadowBoromir = scn.GetShadowCard("boromir");

		scn.StartGame();
		scn.MoveCardsToSupportArea(discover);
		scn.MoveCardsToDiscard(chaser, runner);
		scn.MoveCardsToHand(troop, savage, shadowAragorn, shadowBoromir);
		scn.SetShadowDrawDeck();
		assertInDiscard(chaser, runner);
		assertEquals(4, scn.GetShadowHandCount());
		assertInHand(troop, savage, shadowAragorn, shadowBoromir);

		scn.FreepsPass();
		scn.SkipToMovementDecision();

		// The move to Rivendell Waterfall
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(discover));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(troop, savage);
		scn.ShadowChooseCard(chaser);
		assertInHand(chaser, shadowAragorn, shadowBoromir);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With a ranger at the Waterfall the fellowship moves a third time, in the regroup phase again.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(discover));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(shadowAragorn, shadowBoromir);
		// The Runner is the one tracker in the pile, and is taken unasked.
		assertInHand(runner, chaser);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(shadowAragorn, shadowBoromir, troop, savage);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
