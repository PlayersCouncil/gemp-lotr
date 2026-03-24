package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// FP weapons
					put("sting", "1_313");         // Possession hand weapon
					put("bow", "1_90");            // Aragorn's Bow - possession ranged weapon
					put("anduril", "7_79");        // Artifact hand weapon

					// Shadow weapons
					put("paleBlade", "1_221");     // The Pale Blade - possession hand weapon
					put("raiderBow", "7_155");     // Raider Bow - possession ranged weapon
					put("whip", "2_74");           // Whip of Many Thongs - artifact hand weapon

					// Non-weapon possessions
					put("athelas", "1_94");        // FP possession (not a weapon)
					put("hides", "4_19");          // Shadow possession (not a weapon)

					put("runner", "1_178");
					put("bilbo", "1_284");         // Ally
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
					put("site8", "103_123");       // Gorgoroth Highway
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothHighwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gorgoroth Highway
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 7
		 * Type: Site
		 * Subtype: King
		 * Site Number: 8
		 * Game Text: At the start of the maneuver phase, hinder all weapons.
		 * 	All characters gain <b>enduring</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Gorgoroth Highway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void AllCharactersGainEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var bilbo = scn.GetFreepsCard("bilbo");

		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(bilbo);

		scn.StartGame();

		scn.SkipToSite(7);
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(8, scn.GetCurrentSiteNumber());

		// All characters — companion, minion, ally — should have enduring
		assertTrue(scn.HasKeyword(frodo, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(runner, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(bilbo, Keyword.ENDURING));
	}

	@Test
	public void HindersAllWeaponsButNotOtherPossessionsAtManeuverStart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		// FP weapons on Frodo
		var sting = scn.GetFreepsCard("sting");
		var bow = scn.GetFreepsCard("bow");
		var anduril = scn.GetFreepsCard("anduril");
		var athelas = scn.GetFreepsCard("athelas");

		// Shadow weapons on runner
		var runner = scn.GetShadowCard("runner");
		var paleBlade = scn.GetShadowCard("paleBlade");
		var raiderBow = scn.GetShadowCard("raiderBow");
		var whip = scn.GetShadowCard("whip");
		var hides = scn.GetShadowCard("hides");

		scn.AttachCardsTo(frodo, sting, bow, anduril, athelas);

		scn.StartGame();

		scn.SkipToSite(7);
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, paleBlade, raiderBow, whip, hides);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(8, scn.GetCurrentSiteNumber());

		// Before maneuver: no weapons should be hindered yet
		assertFalse(scn.IsHindered(sting));
		assertFalse(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(paleBlade));
		assertFalse(scn.IsHindered(raiderBow));
		assertFalse(scn.IsHindered(whip));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());

		// After maneuver start: ALL weapons hindered (FP and Shadow, possession and artifact)
		assertTrue(scn.IsHindered(sting));          // FP possession hand weapon
		assertTrue(scn.IsHindered(bow));             // FP possession ranged weapon
		assertTrue(scn.IsHindered(anduril));         // FP artifact hand weapon
		assertTrue(scn.IsHindered(paleBlade));       // Shadow possession hand weapon
		assertTrue(scn.IsHindered(raiderBow));       // Shadow possession ranged weapon
		assertTrue(scn.IsHindered(whip));            // Shadow artifact hand weapon

		// Non-weapon possessions should NOT be hindered
		assertFalse(scn.IsHindered(athelas));        // FP non-weapon
		assertFalse(scn.IsHindered(hides));          // Shadow non-weapon
	}
}
