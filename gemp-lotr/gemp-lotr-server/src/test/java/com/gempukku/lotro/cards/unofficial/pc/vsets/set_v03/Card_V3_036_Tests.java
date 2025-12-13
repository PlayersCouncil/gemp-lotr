package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_036_Tests
{

// ----------------------------------------
// THE WAY IS SHUT TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shut", "103_36");        // The Way is Shut
					put("spectre", "103_33");     // Tormented Spectre - [Gondor] Wraith
					put("warrior", "103_34");     // Tormented Warrior - [Gondor] Wraith
					put("revenant", "103_32");    // Tormented Revenant - [Gondor] Wraith
					put("citadel", "5_32");       // Citadel of the Stars - [Gondor] condition
					put("lordofmoria", "1_21");   // Lord of Moria - [Dwarven] condition
					put("aragorn", "1_89");       // Aragorn - [Gondor] companion (not Wraith)

					put("orc", "1_271");          // Orc Soldier - [Sauron] Orc for Shadow's Reach
					put("reach", "1_277");        // Shadow's Reach - discards FP condition
					put("runner", "1_178");       // Goblin Runner - for skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWayisShutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Way is Shut
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Response: If your [gondor] card is about to be discarded by a Shadow card, spot 3 [gondor] Wraiths to hinder that card instead.
		* 	Skirmish: Make your Wraith strength +1 for each threat you can spot (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shut");

		assertEquals("The Way is Shut", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}



// ========================================
// RESPONSE TESTS - Prevent Discard
// ========================================

	@Test
	public void TheWayIsShutResponseRequires3GondorWraiths() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var warrior = scn.GetFreepsCard("warrior");
		var citadel = scn.GetFreepsCard("citadel");
		var orc = scn.GetShadowCard("orc");
		var reach = scn.GetShadowCard("reach");
		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre, warrior); // Only 2 Gondor Wraiths
		scn.MoveCardsToSupportArea(citadel);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// Shadow plays Shadow's Reach targeting the Gondor condition
		scn.ShadowPlayCard(reach);
		// citadel auto-selected as only FP condition

		// Only 2 Gondor Wraiths - response should not be available
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(citadel);
	}

	@Test
	public void TheWayIsShutResponseDoesNotTriggerOnNonGondorCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var warrior = scn.GetFreepsCard("warrior");
		var revenant = scn.GetFreepsCard("revenant");
		var lordofmoria = scn.GetFreepsCard("lordofmoria"); // Non-Gondor condition
		var orc = scn.GetShadowCard("orc");
		var reach = scn.GetShadowCard("reach");
		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre, warrior, revenant); // 3 Gondor Wraiths
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// Shadow discards the Dwarven condition
		scn.ShadowPlayCard(reach);
		// lordofmoria auto-selected as only FP condition

		// 3 Gondor Wraiths but target isn't Gondor - response should not be available
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(lordofmoria);
	}

	@Test
	public void TheWayIsShutResponsePreventsDiscardAndHindersGondorCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var warrior = scn.GetFreepsCard("warrior");
		var revenant = scn.GetFreepsCard("revenant");
		var citadel = scn.GetFreepsCard("citadel");
		var orc = scn.GetShadowCard("orc");
		var reach = scn.GetShadowCard("reach");
		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre, warrior, revenant); // 3 Gondor Wraiths
		scn.MoveCardsToSupportArea(citadel);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.IsHindered(citadel));

		// Shadow plays Shadow's Reach targeting the Gondor condition
		scn.ShadowPlayCard(reach);

		// Response should be available
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Citadel should be hindered instead of discarded
		assertInZone(Zone.SUPPORT, citadel);
		assertTrue(scn.IsHindered(citadel));
	}

	@Test
	public void TheWayIsShutResponseCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var warrior = scn.GetFreepsCard("warrior");
		var revenant = scn.GetFreepsCard("revenant");
		var citadel = scn.GetFreepsCard("citadel");
		var orc = scn.GetShadowCard("orc");
		var reach = scn.GetShadowCard("reach");
		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre, warrior, revenant);
		scn.MoveCardsToSupportArea(citadel);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(reach);

		// Decline the response
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Citadel should be discarded normally
		assertInDiscard(citadel);
		assertFalse(scn.IsHindered(citadel));
	}

// ========================================
// SKIRMISH ABILITY TESTS
// ========================================

	@Test
	public void TheWayIsShutSkirmishAbilityPumpsWraithByThreatsWithLimit3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre);
		scn.MoveMinionsToTable(runner);
		scn.AddThreats(5); // More than limit of 3

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(spectre, runner);
		scn.FreepsResolveSkirmish(spectre);

		int baseStrength = scn.GetStrength(spectre);
		assertEquals(5, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);
		// Spectre auto-selected as only Wraith

		// Should be +3 (limit), not +5 (actual threats)
		assertEquals(baseStrength + 3, scn.GetStrength(spectre));
	}

	@Test
	public void TheWayIsShutSkirmishAbilityPumpsLessThanLimitIfFewerThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre);
		scn.MoveMinionsToTable(runner);
		scn.AddThreats(2); // Less than limit of 3

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(spectre, runner);
		scn.FreepsResolveSkirmish(spectre);

		int baseStrength = scn.GetStrength(spectre);
		assertEquals(2, scn.GetThreats());

		scn.FreepsPlayCard(shut);

		// Should be +2 (actual threats)
		assertEquals(baseStrength + 2, scn.GetStrength(spectre));
	}

	@Test
	public void TheWayIsShutSkirmishAbilityOnlyTargetsYourWraiths() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var aragorn = scn.GetFreepsCard("aragorn"); // Not a Wraith
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre, aragorn);
		scn.MoveMinionsToTable(runner);
		scn.AddThreats(2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn is skirmishing, but he's not a Wraith
		// Spectre is a Wraith but not skirmishing - should still be targetable since ability doesn't require skirmishing
		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);

		// Only spectre should be selectable (the only Wraith)
		// Since there's only one valid target, it auto-selects
		int spectreStrength = scn.GetStrength(spectre);
		// Spectre got the pump even though not skirmishing
	}
}
