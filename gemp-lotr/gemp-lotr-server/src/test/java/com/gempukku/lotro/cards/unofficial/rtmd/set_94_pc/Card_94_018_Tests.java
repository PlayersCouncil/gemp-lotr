package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_018_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: resistance 6, board filler companion
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_18", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_18"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_18
		 * Type: MetaSite
		 * Game Text: Each companion is resistance -1 for each threat.
		 */

		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_18", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ResistanceDropsByOnePerThreatAndRecoversAsTheyAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var ringBearer = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		int aragornBase = scn.GetResistance(aragorn);
		int rbBase = scn.GetResistance(ringBearer);
		assertEquals(0, scn.GetThreats());

		scn.AddThreats(2);
		assertEquals(aragornBase - 2, scn.GetResistance(aragorn));
		assertEquals(rbBase - 2, scn.GetResistance(ringBearer));

		scn.RemoveThreats(1);
		assertEquals(aragornBase - 1, scn.GetResistance(aragorn));
		assertEquals(rbBase - 1, scn.GetResistance(ringBearer));

		scn.RemoveThreats(1);
		assertEquals(aragornBase, scn.GetResistance(aragorn));
		assertEquals(rbBase, scn.GetResistance(ringBearer));
	}

	@Test
	public void AppliesRegardlessOfWhichSideOwnsTheModSinceThereIsNoYour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var ringBearer = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		int aragornBase = scn.GetResistance(aragorn);
		int rbBase = scn.GetResistance(ringBearer);

		// No "your" - a Shadow-owned copy still drops resistance on every companion, including
		// the Free Peoples player's own.
		scn.AddThreats(2);
		assertEquals(aragornBase - 2, scn.GetResistance(aragorn));
		assertEquals(rbBase - 2, scn.GetResistance(ringBearer));
	}
}
