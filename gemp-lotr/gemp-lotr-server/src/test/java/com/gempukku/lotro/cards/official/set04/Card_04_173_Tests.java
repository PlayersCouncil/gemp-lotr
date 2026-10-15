package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_173_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "4_173");
					// Saruman's Staff, Wizard's Device: unique [isengard] artifact, cost 2, plays on Saruman (strength +2,
					// fierce, damage +1).  "Maneuver: Make the first sentence of Saruman's game text not apply until the
					// regroup phase."
					put("staff", "4_174");
					put("staff2", "4_174");
					// Staff of Saruman, Fallen Istar's Stave: an [isengard] artifact staff of another title, cost 2, that
					// Saruman could bear ("Bearer must be a Wizard"); a Regroup ability only
					put("stave", "13_81");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, cost 2; Damage +1 is its whole text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a Regroup ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					// Saruman's Ambition: an [isengard] condition in the support area; its Skirmish ability is used only on purpose
					put("ambition", "1_133");
					// Weary: an [isengard] condition borne by a companion; its trigger answers an assignment to an [isengard] tracker
					put("weary", "4_212");

					// Supplies of the Mark: a [rohan] condition in the Free Peoples support area; a Fellowship ability only
					put("supplies", "4_290");
					put("armor", "1_8");        // Dwarven Armor: a possession, borne by a Dwarf; changes no number
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a Skirmish ability only
					// Legolas, Greenleaf: archer.  "Archery: Exert Legolas to wound a minion; Legolas does not add to the
					// fellowship archery total."
					put("legolas", "1_50");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a Maneuver ability used only on purpose
					// Quick As May Be: Maneuver event, cost 1.  "Spot a Dwarf companion and an Elf companion to wound a minion."
					put("quick", "4_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The White Council beside Gandalf and two [elven] allies, whose first sentence makes Saruman's first sentence not
	// apply for as long as they are spotted.
	protected VirtualTableScenario GetWhiteCouncilScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "4_173");
					put("staff", "4_174");
					put("ambition", "1_133");
					// The White Council: [gandalf] condition in the support area.  "While you can spot Gandalf and 2
					// [elven] allies, the first sentence of Saruman's game text does not apply."  Its Regroup ability is
					// used only on purpose.
					put("council", "101_18");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a standing strength bonus only
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] ally; a Skirmish ability only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: [elven] ally; a Fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Saruman, Black Traitor
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman may not take wounds during the archery phase and may not be assigned to a skirmish.<br>When you play Saruman, you may discard a condition.<br><b>Shadow:</b> Exert Saruman to play Saruman's Staff from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Black Traitor", card.getBlueprint().getSubtitle());
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
	public void WhenPlayedHeMayDiscardAConditionOfEitherSideInPlayButNotAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var supplies = scn.GetFreepsCard("supplies");
		var saruman = scn.GetShadowCard("saruman");
		var ambition = scn.GetShadowCard("ambition");
		var weary = scn.GetShadowCard("weary");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, weary);
		scn.MoveCardsToSupportArea(supplies, ambition);
		scn.MoveCardsToHand(saruman, savage);

		scn.StartGame();
		scn.SetTwilight(10);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(weary, gimli);
		assertInZone(Zone.SUPPORT, supplies, ambition);
		scn.FreepsPass();

		scn.ShadowPlayCard(saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);

		// A Free Peoples condition, his own condition in his support area and his own condition on a companion; not a
		// possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(supplies, ambition, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(supplies);

		assertInZone(Zone.DISCARD, supplies);
		assertInZone(Zone.SUPPORT, ambition);
		assertAttachedTo(weary, gimli);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another minion played after him, with conditions still in play, sets nothing off.
		scn.ShadowPlayCard(savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, ambition);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheDiscardLeavesEveryConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var supplies = scn.GetFreepsCard("supplies");
		var saruman = scn.GetShadowCard("saruman");
		var ambition = scn.GetShadowCard("ambition");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, weary);
		scn.MoveCardsToSupportArea(supplies, ambition);
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.SetTwilight(10);
		assertAttachedTo(weary, gimli);
		assertInZone(Zone.SUPPORT, supplies, ambition);
		scn.FreepsPass();

		scn.ShadowPlayCard(saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, supplies, ambition);
		assertAttachedTo(weary, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayTheDiscardIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.SetTwilight(10);
		assertAttachedTo(armor, gimli);
		scn.FreepsPass();

		// The only card in play that is no character is a possession: the offer would do nothing, and is not made.
		scn.ShadowPlayCard(saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsSarumanToPlaySarumansStaffFromTheDiscardPileOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var staff2 = scn.GetShadowCard("staff2");
		var stave = scn.GetShadowCard("stave");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(saruman);
		scn.MoveCardsToDiscard(staff, staff2, stave, savage);
		assertInZone(Zone.DISCARD, staff, staff2, stave, savage);
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		// Two copies of the Staff lie there; not the staff of another title he could bear, nor a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(staff));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stave, savage));
		scn.ShadowChooseCardBPFromSelection(staff);

		// One copy of the Staff is on him, and the other copy, the stave and the Savage are left in the pile.
		assertEquals(1, scn.GetAttachedCards(saruman).size());
		assertEquals("Saruman's Staff", scn.GetAttachedCards(saruman).get(0).getBlueprint().getTitle());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, stave, savage);

		// The Staff is unique: with one on him the other copy cannot be played, so the ability is not offered again,
		// though he could exert.
		assertFalse(scn.ShadowActionAvailable(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.MoveCardsToDiscard(staff);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DISCARD, staff);
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhenSarumanCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(saruman);
		scn.MoveCardsToDiscard(staff);
		// Vitality 4 with 3 wounds: Saruman is exhausted.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertInZone(Zone.DISCARD, staff);
		scn.FreepsPass();

		assertEquals(3, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithoutAStaffInHisOwnDiscardPileOrTheTwilightToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var stave = scn.GetShadowCard("stave");
		var freepsStaff = scn.GetFreepsCard("staff");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.MoveCardsToHand(staff);
		scn.MoveCardsToDiscard(stave, freepsStaff);
		assertInZone(Zone.HAND, staff);
		assertInZone(Zone.DISCARD, stave, freepsStaff);
		scn.FreepsPass();

		// His Staff is in his hand, the other player's in that player's discard pile, and his own discard pile holds
		// only a staff of another title: the play is part of the price, and it cannot be made.
		assertInZone(Zone.HAND, staff);
		assertInZone(Zone.DISCARD, stave, freepsStaff);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));

		// His Staff in his discard pile, with one twilight in the pool, one short of its cost.
		scn.MoveCardsToDiscard(staff);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, staff);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(saruman));

		// With two it is offered.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
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

		assertAttachedTo(staff, saruman);
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

		// "And may not be assigned to a skirmish": the Free Peoples player is offered him beside the Troop, and
		// takes the Troop; the Shadow player then assigns Saruman.
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

	@Test
	public void WithHisFirstSentenceMadeNotToApplyByTheWhiteCouncilHisPlayTriggerAndShadowAbilityStillWork() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWhiteCouncilScenario();

		var council = scn.GetFreepsCard("council");
		var gandalf = scn.GetFreepsCard("gandalf");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.MoveCardsToSupportArea(council, ambition);
		scn.MoveCardsToHand(saruman);
		scn.MoveCardsToDiscard(staff);

		scn.StartGame();
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, council, ambition);
		assertInZone(Zone.DISCARD, staff);
		scn.FreepsPass();

		scn.ShadowPlayCard(saruman);
		// Gandalf and two [elven] allies are spotted: his first sentence does not apply, and he may be assigned.
		assertTrue(scn.FreepsCanAssign(saruman));
		// His second sentence still does: the discard is offered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(council, ambition));
		scn.ShadowChooseCard(ambition);
		assertInZone(Zone.DISCARD, ambition);
		assertInZone(Zone.SUPPORT, council);

		// And so does his third: he exerts to play the Staff from the discard pile.
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertAttachedTo(staff, saruman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
