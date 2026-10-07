package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "103_79");
					put("troop1", "1_177");   // Goblin Patrol Troop: strength 13 like Toldea, vitality 3, no text
					put("troop2", "1_177");

					put("aragorn", "1_89");   // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");   // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used here
					put("guard", "1_7");      // Dwarf Guard: strength 4, vitality 2, no text in play
					put("elrond", "1_40");    // Elrond, Lord of Rivendell: ally whose home is site 3, strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Toldea, Blessed with Brutality
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	Each time this minion is assigned to a stronger companion, you may exhaust this minion to hinder that companion.
		 * 	Each time this minion is assigned to a weaker companion, you may exert this minion to exert that companion or add a threat.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Ulaire Toldea", card.getBlueprint().getTitle());
		assertEquals("Blessed with Brutality", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedForSevenAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		scn.MoveCardsToHand(toldea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(toldea));

		scn.ShadowPlayCard(toldea);

		// 13 - 7 to play - 2 roaming: his site number is 3 and the fellowship is at site 2
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, toldea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachAssignmentToAStrongerCompanionMayExhaustToldeaToHinderThatCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Aragorn 8 + 6 = 14, one more than Toldea's 13. Boromir 7 + 8 = 15.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 8));
		scn.SkipToAssignments();

		assertEquals(13, scn.GetStrength(toldea));
		assertEquals(14, scn.GetStrength(aragorn));
		assertEquals(15, scn.GetStrength(boromir));

		// The Free Peoples player makes the assignment.
		scn.FreepsAssignToMinions(aragorn, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsCharAssigned(toldea));

		scn.ShadowChooseOptionalTrigger(toldea);

		// Vitality 3: exhausting him is two exertions.
		assertEquals(2, scn.GetWoundsOn(toldea));
		assertTrue(scn.IsExhausted(toldea));
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		// A hindered character leaves its assignment, so there is no skirmish and the fierce round is next.
		assertFalse(scn.IsCharAssigned(toldea));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// With one wound on him, exhausting Toldea is one exertion more.
		scn.RemoveWoundsFromChar(toldea, 1);
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		// This time the Shadow player makes the assignment.
		scn.ShadowAssignToMinions(boromir, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertEquals(1, scn.GetWoundsOn(toldea));
		assertFalse(scn.IsHindered(boromir));

		scn.ShadowChooseOptionalTrigger(toldea);

		assertEquals(2, scn.GetWoundsOn(toldea));
		assertTrue(scn.IsHindered(boromir));
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.IsCharAssigned(toldea));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningToExhaustLeavesTheAssignmentStanding() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Aragorn 8 + 6 = 14
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(toldea));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedToExhaustAToldeaWhoIsAlreadyExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Vitality 3: two wounds exhaust him.
		scn.AddWoundsToChar(toldea, 2);
		// Aragorn 8 + 6 = 14
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToAssignments();

		assertEquals(14, scn.GetStrength(aragorn));
		assertTrue(scn.IsExhausted(toldea));

		scn.FreepsAssignToMinions(aragorn, toldea);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(aragorn));
		assertEquals(2, scn.GetWoundsOn(toldea));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NeitherOfferIsMadeWhenTheCompanionIsExactlyAsStrong() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Aragorn 8 + 5 = 13, Toldea's own strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();

		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(toldea));

		scn.FreepsAssignToMinions(aragorn, toldea);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// A tie goes to the Shadow side.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertFalse(scn.IsHindered(aragorn));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// One more strength on Aragorn and the next assignment is answered.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, toldea);

		assertEquals(14, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
	}

	@Test
	public void EachAssignmentToAWeakerCompanionMayExertToldeaToExertThatCompanionOrAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Aragorn 8 + 4 = 12, one less than Toldea's 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.SkipToAssignments();

		assertEquals(13, scn.GetStrength(toldea));
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		// The Free Peoples player makes the assignment.
		scn.FreepsAssignToMinions(aragorn, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertEquals(0, scn.GetWoundsOn(toldea));

		scn.ShadowChooseOptionalTrigger(toldea);

		// One exertion, not exhaustion.
		assertEquals(1, scn.GetWoundsOn(toldea));
		assertTrue(scn.ShadowChoiceAvailable("Exert that companion"));
		assertTrue(scn.ShadowChoiceAvailable("Add a threat"));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowChoose("Exert that companion");

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(toldea));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Toldea 13 beats Aragorn 12
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		// This time the Shadow player makes the assignment.
		scn.ShadowAssignToMinions(boromir, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));

		scn.ShadowChooseOptionalTrigger(toldea);

		assertEquals(2, scn.GetWoundsOn(toldea));
		assertTrue(scn.ShadowChoiceAvailable("Exert that companion"));
		assertTrue(scn.ShadowChoiceAvailable("Add a threat"));

		scn.ShadowChoose("Add a threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(toldea));
		assertTrue(scn.IsCharAssignedAgainst(boromir, toldea));

		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningToExertLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(toldea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedToExertAToldeaWhoIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Vitality 3: two wounds exhaust him.
		scn.AddWoundsToChar(toldea, 2);
		scn.SkipToAssignments();

		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.IsExhausted(toldea));

		scn.FreepsAssignToMinions(aragorn, toldea);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(toldea));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheCompanionUnableToExertAThreatIsAddedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(guard, 1);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.SkipToAssignments();

		assertTrue(scn.IsExhausted(guard));
		assertEquals(0, scn.GetThreats());

		scn.FreepsAssignToMinions(guard, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));

		scn.ShadowChooseOptionalTrigger(toldea);

		assertEquals(1, scn.GetWoundsOn(toldea));
		assertFalse(scn.ShadowChoiceAvailable("Add a threat"));
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheCompanionExertsWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Frodo and Aragorn: no more than 2 threats.
		scn.AddThreats(2);
		scn.SkipToAssignments();

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());

		scn.FreepsAssignToMinions(aragorn, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));

		scn.ShadowChooseOptionalTrigger(toldea);

		assertEquals(1, scn.GetWoundsOn(toldea));
		assertFalse(scn.ShadowChoiceAvailable("Exert that companion"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetThreats());

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNeitherPossibleTheOfferIsStillMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(guard, 1);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Frodo and the Dwarf Guard: no more than 2 threats.
		scn.AddThreats(2);
		scn.SkipToAssignments();

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsExhausted(guard));
		assertEquals(0, scn.GetWoundsOn(toldea));

		scn.FreepsAssignToMinions(guard, toldea);

		// The exertion is a cost that can be paid, so the offer is made though it can buy nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
	}

	@Test
	public void WithNeitherPossibleTakingTheOfferExertsToldeaAndAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(guard, 1);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Frodo and the Dwarf Guard: no more than 2 threats.
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));

		scn.ShadowChooseOptionalTrigger(toldea);

		assertEquals(1, scn.GetWoundsOn(toldea));
		assertFalse(scn.ShadowChoiceAvailable("Exert that companion"));
		assertFalse(scn.ShadowChoiceAvailable("Add a threat"));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetThreats());

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnotherMinionBeingAssignedIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var toldea = scn.GetShadowCard("toldea");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, troop1, troop2);
		// Boromir 7 + 8 = 15: stronger than Toldea and the Troops, who are all 13. Aragorn, 8, is weaker.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 8));
		scn.SkipToAssignments();

		assertEquals(13, scn.GetStrength(troop1));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(15, scn.GetStrength(boromir));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{boromir, troop2});

		// Toldea is the minion left over, and the Shadow player leaves him out.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowDeclineAssignments();

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(boromir, troop2));
		assertFalse(scn.IsCharAssigned(toldea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertFalse(scn.IsHindered(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// A Troop, 13, beat Aragorn, 8; Boromir, 15, beat the other.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Toldea is the only fierce minion, and his own assignment on this board is answered.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, toldea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
	}

	@Test
	public void AnAllyAssignedToToldeaIsNotAnsweredWhetherWeakerOrStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Elrond's home is site 3, where he may be assigned to a skirmish.
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(toldea);
		scn.SkipToAssignments();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(13, scn.GetStrength(toldea));

		scn.FreepsAssignToMinions(elrond, toldea);

		// A weaker character who is no companion.
		assertTrue(scn.IsCharAssignedAgainst(elrond, toldea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(elrond);
		scn.PassSkirmishActions();

		// Toldea 13 beats Elrond 8
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Elrond 8 + 6 = 14: a stronger character who is no companion.
		scn.ApplyAdHocModifier(new StrengthModifier(null, elrond, null, 6));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(elrond, toldea);

		assertEquals(14, scn.GetStrength(elrond));
		assertTrue(scn.IsCharAssignedAgainst(elrond, toldea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(elrond));
		assertEquals(0, scn.GetWoundsOn(toldea));

		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
