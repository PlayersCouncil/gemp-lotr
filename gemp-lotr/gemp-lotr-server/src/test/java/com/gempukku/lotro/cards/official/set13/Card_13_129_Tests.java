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

public class Card_13_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hamstrung", "13_129");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3; a Man only while 3 [rohan]
					// companions (or Éowyn) can be spotted, which is never so here; his texts are used only on purpose
					put("merry", "103_85");
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					put("guma", "4_277");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion, strength 7; his skirmish ability is not used
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HamstrungStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Hamstrung
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] companion strength +2 (or +5 if the fellowship has moved twice this turn).
		*/

		var scn = GetScenario();

		var hamstrung = scn.GetFreepsCard("hamstrung");

		assertEquals("Hamstrung", hamstrung.getBlueprint().getTitle());
		assertNull(hamstrung.getBlueprint().getSubtitle());
		assertFalse(hamstrung.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, hamstrung.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, hamstrung.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hamstrung.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(hamstrung, Timeword.SKIRMISH));
		assertEquals(1, hamstrung.getBlueprint().getTwilightCost());
	}

	@Test
	public void AfterOneMoveItMakesARohanCompanionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hamstrung = scn.GetFreepsCard("hamstrung");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(guard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(hamstrung);
		assertInHand(hamstrung);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hamstrung));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{boromir, savage});
		scn.FreepsResolveSkirmish(guard);

		// One move this turn, the one at the end of the fellowship phase.
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));

		scn.FreepsPlayCard(hamstrung);
		// The [rohan] companions, a Man in this skirmish and a Hobbit in none; not the [rohan] Man ally, nor the
		// [gondor] companion, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		// +2, not +5: the fellowship has moved once.
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(boromir));
		assertInDiscard(hamstrung);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonus is gone there.  (The Guard, 6 against 13, is
		// overwhelmed.)
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
	}

	@Test
	public void AfterTheFellowshipHasMovedTwiceThisTurnItMakesTheCompanionStrengthPlus5ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hamstrung = scn.GetFreepsCard("hamstrung");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(hamstrung);
		assertInHand(hamstrung);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// The move at the end of the fellowship phase; no minion, so straight on to the regroup phase, and a second move.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsPlayCard(hamstrung);
		// The one [rohan] companion is chosen unasked: 6 + 5.
		assertEquals(11, scn.GetStrength(guard));
		assertInDiscard(hamstrung);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 11 against 13: the Guard loses and takes a wound, and lives.  The bonus ended with the skirmish.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(6, scn.GetStrength(guard));
	}

	@Test
	public void WithNoRohanCompanionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hamstrung = scn.GetFreepsCard("hamstrung");
		var frodo = scn.GetRingBearer();
		var guma = scn.GetFreepsCard("guma");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(hamstrung);
		assertInHand(hamstrung);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		// A [gondor] companion in the skirmish and a [rohan] ally: no [rohan] companion to choose.
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(hamstrung));
		scn.FreepsPlayCard(hamstrung);

		assertInDiscard(hamstrung);
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
