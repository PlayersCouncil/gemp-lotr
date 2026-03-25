package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_005_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		// Companions to get over the 4 threshold
		put("gimli", "1_13");
		put("aragorn", "1_89");
		put("legolas", "1_50");
		put("boromir", "1_97");
		put("filler1", "1_3");
		put("filler2", "1_3");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_5", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_5"
		);
	}

	@Test
	public void ShadowDrawForCompanionsStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_5
		 * Type: MetaSite
		 * Game Text: For each companion over 4, each of your opponents may draw a card at the start of their Shadow phase.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_5", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ShadowDrawsWhenCompanionsOver4() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_5: With 5 companions (Frodo + 4 others), shadow should draw 1 card.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		// 5 companions total (Frodo + 4), that's 1 over 4
		scn.FreepsPass(); // move to site 2

		// Shadow phase should start with optional trigger to draw
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetShadowHandCount());
	}

	@Test
	public void ShadowDoesNotDrawWith4OrFewerCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_5: No trigger when companions <= 4.

		var scn = GetFreepsScenario();

		// Only Frodo + 3 others = 4 companions, not over threshold
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas");

		scn.StartGame();

		scn.FreepsPass(); // move

		// Shadow should NOT get a draw trigger with only 4 companions
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void YouDoNotDrawWhenShadow() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_5: Shadow's own copy should not let Shadow draw.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		// 5 companions total (Frodo + 4), that's 1 over 4
		scn.FreepsPass(); // move to site 2

		// Shadow phase should not start with optional trigger to draw
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
