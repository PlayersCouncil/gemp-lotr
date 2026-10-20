package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Keyword;
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
 * Steps 3 to 5 of the skirmish procedure (the Rules Council's draft for CR "skirmish phase", binding: Ketura,
 * 2026-10-16 and 2026-10-19), which are ordered steps and not one moment: "skirmish-win and skirmish-lose are going
 * to no longer be tied ambiguous simultaneous triggers, but follow a set order as laid out here" (engine pass 54).
 * Step 4, the winners win, and whatever answers the win is carried out; then step 5, the losers lose: the overwhelm
 * kill (5B) or the wounds (5C), with whatever answers the loss, which the Free Peoples player orders as one moment.
 * A losing side already removed takes no loss effects (5A).  Step 3A, "about to be overwhelmed": a response that
 * changes a strength sends the skirmish back to the start of step 3.
 *
 * Shown with her cards: Lurtz 1_127 bearing Lurtz's Sword 2_43 with Worry 1_162 in play; Treebeard, Enraged
 * Shepherd 15_38; End of the Game 10_30; Footman's Armor 7_93.
 */
public class TheWinIsAnsweredBeforeTheLossTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurtz", "1_127");      // Lurtz, Servant of Isengard: Uruk-hai, strength 13, damage +1, archer
					// Lurtz's Sword: strength +2, damage +1.  "Each time Lurtz wins a skirmish, the Free Peoples
					// player must discard the top 2 cards of his or her draw deck."
					put("sword", "2_43");
					// Lurtz's Sword, the errata: "Each time bearer wins a skirmish, the Free Peoples player must wound
					// a companion or discard the top 3 cards of their draw deck."
					put("sword52", "52_43");
					// Worry: "Each time a companion or ally loses a skirmish involving an Uruk-hai, the opponent must
					// choose to either exert the Ring-bearer or add a burden."
					put("worry", "1_162");
					put("runner", "1_178");     // Goblin Runner: no text that reaches these tests; it takes the archery

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("merry", "1_302");      // Merry, Friend to Sam: strength 3

					// Treebeard, Enraged Shepherd: strength 12.  "Each time Treebeard wins a skirmish, the first
					// Shadow player must exert X minions, where X is the difference between Treebeard's strength and
					// the losing character's strength."
					put("treebeard", "15_38");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					// Long I Fell: "Spot Gandalf to prevent all wounds to him. Any Shadow player may make you wound a
					// minion to prevent this."
					put("fell", "4_97");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1
					put("assassin", "1_262");   // Orc Assassin: strength 6, vitality 2; its ability needs 2 Hobbits
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("flankers", "2_61");    // Goblin Flankers: strength 11, vitality 3; its text is about being played

					// End of the Game: "Make an exhausted [gondor] companion strength +2. If that companion wins this
					// skirmish, heal that companion or make him or her damage +1."
					put("endgame", "10_30");
					put("troop", "1_143");      // Troop of Uruk-hai: strength 9, vitality 4, damage +1

					// Footman's Armor: "Response: If bearer is about to be overwhelmed, discard a [gondor]
					// fortification or a card from hand to make bearer strength +2."
					put("armor", "7_93");
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, no text
					put("patrol", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("lorien", "1_53");      // Lórien Elf, in hand to pay for the Armor
					put("guard", "1_7");        // Dwarf Guard, in hand to pay for the Armor
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzsSwordAnswersTheWinAloneThenWorryAndTheWoundsAreOrdered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");
		var worry = scn.GetShadowCard("worry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		// A minion on the table so that the turn reaches the assignment phase; Lurtz, an archer, comes after archery.
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// 15 against 8: Lurtz wins, and it is no overwhelm.
		assertEquals(15, scn.GetStrength(lurtz));
		assertEquals(8, scn.GetStrength(aragorn));
		int deck = scn.GetFreepsDeckCount();
		scn.PassSkirmishActions();

		// Step 4: the Sword answers the win on its own, ahead of the wounds and of Worry, and nobody is asked to
		// order it.  The two cards are gone while Aragorn has no wound.
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Step 5: only Worry and the wounds for losing stand together, for the Free Peoples player to order.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish damage"));
		assertTrue(scn.FreepsActionAvailable("Worry"));
		assertFalse(scn.FreepsActionAvailable("Lurtz's Sword"));

		scn.FreepsResolveActionOrder("Worry");
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The wounds, with no question: 1 and the damage +1 of Lurtz and of his Sword.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnOverwhelmIsStepFiveTooAfterTheSwordHasAnsweredTheWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");
		var worry = scn.GetShadowCard("worry");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);
		// 15 against 3: an overwhelm.
		assertEquals(3, scn.GetStrength(merry));
		int deck = scn.GetFreepsDeckCount();
		scn.PassSkirmishActions();

		// Step 4 first: the Sword's discard, with Merry still in play.
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// Step 5: the overwhelm (5B) and Worry, ordered by the Free Peoples player.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Resolve skirmish overwhelm"));
		assertTrue(scn.FreepsActionAvailable("Worry"));

		scn.FreepsResolveRuleFirst();
		// Killed with no wound.
		assertInZone(Zone.DEAD, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		// Worry, left over, comes with nothing more to order.
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ALoserKilledByWhatAnswersTheWinTakesNoLossEffectsAndStillLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword52");
		var worry = scn.GetShadowCard("worry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Step 4: the errata Sword is asked of the Free Peoples player at once, with nothing to order, and he
		// wounds Aragorn, who has one vitality left.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsChoiceAvailable("Wound a companion"));
		scn.FreepsChooseOption("Wound a companion");
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.DEAD, aragorn);

		// Step 5: Aragorn still lost the skirmish, so Worry answers it; the losing side is already removed (5A),
		// so no wounds are resolved, and there is nothing to order.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TreebeardsExertionIsAskedBeforeTheLoserHasAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);
		var party = scn.GetShadowCard("party");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Four minions able to exert, so that the Shadow player is asked which three (12 - 9)
		scn.MoveMinionsToTable(party, assassin, savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, party);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);
		assertEquals(12, scn.GetStrength(treebeard));
		assertEquals(9, scn.GetStrength(party));
		scn.PassSkirmishActions();

		// Step 4: Treebeard answers the win with nothing to order, and the loser has no wound yet.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(party));

		scn.ShadowChooseCards(party, assassin, savage);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(savage));

		// Step 5: the wound for losing, on top of the exertion (the Raiding Party has 3 vitality).
		assertEquals(2, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EndOfTheGamesDamageBonusWonAtTheWinCountsInTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var endgame = scn.GetFreepsCard("endgame");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(endgame);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.FreepsPlayCard(endgame);
		scn.ShadowPass();
		scn.FreepsPass();

		// Step 4: End of the Game's choice comes at once, nothing to order, and the Troop has no wound yet.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsChoiceAvailable("damage +1"));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsChooseOption("damage");
		// Step 5: 1 wound and 1 for the damage bonus he has just been given.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWinByASideWhoseLosersWereAlreadyRemovedIsAnsweredWithNoLossEffects() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(treebeard, gandalf);
		scn.MoveCardsToHand(fell);
		var flankers = scn.GetShadowCard("flankers");
		var party = scn.GetShadowCard("party");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(flankers, party, savage);
		// One wound from death.
		scn.AddWoundsToChar(flankers, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, flankers);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);

		// Long I Fell: the Shadow player has the Free Peoples player wound a minion, and it is the Flankers, the
		// skirmish's only minion (step 2C: the Shadow side has lost and Treebeard won; skip to step 4).
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(flankers);
		assertInDiscard(flankers);

		// Step 4: Treebeard answers the win (12 - 11: one minion), with no wounds for losing to order beside it.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// Step 5A: the losing side was already removed, so no loss effects; nobody else is wounded.
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FootmansArmorAnswersAboutToBeOverwhelmedAndTheSkirmishIsResolvedAgainFromStepThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorien = scn.GetFreepsCard("lorien");
		var guard = scn.GetFreepsCard("guard");
		var brigand = scn.GetShadowCard("brigand");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
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

		// Step 3A: he is about to be overwhelmed, and the Armor is offered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsChooseOptionalTrigger(armor);
		scn.FreepsChooseCard(guard);
		assertEquals(10, scn.GetStrength(aragorn));

		// Back to the start of step 3: 21 against 10 is still double, so he is about to be overwhelmed again, and
		// the Armor is offered again.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsChooseOptionalTrigger(armor);
		assertInDiscard(lorien);

		// Step 3 once more: 21 against 12 is not double.  He loses (no overwhelm), takes one wound and lives.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
