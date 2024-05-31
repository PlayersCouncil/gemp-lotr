
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_044_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("fell", "101_44");
					put("orc", "1_270");
					put("scond", "1_242");

					put("runner", "1_178");

					put("fcond", "1_100");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FelltoItsPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Fell to Its Power
		* Side: Free Peoples
		* Culture: sauron
		* Twilight Cost: 1
		* Type: event
		* Subtype: Regroup
		* Game Text: Discard a [Sauron] Orc to add a burden. The Free Peoples player may discard a Free Peoples condition to prevent this.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl fell = scn.GetFreepsCard("fell");

		assertFalse(fell.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fell.getBlueprint().getSide());
		assertEquals(Culture.SAURON, fell.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fell.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, fell.getBlueprint().getRace());
        assertTrue(scn.hasTimeword(fell, Timeword.REGROUP)); // test for keywords as needed
		assertEquals(1, fell.getBlueprint().getTwilightCost());
		//assertEquals(, fell.getBlueprint().getStrength());
		//assertEquals(, fell.getBlueprint().getVitality());
		//assertEquals(, fell.getBlueprint().getResistance());
		//assertEquals(Signet., fell.getBlueprint().getSignet());
		//assertEquals(, fell.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void FellDiscardsAnOrcToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl fell = scn.GetShadowCard("fell");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCharToTable(orc);
		scn.ShadowMoveCardToHand(fell);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetBurdens()); // 1 from bidding
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
		assertTrue(scn.ShadowPlayAvailable(fell));
		scn.ShadowPlayCard(fell);
		assertEquals(2, scn.GetBurdens());
		assertEquals(Zone.DISCARD, orc.getZone());
	}

	@Test
	public void FellRequiresASauronOrcToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl fell = scn.GetShadowCard("fell");
		PhysicalCardImpl runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);
		scn.ShadowMoveCardToHand(fell);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(fell));

	}

	@Test
	public void FreepsCanDiscardAFreepsConditionToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl fell = scn.GetShadowCard("fell");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		PhysicalCardImpl scond = scn.GetShadowCard("scond");
		scn.ShadowMoveCharToTable(orc);
		scn.ShadowMoveCardToSupportArea(scond);
		scn.ShadowMoveCardToHand(fell);

		PhysicalCardImpl fcond = scn.GetFreepsCard("fcond");
		scn.FreepsMoveCardToSupportArea(fcond);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetBurdens()); // 1 from bidding
		assertEquals(Zone.SUPPORT, fcond.getZone());
		assertEquals(Zone.SUPPORT, scond.getZone());

		scn.ShadowPlayCard(fell);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard a Free Peoples condition to prevent adding a burden"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.DISCARD, fcond.getZone());
		assertEquals(Zone.SUPPORT, scond.getZone());
		assertEquals(Zone.DISCARD, orc.getZone());
	}
}
