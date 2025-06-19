
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class Card_V1_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("cart", "1_73");
					put("backstabber", "1_174");
					put("soldier", "1_271");

				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "101_58");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PrancingPonySpareRoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Prancing Pony Spare Room
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 2
		* Type: site
		* Subtype: 
		* Site Number: 2
		* Game Text: Each time you play a minion of strength 6 or less, make it strength +3 until the regroup phase
		 * (unless you can spot a [GANDALF] card).
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl site2 = scn.GetFreepsSite(2);

		assertFalse(site2.getBlueprint().isUnique());
		//assertEquals(Side.FREE_PEOPLE, site2.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, site2.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(site2, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(2, site2.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(2, site2.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void Strength6MinionsGetPlus3StrengthUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl backstabber = scn.GetShadowCard("backstabber");
		scn.MoveCardsToHand(backstabber);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, backstabber.getBlueprint().getStrength());
		scn.ShadowPlayCard(backstabber);
		assertEquals(8, scn.GetStrength(backstabber));
	}

	@Test
	public void Strength7MinionsGetNothing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, soldier.getBlueprint().getStrength());
		scn.ShadowPlayCard(soldier);
		assertEquals(7, scn.GetStrength(soldier));
	}

	@Test
	public void GandalfCardNegatesStrengthPump() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl cart = scn.GetFreepsCard("cart");
		scn.MoveCardsToSupportArea(cart);

		PhysicalCardImpl backstabber = scn.GetShadowCard("backstabber");
		scn.MoveCardsToHand(backstabber);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, backstabber.getBlueprint().getStrength());
		scn.ShadowPlayCard(backstabber);
		assertEquals(5, scn.GetStrength(backstabber));

		scn.MoveCardsToDiscard(cart);
		// Since the modifier is at time of play, this should have no effect
		assertEquals(5, scn.GetStrength(backstabber));
	}
}
