package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("traveler", "5_76");

					put("soldier", "4_254");    // Southron Soldier: [raider] Southron Man, strength 4, vitality 1, no other text
					put("nomad", "7_132");      // Desert Nomad: [raider] Southron Man, strength 9, vitality 2; damage +1 at 2 threats only
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, no Southron, strength 6, vitality 2; more only at 2 burdens
					// Bladetusk Rearguard: [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce mounted
					// Southron minion until the end of the turn ..." of vitality 5 (no Man)
					put("rearguard", "103_43");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("sword", "1_112");      // Ranger's Sword: on Aragorn, strength +2 and damage +1
					// Defend It and Hope: Maneuver event, cost 0: exert a [gondor] Man to wound a minion once (twice
					// an Uruk-hai)
					put("defend1", "4_115");
					put("defend2", "4_115");
					put("defend3", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronTravelerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Southron Traveler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (2)</b>.<br><b>Response:</b> If a Southron is about to take a wound, remove (3) to prevent that wound.
		*/

		var scn = GetScenario();

		var traveler = scn.GetShadowCard("traveler");

		assertEquals("Southron Traveler", traveler.getBlueprint().getTitle());
		assertNull(traveler.getBlueprint().getSubtitle());
		assertFalse(traveler.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, traveler.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, traveler.getBlueprint().getCulture());
		assertEquals(CardType.MINION, traveler.getBlueprint().getCardType());
		assertEquals(Race.MAN, traveler.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(traveler, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(traveler, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(traveler, Keyword.AMBUSH));
		assertEquals(3, traveler.getBlueprint().getTwilightCost());
		assertEquals(8, traveler.getBlueprint().getStrength());
		assertEquals(1, traveler.getBlueprint().getVitality());
		assertEquals(4, traveler.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovingThreePreventsAWoundToEachSouthronItselfAndOneWhoIsNoManIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		var defend3 = scn.GetFreepsCard("defend3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2, defend3);

		var traveler = scn.GetShadowCard("traveler");
		var soldier = scn.GetShadowCard("soldier");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(14);
		scn.FreepsPass();

		// 14 - 5: the Rearguard becomes a Southron minion who is no Man.
		scn.ShadowUseCardAction(rearguard);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		// The Soldier is about to take the wound.
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(soldier);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traveler));
		scn.ShadowChooseOptionalTrigger(traveler);

		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// A Southron who is no Man, the Rearguard.
		assertEquals(0, scn.GetWoundsOn(rearguard));
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(rearguard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traveler));
		scn.ShadowChooseOptionalTrigger(traveler);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(rearguard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The Traveler is a Southron itself.
		assertEquals(0, scn.GetWoundsOn(traveler));
		scn.FreepsPlayCard(defend3);
		scn.FreepsChooseCard(traveler);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traveler));
		scn.ShadowChooseOptionalTrigger(traveler);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(traveler));
		assertInZone(Zone.SHADOW_CHARACTERS, traveler);
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(rearguard));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnEasterlingIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var traveler = scn.GetShadowCard("traveler");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		// The Axeman: a [raider] Man, and no Southron.  The Traveler could be paid for.
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(axeman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(traveler));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void WithTwoInThePoolItIsNotOfferedAndTheWoundIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var traveler = scn.GetShadowCard("traveler");
		var nomad = scn.GetShadowCard("nomad");

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, nomad);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());

		assertEquals(0, scn.GetWoundsOn(nomad));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(nomad);

		// (3) cannot be removed from a pool of 2.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(traveler));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(nomad));
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void EachUsePreventsOnlyTheWoundItAnswersAndIsOfferedAgainForTheSecondWoundOfALostSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var traveler = scn.GetShadowCard("traveler");

		scn.StartGame();
		assertEquals(aragorn, sword.getAttachedTo());
		scn.MoveMinionsToTable(traveler);
		scn.SkipToAssignments();
		scn.SetTwilight(6);
		assertEquals(6, scn.GetTwilight());
		// Aragorn, 8 + 2, beats the Traveler, 8: two wounds for the sword's damage +1.
		scn.FreepsAssignToMinions(aragorn, traveler);
		// The Traveler's ambush (2) is declined, so that the pool stays at 6.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(6, scn.GetTwilight());
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(traveler));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traveler));
		scn.ShadowChooseOptionalTrigger(traveler);

		// The first wound is prevented, and only it: the second has a window of its own.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(traveler));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traveler));
		scn.ShadowChooseOptionalTrigger(traveler);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(traveler));
		assertInZone(Zone.SHADOW_CHARACTERS, traveler);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSeveralMinionsLoseOneSkirmishTheWoundSavedIsASouthronsAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var traveler = scn.GetShadowCard("traveler");
		var soldier = scn.GetShadowCard("soldier");
		var nomad = scn.GetShadowCard("nomad");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, soldier, nomad, axeman);
		// Aragorn, 8 + 22, beats the three together, 4 + 9 + 6 = 19, and is far from doubling them: nobody is
		// overwhelmed, and the three lose together and are wounded together.  The Traveler is in no skirmish.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 22));
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsAssignToMinions(aragorn, soldier);
		// The Soldier's ambush (2) is declined, so that the pool stays at 3.
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowAssignToMinions(aragorn, nomad, axeman);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(30, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(nomad));
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.PassSkirmishActions();

		// Two Southrons are about to take a wound, beside an Easterling.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traveler));
		scn.ShadowChooseOptionalTrigger(traveler);
		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, nomad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(traveler));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nomad);

		// That wound alone was prevented; the others take theirs.
		assertEquals(0, scn.GetWoundsOn(nomad));
		assertInZone(Zone.DISCARD, soldier);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(traveler));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
