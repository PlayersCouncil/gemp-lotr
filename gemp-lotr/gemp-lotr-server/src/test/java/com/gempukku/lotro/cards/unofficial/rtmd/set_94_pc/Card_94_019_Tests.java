package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_019_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89");  // Aragorn, Ranger of the North: FP companion, choice target
		put("boromir", "1_96");  // Boromir: second FP companion, for filter/choice validation
		put("troop", "1_177");   // Goblin Patrol Troop: clean Shadow minion, keeps archery phase alive
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_19", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_19"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_19
		 * Type: MetaSite
		 * Game Text: At the start of each archery phase, exert one of your companions.
		 */

		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_19", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void MandatoryTriggerExertsAChosenCompanionEachArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// Mandatory: a card choice is demanded, not a yes/no
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NoWoundWhenTheRunnerIsShadowSinceYourCompanionFilterIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// CanSpot(your,companion) finds nothing for the Shadow owner - trigger does not fire
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
