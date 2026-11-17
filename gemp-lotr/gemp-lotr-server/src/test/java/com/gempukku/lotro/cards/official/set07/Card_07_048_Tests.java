package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madness", "7_48");
					put("madness2", "7_48");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and no ability
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf; strength +2 only with 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
					put("champion", "7_49");    // Steadfast Champion: another condition of this player's, on Gandalf

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; a "when you play" add (2), harmless cheated in
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StayThisMadnessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stay This Madness
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Gandalf.<br>Each time the fellowship moves, add a threat or discard this condition.<br><b>Regroup:</b> Discard this condition to wound 2 minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("madness");

		assertEquals("Stay This Madness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		// A [gandalf] companion who is not Gandalf.
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(madness, gandalf);

		scn.StartGame();

		assertInHand(madness, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(madness));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(madness));

		scn.FreepsPlayCard(madness);
		assertInZone(Zone.SUPPORT, madness);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveOfTheFellowshipAddsAThreatWhenThatIsChosenAndAsksAgainOnTheRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(madness);
		// Three companions with Frodo, so the threat limit (3) is no obstacle to two threats.
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, madness);
		scn.FreepsPass();

		// Moving from site 1 to site 2.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, madness);

		// No minions, so the game goes on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, madness);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToDiscardItLeavesTheThreatsAndAnotherConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var gandalf = scn.GetFreepsCard("gandalf");
		var champion = scn.GetFreepsCard("champion");
		scn.MoveCardsToSupportArea(madness);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, madness);
		assertAttachedTo(champion, gandalf);
		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		assertInDiscard(madness);
		assertAttachedTo(champion, gandalf);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheMoveDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToSupportArea(madness);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		// Frodo and Gandalf: a threat limit of 2.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, madness);

		scn.FreepsPass();

		// A threat cannot be added, so discarding is all that can be done.
		assertInDiscard(madness);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnAMoveAndDiscardingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var madness2 = scn.GetFreepsCard("madness2");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToSupportArea(madness, madness2);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, madness, madness2);
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
	public void StayingInTheRegroupPhaseAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToMovementDecision();
		// Put down after the first move of the turn, so that the regroup decision is the first it sees.
		scn.MoveCardsToSupportArea(madness);
		assertInZone(Zone.SUPPORT, madness);
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, madness);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void RegroupAbilityDiscardsItToWoundTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var madness2 = scn.GetFreepsCard("madness2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, savage);
		// Put down after the move from site 1, so that its trigger does not take them away first.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.MoveCardsToSupportArea(madness, madness2);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, madness, madness2);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(madness));
		scn.FreepsUseCardAction(madness);

		// The cost: this copy, and only this copy.
		assertInDiscard(madness);
		assertInZone(Zone.SUPPORT, madness2);

		// Minions of two cultures and races are offered, companions are not; two different minions are chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(troop, runner);

		// A wound, not an exertion: the Runner, of vitality 1, is killed.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneMinionTheRegroupAbilityWoundsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.MoveCardsToSupportArea(madness);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, madness);
		assertEquals(0, scn.GetWoundsOn(troop));
		// It has a cost, so it is offered with only one minion to wound.
		assertTrue(scn.FreepsActionAvailable(madness));
		scn.FreepsUseCardAction(madness);

		assertInDiscard(madness);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetFreepsCard("madness");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(madness);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, madness);
		scn.MoveMinionsToTable(troop, savage);

		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(madness));
		assertInZone(Zone.SUPPORT, madness);
	}
}
