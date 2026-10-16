package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shaman", "3_59");

					put("plodder", "6_69");     // Isengard Plodder: [isengard] Orc, strength 7, vitality 3; a regroup ability only
					put("builder", "6_66");     // Isengard Builder: [isengard] Orc, strength 7, vitality 3; text only while stacked
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

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
	public void IsengardShamanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Isengard Shaman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Response:</b> If an [isengard] Orc is about to take a wound, remove (2) to prevent that wound.
		*/

		var scn = GetScenario();

		var shaman = scn.GetFreepsCard("shaman");

		assertEquals("Isengard Shaman", shaman.getBlueprint().getTitle());
		assertNull(shaman.getBlueprint().getSubtitle());
		assertFalse(shaman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shaman.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, shaman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, shaman.getBlueprint().getCardType());
		assertEquals(Race.ORC, shaman.getBlueprint().getRace());
		assertEquals(1, shaman.getBlueprint().getTwilightCost());
		assertEquals(5, shaman.getBlueprint().getStrength());
		assertEquals(2, shaman.getBlueprint().getVitality());
		assertEquals(4, shaman.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovingTwoPreventsAWoundToEachIsengardOrcItselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		var defend3 = scn.GetFreepsCard("defend3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2, defend3);

		var shaman = scn.GetShadowCard("shaman");
		var plodder = scn.GetShadowCard("plodder");
		var builder = scn.GetShadowCard("builder");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, plodder, builder);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(7);
		assertEquals(7, scn.GetTwilight());

		// The Plodder is about to take the wound.
		assertEquals(0, scn.GetWoundsOn(plodder));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(plodder);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(plodder));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// An [isengard] Orc of another title, the Builder.
		assertEquals(0, scn.GetWoundsOn(builder));
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(builder);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(builder));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The Shaman is an [isengard] Orc itself.
		assertEquals(0, scn.GetWoundsOn(shaman));
		scn.FreepsPlayCard(defend3);
		scn.FreepsChooseCard(shaman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertEquals(0, scn.GetWoundsOn(plodder));
		assertEquals(0, scn.GetWoundsOn(builder));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnIsengardUrukHaiOrAMoriaOrcIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2);

		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		// The Uruk Savage: [isengard], and an Uruk-hai, wounded twice by the event.  The Shaman could be paid
		// for, and is offered for neither wound.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.ShadowPass();

		// The Goblin Patrol Troop: an Orc, and [moria].
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop));

		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void WithOneInThePoolItIsNotOfferedAndTheWoundIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var shaman = scn.GetShadowCard("shaman");
		var plodder = scn.GetShadowCard("plodder");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, plodder);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		assertEquals(0, scn.GetWoundsOn(plodder));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(plodder);

		// (2) cannot be removed from a pool of 1.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(plodder));
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void EachUsePreventsOnlyTheWoundItAnswersAndIsOfferedAgainForTheSecondWoundOfALostSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var shaman = scn.GetShadowCard("shaman");
		var plodder = scn.GetShadowCard("plodder");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, plodder);
		scn.SkipToAssignments();
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		// Aragorn, 8 + 2, beats the Plodder, 7: two wounds for the sword's damage +1.
		scn.FreepsAssignAndResolve(aragorn, plodder);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(plodder));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);

		// The first wound is prevented, and only it: the second has a window of its own.
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(plodder));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(plodder));
		assertInZone(Zone.SHADOW_CHARACTERS, plodder);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSeveralMinionsLoseOneSkirmishTheWoundSavedIsAnIsengardOrcsAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shaman = scn.GetShadowCard("shaman");
		var plodder = scn.GetShadowCard("plodder");
		var builder = scn.GetShadowCard("builder");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, plodder, builder, savage, troop);
		// Aragorn, 8 + 30, beats the four together, 7 + 7 + 5 + 13 = 32, and is far from doubling them: nobody
		// is overwhelmed, and the four lose together and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 30));
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsAssignToMinions(aragorn, plodder);
		scn.ShadowAssignToMinions(aragorn, builder, savage, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(38, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(plodder));
		assertEquals(0, scn.GetWoundsOn(builder));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.PassSkirmishActions();

		// Two [isengard] Orcs are about to take a wound, beside an [isengard] Uruk-hai and a [moria] Orc.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);
		assertEquals(1, scn.GetTwilight());

		assertTrue(scn.ShadowHasCardChoiceAvailable(plodder, builder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shaman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(plodder);

		// That wound alone was prevented; the others take theirs.
		assertEquals(0, scn.GetWoundsOn(plodder));
		assertEquals(1, scn.GetWoundsOn(builder));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
