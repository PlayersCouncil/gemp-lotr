package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_095_Tests
{

// ----------------------------------------
// COVER OF DARKNESS, OMEN OF HORROR TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horror", "103_95");      // Cover of Darkness, Omen of Horror
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");        // Second copy
					put("sky3", "103_97");        // Third copy for extra choice testing
					put("orc", "1_271");          // Orc Soldier - [Sauron] Orc
					put("troll", "6_106");        // Troll of Udun - [Sauron] Troll
					put("uruk", "1_151");         // Uruk Savage - Uruk-hai (not Orc or Troll)

					put("aragorn", "1_89");
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
		 * Name: Cover of Darkness, Omen of Horror
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		 *		To play, hinder 2 twilight conditions.
		 *		Skirmish: Exert an Orc or Troll and hinder 2 twilight conditions to make that minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horror");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Horror", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}


// ========================================
// EXTRA COST TESTS
// ========================================

	@Test
	public void OmenOfHorrorRequiresAndHinders2TwilightConditionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToHand(horror, sky1, sky2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// No twilight conditions in play - can't play
		assertFalse(scn.ShadowPlayAvailable(horror));

		// One twilight condition - still can't play
		scn.ShadowPlayCard(sky1);
		assertFalse(scn.ShadowPlayAvailable(horror));

		// Two twilight conditions - now can play
		scn.ShadowPlayCard(sky2);
		assertTrue(scn.ShadowPlayAvailable(horror));

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		scn.ShadowPlayCard(horror);
		// Only 2 twilight conditions, so auto-selected

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertInZone(Zone.SUPPORT, horror);
	}

// ========================================
// SKIRMISH ABILITY TESTS
// ========================================

	@Test
	public void OmenOfHorrorSkirmishAbilityWorksWithOrcAndPumpsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(horror, sky1, sky2);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		int orcStrengthBefore = scn.GetStrength(orc);
		int orcWoundsBefore = scn.GetWoundsOn(orc);
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		assertTrue(scn.ShadowActionAvailable(horror));
		scn.ShadowUseCardAction(horror);
		// Orc auto-selected as only Orc/Troll
		scn.ShadowChooseCards(sky1, sky2);

		assertEquals(orcWoundsBefore + 1, scn.GetWoundsOn(orc));
		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertEquals(orcStrengthBefore + 2, scn.GetStrength(orc));
	}

	@Test
	public void OmenOfHorrorSkirmishAbilityWorksWithTroll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(horror, sky1, sky2);
		scn.MoveMinionsToTable(troll);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troll);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		int trollStrengthBefore = scn.GetStrength(troll);
		int trollWoundsBefore = scn.GetWoundsOn(troll);

		assertTrue(scn.ShadowActionAvailable(horror));
		scn.ShadowUseCardAction(horror);
		scn.ShadowChooseCards(sky1, sky2);

		assertEquals(trollWoundsBefore + 1, scn.GetWoundsOn(troll));
		assertEquals(trollStrengthBefore + 2, scn.GetStrength(troll));
	}

	@Test
	public void OmenOfHorrorSkirmishAbilityDoesNotWorkWithUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var uruk = scn.GetShadowCard("uruk");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(horror, sky1, sky2);
		scn.MoveMinionsToTable(uruk);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		// No Orc or Troll to exert - ability not available
		assertFalse(scn.ShadowActionAvailable(horror));
	}

	@Test
	public void OmenOfHorrorSkirmishAbilityRequires2UnhinderedTwilightConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(horror);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		// Only 1 unhindered twilight condition - can't afford cost of 2
		assertFalse(scn.ShadowActionAvailable(horror));
	}

	@Test
	public void OmenOfHorrorSkirmishAbilityRequiresOrcOrTrollThatCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(horror, sky1, sky2);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		// Wound orc to exhaustion (vitality 1)
		int orcVit = scn.GetVitality(orc);
		scn.AddWoundsToChar(orc, orcVit - 1);
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		// Orc is exhausted, can't exert - ability not available
		assertFalse(scn.ShadowActionAvailable(horror));
	}

	@Test
	public void OmenOfHorrorSkirmishAbilityAllowsChoiceOfMinionAndConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var orc = scn.GetShadowCard("orc");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(horror, sky1, sky2, sky3);
		scn.MoveMinionsToTable(orc, troll);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		int trollStrengthBefore = scn.GetStrength(troll);
		int trollWoundsBefore = scn.GetWoundsOn(troll);

		scn.ShadowUseCardAction(horror);

		// Should be able to choose between Orc and Troll
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(orc, troll));
		scn.ShadowChooseCard(troll);

		// Should be able to choose which 3 of 4 twilight conditions to hinder
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(sky1, sky3);

		assertEquals(trollWoundsBefore + 1, scn.GetWoundsOn(troll));
		assertTrue(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(sky3));
		assertEquals(trollStrengthBefore + 2, scn.GetStrength(troll));
	}
}
