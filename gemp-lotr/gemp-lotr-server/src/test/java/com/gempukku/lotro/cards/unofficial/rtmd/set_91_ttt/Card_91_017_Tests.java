package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_017_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		put("gimli", "1_13");
		put("aragorn", "1_89");
		put("legolas", "1_50");
		put("boromir", "1_97");
		put("merry", "1_302");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_17", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_17"
		);
	}

	@Test
	public void TwilightOnMoveStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_17
		 * Type: MetaSite
		 * Game Text: Each time your fellowship moves, add (3) (or (6) if you can spot 6 companions).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_17", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void Adds3TwilightOnMoveWithFewerThan6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_17: "Each time your fellowship moves, add (3) (or (6) if you can spot 6 companions)."
		// With fewer than 6 companions, should add 3 twilight on move.

		var scn = GetFreepsScenario();

		// Frodo + Gimli + Legolas = 3 companions, well under 6
		scn.MoveCompanionsToTable("gimli", "legolas");

		scn.StartGame();

		scn.FreepsPass(); // move to site 2

		// After move: site shadow number is added, plus companion count, plus 3 from modifier
		// Rather than calculate exact, just verify it's 3 more than without the modifier
		assertEquals(8, scn.GetTwilight());
	}

	@Test
	public void Adds6TwilightOnMoveWith6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_17: With 6+ companions, should add 6 twilight instead of 3.

		var scn = GetFreepsScenario();

		// Frodo + Gimli + Aragorn + Legolas + Boromir + Merry = 6 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir", "merry");

		scn.StartGame();

		scn.FreepsPass(); // move
		scn.FreepsChooseAny(); // Timing collision between site and metasite

		// Should include +6 from the modifier (6+ companions spotted)
		// 2 from site, 6 from companions, +6 from meta-site
		assertEquals(14, scn.GetTwilight());
	}

	@Test
	public void ShadowCopyDoesNotAddTwilightOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_17: Shadow has the modifier. Moving should NOT add extra twilight.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "legolas");

		scn.StartGame();

		// Record twilight, then move
		// Site 2 shadow number + 3 companions (Frodo, Gimli, Legolas)
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight()); // 2 (site) + 3 (companions: Frodo, Gimli, Legolas)
	}
}
