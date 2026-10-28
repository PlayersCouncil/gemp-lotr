package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("easterling", "12_55");
					// A second copy, which answers its own assignment.
					put("easterling2", "12_55");
					// Harad Standard-bearer: another [men] Man minion.  Its skirmish ability is never used.
					put("bearer", "11_84");

					// Dwarf Guard: a Dwarf companion of vitality 2, exhausted by one wound.  "To play, spot a Dwarf"
					// is passed over by the cheat.
					put("guard", "1_7");
					// Aragorn, Ranger of the North: vitality 4; with two wounds he has 2 left, one short of exhausted.
					// His maneuver ability is never used.
					put("aragorn", "1_89");
					// Thrarin, Dwarven Smith: a Dwarf ally of vitality 2, exhausted by his own maneuver ability, which
					// also lets him be assigned.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrutalEasterlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Brutal Easterling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to skirmish an exhausted companion, it is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("easterling");

		assertEquals("Brutal Easterling", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedToAnExhaustedCompanionItIsFierceUntilTheRegroupPhaseAndAgainAtALaterAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		var easterling = scn.GetShadowCard("easterling");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 1);
		// Frodo 1_290, vitality 4: three wounds exhaust him.
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(easterling);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetVitality(guard));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetVitality(frodo));
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));
		scn.FreepsAssignToMinions(guard, easterling);
		assertTrue(scn.IsCharAssignedAgainst(guard, easterling));
		assertTrue(scn.HasKeyword(easterling, Keyword.FIERCE));

		// 11 against 4: Dwarf Guard is overwhelmed and killed.  The minion takes nothing.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertInZone(Zone.SHADOW_CHARACTERS, easterling);

		// Still fierce after its skirmish: the fierce assignment phase comes, the last phase before the regroup phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(easterling, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));

		// A second move, and a second assignment phase: this time the Shadow player assigns it, to the exhausted
		// Ring-bearer, and it is fierce again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertEquals(1, scn.GetVitality(frodo));
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(frodo, easterling);

		assertTrue(scn.IsCharAssignedAgainst(frodo, easterling));
		assertTrue(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotFierceWhenAssignedToACompanionWithTwoVitalityLeftNorForAnotherMinionsAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);

		var easterling = scn.GetShadowCard("easterling");
		var bearer = scn.GetShadowCard("bearer");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveMinionsToTable(easterling, bearer);

		assertEquals(1, scn.GetVitality(guard));
		assertEquals(2, scn.GetVitality(aragorn));

		scn.SkipToAssignments();
		// This minion to the companion with 2 vitality left; the other minion to the exhausted companion.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, easterling},
				new PhysicalCardImpl[]{guard, bearer});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, easterling));
		assertTrue(scn.IsCharAssignedAgainst(guard, bearer));
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(bearer, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopyAnswersOnlyItsOwnAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);

		var easterling = scn.GetShadowCard("easterling");
		var easterling2 = scn.GetShadowCard("easterling2");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveMinionsToTable(easterling, easterling2);

		assertEquals(1, scn.GetVitality(guard));
		assertEquals(2, scn.GetVitality(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, easterling},
				new PhysicalCardImpl[]{guard, easterling2});

		// The copy assigned to the exhausted companion is fierce; the copy assigned to Aragorn is not.
		assertTrue(scn.HasKeyword(easterling2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToAnExhaustedAllyItIsNotFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var easterling = scn.GetShadowCard("easterling");
		var easterling2 = scn.GetShadowCard("easterling2");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(easterling, easterling2);

		assertEquals(1, scn.GetVitality(guard));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		// Vitality 2 with one wound: exhausted.
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetVitality(thrarin));

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(easterling2, Keyword.FIERCE));
		scn.FreepsAssignToMinions(guard, easterling2);
		scn.ShadowAssignToMinions(thrarin, easterling);

		assertTrue(scn.IsCharAssignedAgainst(guard, easterling2));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, easterling));
		// The copy assigned to the exhausted companion is fierce; the one assigned to the exhausted ally is not.
		assertTrue(scn.HasKeyword(easterling2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
