package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_021_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sons", "101_21");

					// Aragorn, Ranger of the North: strength 8.  A maneuver ability, never used here.
					put("aragorn", "1_89");
					// Boromir, Son of Denethor: strength 7.  A skirmish ability, never used here.
					put("boromir", "1_97");
					// Gondorian Ranger: a [gondor] Man companion who is neither of them, strength 5.  His text
					// changes the site number of minions assigned to him and no strength.
					put("ranger", "4_122");

					// Goblin Runner: strength 5, vitality 1.  Its text is about being played, and these never are.
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The one Aragorn who is not a [gondor] card.
	protected VirtualTableScenario GetRohanAragornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sons", "101_21");

					// Aragorn, Defender of Rohan: a [rohan] Man, strength 8.  His text gives [rohan] companions
					// hunter while the Ring-bearer is assigned to a skirmish, and Frodo never is.
					put("aragorn", "17_93");
					// Boromir, Son of Denethor: strength 7.  A skirmish ability, never used here.
					put("boromir", "1_97");
					// Gondorian Ranger: a [gondor] Man companion who is neither of them, strength 5.
					put("ranger", "4_122");

					// Goblin Runner: strength 5, vitality 1.
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SonsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: The Sons of Gondor Have Returned
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While Boromir is assigned to skirmish more than one minion, Aragorn is strength +3.
		 * 	While Aragorn is assigned to skirmish more than one minion, Boromir is strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sons");

		assertEquals("The Sons of Gondor Have Returned", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AragornIsStrengthPlus3WhileBoromirIsAssignedToTwoMinionsAndBoromirIsNotRaisedByAragornsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sons = scn.GetFreepsCard("sons");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(aragorn, boromir, ranger);
		scn.MoveCardsToSupportArea(sons);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(ranger));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, runner1},
				new PhysicalCardImpl[]{aragorn, runner2});
		// The third Runner is left over, and the Shadow player puts it on Boromir as well.
		scn.ShadowAssignToMinions(boromir, runner3);

		assertTrue(scn.IsCharAssignedAgainst(boromir, runner1));
		assertTrue(scn.IsCharAssignedAgainst(boromir, runner3));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner2));
		assertEquals(11, scn.GetStrength(aragorn));     // 8 + 3
		// Aragorn is assigned to one minion, with two more on the table: Boromir gets nothing.
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(ranger));

		// The bonus is there for Aragorn's own skirmish, which is fought while Boromir waits with his two.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 11 overwhelms the Runner's 5.  Boromir's 7 then loses to two Runners' 10.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// With every skirmish over nobody is assigned to anything.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsCharAssigned(boromir));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void AragornKeepsTheBonusWhileBoromirFightsHisTwoMinionsAndLosesItWhenThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sons = scn.GetFreepsCard("sons");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(sons);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(8, scn.GetStrength(aragorn));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, runner1},
				new PhysicalCardImpl[]{aragorn, runner2});
		scn.ShadowAssignToMinions(boromir, runner3);
		assertEquals(11, scn.GetStrength(aragorn));     // 8 + 3

		// A character stays assigned until his skirmish phase has ended, so Boromir is still assigned to two
		// minions while he fights them.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner3));
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir's 7 loses to the two Runners' 10, and his skirmish phase ends.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharAssigned(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BoromirIsStrengthPlus3WhileAragornIsAssignedToTwoMinionsAndAragornIsNotRaisedByBoromirsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sons = scn.GetFreepsCard("sons");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(aragorn, boromir, ranger);
		scn.MoveCardsToSupportArea(sons);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(ranger));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{boromir, runner2});
		// The third Runner is left over, and the Shadow player puts it on Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, runner3);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner3));
		assertTrue(scn.IsCharAssignedAgainst(boromir, runner2));
		assertEquals(10, scn.GetStrength(boromir));     // 7 + 3
		// Boromir is assigned to one minion, with two more on the table: Aragorn gets nothing.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(ranger));

		// The bonus is there for Boromir's own skirmish, which is fought while Aragorn waits with his two.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(boromir));
		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir's 10 overwhelms the Runner's 5.  Aragorn's 8 then loses to two Runners' 10.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// With every skirmish over nobody is assigned to anything.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
	}

	@Test
	public void BoromirKeepsTheBonusWhileAragornFightsHisTwoMinionsAndLosesItWhenThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sons = scn.GetFreepsCard("sons");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(sons);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(7, scn.GetStrength(boromir));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{boromir, runner2});
		scn.ShadowAssignToMinions(aragorn, runner3);
		assertEquals(10, scn.GetStrength(boromir));     // 7 + 3

		// A character stays assigned until his skirmish phase has ended, so Aragorn is still assigned to two
		// minions while he fights them.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner3));
		assertEquals(10, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 loses to the two Runners' 10, and his skirmish phase ends.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(boromir));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NeitherIsRaisedWhenAnotherGondorManIsTheOneAssignedToTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sons = scn.GetFreepsCard("sons");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(aragorn, boromir, ranger);
		scn.MoveCardsToSupportArea(sons);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3, runner4);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(ranger));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{ranger, runner1},
				new PhysicalCardImpl[]{aragorn, runner2},
				new PhysicalCardImpl[]{boromir, runner3});
		scn.ShadowAssignToMinions(ranger, runner4);

		// Four minions are assigned, and Aragorn and Boromir face one each.
		assertTrue(scn.IsCharAssignedAgainst(ranger, runner1));
		assertTrue(scn.IsCharAssignedAgainst(ranger, runner4));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner2));
		assertTrue(scn.IsCharAssignedAgainst(boromir, runner3));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(ranger));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAragornOfAnotherCultureIsRaisedAndRaisesBoromirWhenEachIsAssignedToTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanAragornScenario();

		var sons = scn.GetFreepsCard("sons");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(aragorn, boromir, ranger);
		scn.MoveCardsToSupportArea(sons);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3, runner4);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(ranger));

		// The Free Peoples player assigns nobody, and the Shadow player puts two Runners on each of them.
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1, runner2},
				new PhysicalCardImpl[]{boromir, runner3, runner4});

		assertEquals(11, scn.GetStrength(aragorn));     // 8 + 3
		assertEquals(10, scn.GetStrength(boromir));     // 7 + 3
		assertEquals(5, scn.GetStrength(ranger));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
