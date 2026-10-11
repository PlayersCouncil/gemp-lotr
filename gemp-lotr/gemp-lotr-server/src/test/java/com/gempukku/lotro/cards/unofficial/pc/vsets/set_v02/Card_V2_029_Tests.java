package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_029_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "102_29");

					// Saruman's Staff, Wizard's Device: an [isengard] artifact that plays on Saruman (strength +2, fierce,
					// damage +1).  "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup
					// phase."
					put("staff", "4_174");
					// Isengard Worker, Servant and Retainer: [isengard] Orcs of strength 5, 7 and 8, vitality 3.
					// Each has the same regroup ability, which is only ever used on purpose.
					put("worker", "3_62");
					put("servant", "3_58");
					put("retainer", "3_57");
					// Uruk Savage: an [isengard] minion that is no Orc.  Strength 5, vitality 3; Damage +1 is
					// its whole text.
					put("savage1", "1_151");
					put("savage2", "1_151");
					// Goblin Patrol Troop: an Orc that is not [isengard].  Strength 13, vitality 3, no text.
					put("troop", "1_177");
					// Goblin Runner: another [moria] Orc, strength 5.  Cheated in, so its text never happens.
					put("runner", "1_178");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; Damage +1 in print; "Skirmish: Exert Gimli to make him strength +2"
					put("axe", "1_14");         // Gimli's Battle Axe: strength +2; "He is damage +1. Each time Gimli wins a skirmish, you may wound an Orc."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability that is only ever used on purpose
					put("sword", "1_112");      // Ranger's Sword: strength +2; "He is damage +1" is all it does
					put("legolas", "1_50");     // Legolas, Greenleaf: strength 6, vitality 3; "Archery: Exert Legolas to wound a minion; Legolas does not add to the fellowship archery total."
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; a fellowship ability, and a response when Frodo dies
					put("smeagol", "7_71");     // Smeagol, Always Helps: "Assignment: Assign a minion to Smeagol and add 2 threats to exhaust that minion."
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
					put("blow", "1_5");         // Cleaving Blow: "Skirmish: Make a Dwarf strength +2 and damage +1."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Saruman, Mind of Metal and Wheels
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman may not be assigned to a skirmish.
		 * Companions skirmishing [Isengard] Orcs lose all <b>damage</b> bonuses.
		 * Response: If an [isengard] Orc is about to take a wound, exert Saruman to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Mind of Metal and Wheels", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedFromHandForFourPlusTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 2 for the site.
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.HAND, saruman);
		assertTrue(scn.ShadowPlayAvailable(saruman));

		scn.ShadowPlayCard(saruman);
		// 4 to play + 2 roaming: his site number is 4 and the fellowship is at site 2.
		assertEquals(13 - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedToNeitherPlayerForAssignmentNorToAnAssignmentAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, gimli, smeagol);

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, worker, savage, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// An assignment action that assigns "a minion": every minion but Saruman may be chosen.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(worker);
		assertTrue(scn.IsCharAssignedAgainst(smeagol, worker));

		scn.ShadowPass();
		scn.FreepsPass();

		// The Free Peoples player's assignments: the two minions still unassigned, and not Saruman.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		assertFalse(scn.FreepsCanAssign(saruman));
		assertTrue(scn.FreepsCanAssign(troop));
		scn.FreepsAssignToMinions(aragorn, savage);

		// The Shadow player's assignment of the minions left over: the Troop, and not Saruman.
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(saruman));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertFalse(scn.ShadowCanAssign(saruman));
		assertTrue(scn.ShadowCanAssign(troop));
		scn.ShadowAssignToMinions(gimli, troop);

		assertTrue(scn.IsCharAssigned(worker));
		assertTrue(scn.IsCharAssigned(savage));
		assertTrue(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void CompanionsLoseEveryDamageBonusWhileSkirmishingAnIsengardOrcAndAtNoOtherTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, aragorn, sam);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(aragorn, sword);

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, worker, servant, savage1, savage2, runner);
		// Sam has no damage bonus of his own.  He is given one the way the Sword gives Aragorn his.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, sam, null, Keyword.DAMAGE, 1));
		scn.SkipToAssignments();

		// Gimli: +1 in print and +1 from his Axe.  Aragorn: +1 from the Sword.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DAMAGE));

		// Gimli fights an [isengard] Orc.  Aragorn fights another, with an Uruk-hai beside it.  Sam fights
		// an [isengard] Uruk-hai and a [moria] Orc, neither of them an [isengard] Orc.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, worker},
				new PhysicalCardImpl[]{aragorn, servant},
				new PhysicalCardImpl[]{sam, savage2});
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{sam, runner});

		// Assigned is not skirmishing: nobody has lost anything yet.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, worker));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(gimli, Keyword.DAMAGE));
		// Aragorn is assigned to an [isengard] Orc whose skirmish has not begun.
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DAMAGE));

		// Gimli wins, 6 + 2 against 5, and the Worker takes the one wound of a lost skirmish and not three.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(worker));
		// The Axe's "you may wound an Orc" is left alone.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(axe));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(worker));

		// His skirmish over, Gimli has both bonuses again.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, servant));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		// The Savage in that skirmish is a minion and keeps its own.
		assertEquals(1, scn.GetKeywordCount(savage1, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DAMAGE));

		// Aragorn loses, 8 + 2 against 7 + 5, and takes one wound and one more for the Savage's damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage2));
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingAnIsengardOrcKeepsHisDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, worker, servant);
		// No ally has a damage bonus in print, so Thrarin is given one.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, thrarin, null, Keyword.DAMAGE, 1));
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin's own ability lets him skirmish away from his home site.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, servant},
				new PhysicalCardImpl[]{thrarin, worker});

		// The companion loses his bonus in his skirmish, on the same board.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, servant));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		// Gimli loses, 6 against 7.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(thrarin);
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, worker));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ADamageBonusGivenDuringTheSkirmishIsNotGainedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(blow);

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, worker);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(blow));

		// Cleaving Blow: strength +2 and damage +1.  The strength arrives and the damage bonus does not.
		scn.FreepsPlayCard(blow);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(gimli, Keyword.DAMAGE));

		// Gimli wins, 8 against 5, and the Worker takes one wound.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseExertsSarumanToPreventOneWoundToAnIsengardOrcEachTimeUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, worker, servant, retainer, savage, troop);
		// One wound on Saruman already: vitality 4, so he can exert twice more and no further.
		scn.AddWoundsToChar(saruman, 1);
		// The five minions together are 5 + 7 + 8 + 5 + 13 = 38.  Aragorn is made 8 + 35 = 43: he wins, and
		// does not overwhelm them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 35));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, worker);
		scn.ShadowAssignToMinions(aragorn, servant, retainer, savage, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// All five are about to take the wound of a lost skirmish.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(worker));

		scn.ShadowChooseOptionalTrigger(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		// One wound is prevented, on an [isengard] Orc of the Shadow player's choice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, servant, retainer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, troop, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(worker);

		// The others are still about to be wounded, and Saruman may answer again.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowHasCardChoiceAvailable(servant, retainer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker, savage, troop));
		scn.ShadowChooseCard(servant);

		// Saruman is exhausted.  The Retainer is still about to take a wound and nothing more is offered.
		assertTrue(scn.IsExhausted(saruman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(retainer));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponsePreventsAnArcheryWoundToAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, worker, servant);
		scn.SkipToArcheryWounds();

		// Legolas is the fellowship's one archer, and the Shadow player places his arrow on the Worker.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.ShadowChooseCard(worker);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(worker));

		scn.ShadowChooseOptionalTrigger(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NoResponseToAWoundOnAnUrukHaiOnAMoriaOrcOrOnSarumanHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, worker, savage, troop);
		// Legolas stops counting towards archery fire once he uses his ability, so Aragorn is made an archer.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.ARCHER));
		scn.SkipToPhase(Phase.ARCHERY);

		// An [isengard] Uruk-hai is wounded, with an unwounded [isengard] Orc on the table.
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, worker, savage, troop));
		scn.FreepsChooseCard(savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();

		// A [moria] Orc is wounded.
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();

		// Aragorn's arrow goes to Saruman, who may be given it and is no Orc.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, worker, savage, troop));
		scn.ShadowChooseCard(saruman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffHeIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.AttachCardsTo(saruman, staff);
		scn.SkipToPhase(Phase.MANEUVER);

		// Until the Staff's ability is used the sentence is in force.
		assertFalse(scn.FreepsCanAssign(saruman));
		assertFalse(scn.ShadowCanAssign(saruman));
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.ShadowCanAssign(saruman));

		// The Free Peoples player is offered him beside the Troop, and takes the Troop; the Shadow player then
		// assigns Saruman.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		scn.FreepsAssignToMinions(aragorn, troop);
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(saruman));
		scn.ShadowAssignToMinions(aragorn, saruman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, saruman));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
