package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "1_313");
					put("anduril", "7_79");
					put("bow", "1_90");
					put("aragorn", "1_89");
					put("paleBlade", "1_221");
					put("witchking", "1_237");
					put("savage", "1_151");
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
					put("site9", "103_126");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBindingRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");
					put("reach", "1_137");
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
					put("site9", "103_126");
				}},
				VirtualTableScenario.FOTRFrodo,
				"9_1"
		);
	}

	@Test
	public void CracksofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cracks of Doom
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 9K
		 * Game Text: Mountain.  Underground. Game text on Free Peoples items and The One Ring does not apply. Each minion is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Cracks of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void CracksofDoomGivesMinionsPlusOneStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(runner);

		// Goblin Runner is normally STR 5
		assertEquals(5, scn.GetStrength(runner));

		scn.FreepsPass();

		// Verify we arrived at site 9
		assertEquals(9, scn.GetCurrentSiteNumber());

		// At Cracks of Doom, each minion is strength +1
		// Runner should now be STR 5 + 1 = 6
		assertEquals(6, scn.GetStrength(runner));
	}

	@Test
	public void FPItemGameTextIsDisabledAtCracksOfDoom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(aragorn, anduril, bow);

		scn.StartGame();

		// Before site 9: Anduril's text is active — Aragorn has damage +1
		assertTrue(scn.HasKeyword(aragorn, Keyword.DAMAGE));

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		// At Cracks of Doom: FP item game text does not apply
		// Anduril no longer grants damage +1
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));

		// Anduril's "can't bear another weapon" text is also disabled,
		// so Bow coexists with Anduril on Aragorn
		assertAttachedTo(anduril, aragorn);
		assertAttachedTo(bow, aragorn);
	}

	@Test
	public void ShadowItemStillWorksAtCracksOfDoom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var paleBlade = scn.GetShadowCard("paleBlade");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, paleBlade);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		// Shadow items are NOT affected by Cracks of Doom
		// Pale Blade still grants damage +1 to Witch-king
		assertTrue(scn.HasKeyword(witchking, Keyword.DAMAGE));
	}

	@Test
	public void RingActionsDisabledAtCracksOfDoom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBindingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		// No Fellowship phase at site 9; skip to Maneuver
		scn.SkipToPhase(Phase.MANEUVER);

		// Binding Ring's Maneuver action (exert bearer to wear Ring) should NOT be available
		assertFalse(scn.FreepsActionAvailable(ring));
	}

	@Test
	public void WornRingDoesNotConvertWoundsAtCracksOfDoom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBindingRingScenario();

		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var reach = scn.GetShadowCard("reach");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(reach);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Maneuver: Shadow plays Saruman's Reach
		// Cost: exerts Savage (only Uruk-hai).
		// Effect: FP must exert 2 companions OR put on Ring.
		// With only Frodo, can't exert 2 — forced to put on Ring.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(reach);

		scn.PassManeuverActions();
		scn.PassArcheryActions();
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(frodo, savage);
		scn.PassSkirmishActions();

		// Frodo (str 4: 3 base + 1 Ring) vs Savage (str 6: 5 base + 1 site), damage +1
		// Frodo loses: 1 wound (loss) + 1 wound (damage +1) = 2 wounds
		// Ring trigger (negate wound -> add burden) does NOT fire — ringTextIsInactive
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
	}
}
