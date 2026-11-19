package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_006_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: [rohan] Man companion, who may bear the mount and the spear
					put("firefoot", "4_274");   // Firefoot: [rohan] possession, mount, cost 2; bearer must be a [rohan] Man
					put("spear", "4_288");      // Rider's Spear: [rohan] possession, hand weapon, cost 1; bearer must be a [rohan] Man. No mount
					put("horde", "5_50");       // Foul Horde: [isengard] Orc minion, warg-rider, cost 4
					put("warg", "5_59");        // Sharku's Warg: [isengard] possession, mount, cost 5; bearer must be a warg-rider
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_6", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: [rohan] Man companion, who may bear the mount and the spear
					put("firefoot", "4_274");   // Firefoot: [rohan] possession, mount, cost 2; bearer must be a [rohan] Man
					put("spear", "4_288");      // Rider's Spear: [rohan] possession, hand weapon, cost 1; bearer must be a [rohan] Man. No mount
					put("horde", "5_50");       // Foul Horde: [isengard] Orc minion, warg-rider, cost 4
					put("warg", "5_59");        // Sharku's Warg: [isengard] possession, mount, cost 5; bearer must be a warg-rider
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_6"
		);
	}

	@Test
	public void MountDiscountStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_6
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: The twilight cost of each of your mounts is -5.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_6", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDiscountIsFiveAndOnlyAMountHasIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var firefoot = scn.GetFreepsCard("firefoot");
		var spear = scn.GetFreepsCard("spear");

		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(firefoot, spear);
		// A cost never reads below 0, so a mount of cost 2 hides the size of the discount.  Each possession is
		// made twilight cost +6 here, as East Road makes each companion +2.
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, CardType.POSSESSION, null, new ConstantEvaluator(6)));

		scn.StartGame();

		// 2 printed + 6 - 5
		assertEquals(3, scn.GetTwilightCost(firefoot));
		// 1 printed + 6: a possession the same Man may bear, and no mount
		assertEquals(7, scn.GetTwilightCost(spear));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(firefoot);

		assertAttachedTo(firefoot, eomer);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowMountCostReduced() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_6: Shadow's mount should also be reduced when Shadow has the modifier.

		var scn = GetShadowScenario();

		var horde = scn.GetShadowCard("horde");
		var warg = scn.GetShadowCard("warg");
		var eomer = scn.GetFreepsCard("eomer");
		var firefoot = scn.GetFreepsCard("firefoot");

		scn.MoveMinionsToTable(horde);
		scn.MoveCardsToShadowHand("warg");
		// The other player's mount, with a Man in play who may bear it
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(firefoot);

		scn.StartGame();

		// Sharku's Warg normally costs 5, reduced by 5 = 0
		assertEquals(0, scn.GetTwilightCost(warg));
		// "Your mounts": the Free Peoples player's Firefoot keeps its cost of 2
		assertEquals(2, scn.GetTwilightCost(firefoot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
