package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_193_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("anotherway", "12_40");
					put("onegoodturn", "11_49");
					put("smeagol", "5_28");

					put("leader", "12_34");
					put("gandalf", "1_364");

				}},
				new HashMap<>() {{
					put("Mount Doom", "15_193");
					put("East Road", "11_236");
					put("Ettenmoors", "11_237");
					put("Fangorn Glade", "11_238");
					put("site5", "11_239");
					put("site6", "11_239");
					put("site7", "11_239");
					put("site8", "11_239");
					put("site9", "11_239");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing,
				GenericCardTestHelper.Shadows
		);
	}

	@Test
	public void MountDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mount Doom
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Battleground</b>. <b>Mountain</b>. <b>Underground</b>. Until the end of the game, sites in this region cannot be replaced.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("Mount Doom");

		assertEquals("Mount Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.hasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.hasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	/**
	 * Legacy Ruling #3 decided Mount Doom goes into effect immediately upon being played, and not
	 * merely after a player has arrived at that site.
	 *
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_3">Legacy Ruling #3 on the wiki</a>
	 */

	@Test
	public void MountDoomCannotBeReplacedWhenItIsNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var onegoodturn = scn.GetFreepsCard("onegoodturn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var anotherway = scn.GetFreepsCard("anotherway");
		scn.FreepsMoveCardToSupportArea(anotherway);
		scn.FreepsMoveCharToTable(smeagol);
		scn.FreepsMoveCardToHand(onegoodturn);

		var site1 = scn.GetFreepsSite("East Road");
		var freepsSite3 = scn.GetFreepsSite("Fangorn Glade");
		var shadowSite2 = scn.GetShadowSite("Ettenmoors");
		var mountdoom = scn.GetFreepsSite("mountdoom");

		scn.StartGame(site1);

		//Start at East Road with player one
		scn.FreepsPassCurrentPhaseAction();

		//P1 then moves to P2 Ettenmoors
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertEquals(scn.GetCurrentSite(), shadowSite2);
		assertEquals(scn.GetSite(2), shadowSite2);

		//P1 then stops and we reconcile
		scn.SkipToPhase(Phase.REGROUP);

		//We now play One Good Turn Deserves Another: "Spot Sméagol to play the fellowship's next site.
		// Then you may add a burden to take this card back into hand."
		// Mount Doom should not be replaceable once it is played, even though no player is there yet.
		assertTrue(scn.FreepsPlayAvailable(onegoodturn));
		scn.FreepsPlayCard(onegoodturn);
		scn.FreepsChooseCard(mountdoom);
		assertEquals(scn.GetSite(3), mountdoom);
		scn.FreepsChooseYes(); //take One Good Turn back into hand

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPlayCard(onegoodturn);
		//This ensures that we have skipped over choosing a site and instead OGT has moved on to its other clause.
		assertTrue(scn.FreepsDecisionAvailable("Do you want to add a burden"));
		scn.FreepsChooseNo();
		assertNotEquals(scn.GetSite(3), freepsSite3);
		assertEquals(scn.GetSite(3), mountdoom);

		//There's Another Way: "Discard this condition to replace the fellowship's current site with one
		// from your adventure deck."  This can be activated but the replacement should completely fail,
		// since the current site (site 2) is in the same region as Mount Doom now.
		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsActionAvailable(anotherway));
		scn.FreepsUseCardAction(anotherway);
		assertEquals(scn.GetCurrentSite(), shadowSite2);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void MountDoomCannotBeRemovedDuringTurnItsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anotherway = scn.GetFreepsCard("anotherway");
		scn.FreepsMoveCardToSupportArea(anotherway);

		var site1 = scn.GetFreepsSite("East Road");
		var mountdoom = scn.GetShadowSite("Mount Doom");

		scn.StartGame(site1);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowChooseCardBPFromSelection(mountdoom);
		assertEquals(scn.GetCurrentSite(), mountdoom);
		scn.SkipToPhase(Phase.REGROUP);

		//There's Another Way: "Discard this condition to replace the fellowship's current site with one
		// from your adventure deck."  This can be activated but the replacement should completely fail.
		assertTrue(scn.FreepsActionAvailable(anotherway));
		scn.FreepsUseCardAction(anotherway);
		assertEquals(scn.GetCurrentSite(), mountdoom);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}
}
