package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_145_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("regiment", "7_145");

					// Strong enough that a strength of 11 does not overwhelm them.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingRegimentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Easterling Regiment
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>. <b>Fierce</b>.<br>Each time Easterling Regiment wins a skirmish, you may add (1) for each burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("regiment");

		assertEquals("Easterling Regiment", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachTimeHeWinsASkirmishHeMayAddOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var regiment = scn.GetShadowCard("regiment");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(regiment);
		assertEquals(2, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regiment);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 11 against 8: he wins.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(regiment));
		int twilight = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		// (1) for each of 2 burdens
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());

		// He is fierce, and wins again: offered again, and adds again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, regiment);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(regiment));
		twilight = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningAddsNothingAndTheAmountFollowsTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var regiment = scn.GetShadowCard("regiment");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(regiment);
		assertEquals(3, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regiment);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(regiment));
		int twilight = scn.GetTwilight();
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The fierce win, taken this time: (1) for each of 3 burdens.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, regiment);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(regiment));
		twilight = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var regiment = scn.GetShadowCard("regiment");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(regiment);
		// Aragorn at 12 beats him without doubling his 11, so he takes one wound and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		assertEquals(2, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regiment);
		scn.FreepsResolveSkirmish(aragorn);
		int twilight = scn.GetTwilight();
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(regiment));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(regiment));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoBurdenAWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var regiment = scn.GetShadowCard("regiment");

		scn.StartGame();
		scn.MoveMinionsToTable(regiment);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regiment);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(regiment));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
