
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("cond", "1_317");
					put("farmer", "1_295");
				}},
				new HashMap<>() {{
					put("site1", "101_57");
					put("site2", "1_327");
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
	public void BagEndStudyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Bag End Study
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 
		* Type: site
		* Subtype: 
		* Site Number: 1
		* Game Text: When the fellowship moves from here, you may exert a [shire] companion to play a [shire] condition from your draw deck.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl site1 = scn.GetFreepsSite(1);

		assertFalse(site1.getBlueprint().isUnique());
		//assertEquals(Side.FREE_PEOPLE, site1.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, site1.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(site1, Keyword.SUPPORT_AREA)); // test for keywords as needed
		//assertEquals(, card.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(1, site1.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void WhenMovingAwayExertsAShireCompanionToPlayACondition() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl cond = scn.GetFreepsCard("cond");

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(Zone.DECK, cond.getZone());

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCardBPFromSelection(cond);
		assertEquals(Zone.ATTACHED, cond.getZone());
	}

	@Test
	public void HobbitFarmerDoesntCopyStudy() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl farmer = scn.GetRingBearer();
		scn.MoveCompanionsToTable(farmer);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(scn.GetRingBearer()));
	}
}
