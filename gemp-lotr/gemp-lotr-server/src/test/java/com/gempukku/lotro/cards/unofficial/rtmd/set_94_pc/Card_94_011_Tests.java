package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_011_Tests
{

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: opens an actual skirmish against runner
					put("backstabber", "1_174"); // Goblin Backstabber: clean minion, filter choice + keeps the temp keyword unassigned
					put("runner", "1_178"); // Goblin Runner: clean minion, filter choice + the 2nd skirmish opponent
					put("brigand", "4_10"); // Dunlending Brigand: a [dunland] Man with no text, beside the two [moria] Orcs
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_11", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: opens an actual skirmish against runner
					put("backstabber", "1_174"); // Goblin Backstabber: clean minion, filter choice + keeps the temp keyword unassigned
					put("runner", "1_178"); // Goblin Runner: clean minion, filter choice + the 2nd skirmish opponent
					put("brigand", "4_10"); // Dunlending Brigand: a [dunland] Man with no text, beside the two [moria] Orcs
					put("gimli", "1_13"); // Gimli, Son of Gloin: strength 6, vitality 3; his damage +1 and his skirmish ability play no part
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_11"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_11
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: At the start of the assignment phase, the Shadow player may make a minion gain
		 * lurker until the regroup phase.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_11", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowMayGrantLurkerToAMinionEvenWhenFreepsOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// The trigger is scoped with "player: shadow" (not owner-gated), so it is offered to the
		// Shadow player regardless of which side owns the mod - here the mod is owned by Freeps
		// to prove that.
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var backstabber = scn.GetShadowCard("backstabber");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber, runner);
		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// player: shadow means Shadow is prompted - even though Freeps owns the mod
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mod));

		assertFalse(scn.HasKeyword(backstabber, Keyword.LURKER));
		assertFalse(scn.HasKeyword(runner, Keyword.LURKER));

		scn.ShadowAcceptOptionalTrigger();
		// Either minion, and one of them; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(backstabber);

		assertTrue(scn.HasKeyword(backstabber, Keyword.LURKER));
		assertFalse(scn.HasKeyword(runner, Keyword.LURKER));

		// Positively cross assignment and skirmish phase boundaries (an actual assignment and
		// resolved skirmish against runner) before checking the regroup expiration, rather than
		// relying on SkipToPhase's auto-pass through an empty assignment/skirmish
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(backstabber, Keyword.LURKER));

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.HasKeyword(backstabber, Keyword.LURKER));
		// The skip has passed the regroup phase's first decision for the Free Peoples player.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ShadowMayDeclineTheTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(backstabber, runner);
		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.HasKeyword(backstabber, Keyword.LURKER));
		assertFalse(scn.HasKeyword(runner, Keyword.LURKER));
		// The choice was the Shadow player's: the Free Peoples player is not asked in his place.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheShadowPlayerWhoOwnsTheModGrantsLurkerToAMinionOfEitherCultureAndRaceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var backstabber = scn.GetShadowCard("backstabber");
		var brigand = scn.GetShadowCard("brigand");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber, brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.HasKeyword(backstabber, Keyword.LURKER));
		assertFalse(scn.HasKeyword(brigand, Keyword.LURKER));

		scn.ShadowAcceptOptionalTrigger();
		// A [moria] Orc and a [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber));
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(brigand);

		assertTrue(scn.HasKeyword(brigand, Keyword.LURKER));
		assertFalse(scn.HasKeyword(backstabber, Keyword.LURKER));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, backstabber},
				new PhysicalCardImpl[]{gimli, brigand}
		);
		// Aragorn (8) beats the Backstabber (5); the Brigand's skirmish is the second of the turn.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(brigand, Keyword.LURKER));

		// The Brigand (8) beats Gimli (6) and is still on the table as the regroup phase begins.
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertFalse(scn.HasKeyword(brigand, Keyword.LURKER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedAsTheManeuverOrArcheryPhaseBeginsAndOfferedAsTheAssignmentPhaseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(backstabber, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(backstabber, Keyword.LURKER));
		assertFalse(scn.HasKeyword(runner, Keyword.LURKER));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
	}
}
