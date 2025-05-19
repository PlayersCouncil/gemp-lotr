package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunter", "15_157");
					put("insurance", "102_5");
				}},
				new HashMap<>() {{
					put("site1", "11_239");
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void NurnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Nurn
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 1
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Plains</b>. While you control this site, each of your hunter minions is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Nurn");

		assertEquals("Nurn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NurnGrantsStrengthToHunterMinionsOwnedByControllerAndNotOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hunter1 = scn.GetShadowCard("hunter");
		var insurance = scn.GetShadowCard("insurance");
		var hunter2 = scn.GetFreepsCard("hunter");
		scn.MoveCardsToHand(hunter2);
		scn.MoveCardsToHand(hunter1);
		scn.MoveCardsToSupportArea(insurance);

		var nurn = scn.GetFreepsSite("Nurn");
		var nurn2 = scn.GetShadowSite("Nurn");
		scn.MoveCardsToDiscard(nurn2); //Need to ensure we don't have two on the table

		scn.StartGame(nurn);

		scn.AddTokensToCard(insurance, 1);
		scn.SkipToSite(2);
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseAnyCard();

		scn.ShadowChooseAction("control");
		assertTrue(scn.IsSiteControlled(nurn));
		scn.ShadowPlayCard(hunter1);

		//Base 10 strength, +1 from controlling Nurn
		assertEquals(11, scn.GetStrength(hunter1));
		scn.MoveCardsToDiscard(hunter1);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		//Now we flip sides and confirm that player 1 does not get a boost from
		// Nurn while player 2 controls it
		// (Remember that freeps and shadow have swapped rolws, so the names are backwards)

		scn.SetTwilight(10);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPlayCard(hunter2);

		//Base 10 strength, no bonus from opponent controlling Nurn
		assertEquals(10, scn.GetStrength(hunter2));
	}
}
