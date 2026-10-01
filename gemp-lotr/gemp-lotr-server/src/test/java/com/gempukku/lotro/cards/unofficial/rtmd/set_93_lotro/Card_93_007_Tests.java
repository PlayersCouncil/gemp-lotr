package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_93_007_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn: companion, part of the 4-companion baseline
		put("boromir", "1_96"); // Boromir: companion, part of the 4-companion baseline
		put("legolas", "1_50"); // Legolas: companion, part of the 4-companion baseline
		put("gimli", "1_13"); // Gimli: companion, part of the 4-companion baseline
		put("guard", "1_7"); // Dwarf Guard: 5th companion, pushes the count over 4
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
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_7
		 * Type: MetaSite
		 * Game Text: Each time your fellowship moves, the Shadow player may draw a card for each
		 * companion you can spot over 4.
		 */
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
