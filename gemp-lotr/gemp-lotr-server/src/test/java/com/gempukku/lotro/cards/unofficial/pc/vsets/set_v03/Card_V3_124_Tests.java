package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// FP cards to attach to Frodo
					put("sting", "1_313");         // FP possession (hand weapon)
					put("anduril", "7_79");        // FP artifact (hand weapon)
					put("lastalliance", "1_49");   // FP condition

					// Shadow card to attach to Frodo (should not be affected)
					put("bladeTip", "1_209");      // Shadow condition

					// FP support area cards (should not be affected)
					put("oldToby", "1_305");       // FP possession (pipeweed)
					put("sapling", "9_35");        // FP artifact
					put("promise", "2_112");       // FP condition

					put("runner", "1_178");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "103_124");       // Gorgoroth Wastes
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothWastesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gorgoroth Wastes
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 5
		 * Type: Site
		 * Subtype: King
		 * Site Number: 8
		 * Game Text: When moving here, for each Free Peoples card borne by companions,
		 * 	the Free Peoples player must discard it or add (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Gorgoroth Wastes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void TriggerOnlyTargetsFPCardsOnCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		// FP cards on Frodo — should be subject to discard-or-tax
		var sting = scn.GetFreepsCard("sting");
		var anduril = scn.GetFreepsCard("anduril");
		var lastalliance = scn.GetFreepsCard("lastalliance");

		// Shadow condition on Frodo — should NOT be affected
		var bladeTip = scn.GetShadowCard("bladeTip");

		// FP cards in support area — should NOT be affected
		var oldToby = scn.GetFreepsCard("oldToby");
		var sapling = scn.GetFreepsCard("sapling");
		var promise = scn.GetFreepsCard("promise");

		var runner = scn.GetShadowCard("runner");

		scn.AttachCardsTo(frodo, sting, anduril, lastalliance, bladeTip);
		scn.MoveCardsToSupportArea(oldToby, sapling, promise);

		scn.StartGame();

		scn.SkipToSite(7);
		scn.MoveMinionsToTable(runner);

		int twilightBefore = scn.GetTwilight();

		// Move to site 8 — trigger fires on arrival
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(8, scn.GetCurrentSiteNumber());

		// FP chooses which of their companion-borne cards to discard
		// Blade Tip (Shadow) should not be offered
		assertTrue(scn.FreepsHasCardChoicesAvailable(sting, anduril, lastalliance));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(bladeTip, oldToby, sapling, promise));

		// Discard Sting, keep Anduril and Last Alliance
		scn.FreepsChooseCards(sting);

		// Sting was discarded
		assertInDiscard(sting);
		// Other two FP cards still on Frodo
		assertAttachedTo(anduril, frodo);
		assertAttachedTo(lastalliance, frodo);

		// Blade Tip unaffected — still on Frodo
		assertAttachedTo(bladeTip, frodo);

		// Support area cards completely unaffected
		assertInZone(Zone.SUPPORT, oldToby);
		assertInZone(Zone.SUPPORT, sapling);
		assertInZone(Zone.SUPPORT, promise);

		// Twilight: site shadow number (5) + 1 per companion (Frodo = 1)
		//         + 2 tax for kept cards (Anduril + Last Alliance)
		assertEquals(twilightBefore + 5 + 1 + 2, scn.GetTwilight());
	}
}
