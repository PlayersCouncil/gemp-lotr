package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lindenroot", "5_19");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound, strength 8; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound [shire] Hobbit, vitality 4; no weapon here, so no text
					put("pippin", "103_31");    // Pippin, Trollslayer: unbound [gondor] Hobbit, vitality 4 (to play, spot a [gondor] companion: Aragorn); a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("guardian", "5_17");    // Forest Guardian: a second unhasty [gandalf] Ent companion, not unique; its own assignment ability is not used
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("sword1", "1_299");     // Hobbit Sword: cards to hold in hand, no text that acts there
					put("sword2", "1_299");
					put("sword3", "1_299");

					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion whose race is Hobbit; abilities used only on purpose
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not met when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LindenrootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Lindenroot, Elder Shepherd
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 10
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Unhasty</b>.<br><b>Assignment:</b> Exert an unbound Hobbit or discard 2 cards from hand to
		 * allow Lindenroot to skirmish.<br><b>Skirmish:</b> Exert Lindenroot to wound an [isengard] minion he is
		 * skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lindenroot");

		assertEquals("Lindenroot", card.getBlueprint().getTitle());
		assertEquals("Elder Shepherd", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.UNHASTY));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertingAnUnboundHobbitAllowsLindenrootToBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lindenroot = scn.GetFreepsCard("lindenroot");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(lindenroot, aragorn, merry, pippin, sam);
		scn.MoveAlliesToTable(bounder);

		var ted = scn.GetShadowCard("ted");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(ted, raiders);
		scn.MoveCardsToHand(scn.GetFreepsCard("sword1"), scn.GetFreepsCard("sword2"));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(lindenroot));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(lindenroot));

		scn.FreepsUseCardAction(lindenroot);

		// Both costs can be paid: the player chooses which.
		assertEquals(List.of("Exert an unbound Hobbit", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert an unbound Hobbit");

		// An unbound Hobbit of either culture; not a Ring-bound one, not an unbound companion who is no Hobbit,
		// not a Hobbit who is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, aragorn, lindenroot, bounder, ted));
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsCanAssign(lindenroot));

		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lindenroot));
		scn.FreepsAssignToMinions(lindenroot, raiders);
		assertTrue(scn.IsCharAssignedAgainst(lindenroot, raiders));

		scn.ShadowDeclineAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void DiscardingTwoCardsFromHandAllowsLindenrootToBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindenroot = scn.GetFreepsCard("lindenroot");
		var merry = scn.GetFreepsCard("merry");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(lindenroot, merry, guardian);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.MoveCardsToHand(sword1, sword2, sword3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.FreepsCanAssign(lindenroot));
		assertFalse(scn.FreepsCanAssign(guardian));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsUseCardAction(lindenroot);
		scn.FreepsChoose("Discard 2 cards from hand");

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sword1, sword3);

		assertInZone(Zone.DISCARD, sword1, sword3);
		assertInZone(Zone.HAND, sword2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(lindenroot));
		// The other unhasty companion is not allowed by it.
		assertFalse(scn.FreepsCanAssign(guardian));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lindenroot));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(guardian));
		scn.FreepsAssignToMinions(lindenroot, raiders);
		assertTrue(scn.IsCharAssignedAgainst(lindenroot, raiders));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedWithNoUnboundHobbitAbleToExertAndOneCardInHandAndThenDiscardsBothWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindenroot = scn.GetFreepsCard("lindenroot");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(lindenroot, merry, sam);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// Merry, vitality 4, is exhausted; Sam could exert but is Ring-bound.
		scn.AddWoundsToChar(merry, 3);
		scn.MoveCardsToHand(sword1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(lindenroot));
		assertFalse(scn.FreepsCanAssign(lindenroot));

		// A second card in hand: the discard can be paid, and is the only cost that can.
		scn.MoveCardsToHand(sword2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(lindenroot));

		scn.FreepsUseCardAction(lindenroot);

		assertInZone(Zone.DISCARD, sword1, sword2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsCanAssign(lindenroot));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void TheAllowanceLastsOnlyForTheAssignmentPhaseItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindenroot = scn.GetFreepsCard("lindenroot");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(lindenroot, merry);

		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// With no card in hand only the exertion can be paid, and Merry is the one unbound Hobbit: nothing is asked.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsUseCardAction(lindenroot);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lindenroot));
		scn.FreepsAssignToMinions(lindenroot, raiders);

		// Lindenroot, 10, beats the Raiding Party, 9.
		scn.FreepsResolveSkirmish(lindenroot);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(lindenroot));
		assertEquals(1, scn.GetWoundsOn(raiders));

		// The fellowship moves again, and the same turn has a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(servant);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(lindenroot));
		assertTrue(scn.FreepsActionAvailable(lindenroot));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(lindenroot));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(merry));
	}

	@Test
	public void SkirmishAbilityWoundsAnIsengardMinionHeIsSkirmishingAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindenroot = scn.GetFreepsCard("lindenroot");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(lindenroot, aragorn);

		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant, savage, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		// The minions left over are the Shadow player's to assign, and he may give them to an unhasty companion.
		scn.ShadowAssignToMinions(lindenroot, raiders, servant, runner);

		// Aragorn's skirmish first.  Lindenroot is assigned [isengard] minions whose skirmish has not begun, and the
		// [isengard] Savage is fighting Aragorn: none is a minion he is skirmishing.  He may still exert, for nothing.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharAssignedAgainst(lindenroot, servant));
		assertEquals(0, scn.GetWoundsOn(lindenroot));
		assertTrue(scn.FreepsActionAvailable(lindenroot));

		scn.FreepsUseCardAction(lindenroot);
		assertEquals(1, scn.GetWoundsOn(lindenroot));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn, 8, beats the Savage, 5.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.FreepsResolveSkirmish(lindenroot);
		assertTrue(scn.IsCharSkirmishingAgainst(lindenroot, raiders));
		assertTrue(scn.IsCharSkirmishingAgainst(lindenroot, servant));
		assertTrue(scn.IsCharSkirmishingAgainst(lindenroot, runner));

		scn.FreepsUseCardAction(lindenroot);
		assertEquals(2, scn.GetWoundsOn(lindenroot));

		// An [isengard] Uruk-hai and an [isengard] Orc he is skirmishing; not the [moria] Orc beside them, and not the
		// [isengard] minion that fought Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(raiders, servant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(servant);

		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileLindenrootIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindenroot = scn.GetFreepsCard("lindenroot");
		scn.MoveCompanionsToTable(lindenroot);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(lindenroot, 3);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(lindenroot, raiders);
		scn.FreepsResolveSkirmish(lindenroot);

		assertTrue(scn.IsCharSkirmishingAgainst(lindenroot, raiders));
		assertEquals(3, scn.GetWoundsOn(lindenroot));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(lindenroot));
	}
}
