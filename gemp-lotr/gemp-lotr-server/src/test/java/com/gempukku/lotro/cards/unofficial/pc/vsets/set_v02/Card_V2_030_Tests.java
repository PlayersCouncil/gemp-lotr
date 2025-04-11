package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_030_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("pack", "102_30");
					put("orc1", "5_50");
					put("orc2", "5_52");
					put("orc3", "3_58");

					put("hound", "102_24");
					put("warg", "5_65");
					put("warwarg", "5_64");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void StrengthofthePackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Strength of the Pack
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Mounted [Isengard] Orcs are strength +1.
		* 	While you can spot 2 mounted [isengard] Orcs, your [isengard] mounts are twilight cost -1. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pack");

		assertEquals("Strength of the Pack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StrengthofthePackMakesMountedIsenorcsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pack = scn.GetShadowCard("pack");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var warg = scn.GetShadowCard("warg");
		scn.ShadowMoveCharToTable(orc1, orc2, orc3);
		scn.ShadowMoveCardToHand(pack);
		scn.AttachCardsTo(orc1, warg);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		//base 9 + 4 from warg
		assertEquals(12, scn.GetStrength(orc1));
		assertEquals(7, scn.GetStrength(orc2));
		assertEquals(7, scn.GetStrength(orc3));

		scn.ShadowPlayCard(pack);

		//base 9 + 4 from warg + 1 from Strength of the Pack
		assertEquals(13, scn.GetStrength(orc1));
		//No boost to an unmounted warg-rider or a non-warg-rider
		assertEquals(7, scn.GetStrength(orc2));
		assertEquals(7, scn.GetStrength(orc3));
	}

	@Test
	public void StrengthofthePackDiscountsWargsWhenTwoAreMounted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pack = scn.GetShadowCard("pack");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var hound = scn.GetShadowCard("hound");
		scn.ShadowMoveCharToTable(orc1, orc2, orc3);
		scn.ShadowMoveCardToHand(pack, warwarg, hound);
		scn.AttachCardsTo(orc1, warg);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetTwilightCost(warg));
		assertEquals(4, scn.GetTwilightCost(warwarg));
		assertEquals(3, scn.GetTwilightCost(warg));

		scn.ShadowPlayCard(pack);

		assertEquals(3, scn.GetTwilightCost(warg));
		assertEquals(4, scn.GetTwilightCost(warwarg));
		assertEquals(3, scn.GetTwilightCost(warg));

		scn.ShadowPlayCard(warwarg);

		assertEquals(2, scn.GetTwilightCost(warg));
		assertEquals(3, scn.GetTwilightCost(warwarg));
		assertEquals(2, scn.GetTwilightCost(warg));
	}
}
