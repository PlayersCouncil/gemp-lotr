package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elladan", "14_2");
					put("elrohir", "14_3");     // Elrohir, Son of Elrond: the other twin; his trigger weakens minions, which no test here reads
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf companion of another title, cost 2; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability only

					// Spring Forth Nimbly: [elven] Skirmish event, cost 2, "Make an Elf strength +1 for each card that has
					// an [elven] token on it": with no token in play it gives +0
					put("nimbly", "13_24");
					put("nimbly2", "13_24");
					put("nimbly3", "13_24");
					// An Honorable Charge: [rohan] Skirmish event, cost 1, "Make an unbound companion strength +1 for each
					// [rohan] Man you spot (limit +3)": with no [rohan] Man it gives +0
					put("charge", "4_282");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElladanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Elladan, Son of Elrond
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Elrohir is twilight cost -2.<br>Each time you play an [elven] event during a skirmish involving Elladan or Elrohir, that event is twilight cost -2.
		*/

		var scn = GetScenario();

		var elladan = scn.GetFreepsCard("elladan");

		assertEquals("Elladan", elladan.getBlueprint().getTitle());
		assertEquals("Son of Elrond", elladan.getBlueprint().getSubtitle());
		assertTrue(elladan.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, elladan.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, elladan.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, elladan.getBlueprint().getCardType());
		assertEquals(Race.ELF, elladan.getBlueprint().getRace());
		assertEquals(3, elladan.getBlueprint().getTwilightCost());
		assertEquals(7, elladan.getBlueprint().getStrength());
		assertEquals(3, elladan.getBlueprint().getVitality());
		assertEquals(6, elladan.getBlueprint().getResistance());
	}

	@Test
	public void WhileElladanIsInPlayElrohirCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elladan = scn.GetFreepsCard("elladan");
		var elrohir = scn.GetFreepsCard("elrohir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(elladan, elrohir, legolas);

		scn.StartGame();
		assertInHand(elladan, elrohir, legolas);
		assertEquals(3, scn.GetTwilightCost(elrohir));
		assertEquals(2, scn.GetTwilightCost(legolas));

		scn.FreepsPlayCard(elladan);
		assertInZone(Zone.FREE_CHARACTERS, elladan);

		// Legolas, another Elf companion, is not Elrohir and keeps his printed cost.
		assertEquals(1, scn.GetTwilightCost(elrohir));
		assertEquals(2, scn.GetTwilightCost(legolas));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(elrohir);
		// 3 printed - 2
		assertEquals(pool + 1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, elrohir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachElvenEventPlayedInHisSkirmishCostsTwoLessAndOnlyThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elladan = scn.GetFreepsCard("elladan");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nimbly = scn.GetFreepsCard("nimbly");
		var nimbly2 = scn.GetFreepsCard("nimbly2");
		var nimbly3 = scn.GetFreepsCard("nimbly3");
		var charge = scn.GetFreepsCard("charge");
		scn.MoveCompanionsToTable(elladan, aragorn);
		scn.MoveCardsToHand(nimbly, nimbly2, nimbly3, charge);

		var troop = scn.GetShadowCard("troop");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, warrior);
		// No [elven] skirmish event costs more than 2, and no printed card raises a Free Peoples event's cost: +1 on
		// every event, a number that only arranges the board, so that -2 (3 to 1) is told from -3 (0) and -1 (2).
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, CardType.EVENT, null, new ConstantEvaluator(1)));
		assertInHand(nimbly, nimbly2, nimbly3, charge);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elladan, troop}, new PhysicalCardImpl[]{aragorn, warrior});

		// Aragorn's skirmish first: Elladan is assigned, and his skirmish has not begun.
		scn.FreepsResolveSkirmish(aragorn);
		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(nimbly);
		// 2 printed + 1
		assertEquals(pool + 3, scn.GetTwilight());
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(elladan);
		pool = scn.GetTwilight();
		scn.FreepsPlayCard(nimbly2);
		// 2 printed + 1 - 2
		assertEquals(pool + 1, scn.GetTwilight());

		scn.ShadowPass();
		scn.FreepsPlayCard(nimbly3);
		// Each [elven] event of the skirmish: 2 printed + 1 - 2 again
		assertEquals(pool + 2, scn.GetTwilight());

		scn.ShadowPass();
		scn.FreepsPlayCard(charge);
		scn.FreepsChooseCard(elladan);
		// A [rohan] event pays its whole cost: 1 printed + 1
		assertEquals(pool + 4, scn.GetTwilight());
		assertInDiscard(nimbly, nimbly2, nimbly3, charge);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnElvenEventInElrohirsSkirmishCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elladan = scn.GetFreepsCard("elladan");
		var elrohir = scn.GetFreepsCard("elrohir");
		var nimbly = scn.GetFreepsCard("nimbly");
		scn.MoveCompanionsToTable(elladan, elrohir);
		scn.MoveCardsToHand(nimbly);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// As above: +1 on every event, so that -2 is told from -3.
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, CardType.EVENT, null, new ConstantEvaluator(1)));
		assertInHand(nimbly);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrohir, troop);

		scn.FreepsResolveSkirmish(elrohir);
		assertFalse(scn.IsCharAssigned(elladan));
		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(nimbly);
		scn.FreepsChooseCard(elrohir);
		// 2 printed + 1 - 2
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
