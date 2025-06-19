package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_017_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "53_17");
					put("celeborn", "1_34");
					put("greenleaf", "1_50");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_332");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site2F", "1_329"); //We are cheating here and putting a second site 2 in
					put("site2T", "4_330"); //and a Towers site 2
					put("site2K", "7_337"); //and a King site 2
					put("siteX", "11_231"); //and a Shadows site
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExpandedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "53_17");
					// put other cards in here as needed for the test case
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_332");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site2F", "1_329"); //We are cheating here and putting a second site 2 in
					put("site2T", "4_330"); //and a Towers site 2
					put("site2K", "7_337"); //and a King site 2
					put("siteX", "11_231"); //and a Shadows site
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"expanded"
		);
	}


	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 3
		* Title: *Galadriel, Lady of the Golden Wood
		* Side: Free Peoples
		* Culture: Elven
		* Twilight Cost: 3
		* Type: ally
		* Subtype: Elf
		* Strength: 3
		* Vitality: 3
		* Site Number: 6
		* Game Text: At the start of each of your turns, you may heal another Elf.
		 * Response: If the fellowship is about to move to an opponent's site during the Fellowship phase,
		 * exert Galadriel to replace that site with a forest site of the same site number from your adventure deck.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");

		assertTrue(galadriel.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, galadriel.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, galadriel.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, galadriel.getBlueprint().getCardType());
		assertEquals(Race.ELF, galadriel.getBlueprint().getRace());
		assertEquals(3, galadriel.getBlueprint().getTwilightCost());
		assertEquals(3, galadriel.getBlueprint().getStrength());
		assertEquals(3, galadriel.getBlueprint().getVitality());
		assertTrue(galadriel.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));

	}

	@Test
	public void MovingToOpponentsSiteWithMatchingFellowshipForestSiteTriggers() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2F");
		scn.MoveCompanionsToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		//We have to specify which site here since we cheated and included a second site 2
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertEquals(shadowSite2, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(forestSite);
		assertEquals(forestSite.getBlueprintId(), scn.GetCurrentSite().getBlueprintId());
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void MovingToOpponentsSiteWithMatchingTowersForestSiteTriggers() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2T");
		scn.MoveCompanionsToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		//We have to specify which site here since we cheated and included a second site 2
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertEquals(shadowSite2, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(forestSite);
		assertEquals(forestSite.getBlueprintId(), scn.GetCurrentSite().getBlueprintId());
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void MovingToOpponentsSiteWithMatchingKingForestSiteTriggers() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2K");
		scn.MoveCompanionsToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("site2");

		//cheating to ensure site 2 qualifies
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.name("West Road"), null, Keyword.FOREST));

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		//We have to specify which site here since we cheated and included a second site 2
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertEquals(shadowSite2, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(forestSite);
		assertEquals(forestSite.getBlueprintId(), scn.GetCurrentSite().getBlueprintId());
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void MovingToOpponentsShadowsPathSiteTriggersButCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetExpandedScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2K");
		var freepsSiteX = scn.GetFreepsSite("siteX");
		scn.MoveCompanionsToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("siteX");

		scn.FreepsChooseCardBPFromSelection(freepsSiteX);
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();


		//We have to specify which site here since we cheated and included a second site 2
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertEquals(shadowSite2, scn.GetCurrentSite());

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		//The cheated Towers and Fellowship site 2, but not the Shadows variant (as that doesn't have a site number until played)
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void ReplacedSiteDoesNotTriggerBeforeReplacement() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2F");
		scn.MoveCompanionsToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPassCurrentPhaseAction();

		//We have to specify which site here since we cheated and included a second site 2
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(forestSite);
		//Midgwater Moors as the replaced site 2 would have exerted all hobbits upon arrival.  If everything
		//worked out timing-wise, this should never have been evaluated.
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void HealAbilityDoesNotTriggerOnSelf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCompanionsToTable(galadriel);

		scn.AddWoundsToChar(galadriel, 1);

		scn.StartGame();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void HealAbilityTriggersOnOtherElves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.MoveCompanionsToTable(galadriel, celeborn, greenleaf);

		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(greenleaf, 1);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.FreepsGetCardChoiceCount()); //celeborn and legolas, but not galadriel
		scn.FreepsChooseCard(celeborn);
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(greenleaf));
	}
}
