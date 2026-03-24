package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_3: Each time your fellowship moves, you may kill a companion (except the Ring-bearer) to make the move limit +1.
 *
 * Test cards:
 * - Aragorn (1_89): Gondor companion (sacrifice target)
 * - Boromir (1_96): Gondor companion (second companion to prove selectivity)
 */
public class Card_93_003_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89");
		put("boromir", "1_96");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_3", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_3"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_3", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void KillCompanionToIncreaseMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();

		// Move from site 1 to site 2; trigger fires on move
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(2, scn.GetMoveLimit());

		// Optional trigger — choose to kill a companion
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();

		// Ring-bearer should not be in the choices
		assertFalse(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));

		scn.FreepsChooseCard(aragorn);

		// Aragorn should be dead
		assertEquals(Zone.DEAD, aragorn.getZone());
		assertEquals(3, scn.GetMoveLimit());
	}

	@Test
	public void CanDeclineToKill() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Move from site 1 to site 2
		scn.FreepsPassCurrentPhaseAction();

		// Optional trigger — decline
		scn.FreepsDeclineOptionalTrigger();

		// Aragorn should still be alive
		assertNotEquals(Zone.DEAD, aragorn.getZone());
	}

	@Test
	public void OwnerGatingDoesNotFireForShadowOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Move from site 1 to site 2
		scn.FreepsPassCurrentPhaseAction();

		// Shadow owns the mod — trigger should not fire for Freeps
		// Should proceed directly to shadow phase without offering to kill
		scn.ShadowPassCurrentPhaseAction();
		assertFalse(scn.FreepsDecisionAvailable("Kill"));
	}
}
