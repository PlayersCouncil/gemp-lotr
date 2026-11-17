package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swarming", "14_11");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its trigger is on its play only, and it is cheated in
					put("runner2", "1_178");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Hobbit companion; a fellowship ability used only on purpose
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: Wizard companion; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally; a fellowship ability used only on purpose
					// Gondorian Merchant: Man ally; his maneuver ability needs initiative and 2 cards discarded
					put("merchant", "7_97");
					// Beorn, Skin-Changer: a follower who is a Man, and so a Man who is no character (the only one in print)
					put("beorn", "31_13");
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("secret", "1_71");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwarmingHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Swarming Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At the start of the regroup phase, if you can spot more Man characters than any other race, make the move limit -1 this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swarming");

		assertEquals("Swarming Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithMoreMenThanEachOtherRaceTheMoveLimitIsOneLessAtTheStartOfRegroupForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		// With Frodo: one Hobbit, one Man companion, one Wizard, one Dwarf and one Elf
		scn.MoveCompanionsToTable("aragorn", "gandalf", "gimli", "arwen");
		scn.MoveCardsToHand(secret);

		var swarming = scn.GetShadowCard("swarming");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// The limit cannot read below 1, so from 2 a penalty of 1 and of 2 read the same: raise it first.
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit());

		// Two Men (Aragorn and the Hillman) against one of every other race, the Orc minion among them.
		scn.MoveMinionsToTable(swarming, runner);
		scn.SkipToAssignments();
		// Not yet: the assignment phase is not the regroup phase.
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// 2 + 1 - 1
		assertEquals(2, scn.GetMoveLimit());

		// One move made of two: the fellowship moves again, and the penalty stands for the turn.
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePenaltyIsGoneOnTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn");

		var swarming = scn.GetShadowCard("swarming");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Two Men against one Hobbit
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetMoveLimit());

		// At the limit the turn ends without the question of another move, and the next turn has its own limit.
		scn.FreepsPass();
		scn.ShadowPass();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void AManAllyCountsAmongTheManCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveAlliesToTable("merchant");

		var swarming = scn.GetShadowCard("swarming");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// The Merchant and the Hillman against Frodo and the Orc
		scn.MoveMinionsToTable(swarming, runner);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetMoveLimit());
	}

	@Test
	public void AsManyHobbitCompanionsAsMenLeaveTheLimitAndAManFollowerIsNoCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		scn.MoveCompanionsToTable("aragorn", "sam");
		scn.MoveCardsToSupportArea(beorn);

		var swarming = scn.GetShadowCard("swarming");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beorn);
		// Aragorn and the Hillman against Frodo and Sam; Beorn would make the Men three.
		scn.MoveMinionsToTable(swarming);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void AsManyElvesAsMenLeaveTheLimitWhereOneElfIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "arwen");
		scn.MoveAlliesToTable("celeborn");

		var swarming = scn.GetShadowCard("swarming");

		scn.StartGame();
		// Aragorn and the Hillman against Arwen and Celeborn (and one Hobbit)
		scn.MoveMinionsToTable(swarming);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void AsManyOrcMinionsAsMenLeaveTheLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn");

		var swarming = scn.GetShadowCard("swarming");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		// Aragorn and the Hillman against two Goblin Runners (and one Hobbit)
		scn.MoveMinionsToTable(swarming, runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
	}
}
