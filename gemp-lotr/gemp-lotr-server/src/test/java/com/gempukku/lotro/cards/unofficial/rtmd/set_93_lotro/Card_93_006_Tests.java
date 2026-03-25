package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_6: Skirmishes are resolved in an order decided by the Shadow player.
 *
 * Normally the Free Peoples player (current player) chooses skirmish order.
 * With this modifier, the Shadow player gets to choose instead.
 *
 * Not owner-gated — it applies regardless.
 *
 * Test cards:
 * - Aragorn (1_89), Boromir (1_96): two companions to create two skirmishes
 * - Goblin Runner (1_178) x2: two minions for two separate skirmishes
 */
public class Card_93_006_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89");
		put("boromir", "1_96");
		put("runner1", "1_178");
		put("runner2", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_6", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_6"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_6", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
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
		scn.ShadowResolveSkirmish(aragorn);

		// Now in Aragorn's skirmish — pass through it
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		// Shadow chooses second skirmish
		assertFalse(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
		scn.ShadowResolveSkirmish(boromir);

		// In Boromir's skirmish now
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();
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
