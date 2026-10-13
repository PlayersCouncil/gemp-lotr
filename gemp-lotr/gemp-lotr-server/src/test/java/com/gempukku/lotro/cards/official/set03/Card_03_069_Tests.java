package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "3_69");
					// Saruman's Staff, Wizard's Device: an [isengard] artifact that plays on Saruman (strength +2, fierce,
					// damage +1).  "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup
					// phase."
					put("staff", "4_174");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					// Uruk Shaman: [isengard] Uruk-hai, strength 8, vitality 2, damage +1.  "Maneuver: Remove (2)
					// to heal an Uruk-hai."
					put("shaman", "1_152");
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					// Saruman's Ambition: an [isengard] condition in the support area, and no minion.
					put("ambition", "1_133");

					// Sam, Proper Poet: Ring-bound and not the Ring-bearer; vitality 4.  "Maneuver: Exert Sam twice
					// to make him defender +1 until the regroup phase."
					put("sam", "2_114");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: his text is about bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: his text is about [shire] tales
					// Bounder: a Hobbit ally whose home is site 2, where these assignment phases are; a skirmish
					// ability used only on purpose.
					put("bounder", "1_286");
					// Legolas, Greenleaf: archer.  "Archery: Exert Legolas to wound a minion; Legolas does not add to
					// the fellowship archery total."
					put("legolas", "1_50");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only
					// Quick As May Be: Maneuver event, cost 1.  "Spot a Dwarf companion and an Elf companion to
					// wound a minion."
					put("quick", "4_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "3_69");
					// Uruk-hai Raiding Party: an [isengard] minion; Damage +1 is its whole text
					put("raiders", "1_158");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("merry", "1_303");      // Merry, From O'er the Brandywine: his text is about bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: his text is about [shire] tales
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
		 * Name: Saruman, Servant of the Eye
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman may not take wounds during the archery phase and may not be assigned to a skirmish.<br><b>Assignment:</b> Exert Saruman to assign an [isengard] minion to a companion (except the Ring-bearer). That companion may exert to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Servant of the Eye", card.getBlueprint().getSubtitle());
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
	public void SarumanCanBeWoundedInTheManeuverPhaseButNoArcheryWoundCanBePlacedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var quick = scn.GetFreepsCard("quick");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(quick);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, worker);
		scn.SkipToPhase(Phase.MANEUVER);

		// In the maneuver phase Saruman is wounded like any other minion.
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(quick);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, savage, worker));
		scn.FreepsChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Legolas is the fellowship's one archer.  The Shadow player places the archery wound, and not on Saruman.
		scn.SkipToArcheryWounds();
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnArcheryAbilityThatWoundsAMinionCannotChooseSaruman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, worker);
		scn.SkipToPhase(Phase.ARCHERY);

		// "During the archery phase" is any wound of that phase, not archery fire alone.
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, worker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		scn.FreepsChooseCard(worker);
		assertEquals(1, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithNoOtherMinionSarumanTakesNoArcheryWoundAndNobodyIsAskedToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.ARCHERY);

		// The fellowship's archery total is 1, and there is nobody the wound can be placed on.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Nor is anybody asked to assign: the assignment actions are followed by the regroup phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.ShadowPass();
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SarumanCannotBeAssignedToASkirmishByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToAssignments();

		// The Troop on the same board is offered to each player in turn; Saruman to neither.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsCanAssign(troop));
		assertFalse(scn.FreepsCanAssign(saruman));

		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.ShadowCanAssign(troop));
		assertFalse(scn.ShadowCanAssign(saruman));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void AssignmentAbilityTakesAnIsengardMinionAndACompanionWhoIsNotTheRingBearerAndTheCompanionMayExertToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(bounder);

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToSupportArea(ambition);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, worker, troop);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Site 2 is the Bounder's home, so the Free Peoples player could assign him there himself.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(bounder));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));

		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		// A companion: Sam is Ring-bound and no Ring-bearer.  Not Frodo, and not the ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, bounder));
		scn.ShadowChooseCard(aragorn);
		// An [isengard] minion, Uruk-hai or Orc.  Not the [moria] Troop, and not Saruman, who may not be assigned.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, saruman));
		scn.ShadowChooseCard(savage);

		// Aragorn cannot exert, so there is nothing to ask: the assignment is made.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// A second use: Sam and the Worker are the only two left that can be assigned.
		scn.FreepsPass();
		scn.ShadowUseCardAction(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsChooseYes();

		// Sam exerts, and the Worker is not assigned to him.  Saruman's exertion stands.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(worker));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SarumanCannotAssignMultipleMinionsToSameCompanionEvenWithDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(sam, merry, pippin);

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var shaman = scn.GetShadowCard("shaman");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, shaman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(saruman));
		assertTrue(scn.CanBeAssignedViaAction(sam));
		assertTrue(scn.CanBeAssignedViaAction(savage));
		assertTrue(scn.CanBeAssignedViaAction(shaman));

		scn.ShadowUseCardAction(saruman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, merry, pippin));
		scn.ShadowChooseCard(sam);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, shaman));
		scn.ShadowChooseCard(savage);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();

		// Declined: the Savage is assigned to Sam, and Sam has not exerted beyond the two for his own ability.
		assertTrue(scn.IsCharAssignedAgainst(sam, savage));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.IsCharAssigned(shaman));
		assertFalse(scn.CanBeAssignedViaAction(sam));
		assertFalse(scn.CanBeAssignedViaAction(savage));
		assertTrue(scn.CanBeAssignedViaAction(shaman));

		// Sam is still defender +1, but a card assigns only to a companion who has no minion: he is not
		// offered again, and the Shaman, the one minion left, goes to another companion.
		scn.FreepsPass();
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, frodo));
		scn.ShadowChooseCard(merry);
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(merry, shaman));
		assertFalse(scn.IsCharAssignedAgainst(sam, shaman));
		assertEquals(0, scn.GetWoundsOn(merry));

		// Nothing is left for either player to assign, and the skirmishes the card made are fought.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhenSarumanCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(saruman, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The Savage and Aragorn are both there to be assigned.
		assertTrue(scn.CanBeAssignedViaAction(savage));
		assertTrue(scn.CanBeAssignedViaAction(aragorn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
	}

	@Test
	public void AssignmentAbilityOffersEveryCompanionButTheRingBearerAndASecondUseWithNoMinionLeftAssignsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, raiders, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));

		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));

		// The Raiding Party is the one minion that can be taken: Saruman may not be assigned, and the Troop
		// is not [isengard].  So no minion is asked for.
		scn.ShadowChooseCard(merry);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(merry, raiders));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// No [isengard] minion is left to assign.  The ability has a cost that can be paid, so it is still
		// offered: Saruman exerts for nothing, though Pippin and the Troop are both free.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertFalse(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharAssigned(troop));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(merry, raiders));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffHeTakesAnArcheryWoundAndIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(aragorn, legolas);

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

		// "May not take wounds during the archery phase": Legolas is the fellowship's one archer, and the Shadow
		// player may now place that wound on Saruman.
		scn.SkipToArcheryWounds();
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, troop));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The rest of his text still applies: his own assignment ability is on offer.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowPass();

		// "And may not be assigned to a skirmish": the Free Peoples player is offered him beside the Troop, and
		// takes the Troop; the Shadow player then assigns Saruman.
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
