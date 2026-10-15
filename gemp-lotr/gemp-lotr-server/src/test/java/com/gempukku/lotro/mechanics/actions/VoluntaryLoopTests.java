package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * A loop of both players' actions is found, and put to the players (CR 4.2 "loops"; Ketura on Q-0321 and Q-0329).
 *
 * Comprehensive Rules 4.2, "loops": "The Free Peoples player has Smeagol, Slinker ("Skirmish: Add a burden to make
 * Sméagol strength +2...") skirmishing a minion at Anduin Banks ("Skirmish: Spot your minion and remove a burden to
 * make that minion strength +2."). The repeated adding and removing of a burden for strength bonuses creates a loop,
 * so the Free Peoples player names a number of iterations for these actions. The Shadow player then has two
 * choices: He may name a lower number of iterations ... with the Free Peoples player taking the final action ...
 * He may agree with the Free Peoples player's choice of iterations ... with the Shadow player taking the final
 * action".
 *
 * The loop is found as the player who opened it is about to be offered its first action for the third time: each
 * player has used his action twice, and the board at the start of the third round is the board at the start of
 * the second.  The Free Peoples player then names a number (1 to 99, filled in with 3), whoever opened the loop,
 * and the Shadow player agrees to it or chooses a lower one (0 to one less).  The prompts are her words, with the
 * cards' titles in [[...]] and the number in **...**.
 *
 * What the players settle is carried out by the run (the loop-run item of engine pass 51, built after this one).
 * Until then the player who was about to be asked is asked as before, which is what these tests read after the
 * prompts.
 *
 * The table is the Fellowship path with Anduin Banks 7_341 laid as site 4 in place of Balin's Tomb (as in
 * Card_05_029_Tests).  Uruk Pursuer 4_190 is the minion: strength 5; its texts are about unbound Hobbits, and none
 * is in play.
 */
public class VoluntaryLoopTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sméagol, Slinker: "Skirmish: Add a burden to make Sméagol strength +2 and take no wounds."
					put("smeagol", "5_29");
					put("pursuer", "4_190");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					// Anduin Banks: "Skirmish: Spot your minion and remove a burden to make that minion strength +2."
					put("site4", "7_341");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Merry, Rohirrim Squire 7_320: "Skirmish: If you cannot spot 3 threats, add a threat to make Merry strength +2."
	// Mordor Soldier 7_290: strength 3, "Skirmish: Remove a threat to make this minion strength +4."  The same loop
	// with threats, which the rules do not name: the step is not keyed to the rules' example.
	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_320");
					put("soldier", "7_290");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolAgainstAnduinBanksIsFoundAsHeWouldBeOfferedHisAbilityTheThirdTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		// One round: no loop yet.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsDecisionAvailable("LOOP DETECTED"));

		scn.FreepsUseCardAction(smeagol);
		// Mid-round: the Shadow player is offered the site's ability as ever.
		assertTrue(scn.ShadowActionAvailable(banks));
		scn.ShadowUseCardAction(banks);

		// Two rounds, the board as it was at the start of the second: 0 burdens.  Instead of his actions, the Free
		// Peoples player is asked how many more times the loop runs.
		assertEquals(0, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(smeagol));
		assertEquals(9, scn.GetStrength(pursuer));
		assertFalse(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsDecisionAvailable("LOOP DETECTED: [See loop handling rules here.](https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.2#loops)\n[[Sméagol, Slinker]] and [[Anduin Banks]] can be used forever."
				+ "\nChoose how many more times this loop will execute."));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(99, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsNameLoopIterations(6);

		assertTrue(scn.ShadowDecisionAvailable("LOOP DETECTED: [See loop handling rules here.](https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.2#loops)\nThe Free Peoples player has decided that the loop between "
				+ "[[Sméagol, Slinker]] and [[Anduin Banks]] will be performed **6** more times."));
		assertEquals(List.of("Agree: the loop goes **6** more times, and Free Peoples takes the next action.",
				"Choose a lower number: Shadow will take the next action."), scn.ShadowGetMultipleChoices());
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		scn.ShadowAgreeToLoop();

		// Until the run is built, the Free Peoples player is then asked as he would have been.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		assertEquals(0, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(smeagol));
		assertEquals(9, scn.GetStrength(pursuer));
	}

	@Test
	public void TheShadowPlayerMayChooseANumberLowerThanTheOneNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);

		scn.FreepsNameLoopIterations(6);
		scn.ShadowChoose("Choose a lower number: Shadow will take the next action.");

		assertTrue(scn.ShadowDecisionAvailable("Choose a number lower than **6**. The loop between [[Sméagol, Slinker]] "
				+ "and [[Anduin Banks]] will be performed that many times, ending so that you will take the next action."));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());

		scn.ShadowChoose("2");

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void ShadowLowerLoopNamesTheLowerNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);

		scn.FreepsNameLoopIterations(99);
		scn.ShadowLowerLoop(98);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void ALoopTheShadowPlayerOpensIsFoundAsHeWouldBeOfferedHisAbilityTheThirdTimeAndTheFreePeoplesPlayerNamesTheNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.AddBurdens(1);

		// The Free Peoples player passes first; the Shadow player opens each round from then on.
		scn.FreepsPass();
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowUseCardAction(banks);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(smeagol);

		// 1 burden, as at the start of the second round.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("LOOP DETECTED: [See loop handling rules here.](https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.2#loops)\n[[Anduin Banks]] and [[Sméagol, Slinker]] can be used forever."
				+ "\nChoose how many more times this loop will execute."));

		scn.FreepsNameLoopIterations(4);
		assertTrue(scn.ShadowDecisionAvailable("the loop between [[Anduin Banks]] and [[Sméagol, Slinker]] will be performed **4** more times."));
		scn.ShadowAgreeToLoop();

		// The Shadow player, who was about to be asked, is asked.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(banks));
	}

	@Test
	public void MerryAgainstMordorSoldierIsALoopOfThreatsAndALowerNumberOfZeroIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var merry = scn.GetFreepsCard("merry");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, soldier);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetThreats());
		scn.FreepsUseCardAction(merry);
		scn.ShadowUseCardAction(soldier);
		scn.FreepsUseCardAction(merry);
		scn.ShadowUseCardAction(soldier);

		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(merry));
		assertEquals(11, scn.GetStrength(soldier));
		assertTrue(scn.FreepsDecisionAvailable("LOOP DETECTED: [See loop handling rules here.](https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.2#loops)\n[[Merry, Rohirrim Squire]] and [[Mordor Soldier]] can be used forever."));

		scn.FreepsNameLoopIterations(1);
		assertTrue(scn.ShadowDecisionAvailable("will be performed **1** more times."));

		// Lower than 1 can only be 0: it is not asked.
		scn.ShadowLowerLoop(0);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));
	}
}
