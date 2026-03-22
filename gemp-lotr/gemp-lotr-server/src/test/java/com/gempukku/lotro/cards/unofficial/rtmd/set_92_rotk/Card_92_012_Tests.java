package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_012_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Goblin Runner (1_178): Moria Orc minion, twilight 0, strength 5, vitality 1
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_12", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_12"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_12", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ArcheryPhaseSkippedWhenOwnedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.PassCurrentPhaseActions();

		// Should skip archery and land in assignment
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void ArcheryPhaseNotSkippedWhenOwnedByFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.PassCurrentPhaseActions();

		// Archery should NOT be skipped — freeps owns the modifier
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
	}
}
