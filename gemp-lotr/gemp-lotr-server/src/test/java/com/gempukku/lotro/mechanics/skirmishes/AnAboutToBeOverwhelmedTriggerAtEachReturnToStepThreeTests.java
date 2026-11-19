package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Step 3A of the skirmish procedure (Ketura, 2026-10-20): "If the total strength of one side is at least double the
 * total strength of the other side, the losing side is now 'about to be overwhelmed'. Any responses at this point
 * that alter the strength of either side trigger a return to the start of step 3."
 *
 * Each return to the start of step 3 is a new chance to answer "is about to be overwhelmed", for an optional trigger
 * as for a Response: Fishing Boat 13_48 "should" be offered "on every iteration of the check" (F-0566). The engine
 * keeps one "is about to" window open; an optional trigger used in it is offered again once the strength of either
 * side has changed since it was used, and only while its character is still about to be overwhelmed. The Response
 * side of the same step (Footman's Armor 7_93) is shown in Card_07_093_Tests.
 */
public class AnAboutToBeOverwhelmedTriggerAtEachReturnToStepThreeTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "6_45");     // Sméagol, Poor Creature: strength 3, vitality 4
					// Fishing Boat: vitality +1. "Each time Sméagol is about to be overwhelmed in a skirmish, you may
					// remove 2 [gollum] tokens to make him strength +2 until the end of that skirmish."
					put("boat", "13_48");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");     // Goblin Runner: strength 5
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FishingBoatIsOfferedAgainAfterItsOwnUseLeavesSmeagolStillAboutToBeOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var boat = scn.GetFreepsCard("boat");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, boat);

		scn.StartGame();
		scn.AddTokensToCard(boat, 4);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, troop);
		scn.FreepsResolveSkirmish(smeagol);

		// 3 against 13: about to be overwhelmed.
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(13, scn.GetStrength(troop));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));

		scn.FreepsChooseOptionalTrigger(boat);
		// 5 against 13 is still more than double. His strength has changed: back to the start of step 3, where the
		// Boat is offered again.
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(2, scn.GetCultureTokensOn(boat));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));

		scn.FreepsChooseOptionalTrigger(boat);
		// 7 against 13 is no longer double: he loses, takes one wound and lives.
		assertEquals(0, scn.GetCultureTokensOn(boat));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FishingBoatIsNotOfferedAgainOnceSmeagolIsNoLongerAboutToBeOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var boat = scn.GetFreepsCard("boat");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, boat);

		scn.StartGame();
		scn.AddTokensToCard(boat, 6);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.ShadowAssignToMinions(smeagol, runner2);
		scn.FreepsResolveSkirmish(smeagol);

		// 3 against 5 + 5: about to be overwhelmed.
		assertEquals(3, scn.GetStrength(smeagol));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));

		scn.FreepsChooseOptionalTrigger(boat);
		// 5 against 10 is exactly double: still about to be overwhelmed, and offered again.
		assertEquals(5, scn.GetStrength(smeagol));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));

		scn.FreepsChooseOptionalTrigger(boat);
		// 7 against 10 is no longer double. He is not about to be overwhelmed, so the Boat is not offered again,
		// though 2 tokens are left to pay for it: the skirmish resolves, and he loses, takes one wound and lives.
		assertEquals(2, scn.GetCultureTokensOn(boat));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
