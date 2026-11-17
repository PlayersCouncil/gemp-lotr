package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mounds", "15_122");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3, valiant and nothing else
					// Éowyn, Lady of Ithilien: a [rohan] Man companion, strength 6; "Skirmish: Exert Éowyn to choose an
					// opponent. That opponent must wound a minion for each wound on each minion skirmishing Éowyn."
					put("eowyn", "10_72");
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Éowyn) can be spotted
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion; a skirmish ability used only on purpose
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally; a fellowship ability used only on purpose

					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not met when cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BurialMoundsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Burial Mounds
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Make a [rohan] companion <b>defender +1</b> and minions skirmishing that companion cannot take wounds during a skirmish involving that companion until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mounds");

		assertEquals("Burial Mounds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesARohanCompanionDefenderPlus1AndTheMinionsHeDefendsAgainstTakeNoWoundsInHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mounds = scn.GetFreepsCard("mounds");
		var guard = scn.GetFreepsCard("guard");
		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard, eowyn, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(mounds);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(mounds);
		scn.MoveMinionsToTable(savage1, savage2, runner);
		// One wound on the minion Éowyn will fight, so that her ability has the Shadow player wound one minion.
		scn.AddWoundsToChar(savage2, 1);
		assertEquals(1, scn.GetWoundsOn(savage2));
		// Strength that only arranges the board: the Guard beats the two minions he defends against (11 to 5 + 5).
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 5));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(mounds));
		scn.FreepsPlayCard(mounds);

		// Two [rohan] companions; not the [rohan] ally nor the [gondor] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, eowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, guma));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInDiscard(mounds);
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(eowyn, Keyword.DEFENDER));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, savage1, runner },
				new PhysicalCardImpl[] { eowyn, savage2 }
		);

		// Éowyn's skirmish first. A minion assigned to the Guard and waiting for his skirmish can still be wounded.
		scn.FreepsResolveSkirmish(eowyn);
		assertEquals(0, scn.GetWoundsOn(savage1));
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage1));
		scn.ShadowChooseCard(savage1);
		assertEquals(1, scn.GetWoundsOn(savage1));

		// 6 against 5: Éowyn wins, and the minion skirmishing her, a [rohan] companion not chosen, takes its wound.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage2));

		// The Guard's skirmish, after one already resolved: he is still defender +1, and he wins.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		scn.PassSkirmishActions();
		// Neither minion he skirmished takes the loser's wound: the Savage keeps its 1, the Runner (vitality 1) lives.
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInPlay(runner);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
	}

	@Test
	public void MinionsNotSkirmishingThatCompanionCanBeWoundedDuringHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mounds = scn.GetFreepsCard("mounds");
		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(eowyn, boromir);
		scn.MoveCardsToHand(mounds);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(mounds);
		scn.MoveMinionsToTable(savage1, runner, savage2, troop);
		// One wound on a minion skirmishing Éowyn: her ability then has the Shadow player wound one minion.
		scn.AddWoundsToChar(savage1, 1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		// Strength that only arranges the board: Éowyn beats the two minions she defends against (11 to 5 + 5).
		scn.ApplyAdHocModifier(new StrengthModifier(null, eowyn, null, 5));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Éowyn is the only [rohan] companion: chosen unasked.
		scn.FreepsPlayCard(mounds);
		assertEquals(1, scn.GetKeywordCount(eowyn, Keyword.DEFENDER));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eowyn, savage1, runner },
				new PhysicalCardImpl[] { boromir, savage2 }
		);
		// The Troop is left over, and the Shadow player leaves it unassigned.
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(troop));

		scn.FreepsResolveSkirmish(eowyn);
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsUseCardAction(eowyn);

		// Only the minions skirmishing her cannot take wounds: one waiting for Boromir's skirmish and one in no
		// skirmish can.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage2, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage1, runner));
		scn.ShadowChooseCard(savage2);
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.PassSkirmishActions();
		// She wins, and neither minion she skirmished takes the loser's wound.
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInPlay(runner);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void MakesARohanHobbitCompanionDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mounds = scn.GetFreepsCard("mounds");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		// Two [rohan] companions only, so Merry is no Man.
		scn.MoveCompanionsToTable(merry, guard);
		scn.MoveCardsToHand(mounds);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		assertInHand(mounds);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DEFENDER));
		scn.FreepsPlayCard(mounds);

		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, guard));
		scn.FreepsChooseCard(merry);

		assertInDiscard(mounds);
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mounds = scn.GetFreepsCard("mounds");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(mounds);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		assertInHand(mounds);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsPlayAvailable(mounds));
		scn.FreepsPlayCard(mounds);

		assertInDiscard(mounds);
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AfterTheRegroupPhaseBeginsAMinionSkirmishingHimCanBeWoundedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mounds = scn.GetFreepsCard("mounds");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(mounds);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		assertInHand(mounds);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPlayCard(mounds);
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DEFENDER));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(guard, savage1);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// 6 against 5: the Guard wins, and the Savage takes no wound.
		assertEquals(0, scn.GetWoundsOn(savage1));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));

		// A second move, and a skirmish after the regroup phase began: the Savage loses again and is wounded.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage1);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mounds = scn.GetFreepsCard("mounds");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(mounds);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		assertInHand(mounds);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(mounds));
		assertInHand(mounds);
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
	}
}
