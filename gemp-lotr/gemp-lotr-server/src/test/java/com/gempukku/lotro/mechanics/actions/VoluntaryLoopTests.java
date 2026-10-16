package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
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
 * What the players settle is then carried out at once, before anybody is asked anything (LoopRun).  One iteration
 * is one round as found, the opener's action and then the other player's.  The Shadow player takes the last action
 * if he agreed, and the Free Peoples player if he chose a lower number; where the last round would end with the
 * wrong player, the opener's action is taken once more at the end (CR: "adding one final action at the end of the
 * loop, if necessary").  So a loop the Free Peoples player opens ends on the round as found when the Shadow player
 * agrees, and one action out of step when he lowers it; a loop the Shadow player opens, the other way about.  The
 * player who did not take the last action is asked next, and each test reads who that is, with every strength and
 * burden (or threat) the run left.
 *
 * Then the two looping abilities are barred to the end of the phase, each skirmish being a phase of its own (Ketura,
 * answers/2026-10-15-d.md): still offered, flagged "loopBanned" (FreepsActionLoopBanned), and refused if taken.  The
 * bar is on the card's looping ability: another copy's use of the same ability is not barred.  That a card's OTHER
 * abilities are not barred is not exercised by any card: no card of a printed loop has a second ability of the same
 * phase (engine pass 51's search of the definitions; Army of Udûn 7_264, the one with more, has them in the Shadow
 * and regroup phases).
 *
 * A decision asked inside an action of the loop is answered as it was in the round the loop was found on, the
 * second played by hand.  The rounds run unseen but for the last one or two actions: nothing of them is sent to the
 * clients or recorded for the replay, and a run of 99 sends what a run of 3 sends.
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

	// The rules' loop in the first of two skirmishes: Aragorn 1_89 (strength 8, a Maneuver ability only) against
	// Goblin Patrol Troop 1_177 (strength 13, no text) is the second.  Sméagol's ability is offered in another's
	// skirmish too, and the site's needs only "your minion" spotted.
	protected VirtualTableScenario GetTwoSkirmishesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "5_29");
					put("pursuer", "4_190");
					put("aragorn", "1_89");
					put("troop", "1_177");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
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

	// Merry, Rohirrim Squire 7_320 against two Mordor Soldiers 7_290 in his skirmish: the loop with one of them.
	protected VirtualTableScenario GetTwoSoldiersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_320");
					put("soldier", "7_290");
					put("soldier2", "7_290");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Merry against Troll of the Deep 19_23 (strength 13, "Skirmish: Remove a threat to make an [orc] minion strength
	// +2"; "To play, spot an [orc] Orc" does not stop a cheat).  Denizen of the Black Pit 11_117 (strength 10; +2 only
	// at an underground site, and Ettenmoors is none) is the second [orc] minion, so that the Troll's ability asks
	// which minion is made stronger each time.
	protected VirtualTableScenario GetChoiceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_320");
					put("troll", "19_23");
					put("denizen", "11_117");
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

		// Six more rounds, twelve actions, the last the Shadow player's: Sméagol 7 + 12, the Pursuer 9 + 12, no
		// burden.  The Free Peoples player is asked next, and his ability is barred.
		assertEquals(0, scn.GetBurdens());
		assertEquals(19, scn.GetStrength(smeagol));
		assertEquals(21, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionLoopBanned(smeagol));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
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

		// Two rounds and one more of Sméagol's, so that he takes the last action: Sméagol 7 + 6, the Pursuer 9 + 4,
		// and the burden he added last is still there.  The Shadow player is asked next, and the site is barred.
		assertEquals(1, scn.GetBurdens());
		assertEquals(13, scn.GetStrength(smeagol));
		assertEquals(13, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionLoopBanned(banks));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
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

		// 98 rounds and one more of Sméagol's: 99 uses of his ability, 98 of the site's.
		assertEquals(1, scn.GetBurdens());
		assertEquals(7 + 2 * 99, scn.GetStrength(smeagol));
		assertEquals(9 + 2 * 98, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
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

		// Four rounds end with Sméagol's action, so the site's is taken once more to give the Shadow player the last
		// action: the site five times (the Pursuer 9 + 10), Sméagol four (7 + 8), and the burden removed.  The Free
		// Peoples player is asked next, and his ability is barred.
		assertEquals(0, scn.GetBurdens());
		assertEquals(15, scn.GetStrength(smeagol));
		assertEquals(19, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionLoopBanned(smeagol));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void ALoopTheShadowPlayerOpensAndThenLowersEndsOnTheRoundAsFoundAndHeActsNext() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.AddBurdens(1);

		scn.FreepsPass();
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(smeagol));
		assertEquals(9, scn.GetStrength(pursuer));

		scn.FreepsNameLoopIterations(4);
		scn.ShadowLowerLoop(2);

		// Two rounds, the last action Sméagol's: nothing is added.  The site twice (9 + 4), Sméagol twice (7 + 4),
		// and the burden as it was.  The Shadow player is asked next, and the site is barred.
		assertEquals(1, scn.GetBurdens());
		assertEquals(11, scn.GetStrength(smeagol));
		assertEquals(13, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionLoopBanned(banks));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void ALoopTheShadowPlayerOpensRunsNoMoreWhenHeLowersItToNothing() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.AddBurdens(1);

		scn.FreepsPass();
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);

		scn.FreepsNameLoopIterations(3);
		scn.ShadowLowerLoop(0);

		// No round, and none is needed: the Free Peoples player took the last action already.  The board is as the
		// loop was found, and the Shadow player, who was about to be asked, is asked, with the site barred.
		assertEquals(1, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(smeagol));
		assertEquals(9, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionLoopBanned(banks));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
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

		// No round, and Merry's action once more so that the Free Peoples player takes the last: one threat, Merry
		// 7 + 2, the Soldier as he was.  The Shadow player is asked next, and the Soldier's ability is barred.
		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(merry));
		assertEquals(11, scn.GetStrength(soldier));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionLoopBanned(soldier));
	}

	@Test
	public void TheLoopingAbilitiesAreBarredToTheEndOfTheSkirmishAndNotInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoSkirmishesScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer, troop);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, pursuer}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(smeagol);

		// With two minions in play, the site asks which is made stronger: the Pursuer.
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.ShadowChooseCard(pursuer);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.ShadowChooseCard(pursuer);
		scn.FreepsNameLoopIterations(1);
		scn.ShadowLowerLoop(0);

		// Sméagol's action once more: a burden for the site to remove, and the site barred.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(banks));
		assertTrue(scn.ShadowActionLoopBanned(banks));
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionLoopBanned(smeagol));
		scn.FreepsPass();

		// 9 against 9: the Pursuer wins, and Sméagol takes no wound.  The next skirmish is a phase of its own.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		assertFalse(scn.FreepsActionLoopBanned(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(2, scn.GetBurdens());

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(banks));
		assertFalse(scn.ShadowActionLoopBanned(banks));
		scn.ShadowUseCardAction(banks);
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetBurdens());
		assertEquals(15, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBarIsOnTheLoopingCardsAbilityAndNotOnAnotherCopyOfIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoSoldiersScenario();

		var merry = scn.GetFreepsCard("merry");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, soldier);
		scn.ShadowAssignToMinions(merry, soldier2);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsUseCardAction(merry);
		scn.ShadowUseCardAction(soldier);
		scn.FreepsUseCardAction(merry);
		scn.ShadowUseCardAction(soldier);
		assertTrue(scn.FreepsDecisionAvailable("[[Merry, Rohirrim Squire]] and [[Mordor Soldier]] can be used forever."));
		scn.FreepsNameLoopIterations(2);
		scn.ShadowLowerLoop(1);

		// One round and Merry's action once more: Merry 7 + 4, the Soldier of the loop 11 + 4, a threat left.
		assertEquals(1, scn.GetThreats());
		assertEquals(11, scn.GetStrength(merry));
		assertEquals(15, scn.GetStrength(soldier));
		assertEquals(3, scn.GetStrength(soldier2));

		// The other Soldier's ability, the same text of the same card, is not barred, and removes the threat.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionLoopBanned(soldier));
		assertFalse(scn.ShadowActionLoopBanned(soldier2));
		scn.ShadowUseCardAction(soldier2);
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(soldier2));
		assertEquals(15, scn.GetStrength(soldier));

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionLoopBanned(merry));
	}

	@Test
	public void AChoiceInsideTheLoopIsMadeAsInTheLastRoundPlayedByHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var merry = scn.GetFreepsCard("merry");
		var troll = scn.GetShadowCard("troll");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, denizen);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);

		// The first round makes the Troll stronger, the second the Denizen.
		scn.FreepsUseCardAction(merry);
		scn.ShadowUseCardAction(troll);
		assertTrue(scn.ShadowDecisionAvailable("Choose"));
		scn.ShadowChooseCard(troll);
		scn.FreepsUseCardAction(merry);
		scn.ShadowUseCardAction(troll);
		scn.ShadowChooseCard(denizen);
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(12, scn.GetStrength(denizen));
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsDecisionAvailable("[[Merry, Rohirrim Squire]] and [[Troll of the Deep, Cave Troll]] can be used forever."));
		scn.FreepsNameLoopIterations(3);
		scn.ShadowAgreeToLoop();

		// Three rounds, each choosing the Denizen as the second round did, and nobody asked anything in them.
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(18, scn.GetStrength(denizen));
		assertEquals(13, scn.GetStrength(merry));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionLoopBanned(merry));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void ARunOf99IterationsSendsAndRecordsNoMoreThanARunOf3() throws DecisionResultInvalidException, CardNotFoundException {
		// Ketura on Q-0329: "the server should still perform that many iterations, but it should disable
		// transmission/recording of the game events while doing so, only sending the final 1-2 actions with
		// updated stats".  What each seat is sent is read through a channel joined as the replay's recorder joins one.
		List<List<GameEvent.Type>> sent = new ArrayList<>();
		List<List<GameEvent.Type>> sentToShadow = new ArrayList<>();
		int byHand = 0;
		for (int iterations : new int[]{99, 3}) {
			var scn = GetScenario();

			var smeagol = scn.GetFreepsCard("smeagol");
			var pursuer = scn.GetShadowCard("pursuer");
			scn.MoveCompanionsToTable(smeagol);

			scn.StartGame();
			scn.SkipToSite(3);
			scn.MoveMinionsToTable(pursuer);
			scn.FreepsPass();
			var banks = scn.GetCurrentSite();
			scn.SkipToAssignments();
			scn.FreepsAssignToMinions(smeagol, pursuer);
			scn.FreepsResolveSkirmish(smeagol);

			scn.MarkGameEvents();
			scn.FreepsUseCardAction(smeagol);
			scn.ShadowUseCardAction(banks);
			byHand = scn.GetGameEventsSinceMark(scn.P1()).size();
			scn.FreepsUseCardAction(smeagol);
			scn.ShadowUseCardAction(banks);
			scn.FreepsNameLoopIterations(iterations);

			scn.MarkGameEvents();
			scn.MarkGameLog();
			scn.ShadowAgreeToLoop();

			assertEquals(7 + 2 * iterations, scn.GetStrength(smeagol));
			assertEquals(9 + 2 * iterations, scn.GetStrength(pursuer));
			assertEquals(0, scn.GetBurdens());
			assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

			// The log: the agreement, one line for the rounds not shown, the last round as ever, and the end.
			var log = scn.GetGameLogSinceMark();
			assertTrue(log.getFirst().contains("the Free Peoples player named " + iterations + " more times, and the Shadow player agreed"));
			assertTrue(scn.GameLogSinceMarkSays("was carried out " + (iterations - 1) + " times without being shown"));
			assertEquals(1, log.stream().filter(line -> line.endsWith("is used") && line.contains("Sméagol")).count());
			assertEquals(1, log.stream().filter(line -> line.endsWith("is used") && line.contains("Anduin Banks")).count());
			assertTrue(log.getLast().endsWith("is over: the abilities it used cannot be used again this phase"));

			sent.add(scn.GetGameEventsSinceMark(scn.P1()).stream().map(GameEvent::getType).toList());
			sentToShadow.add(scn.GetGameEventsSinceMark(scn.P2()).stream().map(GameEvent::getType).toList());
		}

		// Each seat is sent the same for 99 iterations as for 3: the round played as ever, the stats once before it.
		assertEquals(sent.get(1), sent.get(0));
		assertEquals(sentToShadow.get(1), sentToShadow.get(0));
		assertEquals(1, sent.get(0).stream().filter(type -> type == GameEvent.Type.ADD_TOKENS).count());
		assertEquals(1, sent.get(0).stream().filter(type -> type == GameEvent.Type.REMOVE_TOKENS).count());
		assertEquals(2, sent.get(0).stream().filter(type -> type == GameEvent.Type.FLASH_CARD_IN_PLAY).count());
		assertEquals(GameEvent.Type.GAME_STATS, sent.get(0).get(1));
		// Fewer than two rounds played by hand would send.
		assertTrue(sent.get(0).size() < 2 * byHand);
	}
}
