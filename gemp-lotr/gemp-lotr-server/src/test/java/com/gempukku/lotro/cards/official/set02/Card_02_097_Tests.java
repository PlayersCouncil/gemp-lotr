package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("consorting", "2_97");
					put("consorting2", "2_97");

					// The two Bilbos, never in play together: the ally and the companion.
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3, home 3F; his fellowship ability is used only on purpose
					put("bilbocomp", "12_119"); // Bilbo, Melancholy Hobbit: [shire] Hobbit companion, strength 3, vitality 4; his start-of-skirmish trigger needs a [shire] skirmish event in the discard pile
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a [shire] Hobbit ally who is no Bilbo, vitality 3; his ability is used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2; his skirmish ability is used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion who is no Bilbo, strength 3, vitality 4; his skirmish ability is used only on purpose

					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."  Vitality 3
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
					put("marksman", "1_176");   // Goblin Marksman: archer, strength 7, vitality 1; no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ConsortingWithWizardsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Consorting With Wizards
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Tale</b>. Bearer must be Bilbo.
		 * 	Bilbo may not take wounds (except during the archery phase).
		 * 	<b>Fellowship</b> <i>or</i> <b>Regroup:</b> Exert Bilbo or discard this condition to remove (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("consorting");

		assertEquals("Consorting With Wizards", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutBilboAndGoesOnTheAllyBilboOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbo");
		var maggot = scn.GetFreepsCard("maggot");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(consorting);

		scn.StartGame();

		// A [shire] Hobbit companion and a [shire] Hobbit ally, neither of them Bilbo.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(consorting));

		scn.MoveAlliesToTable(bilbo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(consorting));

		scn.FreepsPlayCard(consorting);
		// Bilbo is unique, so he is the one bearer there can be, and it goes on him unasked.
		assertAttachedTo(consorting, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedOnTheCompanionBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbocomp");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(bilbo, merry);
		scn.MoveCardsToHand(consorting);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(consorting));
		scn.FreepsPlayCard(consorting);
		assertAttachedTo(consorting, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheAllyBilboBearingItIsNotOfferedForAWoundInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbo");
		var maggot = scn.GetFreepsCard("maggot");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bilbo, maggot, bounder);
		scn.AttachCardsTo(bilbo, consorting);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(consorting, bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);
		// The other two Hobbit allies may be wounded; Bilbo may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(maggot, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo));
		scn.ShadowChooseCard(maggot);

		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheCompanionBilboBearingItTakesNoWoundForASkirmishHeLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbocomp");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(bilbo, merry);
		scn.AttachCardsTo(bilbo, consorting);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		assertAttachedTo(consorting, bilbo);
		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, runner1}, new PhysicalCardImpl[]{merry, runner2});

		assertAttachedTo(consorting, bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));

		// Bilbo 3 against the Runner's 5: he loses, and takes no wound.
		scn.FreepsResolveSkirmish(bilbo);
		// His own start-of-skirmish trigger has a cost and is offered: declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// Merry 3 against the other Runner's 5 loses and is wounded, as a loser is.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheCompanionBilboBearingItTakesAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbocomp");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(bilbo);
		scn.AttachCardsTo(bilbo, consorting);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();

		assertAttachedTo(consorting, bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The minion archery total is 1: Bilbo may take it in the archery phase.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bilbo));
		scn.FreepsChooseCard(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void FellowshipActionAsksWhichCostAndTheAllyBilboExertsToRemoveOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(bilbo, consorting);

		scn.StartGame();
		scn.SetTwilight(2);

		assertAttachedTo(consorting, bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(consorting));

		scn.FreepsUseCardAction(consorting);
		// Both costs can be paid, so the player is asked which.
		assertEquals(List.of("Exert Bilbo", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert Bilbo");

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(consorting, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsTheCompanionBilboAndThenDiscardsTheConditionToRemoveOneEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var consorting2 = scn.GetFreepsCard("consorting2");
		var bilbo = scn.GetFreepsCard("bilbocomp");
		scn.MoveCompanionsToTable(bilbo);
		// Two copies on Bilbo: "this condition" is the copy whose ability is used.
		scn.AttachCardsTo(bilbo, consorting, consorting2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertAttachedTo(consorting, bilbo);
		assertAttachedTo(consorting2, bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(consorting));

		scn.FreepsUseCardAction(consorting);
		assertEquals(List.of("Exert Bilbo", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert Bilbo");
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(consorting, bilbo);

		scn.ShadowPass();
		scn.FreepsUseCardAction(consorting);
		assertEquals(List.of("Exert Bilbo", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Discard this condition");
		assertInDiscard(consorting);
		assertAttachedTo(consorting2, bilbo);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheAllyBilboExhaustedTheConditionIsDiscardedUnaskedAndNobodyElseExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbo");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveAlliesToTable(bilbo, maggot);
		// Vitality 3 with 2 wounds: Bilbo cannot exert.  Farmer Maggot, a [shire] Hobbit ally beside him, could.
		scn.AddWoundsToChar(bilbo, 2);
		scn.AttachCardsTo(bilbo, consorting);

		scn.StartGame();
		scn.SetTwilight(2);

		assertAttachedTo(consorting, bilbo);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(consorting));

		scn.FreepsUseCardAction(consorting);
		// Only the discard can be paid: nothing is asked.
		assertInDiscard(consorting);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var consorting = scn.GetFreepsCard("consorting");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(bilbo, consorting);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		// Bilbo could exert and the condition could be discarded, with twilight to remove.
		assertAttachedTo(consorting, bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(consorting));
	}
}
