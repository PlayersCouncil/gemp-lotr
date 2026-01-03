package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "103_74");

					put("aragorn", "1_89");
					put("boromir", "1_96");
					put("sam", "1_311");
					put("legolas", "1_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Enquea, Magnified through Suffering
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 12
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Fierce. Relentless. <i>(This minion participates in 1 extra round of skirmishes after fierce.)</i>
		* 	At the start of each skirmish involving Ulaire Enquea and a wounded character, you may exert him to heal that character and exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Magnified through Suffering", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.RELENTLESS));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}


// ======== TRIGGER TESTS ========

	@Test
	public void EnqueaCanHealSkirmishingCharacterAndExertAnyCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// At start of skirmish, trigger available
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(boromir));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Enquëa exerts
		assertEquals(1, scn.GetWoundsOn(enquea));

		// Choose to heal Aragorn (only wounded FP character in skirmish)
		// Auto-selected

		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Choose companion to exert - both Aragorn and Boromir valid
		assertTrue(scn.ShadowHasCardChoicesAvailable(aragorn, boromir, frodo));
		scn.ShadowChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
	}

	@Test
	public void EnqueaCanExertSameCompanionThatWasHealed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.ShadowAcceptOptionalTrigger();

		// Aragorn healed (auto-selected)
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Exert Aragorn (can target the same companion)
		scn.ShadowChooseCard(aragorn);

		// Net result: Aragorn back to 1 wound (healed then exerted)
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void EnqueaTriggerNotAvailableIfSkirmishingCharacterNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn not wounded

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// No wounded character in skirmish
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void EnqueaTriggerNotAvailableIfExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(enquea, 3);  // Vitality 4, exhausted

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Enquëa can't exert
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void EnqueaTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Nothing changes
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(enquea));
	}

// ======== RELENTLESS TEST ========

	@Test
	public void EnqueaParticipatesInThreeRoundsOfSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCompanionsToTable(aragorn, boromir, sam);

		scn.StartGame();
		scn.SkipToAssignments();

		// Round 1: Normal assignment
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Aragorn loses (8 str vs 12 str), takes wound
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Round 2: Fierce assignment
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.BothPass();
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		scn.FreepsAssignToMinions(boromir, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();

		// Boromir loses, takes wound
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Round 3: Relentless assignment
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.BothPass();
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		scn.FreepsAssignToMinions(sam, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();


		// No 4th round - should proceed to Regroup
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
