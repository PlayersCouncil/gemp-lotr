package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_7: Each time your fellowship moves, the Shadow player may draw a card for each companion you can spot over 4.
 *
 * Owner-gated to Freeps (via OwnerIsFreeps). "Your" refers to the Freeps player.
 * The Shadow player draws, but the Freeps player controls whether the trigger fires (via "you may").
 *
 * Test cards:
 * - Aragorn (1_89), Boromir (1_96), Legolas (1_50), Gimli (1_13): 4 companions + Frodo = 5 total
 * - Dwarf Guard (1_7): 6th companion to push over 4
 */
public class Card_93_007_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89");
		put("boromir", "1_96");
		put("legolas", "1_50");
		put("gimli", "1_13");
		put("guard", "1_7");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_7"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_7", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ShadowDrawsForCompanionsOver4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");

		// 5 companions + Frodo = 6 total, that's 2 over 4
		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli, guard);

		scn.StartGame();

		int shadowHandBefore = scn.GetShadowHandCount();

		// Move — trigger fires
		scn.FreepsPassCurrentPhaseAction();

		// Optional trigger — accept
		scn.ShadowHasOptionalTriggerAvailable(mod);
		scn.ShadowAcceptOptionalTrigger();

		int shadowHandAfter = scn.GetShadowHandCount();

		// 6 companions total, 2 over 4 = draw 2
		assertEquals(shadowHandBefore + 2, shadowHandAfter);
	}

	@Test
	public void NoDrawWith4OrFewerCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");

		// 3 companions + Frodo = 4 total, exactly 4 = 0 over 4
		scn.MoveCompanionsToTable(aragorn, boromir, legolas);

		scn.StartGame();

		int shadowHandBefore = scn.GetShadowHandCount();

		// Move — trigger should NOT fire (0 cards to draw)
		scn.FreepsPassCurrentPhaseAction();

		int shadowHandAfter = scn.GetShadowHandCount();

		assertEquals(shadowHandBefore, shadowHandAfter);
	}

	@Test
	public void OwnerGatingDoesNotFireForShadowOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli, guard);

		scn.StartGame();

		int shadowHandBefore = scn.GetShadowHandCount();

		scn.FreepsPassCurrentPhaseAction();

		int shadowHandAfter = scn.GetShadowHandCount();

		// Shadow owns the mod — trigger doesn't fire
		assertEquals(shadowHandBefore, shadowHandAfter);
	}
}
