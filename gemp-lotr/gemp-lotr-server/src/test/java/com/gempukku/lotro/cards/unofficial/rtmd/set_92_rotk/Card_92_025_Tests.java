package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class Card_92_025_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_25", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_25"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_25
		 * Type: MetaSite
		 * Game Text: Each time your fellowship moves, add (1) for each burden on the Ring-bearer.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_25", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void AddsTwilightEqualToBurdensOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		scn.AddBurdens(4);

		// Pass fellowship phase to trigger the move to site 2
		scn.FreepsPassCurrentPhaseAction();

		// After the move, twilight should include site 2's shadow number + 4 burdens worth
		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// The move adds: site shadow number + 1 per companion moving + burden count
		// Frodo is the only companion, so +1 for him
		assertEquals( site2Shadow + 1 + 4, twilightAfterMove);
	}

	@Test
	public void NoBurdensAddsNoExtraTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		// Remove the starting burden so we have 0
		scn.RemoveBurdens(1);
		assertEquals(0, scn.GetBurdens());

		int twilightBeforeMove = scn.GetTwilight();

		scn.FreepsPassCurrentPhaseAction();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// No burdens, so only site shadow + companion twilight
		assertEquals(twilightBeforeMove + site2Shadow + 1, twilightAfterMove);
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow owns the modifier; Freeps moving should not trigger extra twilight
		var scn = GetShadowScenario();

		scn.StartGame();

		scn.AddBurdens(4);
		scn.FreepsPassCurrentPhaseAction();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// No extra twilight from burdens since Shadow owns the modifier
		assertEquals( site2Shadow + 1, twilightAfterMove);
	}
}
