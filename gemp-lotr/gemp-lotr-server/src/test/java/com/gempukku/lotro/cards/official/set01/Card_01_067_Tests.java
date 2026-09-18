package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruviel", "1_67");
					put("nelya", "1_233");    // Ulaire Nelya, Lieutenant of Morgul: replaces an opponent's site with yours
					put("speak", "2_26");     // Speak "Friend" and Enter: lets the Free Peoples player play their own next site
					put("gandalf", "1_72");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_352");    // Lothlorien Woods: each ally whose home is site 6 is strength +3
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UruvielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruviel, Maid of Lórien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: While you can spot your site 6, Uruviel has the game text of that site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruviel");

		assertEquals("Uruviel", card.getBlueprint().getTitle());
		assertEquals("Maid of Lórien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void UruvielHasHerOwnersSiteSixGameTextOnlyWhileItIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToSupportArea(uruviel);
		scn.MoveCompanionsToTable(gandalf);

		var freepsWoods = scn.GetFreepsSite(6);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(speak);

		// No site 6 on the path yet: Uruviel has no copied text
		assertEquals(Zone.ADVENTURE_DECK, freepsWoods.getZone());
		assertEquals(5, scn.GetStrength(uruviel));

		// The Free Peoples player plays their own Lothlorien Woods as site 6 (still at site 5, so the site's own
		// text does not apply; only Uruviel's copy of it does)
		scn.FreepsPlayCard(speak);
		assertEquals(Zone.ADVENTURE_PATH, freepsWoods.getZone());
		assertEquals(8, scn.GetStrength(uruviel));
	}

	@Test
	public void UruvielLosesSiteSixGameTextWhenHerOwnersSiteIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1054: replacing the copied site did not refresh Uruviel, so the copied modifier stuck.
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToSupportArea(uruviel);
		scn.MoveCompanionsToTable(gandalf);

		var freepsWoods = scn.GetFreepsSite(6);
		var shadowWoods = scn.GetShadowSite(6);
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(speak);
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPlayCard(speak);
		assertEquals(Zone.ADVENTURE_PATH, freepsWoods.getZone());
		assertEquals(8, scn.GetStrength(uruviel));

		scn.FreepsPassCurrentPhaseAction();          // move 5 -> 6 onto the Free Peoples player's Woods
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(freepsWoods, scn.GetCurrentSite());
		scn.SetTwilight(10);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Nelya swaps in the Shadow player's copy of site 6
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);
		if (scn.ShadowHasCardChoiceAvailable(freepsWoods))
			scn.ShadowChooseCard(freepsWoods);
		if (scn.ShadowDecisionAvailable("Choose site to play"))
			scn.ShadowChooseCardBPFromSelection(shadowWoods);

		assertEquals(Zone.ADVENTURE_DECK, freepsWoods.getZone());
		assertEquals(Zone.ADVENTURE_PATH, shadowWoods.getZone());
		assertEquals(shadowWoods, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(nelya));

		// "your site 6" is gone, so Uruviel is back to her printed strength (the site itself still applies +3 while
		// the fellowship is at it, and Uruviel is home)
		assertEquals(5 + 3, scn.GetStrength(uruviel));
	}
}
