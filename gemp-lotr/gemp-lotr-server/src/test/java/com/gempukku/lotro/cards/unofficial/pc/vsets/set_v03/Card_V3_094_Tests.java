package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_094_Tests
{

// ----------------------------------------
// COVER OF DARKNESS, OMEN OF DREAD TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dread", "103_94");       // Cover of Darkness, Omen of Dread
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");        // Second copy
					put("orc", "1_271");          // Orc Soldier - [Sauron] Orc
					put("mouth", "103_102");      // The Mouth of Sauron - triggers burden/threat on companion losing skirmish

					put("aragorn", "1_89");       // Strength 8, will lose to Mouth (strength 9)
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
		 * Name: Cover of Darkness, Omen of Dread
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		* 	To play, hinder 2 twilight conditions.
		* 	Response: If a burden is added, hinder an Orc and this condition to add a threat.
		* 	Response: If a threat is added, hinder an Orc and this condition to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dread");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Dread", card.getBlueprint().getSubtitle());
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
	public void OmenOfDreadRequiresAndHinders2TwilightConditionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToHand(dread, sky1, sky2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// No twilight conditions in play - can't play
		assertFalse(scn.ShadowPlayAvailable(dread));

		// One twilight condition - still can't play
		scn.ShadowPlayCard(sky1);
		assertFalse(scn.ShadowPlayAvailable(dread));

		// Two twilight conditions - now can play
		scn.ShadowPlayCard(sky2);
		assertTrue(scn.ShadowPlayAvailable(dread));

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		scn.ShadowPlayCard(dread);
		// Only 2 twilight conditions, so auto-selected

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertInZone(Zone.SUPPORT, dread);
	}

// ========================================
// BURDEN RESPONSE TESTS
// ========================================

	@Test
	public void OmenOfDreadRespondsWhenBurdenAddedToAddThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(dread, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(orc, mouth);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// Aragorn (str 8) loses to Mouth of Sauron (str 9)
		// Freeps must choose to add threat or burden
		int burdensBefore = scn.GetBurdens();
		int threatsBefore = scn.GetThreats();
		assertFalse(scn.IsHindered(orc));
		assertFalse(scn.IsHindered(dread));

		// Freeps chooses to add a burden
		scn.FreepsChoose("burden");

		assertEquals(burdensBefore + 1, scn.GetBurdens());

		// Shadow should have response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// Orc is auto-selected as only Orc

		assertTrue(scn.IsHindered(orc));
		assertTrue(scn.IsHindered(dread));
		assertEquals(threatsBefore + 1, scn.GetThreats());
	}

	@Test
	public void OmenOfDreadBurdenResponseRequiresUnhinderedOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(dread, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(orc, mouth);
		scn.HinderCard(orc); // Orc is already hindered
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// Freeps chooses to add a burden
		scn.FreepsChoose("burden");

		// Response should not be available - no unhindered Orc
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}


// ========================================
// THREAT RESPONSE TESTS
// ========================================

	@Test
	public void OmenOfDreadRespondsWhenThreatAddedToAddBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(dread, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(orc, mouth);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// Aragorn (str 8) loses to Mouth of Sauron (str 9)
		// Freeps must choose to add threat or burden
		int burdensBefore = scn.GetBurdens();
		int threatsBefore = scn.GetThreats();
		assertFalse(scn.IsHindered(orc));
		assertFalse(scn.IsHindered(dread));

		// Freeps chooses to add a threat
		scn.FreepsChoose("threat");

		assertEquals(threatsBefore + 1, scn.GetThreats());

		// Shadow should have response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// Orc is auto-selected as only Orc

		assertTrue(scn.IsHindered(orc));
		assertTrue(scn.IsHindered(dread));
		assertEquals(burdensBefore + 1, scn.GetBurdens());
	}

	@Test
	public void OmenOfDreadThreatResponseRequiresUnhinderedOrcAndSelf() throws DecisionResultInvalidException, CardNotFoundException {
		// Combined negative test for threat response
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(dread, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(orc, mouth);
		scn.HinderCard(orc); // Orc is already hindered
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// Freeps chooses to add a threat
		scn.FreepsChoose("threat");

		// Response should not be available - no unhindered Orc
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void OmenOfDreadDeclinedResponseDoesNotHinderOrAddEffect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(dread, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(orc, mouth);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		int burdensBefore = scn.GetBurdens();
		int threatsBefore = scn.GetThreats();

		// Freeps adds a burden
		scn.FreepsChoose("burden");
		assertEquals(burdensBefore + 1, scn.GetBurdens());

		// Shadow declines the response
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(orc));
		assertFalse(scn.IsHindered(dread));
		assertEquals(threatsBefore, scn.GetThreats()); // No threat added
	}
}
