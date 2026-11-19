package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fror", "2_6");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, cost 2, no requirement to play
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, a maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Dwarf
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition in the support area; each Dwarf is damage +1

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3, a regroup ability
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard and no Uruk-hai, strength 8;
					// each companion is resistance -1, and a skirmish ability never used
					put("saruman", "17_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Frór, Gimli's Kinsman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot a Dwarf.
		 * While skirmishing an Uruk-hai, Frór is strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fror");

		assertEquals("Frór", card.getBlueprint().getTitle());
		assertEquals("Gimli's Kinsman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void FrorCannotBePlayedUntilADwarfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToHand(gimli, fror);
		// Everything that is nearly a Dwarf: a [dwarven] card that is no Dwarf, a companion who is no Dwarf, and a
		// Dwarf in the dead pile.
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDeadPile(guard);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.FreepsPlayAvailable(fror));
		assertTrue(scn.FreepsPlayAvailable(gimli));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(fror));

		scn.FreepsPlayCard(fror);
		assertInZone(Zone.FREE_CHARACTERS, fror);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FrorCanBePlayedSpottingADwarfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCardsToHand(fror);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, thrarin);
		assertTrue(scn.FreepsPlayAvailable(fror));
		scn.FreepsPlayCard(fror);
		assertInZone(Zone.FREE_CHARACTERS, fror);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FrorIsStrengthPlus3OnlyWhileHisOwnSkirmishWithAnUrukHaiIsGoingOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		scn.MoveCompanionsToTable(fror);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		assertEquals(5, scn.GetStrength(fror));
		scn.FreepsAssignToMinions(fror, savage);

		// Assigned, but his skirmish has not begun.
		assertTrue(scn.IsCharAssignedAgainst(fror, savage));
		assertFalse(scn.IsCharSkirmishing(fror));
		assertEquals(5, scn.GetStrength(fror));

		scn.FreepsResolveSkirmish(fror);
		assertTrue(scn.IsCharSkirmishingAgainst(fror, savage));
		assertEquals(8, scn.GetStrength(fror));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassSkirmishActions();
		// 8 against 5: the Savage takes the wound, and Frór is no longer skirmishing.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(fror));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(5, scn.GetStrength(fror));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FrorIsStrengthPlus3AgainstAnUrukHaiOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		scn.MoveCompanionsToTable(fror);

		scn.StartGame();

		var sentry = scn.GetShadowCard("sentry");
		scn.MoveMinionsToTable(sentry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(fror, sentry);
		assertEquals(5, scn.GetStrength(fror));

		scn.FreepsResolveSkirmish(fror);
		assertTrue(scn.IsCharSkirmishingAgainst(fror, sentry));
		assertEquals(8, scn.GetStrength(fror));
		assertEquals(8, scn.GetStrength(sentry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FrorGetsNothingAgainstMinionsThatAreNotUrukHaiNorFromAnUrukHaiInAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(fror, guard);

		scn.StartGame();

		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveMinionsToTable(worker, saruman, sentry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{fror, worker}, new PhysicalCardImpl[]{guard, sentry});
		// An [uruk-hai] minion who is no Uruk-hai joins Frór's skirmish with the [isengard] Orc.
		scn.ShadowAssignToMinions(fror, saruman);

		// The Guard skirmishes the Uruk-hai while Frór waits: the bonus is Frór's own, not his fellow Dwarf's.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, sentry));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(fror));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(fror);
		assertTrue(scn.IsCharSkirmishingAgainst(fror, worker));
		assertTrue(scn.IsCharSkirmishingAgainst(fror, saruman));
		assertFalse(scn.IsCharSkirmishingAgainst(fror, sentry));
		assertEquals(5, scn.GetStrength(fror));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
