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

public class Card_12_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "12_106");
					// A second copy, which answers its own assignment.
					put("goblin2", "12_106");
					// Denizen of Khazad-dum: another [orc] Orc minion; strength +2 only at an underground site.
					put("denizen", "11_115");

					// Dwarf Guard: a Dwarf companion of strength 4, overwhelmed by this minion's 10.  "To play, spot a
					// Dwarf" is passed over by the cheat.
					put("guard", "1_7");
					// Gimli, Son of Gloin: a second Dwarf companion.  His skirmish ability is never used.
					put("gimli", "1_13");
					// Aragorn, Ranger of the North: a companion who is no Dwarf.  His maneuver ability is never used.
					put("aragorn", "1_89");
					// Thrarin, Dwarven Smith: a Dwarf ally, whose own maneuver ability lets him be assigned.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VileGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Vile Goblin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to skirmish a Dwarf, this minion is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblin");

		assertEquals("Vile Goblin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedToADwarfItIsFierceUntilTheRegroupPhaseAndAgainAtALaterAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard, gimli);

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		scn.FreepsAssignToMinions(guard, goblin);
		assertTrue(scn.IsCharAssignedAgainst(guard, goblin));
		assertTrue(scn.HasKeyword(goblin, Keyword.FIERCE));

		// 10 against 4: Dwarf Guard is overwhelmed and killed.  The minion takes nothing.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);

		// Still fierce after its skirmish: the fierce assignment phase comes, the last phase before the regroup phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(goblin, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));

		// A second move, and a second assignment phase: this time the Shadow player assigns it, to Gimli, and it is
		// fierce again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(gimli, goblin);

		assertTrue(scn.IsCharAssignedAgainst(gimli, goblin));
		assertTrue(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotFierceWhenAssignedToACompanionWhoIsNoDwarfNorForAnotherMinionsAssignmentToADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);

		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, denizen);

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		// This minion to the Man; the other [orc] Orc to the Dwarf.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, goblin},
				new PhysicalCardImpl[]{guard, denizen});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, goblin));
		assertTrue(scn.IsCharAssignedAgainst(guard, denizen));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(denizen, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopyAnswersOnlyItsOwnAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);

		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, goblin2);

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin2, Keyword.FIERCE));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, goblin},
				new PhysicalCardImpl[]{guard, goblin2});

		// The copy assigned to the Dwarf is fierce; the copy assigned to Aragorn is not.
		assertTrue(scn.HasKeyword(goblin2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToADwarfAllyItIsFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, goblin2);
		assertEquals(0, scn.GetWoundsOn(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin2, Keyword.FIERCE));
		scn.FreepsAssignToMinions(aragorn, goblin2);
		scn.ShadowAssignToMinions(thrarin, goblin);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, goblin2));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, goblin));
		// The copy the Shadow player assigned to the Dwarf ally is fierce; the one on Aragorn is not.
		assertTrue(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin2, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
