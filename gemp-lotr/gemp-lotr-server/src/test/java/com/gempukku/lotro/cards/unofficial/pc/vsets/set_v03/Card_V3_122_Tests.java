package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Shadow events (one per phase)
					put("saruman", "1_136");        // Saruman's Power - Shadow phase
					put("hate", "1_250");           // Hate - Maneuver phase
					put("malice", "3_79");          // Malice - Archery phase
					put("frenzy", "1_171");         // Frenzy - Assignment phase
					put("savagery", "1_139");       // Savagery to Match... - Skirmish phase
					put("gleaming", "3_89");        // Gleaming in the Snow - Regroup phase
					put("returnToMaster", "1_244"); // Return to its Master - Response (reject)

					// FP events (should never be retrievable)
					put("delving", "1_6");          // Delving - Fellowship phase
					put("sentinels", "2_20");       // Secret Sentinels - Maneuver phase
					put("intimidate", "1_76");      // Intimidate - Response

					put("runner", "1_178");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "103_122");   // Desolation of Ash
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesolationOfAshStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desolation of Ash
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 8
		 * Type: Site
		 * Subtype: King
		 * Site Number: 8
		 * Game Text: At the start of each phase, the Shadow player may remove (1) to take
		 * 	a Shadow event for that phase into hand from discard.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Desolation of Ash", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void SiteRetrievesPhaseMatchedShadowEventEachPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		// Shadow events — one per phase
		var saruman = scn.GetShadowCard("saruman");         // Shadow
		var hate = scn.GetShadowCard("hate");               // Maneuver
		var malice = scn.GetShadowCard("malice");           // Archery
		var frenzy = scn.GetShadowCard("frenzy");           // Assignment
		var savagery = scn.GetShadowCard("savagery");       // Skirmish
		var gleaming = scn.GetShadowCard("gleaming");       // Regroup
		var returnToMaster = scn.GetShadowCard("returnToMaster"); // Response (reject)

		// FP events — should never be retrievable
		var delving = scn.GetFreepsCard("delving");         // Fellowship
		var sentinels = scn.GetFreepsCard("sentinels");     // Maneuver
		var intimidate = scn.GetFreepsCard("intimidate");   // Response

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		scn.SkipToSite(7);

		// Place all events in discard piles
		scn.MoveCardsToDiscard(saruman, hate, malice, frenzy, savagery, gleaming, returnToMaster);
		scn.MoveCardsToDiscard(delving, sentinels, intimidate);

		// Place runner for skirmish requirement
		scn.MoveMinionsToTable(runner);

		// Freeps passes Fellowship at site 7 → forced move to site 8
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetTwilight());

		// === SHADOW PHASE ===
		// Only Saruman's Power (Shadow event) should be a valid target
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.HAND, saruman);
		assertEquals(8, scn.GetTwilight());
		scn.ShadowPassCurrentPhaseAction();

		// === MANEUVER PHASE ===
		// Only Hate (Maneuver event) should be a valid target
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.HAND, hate);
		assertEquals(7, scn.GetTwilight());
		scn.PassManeuverActions();

		// === ARCHERY PHASE ===
		// Only Malice (Archery event) should be a valid target
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.HAND, malice);
		assertEquals(6, scn.GetTwilight());
		scn.PassArcheryActions();

		// === ASSIGNMENT PHASE ===
		// Only Frenzy (Assignment event) should be a valid target
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.HAND, frenzy);
		assertEquals(5, scn.GetTwilight());
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(frodo, runner);

		// === SKIRMISH PHASE ===
		// Only Savagery (Skirmish event) should be a valid target
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.HAND, savagery);
		assertEquals(4, scn.GetTwilight());
		scn.PassSkirmishActions();
		scn.FreepsDeclineOptionalTrigger(); // The One Ring from Frodo losig skirmish

		// === REGROUP PHASE ===
		// Only Gleaming in the Snow (Regroup event) should be a valid target
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.HAND, gleaming);
		assertEquals(3, scn.GetTwilight());

		// All rejects should still be in discard — never offered at any phase
		assertInDiscard(returnToMaster); // Response — not a phase event
		assertInDiscard(delving);        // FP Fellowship event
		assertInDiscard(sentinels);      // FP Maneuver event
		assertInDiscard(intimidate);     // FP Response event
	}
}
