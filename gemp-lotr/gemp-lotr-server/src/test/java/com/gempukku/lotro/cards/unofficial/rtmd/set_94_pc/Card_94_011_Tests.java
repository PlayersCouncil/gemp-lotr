package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_011_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: opens an actual skirmish against runner
		put("backstabber", "1_174"); // Goblin Backstabber: clean minion, filter choice + keeps the temp keyword unassigned
		put("runner", "1_178"); // Goblin Runner: clean minion, filter choice + the 2nd skirmish opponent
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_11", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_11
		 * Type: MetaSite
		 * Game Text: At the start of the assignment phase, the Shadow player may make a minion gain
		 * lurker until the regroup phase.
		 */

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
	}
}
