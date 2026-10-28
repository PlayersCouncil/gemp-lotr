package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("advisor", "7_42");
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: vitality 4; a fellowship ability never used here
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is not Gandalf, vitality 4; stronger only beside 3 Ents

					put("guard", "5_83");         // Household Guard: a [rohan] Man companion, valiant and nothing else
					put("merry", "103_85");       // Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only beside 3 [rohan] companions,
					                              // his trigger answers another [rohan] Man exerting and his skirmish ability is never used
					put("herdsman", "7_247");     // Rohirrim Herdsman: a [rohan] Man ally; a regroup ability never used here
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability never used here

					put("siege", "8_106");        // Siege Troop: [sauron] Orc, vitality 4; a regroup ability never used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");       // Goblin Runner: vitality 1, so he cannot exert; adds (2) when played, which he never is here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingsAdvisorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: King's Advisor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Gandalf twice to make an opponent exert a minion for each [rohan] companion you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("advisor");

		assertEquals("King's Advisor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfTwiceAndTheShadowPlayerExertsMinionsOfHisChoiceOnceForEachRohanCompanionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advisor = scn.GetFreepsCard("advisor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var siege = scn.GetShadowCard("siege");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, avenger, guard, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(advisor);

		scn.StartGame();
		assertInHand(advisor);
		scn.MoveMinionsToTable(siege, troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(siege));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsPlayAvailable(advisor));
		scn.FreepsPlayCard(advisor);

		// Gandalf pays both exertions; the other [gandalf] companion does not.
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// The Guard and Merry, a Man and a Hobbit, are the [rohan] companions; the [rohan] ally and the
		// [gondor] companion are not counted.  The player may spot 0 to 2, and all 2 are filled in.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// The Shadow player chooses each minion; the Runner cannot exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(siege, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		// Companions who could exert are no minions.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(avenger, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(siege);
		// For each: the same minion may exert again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(siege, troop));
		scn.ShadowChooseCard(siege);

		assertInDiscard(advisor);
		assertEquals(2, scn.GetWoundsOn(siege));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingOneOfTwoRohanCompanionsMakesOneMinionExertOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advisor = scn.GetFreepsCard("advisor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var siege = scn.GetShadowCard("siege");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, guard, merry);
		scn.MoveCardsToHand(advisor);

		scn.StartGame();
		assertInHand(advisor);
		scn.MoveMinionsToTable(siege, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(siege));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(advisor);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertTrue(scn.ShadowHasCardChoiceAvailable(siege, troop));
		scn.ShadowChooseCard(troop);

		// One exertion, and no second choice.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(siege));
		assertInDiscard(advisor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionGandalfExertsTwiceForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advisor = scn.GetFreepsCard("advisor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(advisor);

		scn.StartGame();
		assertInHand(advisor);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(advisor));
		scn.FreepsPlayCard(advisor);

		// A [gondor] companion and a [rohan] ally: nothing to spot, nothing asked, nobody exerted.
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(advisor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithARohanCompanionSpottedAndNoMinionAbleToExertNothingIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advisor = scn.GetFreepsCard("advisor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, guard);
		scn.MoveCardsToHand(advisor);

		scn.StartGame();
		assertInHand(advisor);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(advisor));
		scn.FreepsPlayCard(advisor);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		scn.FreepsChoose("1");

		// The only minion has 1 vitality: played, paid, and nobody exerts.
		assertInPlay(runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInDiscard(advisor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advisor = scn.GetFreepsCard("advisor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger, guard);
		scn.MoveCardsToHand(advisor);

		scn.StartGame();
		assertInHand(advisor);
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 4 with 2 wounds: he can exert once, not twice.  The fresh Ent Avenger is no Gandalf.
		assertFalse(scn.FreepsPlayAvailable(advisor));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
