package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_040_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fire", "102_40");
					// Guard Commander: a unique [moria] Orc, strength 7, vitality 2.  "Skirmish: Remove (3) to make a
					// [moria] Orc strength +2", never used.
					put("commander", "1_186");
					// Cave Troll of Moria: a unique [moria] Troll, strength 15, vitality 4, Damage +1, Fierce.
					put("troll", "1_165");
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc that is not unique, strength 13, no text
					// Ulaire Lemenya: a unique minion that is not [moria], strength 9, vitality 2.  His text is about
					// [wraith] conditions, and there are none.
					put("lemenya", "1_232");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, never used
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability, never used
					put("boromir", "1_97");      // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability, never used
					put("bounder", "1_286");     // Bounder: a Hobbit ally, vitality 2; a skirmish ability, never used
					// The Saga of Elendil: on a [gondor] companion, vitality +1, so that Aragorn outlives two losses to
					// the Troll.  Its skirmish ability is never used.
					put("saga", "1_114");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThroughFireandWaterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Through Fire and Water
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a unique [moria] minion wins a skirmish, you may remove a threat to exert a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fire");

		assertEquals("Through Fire and Water", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForNothingToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		scn.MoveCardsToHand(fire);

		scn.StartGame();
		scn.FreepsPass();

		// 1 for Frodo + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fire));

		scn.ShadowPlayCard(fire);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fire);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AUniqueMoriaMinionWinningLetsTheShadowPlayerRemoveAThreatToExertACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var commander = scn.GetShadowCard("commander");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(fire);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		scn.MoveMinionsToTable(commander);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, commander);

		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli's 6 loses to the Commander's 7.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fire));
		assertEquals(2, scn.GetThreats());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		// Any companion: the one who lost, one who did not fight, the Ring-bearer.  Not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, aragorn, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder, commander));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyAWinnerBothUniqueAndMoriaMakesTheOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(fire);
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, lemenya, troll);
		scn.AddThreats(1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, troop},
				new PhysicalCardImpl[]{gimli, lemenya},
				new PhysicalCardImpl[]{aragorn, troll});

		// A [moria] minion that is not unique beats Boromir, 13 to 7.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// A unique minion that is not [moria] beats Gimli, 9 to 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// A unique [moria] minion, and a Troll where the other tests have an Orc, beats Aragorn, 15 to 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fire));
	}

	@Test
	public void AUniqueMoriaMinionLosingItsSkirmishMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(fire);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(commander);
		scn.AddThreats(1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, commander);

		assertEquals(0, scn.GetWoundsOn(commander));

		// Aragorn's 8 beats the Commander's 7.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(commander));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoThreatToRemoveItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(fire);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.MoveMinionsToTable(commander);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, commander);

		assertEquals(0, scn.GetThreats());

		scn.PassSkirmishActions();

		// The Commander won, and there is no threat to pay with.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedNothingChangesAndTheSameMinionWinningItsFierceSkirmishMakesTheOfferAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var troll = scn.GetShadowCard("troll");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCardsToSupportArea(fire);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, saga);

		scn.StartGame();

		scn.MoveMinionsToTable(troll);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troll);
		scn.PassSkirmishActions();

		// The Troll's 15 beats Aragorn's 8, with Damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fire));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troll);
		scn.PassFierceSkirmishActions();

		// 4 wounds of 5 vitality: Aragorn is exhausted and cannot exert.
		assertEquals(4, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fire));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));

		scn.ShadowChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(4, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertItIsStillOfferedAndTheThreatIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var commander = scn.GetShadowCard("commander");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(fire);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		scn.MoveMinionsToTable(commander);
		scn.AddThreats(1);
		// Frodo is exhausted, and Gimli will be once he has lost: 2 wounds of 3 vitality.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(gimli, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, commander);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fire));
		assertEquals(1, scn.GetThreats());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
