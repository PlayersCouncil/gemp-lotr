package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "1_313");
					put("toby", "1_305");
					put("promise", "2_112");
					put("runner", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "103_129");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PinnacleofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pinnacle of Doom
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 9K
		 * Game Text: Mountain. At the start of each phase, the Shadow player may remove (1) to hinder a card (except companions or The One Ring).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Pinnacle of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void HindersEachPhaseExcludingCompanionsAndRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sting = scn.GetFreepsCard("sting");
		var toby = scn.GetFreepsCard("toby");
		var promise = scn.GetFreepsCard("promise");
		var runner = scn.GetShadowCard("runner");

		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(toby, promise);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetTwilight()); // shadow number 9 + 1 for Frodo

		// === SHADOW PHASE ===
		// Trigger fires at start of phase
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Companions and The One Ring are excluded from targets
		assertTrue(scn.ShadowHasCardChoiceAvailable(sting, toby, promise, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, ring));

		scn.ShadowChooseCard(sting);
		assertTrue(scn.IsHindered(sting));
		assertEquals(9, scn.GetTwilight());
		scn.ShadowPassCurrentPhaseAction();

		// === MANEUVER PHASE ===
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(toby);
		assertTrue(scn.IsHindered(toby));
		assertEquals(8, scn.GetTwilight());
		scn.PassManeuverActions();

		// === ARCHERY PHASE ===
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(promise);
		assertTrue(scn.IsHindered(promise));
		assertEquals(7, scn.GetTwilight());
		scn.PassArcheryActions();

		// === ASSIGNMENT PHASE ===
		// Trigger fires; only runner is a valid target. Decline to preserve skirmish.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(frodo, runner);

		// === SKIRMISH PHASE ===
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		scn.PassSkirmishActions();
		// Frodo (str 4: 3 base + 1 Ring) loses to Runner (str 5), takes 1 wound.
		// Ruling Ring trigger fires — decline (take wound, not burden).
		scn.FreepsDeclineOptionalTrigger();

		// === REGROUP PHASE ===
		// Can't test regroup since the game ended
		//assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		//scn.ShadowDeclineOptionalTrigger();

		// All three hindered cards still hindered (before Reconcile restores them)
		assertTrue(scn.IsHindered(sting));
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(promise));

		// Twilight unchanged since archery (no triggers accepted)
		assertEquals(7, scn.GetTwilight());
	}
}
