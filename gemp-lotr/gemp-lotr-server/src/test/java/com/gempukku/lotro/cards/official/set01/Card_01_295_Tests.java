package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_295_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("farmer", "1_295");
					put("farmer2", "1_295");
					put("merry", "4_310");
					put("pippin", "4_314");

				}},
				new HashMap<>() {{
					put("site1", "1_321");
					put("site2", "1_327");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	protected GenericCardTestHelper GetDiscountScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("farmer", "1_295");
					put("farmer2", "1_295");
					put("merry", "4_310");
					put("pippin", "4_314");
					put("sam", "1_311");
				}},
				new HashMap<>() {{
					put("site1", "1_323");
					put("site2", "1_327");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FarmerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Hobbit Farmer
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Race: Hobbit
		 * Game Text: While you can spot your site 1, this ally has the game text of that site.
		 *  Fellowship: Exert this ally and spot your opponent's site 1 to replace it with your site 1.
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 1
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("farmer");

		assertEquals("Hobbit Farmer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getAllyHomeSiteNumbers()[0]);
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getAllyHomeSiteBlock());
	}

	@Test
	public void FarmerCopiesAbilitiesOnOwnSite1ButNotOpponentSite1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		//Player 1 version of the Farmer will test copying one's own site
		var freepsFarmer = scn.GetFreepsCard("farmer");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.FreepsMoveCardToSupportArea(freepsFarmer);
		scn.AddWoundsToChar(freepsFarmer, 1); // To ensure the exert ability isn't available
		scn.FreepsMoveCardsToTopOfDeck(merry, pippin);

		//Player 2 version of the Farmer will test copying the enemy site
		var evilFarmer = scn.GetShadowCard("farmer");
		var evilMerry = scn.GetShadowCard("merry");
		var evilPippin = scn.GetShadowCard("pippin");
		scn.ShadowMoveCardToSupportArea(evilFarmer);
		scn.AddWoundsToChar(evilFarmer, 1); // To ensure the exert ability isn't available

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable("Farmer Maggot's Fields"));
		assertTrue(scn.FreepsActionAvailable("Use Hobbit Farmer"));

		scn.SkipToMovementDecision();
		scn.ShadowMoveCardsToTopOfDeck(evilMerry, evilPippin);
		scn.FreepsChooseToStay();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetVitality(evilFarmer));

		//Since the site is player 1's, player 2 should not have access to it via Hobbit Farmer, only natively
		assertTrue(scn.ShadowActionAvailable("Farmer Maggot's Fields"));
		assertFalse(scn.ShadowActionAvailable("Use Hobbit Farmer"));
	}

	@Test
	public void FarmerCopiesModifiersOnOwnSite1ButNotOpponentSite1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetDiscountScenario();

		//Player 1 version of the Farmer will test copying one's own site
		var freepsFarmer = scn.GetFreepsCard("farmer");
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCardToSupportArea(freepsFarmer);
		scn.FreepsMoveCardToHand(sam);

		//Player 2 version of the Farmer will test copying the enemy site
		var evilFarmer = scn.GetShadowCard("farmer");
		var evilSam = scn.GetShadowCard("sam");
		scn.ShadowMoveCardToSupportArea(evilFarmer);
		scn.ShadowMoveCardToHand(evilSam);

		scn.StartGame();

		assertEquals(2, sam.getBlueprint().getTwilightCost());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(sam);
		//Both the Green Hill Country and Hobbit Farmer should have stacked to make Sam -2.
		assertEquals(0, scn.GetTwilight());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		//Since the site is player 1's, player 2 should not have access to it via Hobbit Farmer, only natively
		assertEquals(2, evilSam.getBlueprint().getTwilightCost());
		assertEquals(0, scn.GetTwilight());
		scn.ShadowPlayCard(evilSam);
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void FarmerAbilityExertsToReplaceOpponentsSite1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var opponentSite1 = scn.GetCurrentSite();
		scn.FreepsMoveCardToSupportArea("farmer");

		//Player 2 version of the Farmer will test replacing
		var evilFarmer = scn.GetShadowCard("farmer");
		var evilMerry = scn.GetShadowCard("merry");
		var evilPippin = scn.GetShadowCard("pippin");
		var site1 = scn.GetShadowSite("site1");

		scn.ShadowMoveCardToSupportArea(evilFarmer);

		scn.StartGame();

		assertFalse(scn.FreepsDecisionAvailable("Use Hobbit Farmer"));

		scn.SkipToMovementDecision();
		scn.ShadowMoveCardsToTopOfDeck(evilMerry, evilPippin);
		scn.FreepsChooseToStay();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		assertSame(opponentSite1, scn.GetCurrentSite());
		assertEquals(Zone.ADVENTURE_DECK, site1.getZone());
		assertTrue(scn.ShadowActionAvailable(evilFarmer));
		assertEquals(2, scn.GetVitality(evilFarmer));

		scn.ShadowUseCardAction(evilFarmer);

		assertSame(site1, scn.GetCurrentSite());
		assertEquals(Zone.ADVENTURE_DECK, opponentSite1.getZone());
		assertEquals(1, scn.GetVitality(evilFarmer));

		//Now that we have replaced the site, the Hobbit Farmer should now automatically be copying it.
		assertTrue(scn.ShadowActionAvailable("Farmer Maggot's Fields"));
		assertTrue(scn.ShadowActionAvailable("Use Hobbit Farmer"));
	}

	@Test
	public void FarmerImmediatelyCopiesAbilitiesAfterReplacingDownThePath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var opponentSite1 = scn.GetCurrentSite();
		scn.FreepsMoveCardToSupportArea("farmer");

		//Player 2 version of the Farmer will test replacing
		var evilFarmer = scn.GetShadowCard("farmer");
		var evilMerry = scn.GetShadowCard("merry");
		var evilPippin = scn.GetShadowCard("pippin");
		var site1 = scn.GetShadowSite("site1");

		scn.ShadowMoveCardToSupportArea(evilFarmer);

		scn.StartGame();

		assertFalse(scn.FreepsDecisionAvailable("Use Hobbit Farmer"));

		//Getting everyone far away from site 1
		scn.SkipToSite(3);

		scn.SkipToMovementDecision();
		scn.ShadowMoveCardsToTopOfDeck(evilMerry, evilPippin);
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		assertEquals(Zone.ADVENTURE_PATH, opponentSite1.getZone());
		assertEquals(Zone.ADVENTURE_DECK, site1.getZone());
		assertTrue(scn.ShadowActionAvailable(evilFarmer));
		assertEquals(2, scn.GetVitality(evilFarmer));

		scn.ShadowUseCardAction(evilFarmer);

		assertEquals(Zone.ADVENTURE_PATH, site1.getZone());
		assertEquals(Zone.ADVENTURE_DECK, opponentSite1.getZone());
		assertEquals(1, scn.GetVitality(evilFarmer));

		//Now that we have replaced the site, the Hobbit Farmer should now automatically be copying it.
		assertTrue(scn.ShadowActionAvailable("Use Hobbit Farmer"));
	}

	@Test
	public void FarmerCopiesModifiersDownThePath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetDiscountScenario();

		var farmer = scn.GetFreepsCard("farmer");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var site1 = scn.GetCurrentSite();
		scn.FreepsMoveCardToSupportArea("farmer");
		scn.FreepsMoveCardToHand(merry, pippin);

		var opponentSite1 = scn.GetShadowSite("site1");

		scn.StartGame();

		//Ensuring Green Hill Country's -1 twilight discount is working natively.
		assertEquals(1, pippin.getBlueprint().getTwilightCost());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(pippin);
		assertEquals(0, scn.GetTwilight());

		//Getting everyone far away from site 1
		scn.SkipToSite(3);

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		assertEquals(Zone.ADVENTURE_DECK, opponentSite1.getZone());
		assertEquals(Zone.ADVENTURE_PATH, site1.getZone());

		//We've never replaced the site, but the Hobbit Farmer should still automatically be copying the twilight discount.
		assertEquals(1, merry.getBlueprint().getTwilightCost());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(merry);
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void FarmerImmediatelyCopiesModifiersAfterReplacingDownThePath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetDiscountScenario();

		var opponentSite1 = scn.GetCurrentSite();
		scn.FreepsMoveCardToSupportArea("farmer");

		//Player 2 version of the Farmer will test replacing
		var evilFarmer = scn.GetShadowCard("farmer");
		var evilMerry = scn.GetShadowCard("merry");
		var site1 = scn.GetShadowSite("site1");

		scn.ShadowMoveCardToSupportArea(evilFarmer);

		scn.StartGame();

		assertFalse(scn.FreepsDecisionAvailable("Use Hobbit Farmer"));

		//Getting everyone far away from site 1
		scn.SkipToSite(3);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		assertEquals(Zone.ADVENTURE_PATH, opponentSite1.getZone());
		assertEquals(Zone.ADVENTURE_DECK, site1.getZone());
		assertTrue(scn.ShadowActionAvailable(evilFarmer));
		assertEquals(2, scn.GetVitality(evilFarmer));

		scn.ShadowUseCardAction(evilFarmer);

		assertEquals(Zone.ADVENTURE_PATH, site1.getZone());
		assertEquals(Zone.ADVENTURE_DECK, opponentSite1.getZone());
		assertEquals(1, scn.GetVitality(evilFarmer));

		//Now that we have replaced the site, the Hobbit Farmer should now automatically be copying the twilight discount.
		assertEquals(1, evilMerry.getBlueprint().getTwilightCost());
		assertEquals(0, scn.GetTwilight());
		scn.ShadowPlayCard(evilMerry);
		assertEquals(0, scn.GetTwilight());
	}

}
