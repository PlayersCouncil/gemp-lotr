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

public class Card_11_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throng", "11_102");
					// A second copy, which answers its own assignment.
					put("throng2", "11_102");
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
					// Dwarven Armor: its bearer is overwhelmed only at triple strength, so Dwarf Guard lives through
					// a loss to the Throng (11 against 4: 12 would be wanted).
					put("armor", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThrongofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Throng of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to skirmish a character who has resistance 5 or less, it is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("throng");

		assertEquals("Throng of Harad", card.getBlueprint().getTitle());
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
	public void AssignedToACharacterOfResistance5ItIsFierceUntilTheRegroupPhaseAndAgainAtEachLaterAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, armor);
		assertAttachedTo(armor, guard);

		var throng = scn.GetShadowCard("throng");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(throng);

		assertEquals(5, scn.GetResistance(guard));
		assertFalse(scn.HasKeyword(throng, Keyword.FIERCE));

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(throng, Keyword.FIERCE));
		scn.FreepsAssignToMinions(guard, throng);
		assertTrue(scn.IsCharAssignedAgainst(guard, throng));
		assertTrue(scn.HasKeyword(throng, Keyword.FIERCE));

		// 11 against 4: Dwarf Guard loses and his armor keeps him from being overwhelmed: one wound.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, throng);

		// Still fierce after the skirmish: the fierce assignment phase comes, the last phase before the regroup phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(throng, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(throng, Keyword.FIERCE));

		// A second move, and a second assignment phase: this time the Shadow player assigns it, and it is fierce again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(throng, Keyword.FIERCE));
		assertEquals(5, scn.GetResistance(guard));
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(guard, throng);

		assertTrue(scn.IsCharAssignedAgainst(guard, throng));
		assertTrue(scn.HasKeyword(throng, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotFierceWhenAssignedToACharacterOfResistance6NorForAnotherMinionsAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard, gimli);

		var throng = scn.GetShadowCard("throng");
		var bearer = scn.GetShadowCard("bearer");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(throng, bearer);

		assertEquals(5, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(gimli));

		scn.SkipToAssignments();
		// This minion to the companion of resistance 6; the other minion to the companion of resistance 5.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, throng},
				new PhysicalCardImpl[]{guard, bearer});

		assertTrue(scn.IsCharAssignedAgainst(gimli, throng));
		assertTrue(scn.IsCharAssignedAgainst(guard, bearer));
		assertFalse(scn.HasKeyword(throng, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(bearer, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopyAnswersOnlyItsOwnAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard, gimli);

		var throng = scn.GetShadowCard("throng");
		var throng2 = scn.GetShadowCard("throng2");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(throng, throng2);

		assertEquals(5, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, throng},
				new PhysicalCardImpl[]{guard, throng2});

		// The copy assigned to the companion of resistance 5 is fierce; the copy assigned to Gimli is not.
		assertTrue(scn.HasKeyword(throng2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(throng, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToAnAllyItIsFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var throng = scn.GetShadowCard("throng");

		scn.StartGame();
		scn.MoveMinionsToTable(throng);

		assertEquals(0, scn.GetResistance(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(throng, Keyword.FIERCE));
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(thrarin, throng);

		assertTrue(scn.IsCharAssignedAgainst(thrarin, throng));
		assertTrue(scn.HasKeyword(throng, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
