package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_03_017_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("galadriel", "3_17");
					put("celeborn", "1_34");
					put("greenleaf", "1_50");
				}},
				new HashMap<String, String>() {{
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
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	protected GenericCardTestHelper GetExpandedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("galadriel", "3_17");
					// put other cards in here as needed for the test case
				}},
				new HashMap<String, String>() {{
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
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing,
				"expanded"
		);
	}


	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Galadriel, Lady of the Golden Wood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: At the start of each of your turns, you may heal another Elf.
		 *  Fellowship: Exert Galadriel to play the fellowship's next site if it is a forest (replacing opponent's site if necessary).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", card.getBlueprint().getTitle());
		assertEquals("Lady of the Golden Wood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getAllyHomeSiteNumbers()[0]);
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getAllyHomeSiteBlock());

	}

	@Test
	public void FellowshipActionPlaysNextFellowshipForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2F");
		scn.FreepsMoveCharToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();

		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(Zone.ADVENTURE_DECK, forestSite.getZone());
		assertEquals(null, scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCardBPFromSelection(forestSite);

		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(forestSite, scn.GetSite(2));
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void FellowshipActionPlaysNextTowersForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2T");
		scn.FreepsMoveCharToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();

		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(Zone.ADVENTURE_DECK, forestSite.getZone());
		assertEquals(null, scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCardBPFromSelection(forestSite);

		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(forestSite, scn.GetSite(2));
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void FellowshipActionPlaysNextKingForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("site2K");
		scn.FreepsMoveCharToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("site2");

		//cheating to ensure site 2 qualifies
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.name("West Road"), null, Keyword.FOREST));

		scn.StartGame();

		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(Zone.ADVENTURE_DECK, forestSite.getZone());
		assertEquals(null, scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCardBPFromSelection(forestSite);

		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(forestSite, scn.GetSite(2));
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void FellowshipActionPlaysNextShadowsForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetExpandedScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite("siteX");
		var site1 = scn.GetFreepsSite("site1");
		scn.FreepsMoveCharToTable(galadriel);

		var shadowSite2 = scn.GetShadowSite("siteX");

		scn.FreepsChooseCardBPFromSelection(site1);
		scn.StartGame();

		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(Zone.ADVENTURE_DECK, forestSite.getZone());
		assertEquals(null, scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCardBPFromSelection(forestSite);

		assertEquals(Zone.ADVENTURE_DECK, shadowSite2.getZone());
		assertEquals(forestSite, scn.GetSite(2));
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}

	@Test
	public void HealAbilityTriggersOnElves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.FreepsMoveCharToTable(galadriel, celeborn, greenleaf);

		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(greenleaf, 1);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(3, scn.GetFreepsCardChoiceCount()); //celeborn and legolas and galadriel
		scn.FreepsChooseCard(celeborn);
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(greenleaf));
		assertEquals(1, scn.GetWoundsOn(galadriel));
	}
}
