package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInPlay;

import static org.junit.Assert.*;

public class Card_94_024_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("gimli", "1_13"); // Gimli, Son of Gloin: FP companion, twilight 2
		put("guard", "1_302"); // Merry, Friend to Sam: FP companion, twilight 1, no play requirement
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_24", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_24"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_24
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Your companions are twilight cost +X, where X is the region number (including
		 * during your starting fellowship).
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_24", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(4, mod.getBlueprint().getIntensity());
	}

	@Test
	public void CompanionCostScalesWithRegionNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(gimli, guard);
		scn.StartGame();

		// Region 1 (sites 1-3): +1
		assertEquals(3, scn.GetTwilightCost(gimli));
		assertEquals(2, scn.GetTwilightCost(guard));

		// Region 2 (sites 4-6): +2
		scn.SkipToSite(4);
		assertEquals(4, scn.GetTwilightCost(gimli));
		assertEquals(3, scn.GetTwilightCost(guard));

		// Region 3 (sites 7-9): +3
		scn.SkipToSite(7);
		assertEquals(5, scn.GetTwilightCost(gimli));
		assertEquals(4, scn.GetTwilightCost(guard));
	}

	@Test
	public void SurchargeAppliesDuringStartingFellowshipSelection() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");

		// With the mod: Gimli costs 3, Merry costs 2 (both +1 for region 1). Budget is 4.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));

		scn.FreepsChooseCard(gimli);

		// 3 (Gimli) + 2 (Merry) = 5 > 4, and there is nothing else in the pool to pick:
		// the decision auto-completes once nothing affordable remains.
		assertFalse(scn.FreepsDecisionAvailable("Starting fellowship"));

		scn.ShadowDecided("");
		scn.StartGame(true, false);

		assertInPlay(gimli);
	}

	@Test
	public void WithoutTheModTheSameTwoCompanionsFitTheBudget() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");

		// Without the mod: Gimli costs 2, Merry costs 1. 2 + 1 = 3 <= 4: both fit.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(gimli);

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard));
		scn.FreepsChooseCard(guard);

		scn.ShadowDecided("");
		scn.StartGame(true, false);

		assertInPlay(gimli, guard);
	}

	@Test
	public void OpponentsCompanionsAreUnaffectedWhenShadowOwnsMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(gimli);
		scn.StartGame();

		// "your,companion" does not match Freeps's companion when Shadow owns the mod
		assertEquals(2, scn.GetTwilightCost(gimli));
	}
}
