package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "The skirmish is about to end" is step 6 of the skirmish procedure (the Rules Council's draft for CR "skirmish
 * phase", binding on the campaign: Ketura on Q-0332, 2026-10-16): after the loser's wounds, or his kill, at step 5
 * ("whether canceled, removed, or resolved normally"), and before step 7, where the characters of the skirmish are
 * no longer assigned.  A canceled skirmish (step 2a) skips straight to step 6; a skirmish whose last character of
 * one side is removed (step 2c) skips to step 4, and the side already removed takes no loss effects (step 5a).
 *
 * Shown with her two cards: Loathsome 7_182 ("If a skirmish that involved a Nazgûl is about to end, discard a
 * possession borne by a companion in that skirmish") and Sting, Bane of the Eight Legs 8_113 ("If a fierce
 * skirmish involving bearer is about to end, add a threat to discard a minion involved in that skirmish").
 */
public class TheSkirmishIsAboutToEndAtStepSixTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loathsome", "7_182");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl, strength 9, vitality 2; her text is about [wraith] conditions
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("spear", "7_108");      // Knight's Spear: strength +2; a skirmish ability used only on purpose
					put("armor", "7_93");       // Footman's Armor: a response only when its bearer is about to be overwhelmed
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit, strength 3 (+2 bearing a weapon)
					put("sword", "1_299");      // Hobbit Sword: a hand weapon, "Bearer must be a Hobbit"
					put("stealth", "1_298");    // Hobbit Stealth: "At sites 1 to 5, cancel a skirmish involving a Hobbit"
					// Sméagol, Slippery Sneak: "Skirmish: Add threats equal to the total vitality of the minions
					// Sméagol is skirmishing to discard Sméagol."
					put("smeagol", "8_27");

					put("sting", "8_113");
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4
					put("shelob", "8_25");      // Shelob, Eater of Light: fierce, strength 8, vitality 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheLosingCompanionsWoundIsOnBeforeTheSkirmishIsAboutToEnd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);
		// 8 against 9: Aragorn loses, and is not overwhelmed.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(lemenya));
		scn.PassSkirmishActions();

		// Step 5 before step 6: the loser's wound is on as the response is offered.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);

		assertInDiscard(armor, loathsome);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLosingMinionsWoundIsOnBeforeTheSkirmishIsAboutToEnd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		// 3 + 2 + 6 = 11 against 8: Sam wins, and not by double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 6));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, shelob);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(11, scn.GetStrength(sam));
		scn.PassFierceSkirmishActions();

		// Step 5 before step 6: Shelob's wound is on as Sting is offered.
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertInDiscard(shelob);
	}

	@Test
	public void ALoserKilledByHisWoundIsGoneBeforeTheSkirmishIsAboutToEnd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, shelob);
		scn.FreepsResolveSkirmish(sam);
		// 5 against 8: Sam loses, and is not overwhelmed.
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(shelob));
		scn.PassFierceSkirmishActions();

		// His wound is his fourth: he is killed at step 5, and Sting goes with him before step 6.
		assertInZone(Zone.DEAD, sam);
		assertInDiscard(sting);
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionKilledByHisWoundStillLeavesASkirmishThatInvolvedHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, spear);
		scn.MoveCardsToHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddWoundsToChar(lemenya, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);
		// 8 + 2 against 9: Aragorn wins, and not by double.
		assertEquals(10, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// Lemenya's wound is her second: she is killed at step 5.  The skirmish involved a Nazgûl all the same (CR
		// 4.2 on Loathsome), and a Nazgûl she remains in the discard pile; it is about to end at step 6.
		assertInDiscard(lemenya);
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);

		assertInDiscard(spear, loathsome);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACanceledSkirmishGoesStraightToAboutToEndWithNoWoundPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var stealth = scn.GetFreepsCard("stealth");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToHand(loathsome, stealth);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, lemenya);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(2, scn.GetCurrentSiteNumber());
		// Step 2a: canceled, it skips to step 6.
		scn.FreepsPlayCard(stealth);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);

		// Merry is still the companion in that skirmish at step 6.
		assertInDiscard(sword, loathsome);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishWhoseLastCompanionIsRemovedReachesAboutToEndWithNoLossEffects() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, lemenya);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(smeagol);
		// Step 2c: Sméagol discards himself (2 threats, for Lemenya's 2 vitality), and his side has lost.
		scn.FreepsUseCardAction(smeagol);
		assertInDiscard(smeagol);
		assertEquals(2, scn.GetThreats());

		// Step 5a: the side already removed takes no loss effects, and the winner no wound.  Step 6 is reached.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);

		assertInDiscard(loathsome);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
