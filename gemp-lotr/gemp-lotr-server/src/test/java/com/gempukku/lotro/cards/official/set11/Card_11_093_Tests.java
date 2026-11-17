package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patroller", "11_93");
					// A second copy, which answers its own assignment.
					put("patroller2", "11_93");
					// Harad Standard-bearer: another [men] Man minion.  Its skirmish ability is never used.
					put("bearer", "11_84");

					// Dwarf Guard: a Dwarf companion of resistance 6 (5 with the one burden laid), strength 4,
					// vitality 2.  "To play, spot a Dwarf" is passed over by the cheat.
					put("guard", "1_7");
					// Gimli, Lively Combatant: a Dwarf companion of resistance 7 (6 with the burden).  His draw on a
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
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PatrollerofHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Patroller of Haradrim
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to a character who has resistance 5 or less, you may discard a possession borne by that character.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patroller");

		assertEquals("Patroller of Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedToACharacterOfResistance5ItMayDiscardAPossessionThatCharacterBears() throws DecisionResultInvalidException, CardNotFoundException {
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

		var patroller = scn.GetShadowCard("patroller");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(patroller);

		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(gimli));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);
		assertAttachedTo(ring, guard);
		assertAttachedTo(axe, gimli);
		assertInZone(Zone.SUPPORT, toby);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, patroller);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patroller));
		scn.ShadowAcceptOptionalTrigger();

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
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DeclinedNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, armor, handaxe);

		var patroller = scn.GetShadowCard("patroller");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(patroller);

		assertEquals(5, scn.GetResistance(guard));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, patroller);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patroller));
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedForACharacterOfResistance6NorForAnotherMinionsAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, armor);
		scn.AttachCardsTo(gimli, axe);

		var patroller = scn.GetShadowCard("patroller");
		var bearer = scn.GetShadowCard("bearer");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(patroller, bearer);

		assertEquals(5, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(gimli));
		assertAttachedTo(armor, guard);
		assertAttachedTo(axe, gimli);

		scn.SkipToAssignments();
		// This minion to the companion of resistance 6, bearing a possession; the other minion to the companion of
		// resistance 5.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, patroller},
				new PhysicalCardImpl[]{guard, bearer});

		assertTrue(scn.IsCharAssignedAgainst(gimli, patroller));
		assertTrue(scn.IsCharAssignedAgainst(guard, bearer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(armor, guard);
		assertAttachedTo(axe, gimli);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopyAnswersOnlyItsOwnAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, armor);
		scn.AttachCardsTo(gimli, axe);

		var patroller = scn.GetShadowCard("patroller");
		var patroller2 = scn.GetShadowCard("patroller2");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(patroller, patroller2);

		assertEquals(5, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(gimli));
		assertAttachedTo(armor, guard);
		assertAttachedTo(axe, gimli);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, patroller},
				new PhysicalCardImpl[]{guard, patroller2});

		// The copy assigned to the companion of resistance 5 offers; the copy assigned to Gimli does not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patroller2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(patroller));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(armor);
		assertAttachedTo(axe, gimli);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedWhenTheCharacterBearsNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, ring);

		var patroller = scn.GetShadowCard("patroller");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(patroller);

		assertEquals(5, scn.GetResistance(guard));
		assertAttachedTo(ring, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, patroller);

		// He bears an artifact and no possession: there is nothing to discard (Q-0224).
		assertTrue(scn.IsCharAssignedAgainst(guard, patroller));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(ring, guard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToAnAllyItMayDiscardAPossessionTheAllyBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, armor2);

		var patroller = scn.GetShadowCard("patroller");

		scn.StartGame();
		scn.MoveMinionsToTable(patroller);

		assertEquals(0, scn.GetResistance(thrarin));
		assertAttachedTo(armor2, thrarin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(thrarin, patroller);

		assertTrue(scn.IsCharAssignedAgainst(thrarin, patroller));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patroller));
		scn.ShadowAcceptOptionalTrigger();

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

		var patroller = scn.GetShadowCard("patroller");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(patroller);

		assertEquals(5, scn.GetResistance(guard));
		assertAttachedTo(armor, guard);
		assertAttachedTo(handaxe, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, patroller);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patroller));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(handaxe);
		assertInDiscard(handaxe);

		// 9 against 4: Dwarf Guard loses, and his armor keeps him from being overwhelmed (that wants 12): one wound.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, patroller);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();

		assertEquals(5, scn.GetResistance(guard));
		scn.FreepsAssignToMinions(guard, patroller);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patroller));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(armor);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
