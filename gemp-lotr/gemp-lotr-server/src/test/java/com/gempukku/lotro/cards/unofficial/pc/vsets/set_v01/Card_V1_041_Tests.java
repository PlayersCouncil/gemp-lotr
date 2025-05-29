
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
import static org.junit.Assert.assertTrue;

public class Card_V1_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("eyes", "101_41");
					put("twigul", "2_83");

					put("filler1", "2_75");
					put("filler2", "2_75");
					put("filler3", "2_75");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheirEyesFellUponHimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Their Eyes Fell Upon Him
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: To play, spot a twilight Nazgul.
		* 	Each time the fellowship moves, you may spot a wound on the Ring-bearer to draw a card (or 2 cards if the Ring-bearer is exhausted).
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl eyes = scn.GetFreepsCard("eyes");

		assertFalse(eyes.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, eyes.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, eyes.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, eyes.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(eyes, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, eyes.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		//assertEquals(, card.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void RequiresATwigulToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl eyes = scn.GetShadowCard("eyes");
		PhysicalCardImpl twigul = scn.GetShadowCard("twigul");
		scn.MoveCardsToHand(eyes, twigul);

		scn.StartGame();

		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(eyes));
		scn.ShadowPlayCard(twigul);
		assertTrue(scn.ShadowPlayAvailable(eyes));
	}

	@Test
	public void EachMoveDrawsACardIfRingBearerIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl eyes = scn.GetShadowCard("eyes");
		scn.MoveCardsToSupportArea(eyes);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());

		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
	}

	@Test
	public void MovingDrawsNoCardsIfRingBearerUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl eyes = scn.GetShadowCard("eyes");
		scn.MoveCardsToSupportArea(eyes);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
	}

	@Test
	public void MovingDraws2CardsIfRingBearerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl eyes = scn.GetShadowCard("eyes");
		scn.MoveCardsToSupportArea(eyes);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());

		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
	}


}
