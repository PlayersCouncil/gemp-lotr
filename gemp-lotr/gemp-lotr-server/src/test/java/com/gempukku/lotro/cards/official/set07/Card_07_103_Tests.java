package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hearts", "7_103");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("household", "5_83");   // Household Guard: a [rohan] Man companion; valiant is its whole text

					// Cards for the hand, of several cultures, types and both sides; none is played
					put("knight", "5_35");      // Gondorian Knight: a [gondor] companion
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession
					put("cover", "5_40");       // Take Cover: a [gondor] Fellowship event
					put("runner", "1_178");     // Goblin Runner: a [moria] minion

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeartsRaisedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hearts Raised
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the start of your regroup phase, you may spot a [gondor] Man and discard a card from hand to remove a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hearts");

		assertEquals("Hearts Raised", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseACardDiscardedFromHandRemovesAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearts = scn.GetFreepsCard("hearts");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var armor = scn.GetFreepsCard("armor");
		var cover = scn.GetFreepsCard("cover");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToSupportArea(hearts);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(knight, armor, cover, runner);
		assertInZone(Zone.SUPPORT, hearts);
		assertInHand(knight, armor, cover, runner);

		scn.StartGame();
		// Frodo and Boromir: room for two threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearts));
		scn.FreepsAcceptOptionalTrigger();

		// Any card: a companion, a possession, an event, a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, armor, cover, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertInDiscard(runner);
		assertInHand(knight, armor, cover);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, hearts);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheGondorManAllyIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearts = scn.GetFreepsCard("hearts");
		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(hearts);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(knight);
		assertInZone(Zone.SUPPORT, hearts, merchant);
		assertInHand(knight);

		scn.StartGame();
		// Frodo alone: room for one threat.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearts));
		scn.FreepsAcceptOptionalTrigger();
		// The one card in hand is discarded unasked.
		assertInDiscard(knight);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAGondorHobbitAndARohanManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearts = scn.GetFreepsCard("hearts");
		var pippin = scn.GetFreepsCard("pippin");
		var household = scn.GetFreepsCard("household");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(hearts);
		scn.MoveCompanionsToTable(pippin, household);
		scn.MoveCardsToHand(knight);
		assertInZone(Zone.SUPPORT, hearts);
		assertInHand(knight);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hearts));
		assertEquals(1, scn.GetThreats());
		assertInHand(knight);
	}

	@Test
	public void NotOfferedWithAnEmptyHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearts = scn.GetFreepsCard("hearts");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(hearts);
		scn.MoveCompanionsToTable(boromir);
		assertInZone(Zone.SUPPORT, hearts);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hearts));
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void WithNoThreatItIsStillOfferedAndTheCardIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearts = scn.GetFreepsCard("hearts");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(hearts);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(knight, armor);
		assertInZone(Zone.SUPPORT, hearts);
		assertInHand(knight, armor);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// An optional trigger with a cost is offered whenever the cost can be paid (Ketura, Q-0034).
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearts));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(armor);

		assertInDiscard(armor);
		assertInHand(knight);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhaseAndDecliningAtRegroupLeavesHandAndThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearts = scn.GetFreepsCard("hearts");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hearts);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(knight);
		assertInZone(Zone.SUPPORT, hearts);
		assertInHand(knight);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hearts));

		// Nobody is assigned to the Troop: on to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearts));
		scn.FreepsDeclineOptionalTrigger();

		assertInHand(knight);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheStartOfASecondRegroupPhaseInTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearts = scn.GetFreepsCard("hearts");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(hearts);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(knight, armor);
		assertInZone(Zone.SUPPORT, hearts);
		assertInHand(knight, armor);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearts));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(armor);
		assertInDiscard(armor);
		assertEquals(1, scn.GetThreats());

		// A second move, and with no minion a second regroup phase in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetMoveCount());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearts));
		scn.FreepsAcceptOptionalTrigger();
		// The one card left in hand is discarded unasked.
		assertInDiscard(knight);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
