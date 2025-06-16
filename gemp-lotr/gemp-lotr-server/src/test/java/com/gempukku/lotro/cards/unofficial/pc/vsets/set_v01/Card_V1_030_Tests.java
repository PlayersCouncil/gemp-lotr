
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("saruman", "101_30");
					put("weather", "1_138");
					put("uruk", "1_151");

					put("legolas", "1_51");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Saruman, Fell Voice
		* Side: Free Peoples
		* Culture: isengard
		* Twilight Cost: 4
		* Type: minion
		* Subtype: Wizard
		* Strength: 6
		* Vitality: 4
		* Site Number: 4
		* Game Text: While you cannot spot a weather, Saruman may not take wounds in the archery phase and may not be assigned to a skirmish.
		* 	While at a site bearing a weather, each [isengard] minion is strength +2.
		* 	Response: If the fellowship moves from a site bearing a weather, discard this minion to exert each companion.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl saruman = scn.GetFreepsCard("saruman");

		assertTrue(saruman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, saruman.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, saruman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, saruman.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, saruman.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(saruman, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(4, saruman.getBlueprint().getTwilightCost());
		assertEquals(6, saruman.getBlueprint().getStrength());
		assertEquals(4, saruman.getBlueprint().getVitality());
		//assertEquals(, saruman.getBlueprint().getResistance());
		//assertEquals(Signet., saruman.getBlueprint().getSignet());
		assertEquals(4, saruman.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void SarumanCantFightWithoutWeather() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");
		PhysicalCardImpl weather = scn.GetShadowCard("weather");
		scn.MoveMinionsToTable(saruman);
		scn.MoveCardsToHand(weather);

		PhysicalCardImpl legolas = scn.GetFreepsCard("legolas");
		scn.MoveMinionsToTable(legolas);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetWoundsOn(saruman));

		//assignment
		scn.PassCurrentPhaseActions();
		//assignment automatically skipped
		//assertEquals(0, scn.FreepsGetADParamAsList("minions").size());

		//regroup
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.ShadowPlayCard(weather);
		scn.ShadowChooseCard(scn.GetCurrentSite());

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(saruman));// 1 from weather exert, 1 from archery

		//assignment
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.FreepsGetADParamAsList("minions").size());
	}

	@Test
	public void SarumanBoostsIsengardMinionsWithWeather() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");
		PhysicalCardImpl uruk = scn.GetShadowCard("uruk");
		PhysicalCardImpl weather = scn.GetShadowCard("weather");
		scn.MoveMinionsToTable(saruman, uruk);
		scn.MoveCardsToHand(weather);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(6, scn.GetStrength(saruman));
		assertEquals(5, scn.GetStrength(uruk));

		scn.ShadowPlayCard(weather);
		scn.ShadowChooseCard(scn.GetCurrentSite());
		scn.ShadowChooseCard(uruk);


		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(7, scn.GetStrength(uruk));
	}

	@Test
	public void ResponseSelfDiscardsToExertEveryCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");
		PhysicalCardImpl weather = scn.GetShadowCard("weather");
		scn.MoveMinionsToTable(saruman);
		scn.MoveCardsToHand(weather);

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl legolas = scn.GetFreepsCard("legolas");
		scn.MoveMinionsToTable(legolas);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(weather);
		scn.ShadowChooseCard(scn.GetCurrentSite());

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();

		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(Zone.SHADOW_CHARACTERS, saruman.getZone());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(Zone.DISCARD, saruman.getZone());
	}
}
