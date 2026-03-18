package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_008_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mod", "91_8");
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("boromir", "1_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoveLimitMinusOneStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_8
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: While you can spot 5 or more companions, your move limit is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_8", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void MoveLimitReducedWith5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: "While you can spot 5 or more companions, your move limit is -1."
		// With 5 companions (Frodo + 4), move limit should be reduced from 2 to 1.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		scn.MoveCardsToSupportArea(freepsMod);
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		// Normal move limit is 2, should be reduced to 1
		assertEquals(1, scn.GetMoveLimit());
	}

	@Test
	public void MoveLimitUnchangedWithFewerThan5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: Move limit should remain normal with < 5 companions.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		scn.MoveCardsToSupportArea(freepsMod);
		// Only Frodo + 3 = 4 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas");

		scn.StartGame();

		// Normal move limit should be unchanged
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ShadowCopyDoesNotAffectFreePeoples() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: "While you can spot 5 or more companions, your move limit is -1."

		var scn = GetScenario();

		var shadowMod = scn.GetShadowCard("mod");
		scn.MoveCardsToSupportArea(shadowMod);
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
	}
}
