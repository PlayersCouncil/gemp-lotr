package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The skirmish procedure as amended, binding (Ketura, 2026-10-20: orchestrator/skirmish-procedure-2026-10-20.md,
 * answers/2026-10-20-a.md and -b.md; rulings log "The skirmish procedure as amended, binding"), step by step.  Each
 * test names the step it shows.
 *
 * - 2A: a canceled skirmish ends at once with no winner or loser, and skips to step 5.
 * - 2B / 2C: an excess character removed is neither winner nor loser; when the last character of a side is removed,
 *   "that side and the last removed character is declared the loser and the remaining side is the winner. Skip to
 *   step 4."  Killed or not, "loses a skirmish" answers him (Legacy Ruling 2).
 * - 3A: at least double, the losing side is "about to be overwhelmed"; a response there that alters a strength
 *   returns to the start of step 3 (an optional trigger's side of it: AnAboutToBeOverwhelmedTriggerAtEachReturnToStepThreeTests).
 * - 3C: a side above 0 overwhelms a side at 0; at 0 against 0 the Shadow side wins and does not overwhelm.
 * - 4: the win and the loss are simultaneous; the loss effects (4A to 4D) are "a single sequence of automatic required
 *   effects triggered by the skirmish loss", ordered by the Free Peoples player with every other required trigger of
 *   the moment ("Resolve skirmish damage" or "Resolve skirmish overwhelm" in "Required responses").
 * - 4A: a losing side already removed takes no loss effects.
 * - 4B: the overwhelm is the one decided "during step 3": a required trigger ordered ahead of the loss sequence that
 *   changes a strength neither makes nor undoes one.
 * - 4D: "a wound response/trigger occurs when the skirmish loss issues them, then the death response/trigger ...,
 *   and only after the interruptions to the resolution of the required triggers have been resolved do the optional
 *   responses/triggers to 4 get to resolve."
 * - 5: "about to end" (TheSkirmishIsAboutToEndAtStepSixTests and Card_02_057_Tests, Q-0332, which number it by the
 *   2026-10-16 draft; it is step 5 here).
 *
 * Plundered Armories 1_193 and Úlairë Otsëa 3_86 show 4D as landed in their own files (Q-0342, Q-0343); Footman's
 * Armor 7_93 at 3A in Card_07_093_Tests as well as here.
 *
 * The table is the Fellowship block's; the skirmishes are at Ettenmoors (site 2), whose skirmish ability is never
 * used.  Frodo, Son of Drogo is the Ring-bearer.
 */
public class SkirmishProcedureTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurtz", "1_127");      // Lurtz, Servant of Isengard: Uruk-hai, strength 13, vitality 3, damage +1, archer
					// Lurtz's Sword: strength +2, damage +1.  "Each time Lurtz wins a skirmish, the Free Peoples
					// player must discard the top 2 cards of his or her draw deck."
					put("sword", "2_43");
					// Worry: "Each time a companion or ally loses a skirmish involving an Uruk-hai, the opponent must
					// choose to either exert the Ring-bearer or add a burden."
					put("worry", "1_162");
					// Uruk-hai Rampage: "Response: If your Uruk-hai wins a skirmish, remove (3) to make him fierce
					// until the regroup phase."
					put("rampage", "1_159");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3, +2 while he bears a weapon
					put("hobbitsword", "1_299"); // Hobbit Sword: strength +2; "Bearer must be a Hobbit."
					put("sam", "1_311");        // Sam, Son of Hamfast: "Response: If Frodo dies, make Sam the Ring-bearer"
					// Gilraen's Memorial: "When Aragorn is killed, discard this artifact."  Its fellowship ability is
					// never used.
					put("gilraen", "101_22");

					// Treebeard, Enraged Shepherd: strength 12.  "Each time Treebeard wins a skirmish, the first
					// Shadow player must exert X minions, where X is the difference between Treebeard's strength and
					// the losing character's strength."
					put("treebeard", "15_38");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1
					put("assassin", "1_262");   // Orc Assassin: strength 6, vitality 2; its ability needs 2 Hobbits
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					// End of the Game: "Make an exhausted [gondor] companion strength +2. If that companion wins this
					// skirmish, heal that companion or make him or her damage +1."
					put("endgame", "10_30");
					put("troop", "1_143");      // Troop of Uruk-hai: strength 9, vitality 4, damage +1; no weapons on his opponent

					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3, damage +1
					// Gimli's Battle Axe: strength +2, damage +1.  "Each time Gimli wins a skirmish, you may wound an Orc."
					put("battleaxe", "1_14");
					// Well-crafted Armor: "Each time bearer takes a wound, you may reinforce a [dwarven] token."  Its
					// skirmish ability is never used.
					put("wca", "15_9");
					// Dwarven Axe: strength +2.  "Each time a player's minion loses a skirmish to bearer, that player
					// discards the top card from his or her draw deck."
					put("dwarvenaxe", "1_9");
					// Howling Orc: lurker, strength 8.  "Each time an [orc] minion is killed in a skirmish, you may
					// make this minion strength +2 until the regroup phase."  Never assigned.
					put("howling", "13_109");
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc, strength 5, vitality 1; its text is about being played

					// Strange-looking Men: [men] Man, strength 10, vitality 2; its maneuver ability is never used.
					put("men", "11_100");
					// Lying in Wait: "Bearer is strength +1 for each other [men] minion you can spot. Each time a [men]
					// minion wins a skirmish, make bearer strength +1 until the regroup phase."
					put("lying", "13_95");
					// Harrying Hillman: lurker, strength 9.  "Each time a companion loses a skirmish involving a [men]
					// minion, add (2)."  Never assigned.
					put("hillman", "12_69");
					put("erebor", "1_12");      // Gimli, Dwarf of Erebor: strength 6, vitality 3, damage +1, Aragorn signet
					// You Have My Sword: strength +2, on a companion with the Aragorn signet.  "Each time bearer wins a
					// skirmish, exert Aragorn or discard this condition."
					put("yhms", "101_24");
					// Gríma, Wormtongue: [isengard] Man, strength 4, vitality 3.  His maneuver ability is never used.
					put("grima", "4_154");

					// Footman's Armor: "Response: If bearer is about to be overwhelmed, discard a [gondor]
					// fortification or a card from hand to make bearer strength +2."
					put("footman", "7_93");
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, no text
					put("patrol", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("lorien", "1_53");      // Lórien Elf, in hand to pay for the Armor
					put("guard", "1_7");        // Dwarf Guard, in hand to pay for the Armor

					// Shagrat, Captain of Cirith Ungol: [sauron] Uruk-hai, strength 13, damage +1.  "Each time Shagrat
					// loses a skirmish, you may exert a character."
					put("shagrat", "10_99");
					// Mordor Soldier: [sauron] Orc, strength 3.  His skirmish ability removes a threat, and there is none.
					put("soldier", "7_290");
					// Merry, Unquenchable Hobbit: "Skirmish: If Merry is not assigned to a skirmish, discard him to
					// remove a [sauron] Orc from a skirmish involving an unbound companion."
					put("merry4", "4_311");
					// Pippin, Just a Nuisance: the same, "to remove an Uruk-hai".
					put("pippin4", "4_313");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					// Long I Fell: "Spot Gandalf to prevent all wounds to him. Any Shadow player may make you wound a
					// minion to prevent this."
					put("fell", "4_97");
					put("flankers", "2_61");    // Goblin Flankers: strength 11, vitality 3; its text is about being played

					// Hobbit Stealth: "At sites 1 to 5, cancel a skirmish involving a Hobbit."
					put("stealth", "1_298");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl, strength 9; her text is about [wraith] conditions
					// Loathsome: "If a skirmish that involved a Nazgûl is about to end, discard a possession borne by a
					// companion in that skirmish."
					put("loathsome", "7_182");

					// For 3C, as in SkirmishStrengthTests: Shadow conditions printed strength -1 that Sam may bear
					// (Our Foes Are Weak 11_196 has no other text; Many Riddles 4_159 and Desertion 6_61 have abilities
					// never used), and City Wall 5_33, a [gondor] fortification printed strength -3, laid on the
					// Southron Scout 4_252 (strength 3, ambush, assigned only by the Shadow player).
					put("foes", "11_196");
					put("riddles", "4_159");
					put("desertion", "6_61");
					put("scout", "4_252");
					put("wall", "5_33");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	/**
	 * The board of her item 1: Lurtz bearing his Sword (15, damage +2) against Aragorn (8), Worry in play, with the
	 * skirmish actions passed.  A Goblin Runner is on the table so that the turn reaches the assignment phase; Lurtz,
	 * an archer, comes after archery.
	 */
	private VirtualTableScenario LurtzBeatsAragornWithWorryInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("worry"));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, scn.GetShadowCard("sword"));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(15, scn.GetStrength(lurtz));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// Step 4: the loss sequence, the Sword's win trigger and Worry's loss trigger, all three required, in one
		// "Required responses" for the Free Peoples player, with Aragorn not yet wounded.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(3, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish damage"));
		assertTrue(scn.FreepsActionAvailable("Lurtz's Sword"));
		assertTrue(scn.FreepsActionAvailable("Worry"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		return scn;
	}

	@Test
	public void Step4TheWoundsThenTheSwordThenWorry() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = LurtzBeatsAragornWithWorryInPlay();
		var aragorn = scn.GetFreepsCard("aragorn");
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsResolveRuleFirst();
		// 4C: 1 wound, and 1 for each of Lurtz's two damage bonuses.  Aragorn lives with 1 vitality.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());

		scn.FreepsResolveActionOrder("Lurtz's Sword");
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());

		// Worry, left over, is carried out with nothing more to order.
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4TheWoundsThenWorryThenTheSword() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = LurtzBeatsAragornWithWorryInPlay();
		var aragorn = scn.GetFreepsCard("aragorn");
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveActionOrder("Worry");
		assertEquals(deck, scn.GetFreepsDeckCount());
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());

		// The Sword, left over, discards with nothing more to order.
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4TheSwordThenTheWoundsThenWorry() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = LurtzBeatsAragornWithWorryInPlay();
		var aragorn = scn.GetFreepsCard("aragorn");
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsResolveActionOrder("Lurtz's Sword");
		// The win answered while Aragorn has no wound and nothing of the loss has been done.
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish damage"));
		assertTrue(scn.FreepsActionAvailable("Worry"));

		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());

		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4TheSwordThenWorryThenTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = LurtzBeatsAragornWithWorryInPlay();
		var aragorn = scn.GetFreepsCard("aragorn");
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsResolveActionOrder("Lurtz's Sword");
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		scn.FreepsResolveActionOrder("Worry");
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());

		// The loss sequence, left over, places the wounds with nothing more to order.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4WorryThenTheWoundsThenTheSword() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = LurtzBeatsAragornWithWorryInPlay();
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsResolveActionOrder("Worry");
		// Worry's choice is asked before any wound and before the Sword.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());

		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4WorryThenTheSwordThenTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = LurtzBeatsAragornWithWorryInPlay();
		var aragorn = scn.GetFreepsCard("aragorn");
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsResolveActionOrder("Worry");
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());

		scn.FreepsResolveActionOrder("Lurtz's Sword");
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4EndOfTheGameOrderedBeforeTheLossSequenceAddsItsDamageBonusToTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var endgame = scn.GetFreepsCard("endgame");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(endgame);

		scn.StartGame();
		// Exhausted, for End of the Game.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.FreepsPlayCard(endgame);
		scn.ShadowPass();
		scn.FreepsPass();
		// 10 against 9.
		assertEquals(10, scn.GetStrength(aragorn));

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish damage"));
		assertTrue(scn.FreepsActionAvailable("End of the Game"));

		scn.FreepsResolveActionOrder("End of the Game");
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsChoiceAvailable("damage +1"));
		scn.FreepsChooseOption("damage");

		// 4C: the damage bonus counts, as it is there when the loss sequence begins: 2 wounds.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4EndOfTheGameOrderedAfterTheLossSequenceActsAfterTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var endgame = scn.GetFreepsCard("endgame");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(endgame);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.FreepsPlayCard(endgame);
		scn.ShadowPass();
		scn.FreepsPass();

		scn.FreepsResolveRuleFirst();
		// The wound is placed first, and End of the Game's choice is asked after it.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsChoiceAvailable("damage +1"));
		scn.FreepsChooseOption("damage");

		// The damage bonus comes too late for the wounds: still 1.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4TreebeardOrderedBeforeTheLossSequenceHasTheMinionsExertedBeforeTheLoserIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var party = scn.GetShadowCard("party");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(treebeard);

		scn.StartGame();
		// Four minions able to exert, so that the Shadow player is asked which three (12 - 9).
		scn.MoveMinionsToTable(party, assassin, savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, party);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);
		assertEquals(12, scn.GetStrength(treebeard));
		assertEquals(9, scn.GetStrength(party));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		scn.FreepsResolveActionOrder("Treebeard");

		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(party));
		scn.ShadowChooseCards(party, assassin, savage);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(savage));

		// Then the loss sequence, left over: the Raiding Party's wound for losing, on top of its exertion.
		assertEquals(2, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4TreebeardOrderedAfterTheLossSequenceHasTheMinionsExertedAfterTheLoserIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var party = scn.GetShadowCard("party");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(treebeard);

		scn.StartGame();
		scn.MoveMinionsToTable(party, assassin, savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, party);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);
		scn.PassSkirmishActions();

		scn.FreepsResolveRuleFirst();
		// The wound for losing is on as the exertions are asked.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.GetWoundsOn(party));
		scn.ShadowChooseCards(party, assassin, savage);

		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4DAnOptionalTriggerOnTheLosersWoundIsOfferedAtEachWoundAndTheOptionalAnswerToTheWinAfterThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var rampage = scn.GetShadowCard("rampage");
		var gimli = scn.GetFreepsCard("gimli");
		var wca = scn.GetFreepsCard("wca");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, wca, battleaxe);
		scn.MoveCardsToSupportArea(rampage);

		scn.StartGame();
		scn.AddTokensToCard(wca, 1);
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gimli, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(6);
		// 8 against 13: not double.  2 wounds (1, and Lurtz's damage +1).
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(13, scn.GetStrength(lurtz));
		scn.PassSkirmishActions();

		// The first wound, and the Armor answers it before the second is placed.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wca));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseOptionalTrigger(wca);
		assertEquals(2, scn.GetCultureTokensOn(wca));

		// The second wound, answered the same way.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wca));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseOptionalTrigger(wca);
		assertEquals(3, scn.GetCultureTokensOn(wca));

		// Only then the Response to Lurtz's win.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage));
		// (Declined: a fierce Lurtz would bring the fierce round's assignment phase.)
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4DARequiredTriggerOnTheLosersDeathComesAtTheDeathBeforeTheRequiredTriggersStillPendingAndTheOptionalAnswerToTheWinComesLast() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var rampage = scn.GetShadowCard("rampage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gilraen = scn.GetFreepsCard("gilraen");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gilraen);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("worry"), rampage);

		scn.StartGame();
		// One wound on him, so that the three wounds of losing kill him.
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, scn.GetShadowCard("sword"));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(6);
		int deck = scn.GetFreepsDeckCount();
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(3, scn.FreepsGetActionCount());
		scn.FreepsResolveRuleFirst();

		// Aragorn is killed on his third wound, and Gilraen's Memorial is discarded at his death (with no threats,
		// nothing else answers the death).  The Sword and Worry are still to come.
		assertInZone(Zone.DEAD, aragorn);
		assertInDiscard(gilraen);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Lurtz's Sword"));
		assertTrue(scn.FreepsActionAvailable("Worry"));

		scn.FreepsResolveActionOrder("Lurtz's Sword");
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());

		// Only after every required answer, the Response to Lurtz's win.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4DAResponseToTheLosersDeathComesAtTheDeathBeforeTheRequiredTriggersStillPendingAndTheOptionalAnswerToTheWinComesLast() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var rampage = scn.GetShadowCard("rampage");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("worry"), rampage);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, scn.GetShadowCard("sword"));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(6);
		int deck = scn.GetFreepsDeckCount();
		// 15 against Frodo's 4: an overwhelm.
		assertEquals(4, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(3, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish overwhelm"));
		scn.FreepsResolveRuleFirst();

		// 4B: Frodo is killed, and Sam's Response to his death is offered at once, before the Sword and Worry.
		assertInZone(Zone.DEAD, frodo);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);
		assertSame(sam, scn.GetRingBearer());

		// Then the required triggers still pending, in the Free Peoples player's order.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		scn.FreepsResolveActionOrder("Worry");
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		// Only then the Response to Lurtz's win.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4DAnOptionalTriggerOnTheLosersDeathComesBeforeTheOptionalTriggerOnTheWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		var goblin = scn.GetShadowCard("goblin");
		var howling = scn.GetShadowCard("howling");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, battleaxe);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, howling);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		// 8 against 5: not double.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(goblin));
		scn.PassSkirmishActions();

		// The Goblin dies of his first wound, and the Howling Orc's answer to the death is offered first.
		assertInDiscard(goblin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howling));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseOptionalTrigger(howling);
		assertEquals(10, scn.GetStrength(howling));

		// Then the Axe's answer to Gimli's win.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(battleaxe));
		scn.FreepsChooseOptionalTrigger(battleaxe);
		assertEquals(1, scn.GetWoundsOn(howling));
	}

	@Test
	public void Step4BARequiredWinTriggerThatMakesTheWinnerDoubleMakesNoOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var men = scn.GetShadowCard("men");
		var lying = scn.GetShadowCard("lying");
		var hillman = scn.GetShadowCard("hillman");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(men, hillman);
		scn.AttachCardsTo(men, lying);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, men);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		// 10 + 1 for the Hillman against 6: not double at step 3.
		assertEquals(11, scn.GetStrength(men));
		assertEquals(6, scn.GetStrength(gimli));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish damage"));
		scn.FreepsResolveActionOrder("Lying in Wait");
		// 12 against 6 is double now.
		assertEquals(12, scn.GetStrength(men));

		// The loss sequence is still the one step 3 decided: wounds, not an overwhelm.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish damage"));
		assertFalse(scn.FreepsActionAvailable("Resolve skirmish overwhelm"));
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step4BARequiredWinTriggerThatTakesTheWinnerUnderDoubleUndoesNoOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erebor = scn.GetFreepsCard("erebor");
		var yhms = scn.GetFreepsCard("yhms");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(erebor, aragorn);
		scn.AttachCardsTo(erebor, yhms);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(erebor, grima);
		scn.FreepsResolveSkirmish(erebor);
		// 6 + 2 against 4: double at step 3.
		assertEquals(8, scn.GetStrength(erebor));
		assertEquals(4, scn.GetStrength(grima));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish overwhelm"));
		scn.FreepsResolveActionOrder("You Have My Sword");
		scn.FreepsChooseOption("Discard");
		assertInDiscard(yhms);
		// 6 against 4 is not double now.
		assertEquals(6, scn.GetStrength(erebor));

		// The overwhelm decided at step 3 stands: Gríma is killed, with no wound.
		assertInDiscard(grima);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step3CASideAboveZeroOverwhelmsASideAtZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, scn.GetShadowCard("foes"), scn.GetShadowCard("riddles"), scn.GetShadowCard("desertion"));

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, grima);
		scn.FreepsResolveSkirmish(sam);
		// 3 - 1 - 1 - 1 against 4.
		assertEquals(0, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(grima));
		scn.PassSkirmishActions();

		// Killed with all four of his vitality left.
		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step3CZeroAgainstZeroIsAShadowWinWithNoOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, scn.GetShadowCard("foes"), scn.GetShadowCard("riddles"), scn.GetShadowCard("desertion"));

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.AttachCardsTo(scout, scn.GetFreepsCard("wall"));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(sam, scout);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetStrength(sam));
		assertEquals(0, scn.GetStrength(scout));
		scn.PassSkirmishActions();

		// The Shadow side wins; Sam takes one wound for losing and lives.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step3AFootmansArmorIsOfferedAgainWhileStillDoubleAndTheSkirmishIsResolvedFromTheStartOfStep3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetFreepsCard("footman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorien = scn.GetFreepsCard("lorien");
		var guard = scn.GetFreepsCard("guard");
		var brigand = scn.GetShadowCard("brigand");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, footman);
		scn.MoveCardsToHand(lorien, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, patrol);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowAssignToMinions(aragorn, patrol);
		scn.FreepsResolveSkirmish(aragorn);
		// 8 + 13 against 8.
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// 3A: he is about to be overwhelmed, and the Armor is offered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(footman));
		scn.FreepsChooseOptionalTrigger(footman);
		scn.FreepsChooseCard(guard);
		assertEquals(10, scn.GetStrength(aragorn));

		// Back to the start of step 3: 21 against 10 is still double, and the Armor is offered again.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(footman));
		scn.FreepsChooseOptionalTrigger(footman);
		assertInDiscard(lorien);

		// Step 3 once more: 21 against 12 is not double (3B).  He loses, takes one wound and lives.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step2CTheLastMinionRemovedLosesTheSkirmishAndTheLossEffectsAreSkipped() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry4 = scn.GetFreepsCard("merry4");
		var pippin4 = scn.GetFreepsCard("pippin4");
		var shagrat = scn.GetShadowCard("shagrat");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn, merry4, pippin4);

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shagrat);
		scn.ShadowAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		// 2B: Merry removes the Mordor Soldier, and Shagrat is left in the skirmish.
		scn.FreepsUseCardAction(merry4);
		assertFalse(scn.IsCharSkirmishing(soldier));
		scn.ShadowPass();

		// 2C: Pippin removes Shagrat, the side's last character.  He is the loser, and his "each time Shagrat loses
		// a skirmish" is offered.
		scn.FreepsUseCardAction(pippin4);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shagrat));
		scn.ShadowDeclineOptionalTrigger();

		// 4A: the losing side was already removed: no wounds on anyone.
		assertEquals(0, scn.GetWoundsOn(shagrat));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step2BAMinionRemovedWhileAnotherIsLeftIsNoLoser() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry4 = scn.GetFreepsCard("merry4");
		var pippin4 = scn.GetFreepsCard("pippin4");
		var shagrat = scn.GetShadowCard("shagrat");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn, merry4, pippin4);

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shagrat);
		scn.ShadowAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		// 2B: Pippin removes Shagrat first, with the Mordor Soldier left: Shagrat is neither winner nor loser.
		scn.FreepsUseCardAction(pippin4);
		assertFalse(scn.IsCharSkirmishing(shagrat));
		scn.ShadowPass();

		// 2C: Merry removes the Soldier, the last.  The Soldier is the loser, and Shagrat's trigger is not offered.
		scn.FreepsUseCardAction(merry4);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shagrat));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step2CTheLastMinionKilledInTheSkirmishLosesItAndTheLossEffectsAreSkipped() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		var flankers = scn.GetShadowCard("flankers");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.AttachCardsTo(gimli, dwarvenaxe);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(flankers, savage);
		// One wound from death.
		scn.AddWoundsToChar(flankers, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, flankers);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		int shadowDeck = scn.GetShadowDeckCount();

		// Long I Fell: the Shadow player has the Free Peoples player wound a minion, and it is the Flankers, the
		// skirmish's only minion.
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(flankers);
		assertInDiscard(flankers);

		// 2C: the Flankers, killed, are the loser, and the Dwarven Axe's "each time a player's minion loses a
		// skirmish to bearer" answers them.  4A: the losing side is already removed, so there is no loss sequence
		// to order beside the Axe.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());

		// 4A: no loss effects; nobody else is wounded.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Step2AACanceledSkirmishHasNoWinnerOrLoserAndGoesToAboutToEnd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var stealth = scn.GetFreepsCard("stealth");
		var lurtz = scn.GetShadowCard("lurtz");
		var lemenya = scn.GetShadowCard("lemenya");
		var loathsome = scn.GetShadowCard("loathsome");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, hobbitsword);
		scn.MoveCardsToHand(stealth);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("worry"));
		scn.MoveCardsToHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz, lemenya);
		scn.AttachCardsTo(lurtz, scn.GetShadowCard("sword"));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, lurtz);
		scn.ShadowAssignToMinions(merry, lemenya);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(merry);
		int deck = scn.GetFreepsDeckCount();

		// 2A: canceled at site 2.
		scn.FreepsPlayCard(stealth);

		// Step 5: "about to end" is reached; no win trigger (the Sword) and no loss trigger (Worry) has answered,
		// and Merry, who would have been overwhelmed, has no wound.
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.ShadowPlayCard(loathsome);
		assertInDiscard(hobbitsword);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
