package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_093_Tests
{

// ----------------------------------------
// COVER OF DARKNESS TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cover", "103_93");
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");        // Second copy
					put("sky3", "103_97");        // Third copy for choice test
					put("troll", "6_106");        // Troll of Udun - [Sauron] Troll, cost 10

					put("aragorn", "1_89");       // Test companion
					put("anduril", "7_79");        // Anduril - FP artifact
					put("athelas", "1_94");        // Athelas - FP possession
					put("lastalliance", "1_49");  // Last Alliance - FP condition
					put("breath", "1_207");       // Black Breath - Shadow condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Despair
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		* 	To play, hinder 2 twilight conditions.
		* 	Skirmish: Exert a Troll twice and hinder this condition to discard a Free Peoples card borne by a companion that Troll is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cover");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Despair", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



// ========================================
// EXTRA COST TESTS - Hinder 2 Twilight Conditions
// ========================================

	@Test
	public void CoverOfDarknessRequires2TwilightConditionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetShadowCard("cover");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToHand(cover, sky1, sky2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// No twilight conditions in play - can't play
		assertFalse(scn.ShadowPlayAvailable(cover));

		// One twilight condition - still can't play
		scn.ShadowPlayCard(sky1);
		assertFalse(scn.ShadowPlayAvailable(cover));

		// Two twilight conditions - now can play
		scn.ShadowPlayCard(sky2);

		assertTrue(scn.ShadowPlayAvailable(cover));

		scn.ShadowPlayCard(cover);
		// Only 2 twilight conditions, so auto-selected

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertInZone(Zone.SUPPORT, cover);
	}

	@Test
	public void CoverOfDarknessAllowsChoiceWhenMoreThan2TwilightConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetShadowCard("cover");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		scn.MoveCardsToHand(cover);
		scn.MoveCardsToSupportArea(sky1, sky2, sky3);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(cover);

		// Should be asked to choose which 2 to hinder
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(sky1, sky3);

		assertTrue(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(sky3));
	}

// ========================================
// SKIRMISH ABILITY TESTS
// ========================================

	@Test
	public void CoverOfDarknessSkirmishAbilityCannotBeUsedIfAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetShadowCard("cover");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCardsToSupportArea(cover, sky1, sky2);
		scn.HinderCard(sky1, sky2, cover); // Cover is also hindered
		scn.MoveMinionsToTable(troll);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troll);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		// Cover is already hindered, can't use ability
		assertFalse(scn.ShadowActionAvailable(cover));
	}

	@Test
	public void CoverOfDarknessSkirmishAbilityTargetsOnlyFPCardsOnSkirmishingCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetShadowCard("cover");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var athelas = scn.GetFreepsCard("athelas");
		var lastalliance = scn.GetFreepsCard("lastalliance");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToSupportArea(cover, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(troll);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, athelas, lastalliance, breath);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troll);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		int trollWoundsBefore = scn.GetWoundsOn(troll);
		assertFalse(scn.IsHindered(cover));

		assertInZone(Zone.ATTACHED, anduril);

		scn.ShadowUseCardAction(cover);
		// Troll is auto-selected as the only troll

		// Verify costs paid
		assertEquals(trollWoundsBefore + 2, scn.GetWoundsOn(troll));
		assertTrue(scn.IsHindered(cover));

		// Verify targeting: 3 FP cards valid, Shadow condition (Black Breath) not valid
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(anduril, athelas, lastalliance));
		assertTrue(scn.ShadowHasCardChoicesNotAvailable(breath));

		scn.ShadowChooseCard(anduril);

		assertInDiscard(anduril);
		assertInZone(Zone.ATTACHED, athelas);
		assertInZone(Zone.ATTACHED, lastalliance);
	}

}
