package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("citadel", "3_40");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man, strength 7, vitality 3; no ability
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit, vitality 4; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1, on a [gondor] Man; lets the Merchant carry a wound
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, strength 6, vitality 3; exerts only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who exerts to take part in skirmishes
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, strength 2, home site 2; a skirmish ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; cheated in, it adds nothing
					put("runner2", "1_178");
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text; it beats Gimli without overwhelming him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEndOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("citadel", "3_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], vitality 4
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor], vitality 3
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven], strength 6, vitality 3
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; Gimli beats it
					// Dunlending Brigand: strength 8, vitality 1, no text; it beats Gimli without overwhelming him
					put("brigand", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CitadelofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Citadel of Minas Tirith
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	You may heal a [gondor] companion at the end of each turn during which no companion or ally lost a skirmish.
		*/

		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");

		assertEquals("Citadel of Minas Tirith", citadel.getBlueprint().getTitle());
		assertNull(citadel.getBlueprint().getSubtitle());
		assertFalse(citadel.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, citadel.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, citadel.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, citadel.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(citadel, Keyword.SUPPORT_AREA));
		assertEquals(1, citadel.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheEndOfATurnInWhichNoCompanionOrAllyLostASkirmishAGondorCompanionMayBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveCardsToSupportArea(citadel);
		assertInZone(Zone.SUPPORT, citadel);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		// Gimli, 6 against 5, wins.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The turn ends.
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(citadel));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));

		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AtTheEndOfATurnInWhichACompanionLostASkirmishNoHealIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveCardsToSupportArea(citadel);
		assertInZone(Zone.SUPPORT, citadel);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, brigand);
		// Gimli, 6 against 8, loses: a companion of another culture than the two the Citadel could heal.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(brigand));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		// The turn ends with nothing offered, and the other player's begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, citadel);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void DeclinedItHealsNobodyAndIsOfferedAgainAtTheEndOfTheNextTurnToHealAGondorCompanionOfEitherRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(citadel);
		assertAttachedTo(narsil, merchant);
		assertInZone(Zone.SUPPORT, citadel);

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merchant, 1);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));

		// Turn 1: no minion, so no skirmish and nobody lost one.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(citadel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Turn 3, the Free Peoples player's next: offered again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		// The hand the reconcile of turn 1 drew is reconciled first.
		scn.FreepsDeclineReconciliation();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(citadel));
		scn.FreepsAcceptOptionalTrigger();
		// The wounded [gondor] Man and [gondor] Hobbit; not Frodo, nor the wounded [gondor] ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, merchant));
		scn.FreepsChooseCard(pippin);
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ACompanionWhoLostTheFirstSkirmishStopsItThoughAnotherWonTheSecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var brigand = scn.GetShadowCard("brigand");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(boromir, pippin, gimli);
		scn.MoveCardsToSupportArea(citadel);
		assertInZone(Zone.SUPPORT, citadel);

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(pippin, 1);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		scn.MoveMinionsToTable(brigand, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { boromir, brigand }, new PhysicalCardImpl[] { gimli, runner1 });

		// Boromir, a [gondor] Man, 7 against 8, loses the first skirmish.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		// Gimli, 6 against 5, wins the second.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnAllyWhoLostASkirmishStopsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(citadel);
		assertInZone(Zone.SUPPORT, citadel);

		scn.StartGame();
		scn.AddWoundsToChar(pippin, 1);
		assertEquals(1, scn.GetWoundsOn(pippin));
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { thrarin, runner1 }, new PhysicalCardImpl[] { boromir, runner2 });
		// Thrarin, 4 against 5, loses, and his second wound kills him; Boromir, 7 against 5, wins.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnAllyOfAnotherCultureAndRaceWhoLostASkirmishStopsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(citadel);
		assertInZone(Zone.SUPPORT, citadel);
		assertInZone(Zone.SUPPORT, bounder);

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 1);
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(runner1);
		// The fellowship moves to site 2, the Bounder's home.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignAndResolve(bounder, runner1);
		// The Bounder, a [shire] Hobbit, 2 against 5, loses and is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, bounder);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ASkirmishLostOnTheTurnBeforeDoesNotStopIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveCardsToSupportArea(citadel);
		assertInZone(Zone.SUPPORT, citadel);

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(brigand);

		// Turn 1: Gimli, 6 against 8, loses: nothing offered.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, brigand);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Turn 3: no skirmish; the loss of turn 1 does not count.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		// The hand the reconcile of turn 1 drew is reconciled first.
		scn.FreepsDeclineReconciliation();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(citadel));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void IsNotOfferedWithNoWoundedGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(citadel);
		assertAttachedTo(narsil, merchant);
		assertInZone(Zone.SUPPORT, citadel);

		scn.StartGame();
		// Only Frodo and the [gondor] ally are wounded.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merchant, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
