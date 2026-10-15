package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig's answers to the loop step's prompts (CR 4.2 "loops"; LoopNegotiation): FreepsNameLoopIterations,
 * ShadowAgreeToLoop and ShadowLowerLoop.  Each answers its own prompt and nothing else: called where its prompt is
 * not pending it fails, naming what it was to do and what the player is being asked, and answers nothing.
 * ShadowLowerLoop names the lower number where it is asked, and where the only lower number is 0 (which the game
 * does not ask) takes 0 and refuses any other.
 *
 * The board is the rules' loop: Sméagol, Slinker 5_29 skirmishing Uruk Pursuer 4_190 at Anduin Banks 7_341.
 */
public class LoopAnswerTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "5_29");
					put("pursuer", "4_190");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "7_341");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Fails unless the rig refuses the call, saying each of the given things.
	private static void assertRefused(Runnable call, String... said) {
		String refusal = null;
		try {
			call.run();
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	// Sméagol and the site each used twice: the Free Peoples player is at the loop's first prompt.
	private static VirtualTableScenario AtTheLoopsFirstPrompt(VirtualTableScenario scn) throws DecisionResultInvalidException, CardNotFoundException {
		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);

		var banks = scn.GetCurrentSite();
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		assertTrue(scn.FreepsDecisionAvailable("LOOP DETECTED"));
		return scn;
	}

	@Test
	public void EachAnswerIsRefusedWhereItsPromptIsNotPending() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertRefused(() -> scn.FreepsNameLoopIterations(3), "name the loop's iterations", "Choose action to play or Pass");
		assertRefused(scn::FreepsAgreeToLoop, "agree to the loop's iterations", "Choose action to play or Pass");
		assertRefused(() -> scn.FreepsLowerLoop(1), "lower the loop's iterations", "Choose action to play or Pass");
		assertRefused(scn::ShadowAgreeToLoop, "agree to the loop's iterations", "nothing is asked");

		// Nothing was answered.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
	}

	@Test
	public void TheShadowPlayersAnswersAreRefusedAtTheFreePeoplesPlayersPrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = AtTheLoopsFirstPrompt(GetScenario());

		assertRefused(scn::ShadowAgreeToLoop, "agree to the loop's iterations");
		assertRefused(scn::FreepsAgreeToLoop, "agree to the loop's iterations", "LOOP DETECTED");

		scn.FreepsNameLoopIterations(5);
		assertRefused(() -> scn.ShadowNameLoopIterations(2), "name the loop's iterations", "The Free Peoples player has decided");
		assertTrue(scn.ShadowDecisionAvailable("will be performed **5** more times."));
	}

	@Test
	public void ShadowLowerLoopNamesTheNumberWhereItIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = AtTheLoopsFirstPrompt(GetScenario());

		scn.FreepsNameLoopIterations(5);
		scn.ShadowLowerLoop(4);

		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShadowLowerLoopTakesZeroWhereNothingElseIsLowerAndRefusesAnyOtherNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = AtTheLoopsFirstPrompt(GetScenario());

		scn.FreepsNameLoopIterations(1);
		assertRefused(() -> scn.ShadowLowerLoop(1), "the only lower number is 0");
	}
}
