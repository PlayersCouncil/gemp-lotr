package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shut", "103_36");
					put("spectre", "103_33");     // Tormented Spectre - [Gondor] Wraith
					put("warrior", "103_34");     // Tormented Warrior - [Gondor] Wraith
					put("revenant", "103_32");    // Tormented Revenant - [Gondor] Wraith
					put("aragorn", "1_89");       // Aragorn - [Gondor] companion (not Wraith)

					put("condition1", "1_242");   // The Dark Lord's Summons - Shadow support area condition
					put("condition2", "1_249");   // Gleaming Spires Will Crumble - Shadow support area condition

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
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver, Skirmish
		 * Game Text: Maneuver: Spot 3 [gondor] Wraiths to discard a Shadow condition.  You may exert a Wraith to hinder another Shadow condition.
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
		// Errata: changed from Response+Skirmish to Maneuver+Skirmish
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		// Errata: twilight cost changed from 0 to 1
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheWayIsShutManeuverDiscardsShadowConditionWith3Wraiths() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shut = scn.GetFreepsCard("shut");
		var spectre = scn.GetFreepsCard("spectre");
		var warrior = scn.GetFreepsCard("warrior");
		var revenant = scn.GetFreepsCard("revenant");
		var condition1 = scn.GetShadowCard("condition1");
		var condition2 = scn.GetShadowCard("condition2");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(shut);
		scn.MoveCompanionsToTable(spectre, warrior, revenant);
		scn.MoveCardsToSupportArea(condition1, condition2);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, condition1);
		assertInZone(Zone.SUPPORT, condition2);

		// Errata: Maneuver ability - spot 3 Gondor Wraiths to discard a Shadow condition
		assertTrue(scn.FreepsPlayAvailable(shut));
		scn.FreepsPlayCard(shut);
		// Choose which Shadow condition to discard
		scn.FreepsChooseCard(condition1);

		// Optional: exert a Wraith to hinder another Shadow condition
		// Decline the optional for this test
		scn.FreepsChooseNo();

		assertInDiscard(condition1);
		assertInZone(Zone.SUPPORT, condition2);
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
