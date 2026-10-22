package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronger", "8_47");
					// Dead Man of Dunharrow: [gondor] Wraith companion, strength 6, vitality 3, enduring; his "when you
					// play" exertions are not set off, since he is cheated in
					put("deadman", "10_27");
					// Oathbreaker: [gondor] Wraith companion, strength 6, vitality 3, enduring; a skirmish ability used
					// only on purpose
					put("oath", "8_41");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					put("boromir", "1_97");

					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its "when you play" is not set off, since it
					// is cheated in
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					// Corpse Lights: [sauron] Wraith minion, strength 7, vitality 2; a skirmish ability used only on purpose
					put("lights", "6_99");
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound
					// Hobbits
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongerandMoreTerribleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Stronger and More Terrible
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, add a threat.<br>Each time your [gondor] Wraith wins a skirmish, you may remove a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stronger");

		assertEquals("Stronger and More Terrible", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithTheThreatsAtTheirLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronger = scn.GetFreepsCard("stronger");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(deadman);
		scn.MoveCardsToHand(stronger);

		scn.StartGame();
		// Frodo and Dead Man of Dunharrow: a threat limit of 2, reached.
		scn.AddThreats(2);
		// The choice of fellowship actions was sent before the threats were added.
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertInHand(stronger);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stronger));
	}

	@Test
	public void PlayingItAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronger = scn.GetFreepsCard("stronger");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(deadman);
		scn.MoveCardsToHand(stronger);

		scn.StartGame();
		// One below the limit of 2.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertInHand(stronger);

		assertTrue(scn.FreepsPlayAvailable(stronger));
		scn.FreepsPlayCard(stronger);

		assertInZone(Zone.SUPPORT, stronger);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachGondorWraithWinningASkirmishMayRemoveAThreatAndAGondorManWinningDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronger = scn.GetFreepsCard("stronger");
		var deadman = scn.GetFreepsCard("deadman");
		var oath = scn.GetFreepsCard("oath");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCardsToSupportArea(stronger);
		scn.MoveCompanionsToTable(deadman, oath, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		// Four companions: room for the 2 threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, stronger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner1}, new PhysicalCardImpl[]{deadman, runner2},
				new PhysicalCardImpl[]{oath, runner3});
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Boromir, a [gondor] Man who is no Wraith, wins (7 against 5): nothing is offered.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stronger));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(2, scn.GetThreats());

		// Dead Man of Dunharrow wins (6 against 5).
		scn.FreepsResolveSkirmish(deadman);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronger));
		scn.FreepsChooseOptionalTrigger(stronger);
		assertEquals(1, scn.GetThreats());

		// Oathbreaker, a Wraith of another title, wins (6 against 5).
		scn.FreepsResolveSkirmish(oath);
		scn.PassSkirmishActions();
		assertInDiscard(runner3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronger));
		scn.FreepsChooseOptionalTrigger(stronger);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronger = scn.GetFreepsCard("stronger");
		var deadman = scn.GetFreepsCard("deadman");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(stronger);
		scn.MoveCompanionsToTable(deadman);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman, runner1);
		scn.FreepsResolveSkirmish(deadman);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronger));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheWraithLosesASkirmishToASauronWraith() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronger = scn.GetFreepsCard("stronger");
		var deadman = scn.GetFreepsCard("deadman");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToSupportArea(stronger);
		scn.MoveCompanionsToTable(deadman);

		scn.StartGame();
		scn.MoveMinionsToTable(lights);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(deadman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman, lights);
		// Corpse Lights, a Wraith of the opponent's and of another culture, wins (7 against 6), and Dead Man of
		// Dunharrow loses and lives.
		scn.FreepsResolveSkirmish(deadman);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(deadman));
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stronger));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronger = scn.GetFreepsCard("stronger");
		var deadman = scn.GetFreepsCard("deadman");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(stronger);
		scn.MoveCompanionsToTable(deadman);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		assertEquals(0, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman, runner1);
		scn.FreepsResolveSkirmish(deadman);
		scn.PassSkirmishActions();

		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stronger));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningAgainInTheFierceRoundOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronger = scn.GetFreepsCard("stronger");
		var deadman = scn.GetFreepsCard("deadman");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(stronger);
		scn.MoveCompanionsToTable(deadman);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// Frodo and Dead Man of Dunharrow: a threat limit of 2.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman, pursuer);
		// 6 against 5.
		scn.FreepsResolveSkirmish(deadman);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronger));
		scn.FreepsChooseOptionalTrigger(stronger);
		assertEquals(1, scn.GetThreats());

		// The same Wraith against the same fierce minion: a second win.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(deadman, pursuer);
		scn.FreepsResolveSkirmish(deadman);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronger));
		scn.FreepsChooseOptionalTrigger(stronger);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
