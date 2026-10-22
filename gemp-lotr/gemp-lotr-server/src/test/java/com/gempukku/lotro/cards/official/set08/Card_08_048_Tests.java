package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_048_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swept", "8_48");
					put("swept2", "8_48");

					put("oath", "8_41");        // Oathbreaker: [gondor] Wraith companion, strength 6, vitality 3, enduring
					put("oath2", "8_41");
					put("sleepless", "8_45");   // Sleepless Dead: [gondor] Wraith companion, strength 7, vitality 3, enduring
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3, no text

					put("deadones", "6_100");   // Dead Ones: [sauron] Wraith minion, strength 5, vitality 1 (always exhausted)
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, "Regroup: Exert this minion to wound a companion (except the Ring-bearer)"
					put("slayer2", "3_93");
					put("patrol", "1_177");     // Goblin Patrol Troop: strength 13, no text
					// A Path Appointed: a [gondor] support-area condition of the same player; its two abilities are used
					// only on purpose
					put("path", "8_42");

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SweptAwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Swept Away
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time the fellowship moves, add a threat or discard this condition.<br>While you have initiative, exhausted [gondor] Wraiths cannot take wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swept");

		assertEquals("Swept Away", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMoveOfTheFellowshipAddsAThreatWhenThatIsChosenAndAsksAgainOnTheRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(swept);
		// Three companions with Frodo, so the threat limit (3) is no obstacle to two threats.
		scn.MoveCompanionsToTable(oath, knight);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, swept);
		scn.FreepsPass();

		// Moving from site 1 to site 2.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, swept);

		// No minions, so the game goes on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, swept);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToDiscardItLeavesTheThreatsAndAnotherConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var path = scn.GetFreepsCard("path");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCardsToSupportArea(swept, path);
		scn.MoveCompanionsToTable(oath);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, swept, path);
		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		assertInDiscard(swept);
		assertInZone(Zone.SUPPORT, path);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheMoveDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCardsToSupportArea(swept);
		scn.MoveCompanionsToTable(oath);

		scn.StartGame();
		// Frodo and Oathbreaker: a threat limit of 2.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, swept);

		scn.FreepsPass();

		// A threat cannot be added, so discarding is all that can be done.
		assertInDiscard(swept);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingInTheRegroupPhaseAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(oath);

		scn.StartGame();
		scn.SkipToMovementDecision();
		// Put down after the first move of the turn, so that the regroup decision is the first it sees.
		scn.MoveCardsToSupportArea(swept);
		assertInZone(Zone.SUPPORT, swept);
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, swept);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithInitiativeAnExhaustedGondorWraithCannotBeWoundedAndWithoutItHeCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		var oath2 = scn.GetFreepsCard("oath2");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");
		var card4 = scn.GetFreepsCard("card4");
		scn.MoveCardsToSupportArea(swept);
		scn.MoveCompanionsToTable(oath, oath2, knight);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), card4);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, slayer2);
		// Oathbreaker and the Knight are exhausted (vitality 3, 2 wounds); the second Oathbreaker is not.
		scn.AddWoundsToChar(oath, 2);
		scn.AddWoundsToChar(oath2, 1);
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(oath));
		assertEquals(1, scn.GetWoundsOn(oath2));
		assertEquals(2, scn.GetWoundsOn(knight));

		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");
		// Taken off again, so that a companion's death does not turn it into a wound.
		scn.RemoveThreats(1);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(oath2, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(oath, frodo));
		scn.ShadowChooseCard(oath2);
		// Oathbreaker 2 is now exhausted as well.
		assertEquals(2, scn.GetWoundsOn(oath2));
		assertEquals(2, scn.GetWoundsOn(oath));

		// Down to 3 cards in hand: the Shadow has initiative.
		scn.MoveCardsToDiscard(card4);
		scn.FreepsPass();
		assertFalse(scn.FreepsHasInitiative());
		scn.ShadowUseCardAction(slayer2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(oath, oath2, knight));
		scn.ShadowChooseCard(oath);

		// The third wound kills him.
		assertInZone(Zone.DEAD, oath);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithInitiativeAnExhaustedGondorWraithTakesNoWoundForLosingASkirmishAndAnExhaustedSauronWraithDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var sleepless = scn.GetFreepsCard("sleepless");
		var knight = scn.GetFreepsCard("knight");
		var patrol = scn.GetShadowCard("patrol");
		var deadones = scn.GetShadowCard("deadones");
		scn.MoveCardsToSupportArea(swept);
		scn.MoveCompanionsToTable(sleepless, knight);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, deadones);
		// Sleepless Dead exhausted: vitality 3, 2 wounds, strength 7 + 4 for enduring = 11.
		scn.AddWoundsToChar(sleepless, 2);
		assertEquals(2, scn.GetWoundsOn(sleepless));

		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sleepless, patrol}, new PhysicalCardImpl[]{knight, deadones});
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.IsExhausted(deadones));

		// 13 against 11: Sleepless Dead loses, but is not overwhelmed.
		scn.FreepsResolveSkirmish(sleepless);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sleepless));
		assertInZone(Zone.FREE_CHARACTERS, sleepless);

		// 6 against 5: Dead Ones, a Wraith of another culture, takes the wound and dies.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertInDiscard(deadones);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnAMoveAndDiscardingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var swept2 = scn.GetFreepsCard("swept2");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCardsToSupportArea(swept, swept2);
		scn.MoveCompanionsToTable(oath);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsPass();

		// Each copy puts its own question.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		// Only the copy that was asked is gone.
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetThreats());
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
