package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class Card_92_017_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("guard", "1_7");
		// Goblin Runner (1_178): Moria Orc minion, twilight 0
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_17", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_17"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_17
		 * Type: MetaSite
		 * Game Text: Each time your fellowship moves, add a threat.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_17", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void AddsThreatOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("guard");

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(1, scn.GetThreats());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetThreats());
	}

	@Test
	public void DoesNotAddThreatWhenOwnedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		assertEquals(0, scn.GetThreats());

		scn.SkipToSite(3);

		// No threats added — shadow owns the modifier
		assertEquals(0, scn.GetThreats());
	}
}
