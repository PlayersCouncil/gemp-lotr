package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_013_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		// Aragorn, Ranger of the North (1_89): FP companion with maneuver action
		// Maneuver: Exert Aragorn to make him defender+1 until start of regroup.
		put("aragorn", "1_89");
		// Saruman, Keeper of Isengard (53_68): Shadow minion with maneuver action
		// Maneuver: Exert Saruman to make an Uruk-hai fierce until start of regroup.
		put("saruman", "53_68");
		// Uruk Savage (1_151): Isengard Uruk-hai minion, needed on the table for maneuver phase to occur
		put("savage", "1_151");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_13", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_13"
		);
	}

	@Test
	public void NoManeuverActionsStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_13
		 * Type: MetaSite
		 * Game Text: You may not perform maneuver phase actions.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_13", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsCannotUseManeuverActionsWithMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_13: "You may not perform maneuver phase actions."
		// FP has the modifier. Aragorn's maneuver ability should be unavailable.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		// Freeps should NOT be able to use Aragorn's maneuver action
		assertFalse(scn.FreepsActionAvailable(aragorn));
	}

	@Test
	public void ShadowCanStillUseManeuverActionsWhenFreepsHasMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_13: Freeps has the modifier. Shadow should still be able to use their actions.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(saruman, savage);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Shadow should still be able to use Saruman's maneuver action
		assertTrue(scn.ShadowActionAvailable(saruman));
	}

	@Test
	public void ShadowCannotUseManeuverActionsWithShadowMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_13: Shadow has the modifier. Saruman's maneuver action should be unavailable.

		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(saruman, savage);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Shadow should NOT be able to use Saruman's maneuver action
		assertFalse(scn.ShadowActionAvailable(saruman));
	}
}
