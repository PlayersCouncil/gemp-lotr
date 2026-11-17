package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_006_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fror", "52_6");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, cost 2, no requirement to play
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin: Dwarf ally, a maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Dwarf
					put("mountains", "52_1");   // Beneath the Mountains: a [dwarven] card that is not a Dwarf

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3, a regroup ability
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
		 * Name: Fror, Gimli's Kinsman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot a Dwarf.
		 * While skirmishing an Uruk-hai, Fror is strength +3.
		 */

		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");

		assertEquals("Fror", fror.getBlueprint().getTitle());
		assertEquals("Gimli's Kinsman", fror.getBlueprint().getSubtitle());
		assertTrue(fror.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, fror.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, fror.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, fror.getBlueprint().getCardType());
		assertEquals(Race.DWARF, fror.getBlueprint().getRace());
		assertEquals(2, fror.getBlueprint().getTwilightCost());
		assertEquals(6, fror.getBlueprint().getStrength());
		assertEquals(3, fror.getBlueprint().getVitality());
		assertEquals(6, fror.getBlueprint().getResistance());
	}

	@Test
	public void FrorRequiresDwarfSpotToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli, fror);
		// A companion who is not a Dwarf and a [dwarven] card that is not a Dwarf.
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveFreepsCardsToSupportArea("mountains");

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(fror));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(fror));

		scn.FreepsPlayCard(fror);
		assertInZone(Zone.FREE_CHARACTERS, fror);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FrorCanBePlayedSpottingADwarfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		scn.MoveCardsToHand(fror);
		scn.MoveAlliesToTable("thrarin");

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(fror));
		scn.FreepsPlayCard(fror);
		assertInZone(Zone.FREE_CHARACTERS, fror);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FrorIsStrengthPlus3AgainstUruks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		scn.MoveCompanionsToTable(fror);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(fror, savage);

		// Assigned, but his skirmish has not begun.
		assertEquals(6, scn.GetStrength(fror));

		scn.FreepsResolveSkirmish(fror);
		assertEquals(9, scn.GetStrength(fror));

		scn.PassSkirmishActions();
		// 9 against 5: the savage takes the wound, and Fror is no longer skirmishing.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(fror));
		assertEquals(6, scn.GetStrength(fror));
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
		assertEquals(6, scn.GetStrength(fror));

		scn.FreepsResolveSkirmish(fror);
		assertEquals(9, scn.GetStrength(fror));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FrorGetsNothingAgainstAnIsengardOrcNorFromAnUrukHaiInAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(fror, guard);

		scn.StartGame();

		var worker = scn.GetShadowCard("worker");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveMinionsToTable(worker, sentry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{fror, worker}, new PhysicalCardImpl[]{guard, sentry});

		// Fror skirmishes an [isengard] Orc while an Uruk-hai waits for the guard.
		scn.FreepsResolveSkirmish(fror);
		assertEquals(6, scn.GetStrength(fror));
		assertEquals(4, scn.GetStrength(guard));

		scn.PassSkirmishActions();
		// 6 against 5: the worker takes the wound.
		assertEquals(1, scn.GetWoundsOn(worker));

		// The guard skirmishes the Uruk-hai: the bonus is Fror's own, not his fellow Dwarf's.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(fror));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
