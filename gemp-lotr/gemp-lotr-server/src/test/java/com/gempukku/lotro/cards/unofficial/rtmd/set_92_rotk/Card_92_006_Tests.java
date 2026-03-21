package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_006_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Eomer, Third Marshal of Riddermark (4_267): Rohan Man companion, twilight 3
		put("eomer", "4_267");
		// Firefoot (4_274): Rohan mount, twilight 2, bearer must be Rohan Man
		put("firefoot", "4_274");
		// Foul Horde (5_50): Isengard warg-rider minion, twilight 5
		put("horde", "5_50");
		// Sharku's Warg (5_59): Isengard mount, twilight 2, bearer must be warg-rider
		put("warg", "5_59");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_6", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_6"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_6
		 * Type: MetaSite
		 * Game Text: The twilight cost of each of your mounts is -5.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_6", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsMountCostReduced() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_6: "The twilight cost of each of your mounts is -5."
		// Firefoot costs 2, should be reduced to 0 (can't go negative).

		var scn = GetFreepsScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var firefoot = scn.GetFreepsCard("firefoot");

		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(firefoot);

		scn.StartGame();

		// Firefoot normally costs 2, reduced by 5 = 0
		assertEquals(0, scn.GetTwilightCost(firefoot));
	}

	@Test
	public void ShadowMountCostReduced() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_6: Shadow's mount should also be reduced when Shadow has the modifier.

		var scn = GetShadowScenario();

		var horde = scn.GetShadowCard("horde");
		var warg = scn.GetShadowCard("warg");

		scn.MoveMinionsToTable(horde);
		scn.MoveCardsToShadowHand("warg");

		scn.StartGame();

		// Sharku's Warg normally costs 2, reduced by 5 = 0
		assertEquals(0, scn.GetTwilightCost(warg));
	}

	@Test
	public void OpponentMountNotReduced() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_6: "your mounts" — opponent's mounts should not be affected.
		// Freeps has the modifier; Shadow's mount should be full cost.

		var scn = GetFreepsScenario();

		var horde = scn.GetShadowCard("horde");
		var warg = scn.GetShadowCard("warg");

		scn.MoveMinionsToTable(horde);
		scn.MoveCardsToShadowHand("warg");

		scn.StartGame();

		// Shadow's Sharku's Warg should still cost 5 (not reduced by Freeps' copy)
		assertEquals(5, scn.GetTwilightCost(warg));
	}
}
