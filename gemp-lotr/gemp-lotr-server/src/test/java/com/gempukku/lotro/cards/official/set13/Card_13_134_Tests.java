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

public class Card_13_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ride", "13_134");
					// Théoden, Tall and Proud: [rohan] Man companion, strength 7, vitality 3; valiant, a cost reduction
					// and a trigger on his own death, none of which comes up here
					put("theoden", "8_92");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3; his texts are used only on purpose
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
	public void RideWithMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Ride With Me
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make your [rohan] companion strength +2 (or exert Théoden to make that companion strength +5 and <b>damage +1</b> instead).
		*/

		var scn = GetScenario();

		var ride = scn.GetFreepsCard("ride");

		assertEquals("Ride With Me", ride.getBlueprint().getTitle());
		assertNull(ride.getBlueprint().getSubtitle());
		assertFalse(ride.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ride.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, ride.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, ride.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(ride, Timeword.SKIRMISH));
		assertEquals(1, ride.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTheodenExhaustedItMakesARohanCompanionStrengthPlus2ForThatSkirmishWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ride = scn.GetFreepsCard("ride");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(theoden, guard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(ride);
		assertInHand(ride);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Vitality 3, 2 wounds: Théoden cannot exert.  The Guard, another [rohan] companion, could.
		scn.AddWoundsToChar(theoden, 2);
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{boromir, savage});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsPlayCard(ride);
		// Nothing asked about Théoden: straight to the target.  The [rohan] companions, a Man in this skirmish, a
		// Hobbit in none and Théoden himself; not the [rohan] Man ally, the [gondor] companion or Frodo.
		assertFalse(scn.FreepsDecisionAvailable("exert"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry, theoden));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		// 3 + 2; nobody exerted.
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(6, scn.GetStrength(guard));
		assertInDiscard(ride);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonus is gone there.  (The Guard, 6 against 13, is
		// overwhelmed.)
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
	}

	@Test
	public void ExertingTheodenMakesThatCompanionStrengthPlus5AndDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ride = scn.GetFreepsCard("ride");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(theoden, guard, boromir);
		scn.MoveCardsToHand(ride);
		assertInHand(ride);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{boromir, savage});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(theoden));

		scn.FreepsPlayCard(ride);
		// Théoden can exert: the player is asked whether to pay for the greater bonus (Q-0089).
		assertTrue(scn.FreepsDecisionAvailable("exert"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, theoden));
		scn.FreepsChooseCard(guard);

		// 6 + 5 and damage +1 on the companion chosen; Théoden, who paid, is not strengthened.
		assertEquals(11, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertInDiscard(ride);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 11 against 13: the Guard loses and takes a wound.  In the next skirmish both bonuses are gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
	}

	@Test
	public void WithTheodenAbleToExertThePlayerMayTakeThePlus2Instead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ride = scn.GetFreepsCard("ride");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(ride);
		assertInHand(ride);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsPlayCard(ride);
		assertTrue(scn.FreepsDecisionAvailable("exert"));
		scn.FreepsChooseNo();
		scn.FreepsChooseCard(guard);

		// 6 + 2, and Théoden unexerted.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertInDiscard(ride);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ride = scn.GetFreepsCard("ride");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(ride);
		assertInHand(ride);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ride));

		// In the skirmish that follows, on the same board, it is.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(ride));
	}

	@Test
	public void WithNoRohanCompanionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ride = scn.GetFreepsCard("ride");
		var frodo = scn.GetRingBearer();
		var guma = scn.GetFreepsCard("guma");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(ride);
		assertInHand(ride);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		// A [gondor] companion in the skirmish and a [rohan] ally: no [rohan] companion to choose, and no Théoden.
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(ride));
		scn.FreepsPlayCard(ride);

		assertInDiscard(ride);
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
