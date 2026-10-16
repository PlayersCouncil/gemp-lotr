package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: FreepsActionLoopBanned and ShadowActionLoopBanned (CardActionLoopBanned), the reading of
 * the "loopBanned" flag the game sends with each action on offer (CR 4.2 "loops"; Q-0329).  False for an action no
 * loop has used, true for each of the two once the loop has run; it fails where the card offers the player no
 * action, naming the card.
 *
 * The board is the rules' loop: Sméagol, Slinker 5_29 skirmishing Uruk Pursuer 4_190 at Anduin Banks 7_341.
 */
public class LoopBanReadingTests
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

	@Test
	public void TheFlagReadsFalseBeforeTheLoopAndTrueForEachOfItsAbilitiesAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

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

		assertFalse(scn.FreepsActionLoopBanned(smeagol));
		assertFalse(scn.FreepsActionLoopBanned(smeagol, "Use Sméagol"));
		scn.FreepsUseCardAction(smeagol);
		assertFalse(scn.ShadowActionLoopBanned(banks));
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsNameLoopIterations(1);
		scn.ShadowLowerLoop(0);

		assertTrue(scn.ShadowActionLoopBanned(banks));
		assertTrue(scn.ShadowActionLoopBanned(banks, "Use Anduin Banks"));
		scn.ShadowPass();
		assertTrue(scn.FreepsActionLoopBanned(smeagol));

		String refusal = null;
		try {
			scn.ShadowActionLoopBanned(banks);
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		assertTrue(refusal, refusal.contains("Anduin Banks"));
	}
}
