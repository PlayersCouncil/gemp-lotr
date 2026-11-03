package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "5_56");

					// Saruman's Staff, Wizard's Device: an [isengard] artifact that plays on Saruman (strength +2, fierce,
					// damage +1).  "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup
					// phase."
					put("staff", "4_174");
					put("raiders", "4_207");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1, no text
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6, vitality 2; a Shadow ability used only on purpose
					put("overseer", "3_65");    // Orc Overseer: [isengard] Orc, vitality 4; a Regroup ability used only on purpose
					put("goblins", "1_177");    // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

					put("sword", "1_160");      // Uruk-hai Sword: [isengard] hand weapon, bearer must be an Uruk-hai; a win trigger only
					put("scaffolding", "6_73"); // Scaffolding: [isengard] possession to the support area; its texts need a regroup discard or stacked cards
					put("discover", "4_213");   // What Did You Discover?: [isengard] support-area condition; a regroup move trigger costing 2 cards from hand
					put("bow", "18_119");       // Lurtz's Bow: [uruk-hai] ranged weapon, bearer must be an Uruk-hai; its texts need Lurtz
					put("broadsword", "4_142"); // Broad-bladed Sword: [isengard] hand weapon, bearer must be an Uruk-hai; a standing wound bar only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf archer; Archery: exert to wound a minion
					put("quick", "4_53");       // Quick As May Be: Maneuver, spot a Dwarf and an Elf companion to wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 3 is Rivendell Waterfall: "While you can spot a ranger at Rivendell Waterfall, the move limit is +1 for
	// this turn."  Aragorn is a ranger, so a fellowship that reaches it on a regroup move may move once more.
	protected VirtualTableScenario GetThreeMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "5_56");
					put("overseer", "3_65");    // Orc Overseer: [isengard] Orc, vitality 4; a Regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The White Council beside Gandalf and two [elven] allies, whose first sentence makes Saruman's first sentence
	// not apply for as long as they are spotted, the regroup phase included.
	protected VirtualTableScenario GetWhiteCouncilScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "5_56");
					put("overseer", "3_65");    // Orc Overseer: [isengard] Orc, vitality 4; a Regroup ability used only on purpose
					put("raiders", "4_207");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, no text
					put("sword", "1_160");      // Uruk-hai Sword: [isengard] hand weapon, bearer must be an Uruk-hai; a win trigger only
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
		 * Set: 5
		 * Name: Saruman, Master of Foul Folk
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
		 * 	Each time the fellowship moves, you may heal each [isengard] Orc twice.
		 * 	<b>Shadow:</b> Exert Saruman to play an [isengard] possession from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Master of Foul Folk", card.getBlueprint().getSubtitle());
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
		// Laid after the move, so that his move trigger has nothing to answer.
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, raiders, forger);
		scn.SkipToPhase(Phase.MANEUVER);

		// In the maneuver phase Saruman can be wounded like any other minion.
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(quick);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, raiders, forger));
		scn.FreepsChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Legolas is the fellowship's one archer.  The Shadow player places the archery wound: on the Uruk-hai or
		// the Orc beside him, and not on Saruman.
		scn.SkipToArcheryWounds();
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, forger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		scn.ShadowChooseCard(raiders);

		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetWoundsOn(saruman));
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
		scn.FreepsPass();
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
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, forger);

		scn.SkipToAssignments();

		// The Forger on the same board is offered to each player in turn; Saruman to neither.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(forger));
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
	public void EachMoveMayHealEachIsengardOrcTwiceAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var overseer = scn.GetShadowCard("overseer");
		var forger = scn.GetShadowCard("forger");
		var raiders = scn.GetShadowCard("raiders");
		var goblins = scn.GetShadowCard("goblins");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, overseer, forger, raiders, goblins);
		scn.AddWoundsToChar(overseer, 3);
		scn.AddWoundsToChar(forger, 1);
		scn.AddWoundsToChar(raiders, 1);
		scn.AddWoundsToChar(goblins, 1);
		scn.AddWoundsToChar(saruman, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.SkipToMovementDecision();

		assertEquals(3, scn.GetWoundsOn(overseer));
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(goblins));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The second move of the turn, with the minions of the first still on the table.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);

		// Each [isengard] Orc, of two titles, is healed twice: 3 wounds to 1, and 1 to none.  An [isengard] Uruk-hai,
		// a [moria] Orc, Saruman (an [isengard] Wizard) and a companion are not healed.
		assertEquals(1, scn.GetWoundsOn(overseer));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(goblins));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheHealAtAMoveHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var overseer = scn.GetShadowCard("overseer");
		var forger = scn.GetShadowCard("forger");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, overseer, forger);
		scn.AddWoundsToChar(overseer, 3);
		scn.AddWoundsToChar(forger, 1);
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetWoundsOn(overseer));
		assertEquals(1, scn.GetWoundsOn(forger));

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetWoundsOn(overseer));
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var overseer = scn.GetShadowCard("overseer");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, overseer);
		scn.AddWoundsToChar(overseer, 3);
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(3, scn.GetWoundsOn(overseer));

		// The fellowship stays where it is: nothing is offered, and the other player's turn begins (the minions of
		// the turn, the Overseer among them, discarded at its end with their wounds).
		scn.FreepsChooseToStay();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertInDiscard(overseer);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithNoIsengardOrcWoundedTheHealIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var forger = scn.GetShadowCard("forger");
		var raiders = scn.GetShadowCard("raiders");
		var goblins = scn.GetShadowCard("goblins");

		scn.StartGame();
		scn.FreepsPass();
		// An [isengard] Orc unwounded; wounds only on what the heal does not reach.
		scn.MoveMinionsToTable(saruman, forger, raiders, goblins);
		scn.AddWoundsToChar(raiders, 1);
		scn.AddWoundsToChar(goblins, 1);
		scn.AddWoundsToChar(saruman, 1);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(goblins));
		assertEquals(1, scn.GetWoundsOn(saruman));

		scn.FreepsChooseToMove();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(goblins));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveOfATurnOffersTheHealAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeMoveScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var overseer = scn.GetShadowCard("overseer");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, overseer);
		scn.AddWoundsToChar(overseer, 3);
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetWoundsOn(overseer));

		// The move from site 2 to Rivendell Waterfall.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertEquals(1, scn.GetWoundsOn(overseer));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With a ranger at the Waterfall the fellowship may move a third time, and Saruman and the Overseer are there.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertEquals(0, scn.GetWoundsOn(overseer));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsSarumanToPlayAnIsengardPossessionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var sword = scn.GetShadowCard("sword");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var staff = scn.GetShadowCard("staff");
		var discover = scn.GetShadowCard("discover");
		var bow = scn.GetShadowCard("bow");
		// The Free Peoples player's own copy, in his own discard pile, of a title the Shadow player's piles do not hold.
		var broadsword = scn.GetFreepsCard("broadsword");
		scn.MoveCardsToDiscard(sword, scaffolding, staff, discover, bow, broadsword);
		assertInDiscard(sword, scaffolding, staff, discover, bow, broadsword);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, raiders, savage, forger);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertInDiscard(sword, scaffolding, staff, discover, bow, broadsword);
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));

		// [isengard] possessions, one borne and one for the support area; not an [isengard] artifact (the Staff,
		// which could go on Saruman), an [isengard] condition, a possession of another culture that an Uruk-hai
		// could bear, nor a card in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, scaffolding));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(staff, discover, bow, broadsword));
		scn.ShadowChooseCard(sword);

		// The Sword goes on an Uruk-hai of the Shadow player's choice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, forger));
		scn.ShadowChooseCard(savage);
		assertAttachedTo(sword, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Used again: the support-area possession, the one left that may be played, goes to the support area unasked.
		scn.ShadowUseCardAction(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertInZone(Zone.SUPPORT, scaffolding);
		assertInDiscard(staff, discover, bow, broadsword);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhenSarumanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToDiscard(sword);
		assertInDiscard(sword);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, raiders);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(saruman, 3);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertInDiscard(sword);
		assertTrue(scn.IsExhausted(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoIsengardPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var staff = scn.GetShadowCard("staff");
		var discover = scn.GetShadowCard("discover");
		var bow = scn.GetShadowCard("bow");
		var broadsword = scn.GetFreepsCard("broadsword");
		scn.MoveCardsToDiscard(staff, discover, bow, broadsword);
		assertInDiscard(staff, discover, bow, broadsword);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, raiders);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The play is the ability's price (Q-0062): with nothing there it may play, it is not offered, though
		// Saruman can exert.
		assertInDiscard(staff, discover, bow, broadsword);
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffHeTakesAnArcheryWoundAndIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var saruman = scn.GetShadowCard("saruman");
		var forger = scn.GetShadowCard("forger");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, forger);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
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
	public void WithHisFirstSentenceMadeNotToApplyByTheWhiteCouncilHisMoveTriggerAndShadowAbilityStillWork() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWhiteCouncilScenario();

		var council = scn.GetFreepsCard("council");
		var gandalf = scn.GetFreepsCard("gandalf");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.MoveCardsToSupportArea(council);

		var saruman = scn.GetShadowCard("saruman");
		var overseer = scn.GetShadowCard("overseer");
		var raiders = scn.GetShadowCard("raiders");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToDiscard(sword);
		assertInDiscard(sword);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, council);
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, overseer, raiders);
		scn.AddWoundsToChar(overseer, 3);

		// Gandalf and two [elven] allies are spotted: his first sentence does not apply, and he may be assigned.
		assertTrue(scn.FreepsCanAssign(saruman));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsCanAssign(saruman));
		assertEquals(3, scn.GetWoundsOn(overseer));

		// His second sentence still applies: the move heals the Overseer twice.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertEquals(1, scn.GetWoundsOn(overseer));

		// And his third: in the Shadow phase after that move he exerts to play the Sword from the discard pile.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInDiscard(sword);
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertAttachedTo(sword, raiders);
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
