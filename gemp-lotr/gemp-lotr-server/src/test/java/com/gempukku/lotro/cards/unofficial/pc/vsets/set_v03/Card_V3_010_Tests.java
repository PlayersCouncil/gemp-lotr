package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("broken", "103_10");
					put("gandalf", "1_364");
					put("gandalfsstaff", "2_22");

					// Shadow cards
					put("runner", "1_178"); // Minion
					put("runner2", "1_178"); // Second minion to keep unhindered
					put("ithilstone", "9_47"); // Shadow Artifact
					put("ships", "8_65"); // Shadow Possession
					put("armory", "1_173"); // Shadow Condition

					// Free Peoples cards to hinder (should NOT be valid targets)
					put("catapult", "8_32"); // FP Possession
					put("sapling", "9_35"); // FP Artifact
					put("moria", "1_21"); // FP Condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YourStaffisBrokenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Your Staff is Broken
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of each Assignment phase, you may exert Gandalf bearing an artifact to discard a hindered Shadow card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("broken");

		assertEquals("Your Staff is Broken", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void YourStaffIsBrokenDoesNotTriggerWithoutArtifactOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var broken = scn.GetFreepsCard("broken");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(gandalf);
		// No artifact on Gandalf
		scn.MoveCardsToSupportArea(broken);
		scn.MoveMinionsToTable(runner, runner2);

		scn.StartGame();

		// Hinder one runner, keep runner2 unhindered to prevent phase skip
		scn.HinderCard(runner);
		assertTrue(scn.IsHindered(runner));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Should NOT have optional trigger - Gandalf has no artifact
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void YourStaffIsBrokenOnlyTargetsHinderedShadowCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfsstaff = scn.GetFreepsCard("gandalfsstaff");
		var broken = scn.GetFreepsCard("broken");

		// Shadow cards
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var ithilstone = scn.GetShadowCard("ithilstone");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");

		// FP cards
		var catapult = scn.GetFreepsCard("catapult");
		var sapling = scn.GetFreepsCard("sapling");
		var moria = scn.GetFreepsCard("moria");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gandalfsstaff);
		scn.MoveCardsToSupportArea(broken, catapult, sapling, moria);
		scn.MoveMinionsToTable(runner, runner2);
		scn.MoveCardsToSupportArea(ithilstone, ships, armory);

		scn.StartGame();

		// Hinder some Shadow cards (runner, ithilstone) and some FP cards (catapult, sapling)
		// Keep runner2 unhindered to prevent phase skip
		scn.HinderCard(runner);
		scn.HinderCard(ithilstone);
		scn.HinderCard(catapult);
		scn.HinderCard(sapling);
		// runner2, ships, armory, moria are NOT hindered

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Should have optional trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Should only have 2 valid targets: hindered Shadow cards (runner, ithilstone)
		// NOT: runner2, ships, armory (not hindered), catapult, sapling (FP, even though hindered)
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoicesAvailable(runner, ithilstone));

		// Choose runner to discard
		scn.FreepsChooseCard(runner);

		// Gandalf should be exerted
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Runner should be discarded
		assertInZone(Zone.DISCARD, runner);

		// Ithilstone should still be hindered and in play
		assertTrue(scn.IsHindered(ithilstone));
	}

	@Test
	public void YourStaffIsBrokenCanDeclineTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfsstaff = scn.GetFreepsCard("gandalfsstaff");
		var broken = scn.GetFreepsCard("broken");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gandalfsstaff);
		scn.MoveCardsToSupportArea(broken);
		scn.MoveMinionsToTable(runner, runner2);

		scn.StartGame();

		// Hinder one runner, keep runner2 unhindered
		scn.HinderCard(runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Decline optional trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Gandalf should not be exerted
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Runner should still be in play
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.IsHindered(runner));
	}

	@Test
	public void YourStaffIsBrokenExertsButFizzlesWithNoHinderedShadowCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfsstaff = scn.GetFreepsCard("gandalfsstaff");
		var broken = scn.GetFreepsCard("broken");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gandalfsstaff);
		scn.MoveCardsToSupportArea(broken);
		scn.MoveMinionsToTable(runner);
		// Runner is NOT hindered, so no phase skip issue

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.IsHindered(runner));

		// Should still have optional trigger (cost can be paid)
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Gandalf should be exerted (cost paid)
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Runner should still be in play (effect fizzled - no valid targets)
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
	}

	@Test
	public void YourStaffDoesNotTriggerIfOnlyArtifactOnGandalfIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");

		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("gandalfsstaff");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(broken);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.HinderCard(staff);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}
}
