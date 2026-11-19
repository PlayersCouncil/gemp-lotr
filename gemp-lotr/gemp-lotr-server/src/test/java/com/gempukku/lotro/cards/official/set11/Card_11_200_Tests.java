package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_200_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruthless", "11_200");
					// A second copy, which answers its own assignment.
					put("ruthless2", "11_200");
					// Cirith Ungol Guard: another Uruk-hai minion, damage +1 and no other text.
					put("cuguard", "10_81");

					// Dwarf Guard: a Dwarf companion of resistance 6 (3 with the three burdens laid), strength 4,
					// vitality 2.  "To play, spot a Dwarf" is passed over by the cheat.
					put("guard", "1_7");
					// Gimli, Lively Combatant: a Dwarf companion of resistance 7 (4 with the burdens).  His draw on a
					// won skirmish is never reached.
					put("gimli", "11_8");
					// Thrarin, Dwarven Smith: a Dwarf ally, resistance 0 (CR 4.2 "resistance").  His maneuver
					// ability lets him be assigned.
					put("thrarin", "1_27");

					// Dwarven Armor: a [dwarven] armor possession; its bearer is overwhelmed only at triple strength.
					put("armor", "1_8");
					put("armor2", "1_8");
					// Hand Axe: a [dwarven] hand weapon possession, strength +1.  Its archery ability is never used.
					put("handaxe", "2_10");
					// Ring of Artifice: a [dwarven] ARTIFACT a Dwarf bears (damage +1; no strength while its owner's
					// discard pile holds no [dwarven] artifact).  Its regroup ability is never used.
					put("ring", "17_4");
					// Dwarven Axe: a [dwarven] hand weapon possession, on Gimli.  Its trigger answers a minion
					// losing to its bearer, which no test reaches.
					put("axe", "1_9");
					// Old Toby: a Free Peoples possession that nobody bears, in the support area.
					put("toby", "1_305");

					// Aragorn, Ranger of the North: a [gondor] Man companion, resistance 6 (3 with the burdens), so a
					// character who is no Dwarf.  His maneuver ability is used only on purpose.
					put("aragorn", "1_89");
					// Ranger's Sword: a [gondor] hand weapon possession, bearer must be Aragorn.  (Not his bow, which
					// makes him an archer and has the archery wound exhaust this minion before the assignments.)
					put("sword", "1_112");
					// Flaming Brand: a [gondor] hand weapon possession on a Man, borne in addition to 1 other hand
					// weapon; its Nazgûl bonus is never reached.
					put("brand", "2_32");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RuthlessUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Ruthless Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: Each time this minion is assigned to skirmish a character who has resistance 3 or less, you may exert this minion to discard a possession borne by that character.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruthless");

		assertEquals("Ruthless Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedToACharacterOfResistance3ItMayExertToDiscardAPossessionThatCharacterBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		var ring = scn.GetFreepsCard("ring");
		var axe = scn.GetFreepsCard("axe");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, armor, handaxe, ring);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(toby);

		var ruthless = scn.GetShadowCard("ruthless");
		var ruthless2 = scn.GetShadowCard("ruthless2");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(ruthless, ruthless2);

		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(3, scn.GetResistance(guard));
		assertEquals(4, scn.GetResistance(gimli));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);
		assertAttachedTo(ring, guard);
		assertAttachedTo(axe, gimli);
		assertInZone(Zone.SUPPORT, toby);
		assertEquals(0, scn.GetWoundsOn(ruthless));
		assertEquals(0, scn.GetWoundsOn(ruthless2));

		scn.SkipToAssignments();
		// This copy to the companion of resistance 3; the other copy to Gimli, of resistance 4.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, ruthless},
				new PhysicalCardImpl[]{gimli, ruthless2});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruthless));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ruthless2));
		scn.ShadowChooseOptionalTrigger(ruthless);
		assertEquals(1, scn.GetWoundsOn(ruthless));
		assertEquals(0, scn.GetWoundsOn(ruthless2));

		// The two possessions Dwarf Guard bears; not his artifact, nor Gimli's possession, nor the one nobody bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, handaxe));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, axe, toby));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(handaxe);

		assertInDiscard(handaxe);
		assertAttachedTo(armor, guard);
		assertAttachedTo(ring, guard);
		assertAttachedTo(axe, gimli);
		assertInZone(Zone.SUPPORT, toby);
		assertEquals(0, scn.GetWoundsOn(ruthless2));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToACharacterWhoIsNoDwarfItMayDiscardOneOfHisPossessionsThatIsNotDwarven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var brand = scn.GetFreepsCard("brand");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword, brand); // lint-ok: Flaming Brand may be borne in addition to 1 other hand weapon

		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(ruthless);

		assertEquals(3, scn.GetResistance(aragorn));
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(brand, aragorn);
		assertEquals(0, scn.GetWoundsOn(ruthless));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ruthless);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruthless));
		scn.ShadowChooseOptionalTrigger(ruthless);
		assertEquals(1, scn.GetWoundsOn(ruthless));

		// A [gondor] Man bearing two [gondor] hand weapons: both are offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, brand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertInDiscard(sword);
		assertAttachedTo(brand, aragorn);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DeclinedNothingIsExertedOrDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, armor, handaxe);

		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(ruthless);

		assertEquals(3, scn.GetResistance(guard));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);
		assertEquals(0, scn.GetWoundsOn(ruthless));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, ruthless);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruthless));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(ruthless));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedForACharacterOfResistance4NorForAnotherMinionsAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, armor);
		scn.AttachCardsTo(gimli, axe);

		var ruthless = scn.GetShadowCard("ruthless");
		var cuguard = scn.GetShadowCard("cuguard");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(ruthless, cuguard);

		assertEquals(3, scn.GetResistance(guard));
		assertEquals(4, scn.GetResistance(gimli));
		assertAttachedTo(armor, guard);
		assertAttachedTo(axe, gimli);
		assertEquals(0, scn.GetWoundsOn(ruthless));

		scn.SkipToAssignments();
		// This minion to the companion of resistance 4, bearing a possession; the other Uruk-hai to the companion
		// of resistance 3.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, ruthless},
				new PhysicalCardImpl[]{guard, cuguard});

		assertTrue(scn.IsCharAssignedAgainst(gimli, ruthless));
		assertTrue(scn.IsCharAssignedAgainst(guard, cuguard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(ruthless));
		assertAttachedTo(armor, guard);
		assertAttachedTo(axe, gimli);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithOnlyAnArtifactBorneItIsOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, ring);

		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(ruthless);

		assertEquals(3, scn.GetResistance(guard));
		assertAttachedTo(ring, guard);
		assertEquals(0, scn.GetWoundsOn(ruthless));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, ruthless);

		// He bears an artifact and no possession: the offer has a cost, so it is made, and the exertion buys nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruthless));
		scn.ShadowChooseOptionalTrigger(ruthless);

		assertEquals(1, scn.GetWoundsOn(ruthless));
		assertAttachedTo(ring, guard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToAnAllyItMayExertToDiscardAPossessionTheAllyBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, armor2);

		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.MoveMinionsToTable(ruthless);

		assertEquals(0, scn.GetResistance(thrarin));
		assertAttachedTo(armor2, thrarin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(ruthless));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(thrarin, ruthless);

		assertTrue(scn.IsCharAssignedAgainst(thrarin, ruthless));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruthless));
		scn.ShadowChooseOptionalTrigger(ruthless);

		assertEquals(1, scn.GetWoundsOn(ruthless));
		assertInDiscard(armor2);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseOfTheTurnItIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, armor, handaxe);

		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(ruthless);
		// Vitality 2 raised to 3 by hand, so that it can exert in each of the two assignment phases.
		scn.ApplyAdHocModifier(new VitalityModifier(null, ruthless, null, 1));

		assertEquals(3, scn.GetResistance(guard));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);
		assertEquals(0, scn.GetWoundsOn(ruthless));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, ruthless);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruthless));
		scn.ShadowChooseOptionalTrigger(ruthless);
		scn.ShadowChooseCard(handaxe);
		assertEquals(1, scn.GetWoundsOn(ruthless));
		assertInDiscard(handaxe);

		// 10 against 4: Dwarf Guard loses, and his armor keeps him from being overwhelmed (that wants 12): one wound.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, ruthless);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();

		assertEquals(3, scn.GetResistance(guard));
		scn.FreepsAssignToMinions(guard, ruthless);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruthless));
		scn.ShadowChooseOptionalTrigger(ruthless);

		assertEquals(2, scn.GetWoundsOn(ruthless));
		assertInDiscard(armor);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, armor, handaxe);

		var ruthless = scn.GetShadowCard("ruthless");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(ruthless);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(ruthless, 1);

		assertEquals(1, scn.GetWoundsOn(ruthless));
		assertEquals(3, scn.GetResistance(guard));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, ruthless);

		assertTrue(scn.IsCharAssignedAgainst(guard, ruthless));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(ruthless));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
