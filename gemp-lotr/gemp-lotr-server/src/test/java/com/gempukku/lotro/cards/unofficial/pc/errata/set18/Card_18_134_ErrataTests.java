package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_134_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("anotherway", "12_40");
					put("onegoodturn", "11_49");
					put("smeagol", "5_28");
					// put other cards in here as needed for the test case
				}},
				new HashMap<>() {{
					put("doorway", "68_134");
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
				"open"
		);
	}

	@Test
	public void DoorwaytoDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 18
		* Title: Doorway to Doom
		* Unique: False
		* Side: 
		* Culture: 
		* Twilight Cost: 1
		* Type: site
		* Subtype: Standard
		* Game Text: <b>Mountain.</b> When the fellowship moves to this site, each player wounds two of their characters.
		 *  This site cannot be replaced.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var doorway = scn.GetFreepsSite("Doorway to Doom");

		assertFalse(doorway.getBlueprint().isUnique());
		assertEquals(CardType.SITE, doorway.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(doorway, Keyword.MOUNTAIN));
		assertEquals(1, doorway.getBlueprint().getTwilightCost());
		assertEquals(SitesBlock.SHADOWS, doorway.getBlueprint().getSiteBlock());
		assertEquals(0, doorway.getBlueprint().getSiteNumber());
	}

	@Test
	public void DoorwaytoDoomCannotBeReplacedWhenItIsNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var onegoodturn = scn.GetFreepsCard("onegoodturn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.FreepsMoveCharToTable(smeagol);
		scn.FreepsMoveCardToHand(onegoodturn);

		var site1 = scn.GetFreepsSite("East Road");
		var freepsSite3 = scn.GetFreepsSite("Fangorn Glade");
		var shadowSite2 = scn.GetShadowSite("Ettenmoors");
		var doorway = scn.GetFreepsSite("Doorway to Doom");

		scn.FreepsChooseCardBPFromSelection(site1);
		scn.SkipStartingFellowships();

		scn.StartGame();

		//Start at East Road with player one
		scn.FreepsPassCurrentPhaseAction();

		//P1 then moves to P2 Ettenmoors
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertEquals(scn.GetCurrentSite(), shadowSite2);
		assertEquals(scn.GetSite(2), shadowSite2);

		//P1 then stops and we reconcile
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		//We've now switched roles, so the "Freeps" and "Shadow" monikers will be backwards
		//P2 at site 1, East Road
		assertEquals(scn.GetCurrentSite(), site1);
		assertEquals(scn.GetSite(1), site1);

		//P2 moves ahead
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.ShadowPassCurrentPhaseAction(); //P2
		scn.FreepsPassCurrentPhaseAction(); //P1
		scn.FreepsDeclineReconciliation(); //P1
		scn.ShadowChooseToMove(); //P2

		//We now move to Doorway
		scn.FreepsChooseCardBPFromSelection(doorway);
		assertEquals(scn.GetCurrentSite(), doorway);
		assertEquals(scn.GetSite(3), doorway);

		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.ShadowPassCurrentPhaseAction(); //P2
		scn.FreepsPassCurrentPhaseAction(); //P1
		scn.FreepsDeclineReconciliation(); //P1
		scn.ShadowDeclineReconciliation();

		//Okay, we've now switched back and everything is normal.
		//Shadow is on site 3, which is Doorway to Doom.  Freeps is on site 2
		assertEquals(scn.GetCurrentSite(), shadowSite2);
		assertEquals(scn.GetSite(2), shadowSite2);

		//We now play One Good Turn Deserves Another: "Spot Sméagol to play the fellowship's next site.
		// Then you may add a burden to take this card back into hand."  Doorway should not be replacable
		// using this, but it did so anyway by default.
		assertTrue(scn.FreepsPlayAvailable(onegoodturn));
		scn.FreepsPlayCard(onegoodturn);
		//This ensures that we have skipped over choosing a site and instead OGT has moved on to its other clause.
		assertTrue(scn.FreepsDecisionAvailable("Do you want to return"));

		assertNotEquals(scn.GetSite(3), freepsSite3);
		assertEquals(scn.GetSite(3), doorway);
	}

	@Test
	public void DoorwayCannotBeRemovedDuringTurnItsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anotherway = scn.GetFreepsCard("anotherway");
		scn.FreepsMoveCardToSupportArea(anotherway);

		var site1 = scn.GetFreepsSite("East Road");
		var doorway = scn.GetShadowSite("Doorway to Doom");

		scn.FreepsChooseCardBPFromSelection(site1);
		scn.SkipStartingFellowships();

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowChooseCardBPFromSelection(doorway);
		assertEquals(scn.GetCurrentSite(), doorway);
		scn.SkipToPhase(Phase.REGROUP);

		//There's Another Way: Discard this condition to replace the fellowship's current site with one
		// from your draw deck.  This can be activated but the replacement should completely fail.
		assertTrue(scn.FreepsActionAvailable(anotherway));
		scn.FreepsUseCardAction(anotherway);
		assertEquals(scn.GetCurrentSite(), doorway);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}
}
