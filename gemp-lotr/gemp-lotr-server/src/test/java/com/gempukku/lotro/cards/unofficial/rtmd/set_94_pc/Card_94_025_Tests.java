package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_025_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		// Dwarf Guard (1_7) x10, non-unique: padding to raise the threat limit (= companion count)
		// high enough that AddThreats(5) does not fizzle.
		put("guard1", "1_7");
		put("guard2", "1_7");
		put("guard3", "1_7");
		put("guard4", "1_7");
		put("guard5", "1_7");
		put("guard6", "1_7");
		put("guard7", "1_7");
		put("guard8", "1_7");
		put("guard9", "1_7");
		put("guard10", "1_7");
	}};

	private void addThreatLimitPadding(VirtualTableScenario scn) {
		scn.MoveCompanionsToTable(
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"), scn.GetFreepsCard("guard6"),
				scn.GetFreepsCard("guard7"), scn.GetFreepsCard("guard8"), scn.GetFreepsCard("guard9"),
				scn.GetFreepsCard("guard10"));
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_25", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_25"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_25
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Each time your fellowship moves during the regroup phase in region 3, add 5
		 * threats.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_25", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(4, mod.getBlueprint().getIntensity());
	}

	@Test
	public void MovingOutOfRegion3AddsFiveThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		addThreatLimitPadding(scn);

		scn.StartGame();
		scn.SkipToSite(7);

		scn.SkipToMovementDecision();
		int threatsBefore = scn.GetThreats();

		// Departs site 7 (region 3) via the Regroup-phase "another move?" decision.
		scn.FreepsChooseToMove();
		assertEquals(threatsBefore + 5, scn.GetThreats());
	}

	@Test
	public void MovingOutOfRegion2DoesNotAddThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		addThreatLimitPadding(scn);

		scn.StartGame();
		scn.SkipToSite(5);

		scn.SkipToMovementDecision();
		int threatsBefore = scn.GetThreats();

		// Departs site 5 (region 2)
		scn.FreepsChooseToMove();
		assertEquals(threatsBefore, scn.GetThreats());
	}

	@Test
	public void FellowshipPhaseMoveDoesNotAddThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		addThreatLimitPadding(scn);

		scn.StartGame();
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		int threatsBefore = scn.GetThreats();

		// The mandatory once-per-turn move (site 7 -> 8) runs automatically inside the ordinary
		// Fellowship phase, not the Regroup phase, so it must not add threats.
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(threatsBefore, scn.GetThreats());
	}

	@Test
	public void RegroupPhaseMoveAfterFirstMoveAddsThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		addThreatLimitPadding(scn);

		scn.StartGame();
		scn.SkipToSite(6);

		// This turn's mandatory Fellowship-phase move (site 6 -> 7) already happened, landing on
		// the Regroup "another move?" decision at site 7 with one move already spent this turn.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetCurrentSiteNumber());
		int threatsBefore = scn.GetThreats();

		// The optional Regroup-phase move (site 7 -> 8) departs a region-3 site during Phase.REGROUP,
		// exactly what the game text describes.
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(threatsBefore + 5, scn.GetThreats());
	}

	@Test
	public void DoesNotFireWhenShadowOwnsMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		addThreatLimitPadding(scn);

		scn.StartGame();
		scn.SkipToSite(7);

		scn.SkipToMovementDecision();
		int threatsBefore = scn.GetThreats();

		// requires: OwnerIsFreeps is false here
		scn.FreepsChooseToMove();
		assertEquals(threatsBefore, scn.GetThreats());
	}
}
