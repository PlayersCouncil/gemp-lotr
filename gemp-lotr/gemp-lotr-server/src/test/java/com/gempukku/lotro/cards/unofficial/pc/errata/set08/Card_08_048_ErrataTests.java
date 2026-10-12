package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_048_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swept", "58_48");
					put("swept2", "58_48");

					put("oath", "8_41");        // Oathbreaker: [gondor] Wraith companion, strength 6, vitality 3, enduring
					put("oath2", "8_41");
					put("sleepless", "8_45");   // Sleepless Dead: [gondor] Wraith companion, strength 7, vitality 3, enduring
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3, no text

					put("deadones", "6_100");   // Dead Ones: [sauron] Wraith minion, strength 5, vitality 1 (always exhausted)
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, "Regroup: Exert this minion to wound a companion (except the Ring-bearer)"
					put("slayer2", "3_93");
					put("patrol", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("reach", "1_277");      // Shadow's Reach: Shadow event, "Exert a [sauron] Orc to discard a Free Peoples condition"
					put("ravager", "10_94");    // Orc Ravager: "Shadow: Remove 2 threats to discard a condition"

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
		 * Game Text: Each time the fellowship moves, add a threat or discard this condition.
		 * While you have initiative, exhausted [gondor] Wraiths cannot take wounds.
		 * Response: If a Shadow card is about to discard this condition, discard 2 cards from hand to prevent that.
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
	public void ChoosingToDiscardItLeavesTheThreatsAndOffersNoResponseToItsOwnDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCardsToSupportArea(swept);
		scn.MoveCompanionsToTable(oath);
		// Enough cards to pay for the response, were it offered.
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));

		scn.StartGame();

		scn.FreepsPass();
		assertEquals(0, scn.GetThreats());
		scn.FreepsChooseOption("Discard");

		// This condition, a Free Peoples card, discarded itself: the response is for a Shadow card's discard.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(swept));
		assertInDiscard(swept);
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetFreepsHandCount());
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
	public void AShadowEventAboutToDiscardItIsPreventedByDiscardingTwoCardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var slayer = scn.GetShadowCard("slayer");
		var reach = scn.GetShadowCard("reach");
		scn.MoveCompanionsToTable(oath);
		scn.MoveCardsToHand(card1, card2, card3, reach);

		scn.MoveCardsToSupportArea(swept);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(reach);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(swept));
		assertEquals(3, scn.GetFreepsHandCount());
		scn.FreepsChooseOptionalTrigger(swept);
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(card1, card2);

		assertInDiscard(card1, card2);
		assertInHand(card3);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, swept);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertInDiscard(reach);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowMinionAboutToDiscardItAlsoGivesTheResponseAndDecliningLetsItGo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		var ravager = scn.GetShadowCard("ravager");
		scn.MoveCompanionsToTable(oath);
		// Exactly the two cards the response costs.
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"));

		scn.MoveCardsToSupportArea(swept);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager);
		// Frodo and Oathbreaker: a threat limit of 2. With the one the move adds, the 2 threats Orc Ravager removes.
		scn.AddThreats(1);
		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowUseCardAction(ravager);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(swept));
		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(swept);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyOneCardInHandTheResponseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var oath = scn.GetFreepsCard("oath");
		var slayer = scn.GetShadowCard("slayer");
		var reach = scn.GetShadowCard("reach");
		scn.MoveCompanionsToTable(oath);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), reach);

		scn.MoveCardsToSupportArea(swept);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(reach);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(swept));
		assertInDiscard(swept);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
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

	@Test
	public void WithTwoCopiesOnlyTheCopyAboutToBeDiscardedGivesTheResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swept = scn.GetFreepsCard("swept");
		var swept2 = scn.GetFreepsCard("swept2");
		var oath = scn.GetFreepsCard("oath");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var slayer = scn.GetShadowCard("slayer");
		var reach = scn.GetShadowCard("reach");
		scn.MoveCompanionsToTable(oath);
		scn.MoveCardsToHand(card1, card2, card3, reach);
		scn.MoveCardsToSupportArea(swept, swept2);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPass();
		// Frodo and Oathbreaker: a threat limit of 2, which the two moves' questions fill.
		scn.FreepsChooseOption("Add a threat");
		scn.FreepsChooseOption("Add a threat");

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(reach);
		// Both are Free Peoples conditions, so the Shadow player chooses which to discard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swept, swept2));
		scn.ShadowChooseCard(swept2);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(swept2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(swept));
		scn.FreepsChooseOptionalTrigger(swept2);
		scn.FreepsChooseCards(card1, card2);

		assertInDiscard(card1, card2);
		assertInZone(Zone.SUPPORT, swept, swept2);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertInDiscard(reach);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
