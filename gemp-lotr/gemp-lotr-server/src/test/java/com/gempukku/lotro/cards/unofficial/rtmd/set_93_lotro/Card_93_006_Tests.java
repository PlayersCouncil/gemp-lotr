package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_93_006_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: companion for the first skirmish
					put("boromir", "1_96"); // Boromir: companion for the second skirmish
					put("runner1", "1_178"); // Goblin Runner: minion for the first skirmish
					put("runner2", "1_178"); // Goblin Runner: minion for the second skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_6", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: companion for the first skirmish
					put("boromir", "1_96"); // Boromir: companion for the second skirmish
					put("runner1", "1_178"); // Goblin Runner: minion for the first skirmish
					put("runner2", "1_178"); // Goblin Runner: minion for the second skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_6"
		);
	}

	// The same table with no modifier on either side
	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: companion for the first skirmish
					put("boromir", "1_96"); // Boromir: companion for the second skirmish
					put("runner1", "1_178"); // Goblin Runner: minion for the first skirmish
					put("runner2", "1_178"); // Goblin Runner: minion for the second skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_6
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: Skirmishes are resolved in an order decided by the Shadow player.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_6", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowPlayerChoosesSkirmishOrder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(runner1, runner2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{boromir, runner2}
		);

		// With the modifier active, Shadow player should be choosing skirmish order
		// (normally Freeps chooses)
		assertFalse(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir));
		scn.ShadowResolveSkirmish(aragorn);

		// Now in Aragorn's skirmish — pass through it
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(boromir));
		scn.FreepsPass();
		scn.ShadowPass();

		// Aragorn (8) beat his Runner (5, vitality 1); Boromir's has not been fought yet
		assertInZone(Zone.DISCARD, runner1);
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);

		// Shadow chooses second skirmish
		assertFalse(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
		scn.ShadowResolveSkirmish(boromir);

		// In Boromir's skirmish now
		assertTrue(scn.IsCharSkirmishing(boromir));
		scn.FreepsPass();
		scn.ShadowPass();

		assertInZone(Zone.DISCARD, runner1, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutTheModTheFreePeoplesPlayerChoosesSkirmishOrder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(runner1, runner2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{boromir, runner2}
		);

		// The rule: the order is the Free Peoples player's, and the Shadow player is asked nothing
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertFalse(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.IsCharSkirmishing(boromir));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		scn.FreepsPass();
		scn.ShadowPass();

		// And the next one as well
		assertInZone(Zone.DISCARD, runner2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertFalse(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AffectsSelf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner1);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);

		// With the modifier active, Shadow player should be choosing skirmish order
		// (normally Freeps chooses)
		assertFalse(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
	}
}
