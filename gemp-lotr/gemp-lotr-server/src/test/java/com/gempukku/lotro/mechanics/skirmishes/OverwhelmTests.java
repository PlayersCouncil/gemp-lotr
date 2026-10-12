package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.OverwhelmedByMultiplierModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Overwhelming.
 *
 * Comprehensive Rules 4.2, "overwhelmed": if the total strength of one side in a skirmish is at least double the
 * total strength of the other side, all the characters on the losing side are overwhelmed and killed, regardless of
 * how many wounds or how much vitality each has.
 *
 * Player's Council ruling (Ketura on Q-0044, 2026-10-06): overwhelm is resolved minion by minion.  A minion who
 * cannot be overwhelmed "unless its strength is tripled" is the only one spared, and only while the winning side's
 * strength is under three times that minion's own strength; every other minion on its side is overwhelmed at double
 * the side's total as usual.  A minion who is spared has lost the skirmish and takes the wounds for losing it.
 *
 * A Free Peoples side is one character, so a protected companion is overwhelmed at triple his own strength, as
 * he always was.
 */
public class OverwhelmTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Uruk Savage: strength 5, vitality 3; damage +1, which does nothing for a minion that loses
					put("savage1", "1_151");
					put("savage2", "1_151");
					// Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop1", "1_177");
					put("troop2", "1_177");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, damage +1
					// Dwarven Armor: bearer may not be overwhelmed unless his or her strength is tripled
					put("armor", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TwoMinionsFacingExactlyDoubleTheirTotalAreBothOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		// 8 + 12 = 20: exactly double the 5 + 5 of the two Savages.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 12));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.ShadowAssignToMinions(aragorn, savage2);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(20, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertInZone(Zone.SHADOW_CHARACTERS, savage1, savage2);

		scn.PassCurrentPhaseActions();
		// Each had 3 vitality and no wound: nothing but an overwhelm puts them in the discard pile.
		assertInZone(Zone.DISCARD, savage1, savage2);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoMinionsFacingOneShortOfDoubleTheirTotalEachTakeAWoundForLosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		// 8 + 11 = 19: one short of double the 5 + 5 of the two Savages, and nearly four times either one of them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 11));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.ShadowAssignToMinions(aragorn, savage2);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(19, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.SHADOW_CHARACTERS, savage1, savage2);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AProtectedMinionIsSparedAtDoubleTheSidesTotalAndTheMinionBesideItIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage1");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		// The Troop cannot be overwhelmed unless its strength is tripled.
		scn.ApplyAdHocModifier(new OverwhelmedByMultiplierModifier(null, troop, null, 3));
		// 6 + 30 = 36: exactly double the side's 13 + 5, and under three times the Troop's own 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 30));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.ShadowAssignToMinions(gimli, savage);

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(36, scn.GetStrength(gimli));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(3, scn.GetOverwhelmMultiplier(troop));
		assertEquals(2, scn.GetOverwhelmMultiplier(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassCurrentPhaseActions();
		// The Savage, with 3 vitality and no wound, is overwhelmed.
		assertInZone(Zone.DISCARD, savage);
		// The Troop has only lost: 1 wound for losing and 1 for Gimli's damage +1, on its vitality of 3.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AProtectedMinionFacingTripleItsOwnStrengthIsOverwhelmedWithTheMinionBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.ApplyAdHocModifier(new OverwhelmedByMultiplierModifier(null, troop, null, 3));
		// 8 + 31 = 39: exactly three times the Troop's own 13, and far short of three times the side's 18.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 31));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(39, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(3, scn.GetOverwhelmMultiplier(troop));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DISCARD, troop, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AProtectedMinionFacingTripleItsOwnStrengthIsNotOverwhelmedWhileItsSideIsUnderDouble() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		// This time the Savage is the one protected.
		scn.ApplyAdHocModifier(new OverwhelmedByMultiplierModifier(null, savage, null, 3));
		// 8 + 12 = 20: four times the Savage's own 5, and not double the side's 13 + 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 12));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(20, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetOverwhelmMultiplier(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassCurrentPhaseActions();
		// Nobody is overwhelmed in a skirmish lost to less than double: both have lost and take a wound.
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AProtectedCompanionOneShortOfTripledTakesAWoundWhereAnotherCompanionIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		// 13 + 4 = 17: one short of triple Gimli's 6, and more than double Aragorn's 8.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop1, null, 4));
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop2, null, 4));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{aragorn, troop2});

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(17, scn.GetStrength(troop1));
		assertEquals(3, scn.GetOverwhelmMultiplier(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(17, scn.GetStrength(troop2));
		assertEquals(2, scn.GetOverwhelmMultiplier(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.PassCurrentPhaseActions();
		// Aragorn had 4 vitality and no wound.
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AProtectedCompanionFacingMinionsWhoseTotalIsTripleHisStrengthIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage1");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.ShadowAssignToMinions(gimli, savage);

		scn.FreepsResolveSkirmish(gimli);
		// 13 + 5 = 18: neither minion is triple Gimli's 6, and the two together are exactly that.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(3, scn.GetOverwhelmMultiplier(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.PassCurrentPhaseActions();
		// Gimli had 3 vitality and no wound: the 2 wounds of a lost skirmish (one for the Savage's damage +1) would
		// have left him alive.
		assertInZone(Zone.DEAD, gimli);
		assertInZone(Zone.DISCARD, armor);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
