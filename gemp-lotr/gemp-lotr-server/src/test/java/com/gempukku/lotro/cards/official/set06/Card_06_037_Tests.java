package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "6_37");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound [shire] Hobbit, vitality 4; no weapon here, so no text
					put("pippin", "103_31");    // Pippin, Trollslayer: unbound [gondor] Hobbit, vitality 4; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("guardian", "5_17");    // Forest Guardian: a second unhasty [gandalf] Ent companion; its own assignment ability is not used
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: an Ent companion who is not unhasty, strength 6; +2 only while 3 Ents are spotted
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Ent
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion whose race is Hobbit; abilities used only on purpose
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: strength 7; a regroup ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its play trigger is not met when cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreebeardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Treebeard, Guardian of the Forest
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 12
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Unhasty</b>.<br><b>Assignment:</b> Exert an unbound Hobbit to allow Treebeard to skirmish.<br><b>Skirmish:</b> Exert Treebeard to make an Ent <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("treebeard");

		assertEquals("Treebeard", card.getBlueprint().getTitle());
		assertEquals("Guardian of the Forest", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.UNHASTY));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertingAnUnboundHobbitAllowsTreebeardAndNoOtherUnhastyCompanionToBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var guardian = scn.GetFreepsCard("guardian");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(treebeard, aragorn, merry, pippin, sam, guardian);
		scn.MoveAlliesToTable(bounder);

		var ted = scn.GetShadowCard("ted");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(ted, raiders);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(treebeard));
		assertFalse(scn.FreepsCanAssign(guardian));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(treebeard));

		scn.FreepsUseCardAction(treebeard);
		// An unbound Hobbit of either culture; not a Ring-bound one, not an unbound companion who is no Hobbit,
		// not a Hobbit who is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, aragorn, treebeard, guardian, bounder, ted));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsCanAssign(treebeard));
		// The other unhasty companion is not allowed by it.
		assertFalse(scn.FreepsCanAssign(guardian));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(treebeard));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(guardian));
		scn.FreepsAssignToMinions(treebeard, raiders);
		assertTrue(scn.IsCharAssignedAgainst(treebeard, raiders));
		scn.ShadowDeclineAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhileTheOnlyUnboundHobbitIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(treebeard, merry, sam);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// Merry, vitality 4, is exhausted; Sam could exert but is Ring-bound.
		scn.AddWoundsToChar(merry, 3);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsActionAvailable(treebeard));
		assertFalse(scn.FreepsCanAssign(treebeard));
	}

	@Test
	public void TheAllowanceLastsOnlyForTheAssignmentPhaseItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(treebeard, merry);

		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(merry));
		// Merry is the one unbound Hobbit: nothing is asked.
		scn.FreepsUseCardAction(treebeard);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(treebeard));
		scn.FreepsAssignToMinions(treebeard, raiders);
		// Treebeard, 12, beats the Raiding Party, 9.
		scn.FreepsResolveSkirmish(treebeard);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(1, scn.GetWoundsOn(raiders));

		// The fellowship moves again, and the same turn has a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(servant);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(treebeard));
		assertTrue(scn.FreepsActionAvailable(treebeard));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(treebeard));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(merry));
	}

	@Test
	public void SkirmishAbilityMakesAnEntInOrOutOfTheSkirmishDamagePlusOneForThatSkirmishUntilTreebeardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var treebeard = scn.GetFreepsCard("treebeard");
		var avenger = scn.GetFreepsCard("avenger");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(treebeard, avenger, gandalf);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{avenger, runner}, new PhysicalCardImpl[]{gandalf, troop});
		scn.FreepsResolveSkirmish(avenger);
		assertTrue(scn.IsCharSkirmishingAgainst(avenger, runner));
		// Treebeard is in no skirmish.
		assertFalse(scn.IsCharAssigned(treebeard));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(treebeard));

		scn.FreepsUseCardAction(treebeard);
		assertEquals(1, scn.GetWoundsOn(treebeard));
		// An Ent outside the skirmish (Treebeard himself) and one in it; not a [gandalf] companion who is no Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(treebeard, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(treebeard);
		assertEquals(1, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		scn.ShadowPass();
		scn.FreepsUseCardAction(treebeard);
		scn.FreepsChooseCard(avenger);
		assertEquals(2, scn.GetWoundsOn(treebeard));
		assertEquals(1, scn.GetKeywordCount(avenger, Keyword.DAMAGE));

		// A second use on the same Ent adds to the first.
		scn.ShadowPass();
		scn.FreepsUseCardAction(treebeard);
		scn.FreepsChooseCard(avenger);
		assertEquals(3, scn.GetWoundsOn(treebeard));
		assertEquals(2, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));

		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(treebeard));

		// Ent Avenger, 6, beats the Goblin Runner, 5.  In the next skirmish the bonuses are gone.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertEquals(0, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
