package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "7_5");
					put("dark2", "7_5");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, cost 2, vitality 3; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion, here only stacked
					put("endurance", "2_4");    // Endurance of Dwarves: on a Dwarf, vitality +1; a skirmish ability used only on purpose
					put("lord", "1_21");        // Lord of Moria: a [dwarven] support-area condition, which is no Dwarf; each Dwarf is damage +1
					put("rock", "4_50");        // Here Is Good Rock: [dwarven] condition that play stacks Free Peoples cards on; a skirmish ability only

					// For the discard piles.
					put("axestrike", "1_3");    // Axe Strike: [dwarven] Skirmish event
					put("halls", "1_26");       // Their Halls of Stone: [dwarven] Skirmish event
					put("company", "4_42");     // Best Company: [dwarven] Skirmish event, for the other player's discard pile
					put("delving", "1_6");      // Delving: [dwarven] Fellowship event
					put("swordarm", "1_116");   // Swordarm of the White Tower: [gondor] Skirmish event

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkWaysStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Dark Ways
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Dwarf.<br>Each time the fellowship moves, add a threat or discard this condition.<br><b>Regroup:</b> Discard this condition to heal a Dwarf twice and take a [dwarven] skirmish event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");

		assertEquals("Dark Ways", dark.getBlueprint().getTitle());
		assertNull(dark.getBlueprint().getSubtitle());
		assertFalse(dark.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, dark.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, dark.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, dark.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(dark, Keyword.SUPPORT_AREA));
		assertEquals(0, dark.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilADwarfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		var rock = scn.GetFreepsCard("rock");
		var guard = scn.GetFreepsCard("guard");
		// A companion who is no Dwarf, a [dwarven] card that is no Dwarf, and a Dwarf stacked, who is not in play.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lord, rock);
		scn.StackCardsOn(rock, guard);
		scn.MoveCardsToHand(dark, gimli);

		scn.StartGame();

		assertInHand(dark, gimli);
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(dark));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(dark));

		scn.FreepsPlayCard(dark);
		assertInZone(Zone.SUPPORT, dark);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(dark);

		scn.StartGame();

		assertInHand(dark);
		assertTrue(scn.FreepsPlayAvailable(dark));

		scn.FreepsPlayCard(dark);
		assertInZone(Zone.SUPPORT, dark);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveOfTheFellowshipAddsAThreatWhenThatIsChosenAndAsksAgainOnTheRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(dark);
		// Three companions with Frodo, so the threat limit (3) is no obstacle to two threats.
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, dark);
		scn.FreepsPass();

		// Moving from site 1 to site 2.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, dark);

		// No minions, so the game goes on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, dark);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToDiscardItLeavesTheThreatsAndAnotherConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var lord = scn.GetFreepsCard("lord");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(dark, lord);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, dark, lord);
		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		assertInDiscard(dark);
		assertInZone(Zone.SUPPORT, lord);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheMoveDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(dark);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// Frodo and Gimli: a threat limit of 2.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, dark);

		scn.FreepsPass();

		// A threat cannot be added, so discarding is all that can be done.
		assertInDiscard(dark);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnAMoveAndDiscardingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var dark2 = scn.GetFreepsCard("dark2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(dark, dark2);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, dark, dark2);
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

		var dark = scn.GetFreepsCard("dark");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SkipToMovementDecision();
		// Put down after the first move of the turn, so that the regroup decision is the first it sees.
		scn.MoveCardsToSupportArea(dark);
		assertInZone(Zone.SUPPORT, dark);
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, dark);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void RegroupAbilityDiscardsItToHealADwarfTwiceAndTakeADwarvenSkirmishEventIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var dark2 = scn.GetFreepsCard("dark2");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var endurance = scn.GetFreepsCard("endurance");
		var lord = scn.GetFreepsCard("lord");
		var axestrike = scn.GetFreepsCard("axestrike");
		var halls = scn.GetFreepsCard("halls");
		var delving = scn.GetFreepsCard("delving");
		var swordarm = scn.GetFreepsCard("swordarm");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Gimli at vitality 4, so that he can carry 3 wounds: one heal, two and three read 2, 1 and 0.
		scn.AttachCardsTo(gimli, endurance);
		scn.MoveCardsToSupportArea(dark, dark2);
		scn.MoveCardsToDiscard(axestrike, halls, delving, swordarm, lord);
		// The other player's own [dwarven] skirmish event, in his discard pile, of a title this player's pile does not hold.
		var theirCompany = scn.GetShadowCard("company");
		scn.MoveCardsToDiscard(theirCompany);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 3);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertInZone(Zone.SUPPORT, dark, dark2);
		assertAttachedTo(endurance, gimli);
		assertEquals(3, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(axestrike, halls, delving, swordarm, lord, theirCompany);

		// The move to site 2: each copy asks, and a threat is added for each.
		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SUPPORT, dark, dark2);
		assertTrue(scn.FreepsActionAvailable(dark));
		scn.FreepsUseCardAction(dark);

		// The cost: this copy, and only this copy, is discarded.
		assertInDiscard(dark);
		assertInZone(Zone.SUPPORT, dark2);

		// A wounded Dwarf companion and a wounded Dwarf ally are offered; wounded companions who are no Dwarf are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		// Two wounds of three healed from the one Dwarf.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// [dwarven] skirmish events in his own discard pile are offered; a [dwarven] Fellowship event, a [gondor]
		// Skirmish event, a [dwarven] condition and the other player's own event are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axestrike, halls));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(delving, swordarm, lord, theirCompany));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(axestrike);

		assertInHand(axestrike);
		assertInDiscard(halls, delving, swordarm, lord, theirCompany);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void EachHalfIsCarriedOutWhereTheOtherHasNothingToActOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var dark2 = scn.GetFreepsCard("dark2");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(dark, dark2);
		scn.MoveCardsToDiscard(axestrike);

		scn.StartGame();
		// Only a companion who is no Dwarf is wounded.
		scn.AddWoundsToChar(aragorn, 2);
		assertInZone(Zone.SUPPORT, dark, dark2);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInDiscard(axestrike);

		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");
		scn.FreepsChooseOption("Add a threat");
		scn.SkipToPhase(Phase.REGROUP);

		// No wounded Dwarf: offered all the same (it has a cost), nothing healed, and the event taken.
		assertTrue(scn.FreepsActionAvailable(dark));
		scn.FreepsUseCardAction(dark);
		assertInDiscard(dark);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInHand(axestrike);

		// Now a wounded Dwarf and no [dwarven] skirmish event in the discard pile: he is healed and nothing taken.
		scn.ShadowPass();
		scn.AddWoundsToChar(gimli, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(dark2));
		scn.FreepsUseCardAction(dark2);
		assertInDiscard(dark2);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(axestrike);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetFreepsCard("dark");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(dark);
		scn.MoveCardsToDiscard(axestrike);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInDiscard(axestrike);
		assertInZone(Zone.SUPPORT, dark);
		scn.MoveMinionsToTable(troop);

		scn.FreepsPass();
		scn.FreepsChooseOption("Add a threat");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dark));
		assertInZone(Zone.SUPPORT, dark);
	}
}
