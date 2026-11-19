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
 * A character who replaced another in a skirmish is "involved" in it when it is about to end (step 5 of the skirmish
 * procedure as amended, binding: Ketura, 2026-10-20; CR 4.2 "skirmish phase"), and the character he replaced is no
 * longer involved (F-0563, engine pass 57).  A character removed from the skirmish in no one's place, or killed in
 * it, is still involved, as before (TheSkirmishIsAboutToEndAtStepSixTests).
 *
 * Shown with Sting, Bane of the Eight Legs 8_113 ("If a fierce skirmish involving bearer is about to end, add a
 * threat to discard a minion involved in that skirmish") and Brave and Loyal 10_105 on the Free Peoples side, and
 * with Loathsome 7_182 ("If a skirmish that involved a Nazgûl is about to end, discard a possession borne by a
 * companion in that skirmish") and Controlled By The Ring 15_41 on the Shadow side.
 */
public class ACharacterWhoReplacedAnotherIsInvolvedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "8_113");
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3, +2 only bearing a weapon
					put("brave", "10_105");     // Brave and Loyal: exert an unassigned Hobbit companion to have him
					                            // replace a companion (except the Ring-bearer) in a skirmish
					put("shelob", "8_25");      // Shelob, Eater of Light: fierce, strength 8, vitality 8

					put("loathsome", "7_182");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl, strength 9, vitality 2; her text is about [wraith] conditions
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: strength 5, vitality 4; a regroup ability only
					put("controlled", "15_41"); // Controlled By The Ring: "Skirmish: If Gollum is not assigned to a
					                            // skirmish, discard this condition to have him replace a minion skirmishing a companion"
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("spear", "7_108");      // Knight's Spear: strength +2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACompanionWhoReplacedAnotherInAFierceSkirmishIsInvolvedAndStingOnHimIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var brave = scn.GetFreepsCard("brave");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(sam, merry);
		scn.AttachCardsTo(sam, sting);
		scn.MoveCardsToHand(brave);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		// 3 + 2 + 6 = 11 against 8: Sam wins, and not by double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 6));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, shelob);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsPlayCard(brave);
		scn.FreepsChooseCard(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertEquals(11, scn.GetStrength(sam));
		scn.PassFierceSkirmishActions();

		// Sam came into the skirmish in Merry's place: it involves him as it is about to end.
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertInDiscard(shelob);
	}

	@Test
	public void ACompanionReplacedInAFierceSkirmishIsNoLongerInvolvedAndStingOnHimIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var brave = scn.GetFreepsCard("brave");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(sam, merry);
		scn.AttachCardsTo(sam, sting);
		scn.MoveCardsToHand(brave);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		// 3 + 6 = 9 against 8: Merry wins, and not by double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, merry, null, 6));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, shelob);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPlayCard(brave);
		scn.FreepsChooseCard(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(sam));
		assertEquals(9, scn.GetStrength(merry));
		scn.PassFierceSkirmishActions();

		// Merry took Sam's place: the skirmish no longer involves Sam, and Sting on him is not offered.
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertInZone(Zone.ATTACHED, sting);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionReplacedInASkirmishIsNoLongerInvolvedAndLoathsomeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var gollum = scn.GetShadowCard("gollum");
		var controlled = scn.GetShadowCard("controlled");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, spear);
		scn.MoveCardsToHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, gollum);
		scn.AttachCardsTo(gollum, controlled);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowUseCardAction(controlled);
		assertTrue(scn.IsCharSkirmishing(gollum));
		assertFalse(scn.IsCharSkirmishing(lemenya));
		scn.PassSkirmishActions();

		// Gollum took Lemenya's place, and a Nazgûl she remains: the skirmish no longer involves her, and no
		// Nazgûl is involved as it is about to end.
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertInHand(loathsome);
		assertInZone(Zone.ATTACHED, spear);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionNotReplacedIsInvolvedAndLoathsomeIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var gollum = scn.GetShadowCard("gollum");
		var controlled = scn.GetShadowCard("controlled");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, spear);
		scn.MoveCardsToHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, gollum);
		scn.AttachCardsTo(gollum, controlled);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowPass();

		// The same board with Controlled By The Ring left unused: Lemenya is involved, and Loathsome is offered.
		assertTrue(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPlayCard(loathsome);
		assertInDiscard(spear, loathsome);
	}
}
