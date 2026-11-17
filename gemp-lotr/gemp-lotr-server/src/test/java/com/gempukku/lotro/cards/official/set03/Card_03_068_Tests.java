package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "3_68");

					// Saruman's Staff, Wizard's Device: an [isengard] artifact that plays on Saruman (strength +2, fierce,
					// damage +1).  "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup
					// phase."
					put("staff", "4_174");
					put("raiders", "4_207");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("tyrant", "11_204");    // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3, damage +1, toil 1
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6, vitality 2; a Shadow ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: ranger, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6, vitality 3
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf archer; Archery: exert to wound a minion
					put("quick", "4_53");       // Quick As May Be: Maneuver, spot a Dwarf and an Elf companion to wound a minion
					put("quick2", "4_53");
					put("pursuit", "1_111");    // Pursuit Just Behind: Regroup, exert a ranger companion to wound every minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Saruman, Keeper of Isengard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman may not take wounds during the archery phase and may not be assigned to a skirmish.
		 * Uruk-hai are fierce.
		 * Response: If an Uruk-hai is about to take a wound, exert Saruman to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Keeper of Isengard", card.getBlueprint().getSubtitle());
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
	public void SarumanCanBeWoundedInTheManeuverPhaseButTheArcheryWoundCannotBePlacedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var quick = scn.GetFreepsCard("quick");
		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var forger = scn.GetShadowCard("forger");

		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(quick);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, raiders, forger);
		scn.SkipToPhase(Phase.MANEUVER);

		// In the maneuver phase Saruman can be wounded like any other minion.  He is no Uruk-hai, so his own
		// wound is not one he may answer.
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(quick);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, raiders, forger));
		scn.FreepsChooseCard(saruman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Legolas is the fellowship's one archer.  The Shadow player places the archery wound, and not on Saruman.
		scn.SkipToArcheryWounds();
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, forger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		scn.ShadowChooseCard(raiders);

		// Exerting is not taking a wound: he can still pay his response's exertion in the archery phase.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnArcheryAbilityThatWoundsAMinionCannotChooseSaruman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, raiders, forger);
		scn.SkipToPhase(Phase.ARCHERY);

		// "During the archery phase" is any wound of that phase, not archery fire alone.
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(raiders, forger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		scn.FreepsChooseCard(forger);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void SarumanCannotBeAssignedToASkirmishByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var forger = scn.GetShadowCard("forger");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, forger);

		scn.SkipToAssignments();

		// The Forger on the same board is offered to each player in turn; Saruman to neither.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(forger));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsCanAssign(forger));
		assertFalse(scn.FreepsCanAssign(saruman));

		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowHasAssignmentTargetAvailable(forger));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.ShadowCanAssign(forger));
		assertFalse(scn.ShadowCanAssign(saruman));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void UrukHaiOfEveryCultureAreFierceOnceSarumanIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var sentry = scn.GetShadowCard("sentry");
		var tyrant = scn.GetShadowCard("tyrant");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, sentry, tyrant, forger);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Uruk-hai of [isengard], [sauron] and [uruk-hai], and an [isengard] Orc: none is fierce yet.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(tyrant, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(forger, Keyword.FIERCE));

		scn.ShadowPlayCard(saruman);

		// Every Uruk-hai is fierce; the Orc of the same culture is not, and neither is Saruman, a Wizard.
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(tyrant, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(forger, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseExertsSarumanToPreventAWoundToAnUrukHaiAndIsNotOfferedForAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var quick = scn.GetFreepsCard("quick");
		var quick2 = scn.GetFreepsCard("quick2");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");

		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(quick, quick2);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, sentry, forger);

		scn.SkipToPhase(Phase.MANEUVER);

		// An [isengard] Orc about to take a wound: no response.
		scn.FreepsPlayCard(quick);
		scn.FreepsChooseCard(forger);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(forger));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();

		// A [sauron] Uruk-hai about to take a wound.
		scn.FreepsPlayCard(quick2);
		scn.FreepsChooseCard(sentry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);

		// Saruman exerts and the wound is prevented.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(forger));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileSarumanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var quick = scn.GetFreepsCard("quick");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");

		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(quick);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, sentry);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(saruman, 3);

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(quick);
		scn.FreepsChooseCard(sentry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AgainstAWoundToEveryMinionTheResponseIsOfferedForEachUrukHaiAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuit = scn.GetFreepsCard("pursuit");
		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pursuit);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, raiders, sentry, forger);

		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(pursuit);

		// The wounds are placed one at a time, in no set order, and each Uruk-hai's is answered on its own:
		// the first is prevented and the second is let through.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();

		// Neither the Orc's wound nor Saruman's own was offered to be answered.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(raiders) + scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(forger));
		// 1 exertion + 1 wound from Pursuit Just Behind: outside the archery phase he can be wounded.
		assertEquals(2, scn.GetWoundsOn(saruman));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffHeTakesAnArcheryWoundAndIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var saruman = scn.GetShadowCard("saruman");
		// Isengard Forger: an Orc, so that nothing here is an Uruk-hai whose wound Saruman could answer.
		var forger = scn.GetShadowCard("forger");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, forger);
		scn.AttachCardsTo(saruman, staff);
		scn.SkipToPhase(Phase.MANEUVER);

		// Until the Staff's ability is used the sentence is in force.
		assertFalse(scn.FreepsCanAssign(saruman));
		assertFalse(scn.ShadowCanAssign(saruman));
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.ShadowCanAssign(saruman));

		// "May not take wounds during the archery phase": Legolas is the fellowship's one archer, and the Shadow
		// player may now place that wound on Saruman.
		scn.SkipToArcheryWounds();
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, forger));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(forger));

		// "And may not be assigned to a skirmish": the Free Peoples player is offered him beside the Forger, and
		// takes the Forger; the Shadow player then assigns Saruman.
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(forger));
		scn.FreepsAssignToMinions(aragorn, forger);
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(saruman));
		scn.ShadowAssignToMinions(aragorn, saruman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, forger));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, saruman));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyTheUrukHaiAreStillFierceAndHisResponseIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var quick = scn.GetFreepsCard("quick");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(quick);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, sentry);
		scn.AttachCardsTo(saruman, staff);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.FreepsCanAssign(saruman));
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);

		// Only the first sentence is switched off: Saruman can now be assigned, and the second and third
		// sentences go on working.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));

		scn.FreepsPlayCard(quick);
		scn.FreepsChooseCard(sentry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
