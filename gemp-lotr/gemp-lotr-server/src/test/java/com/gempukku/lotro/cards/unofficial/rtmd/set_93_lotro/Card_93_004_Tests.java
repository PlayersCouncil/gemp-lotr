package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_4: Wound one of your characters after each skirmish in which you did not play a skirmish event.
 *
 * Test cards:
 * - Aragorn (1_89): Gondor companion (potential wound target)
 * - Goblin Runner (1_178): Moria Orc minion
 * - Strength of Spirit (1_85): Gondor skirmish event
 */
public class Card_93_004_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89");
		put("runner", "1_178");
		// Swordarm of the White Tower: skirmish event
		put("event", "1_116");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_4"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void WoundsWhenNoSkirmishEventPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// Don't play any skirmish events — just pass
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		// After skirmish resolves, the trigger fires — wound one of your characters
		// Aragorn has 0 wounds before, should be asked to choose a wound target
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void NoWoundWhenSkirmishEventPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var event = scn.GetFreepsCard("event");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// Play the skirmish event
		scn.FreepsPlayCard(event);

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// No wound trigger should fire because we played a skirmish event
		// Should proceed past the skirmish without a wound choice
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OwnerGatingDoesNotFireForShadowOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		// Shadow owns the mod — trigger should not fire
		// Should proceed without wound choice; Aragorn should be unwounded
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
