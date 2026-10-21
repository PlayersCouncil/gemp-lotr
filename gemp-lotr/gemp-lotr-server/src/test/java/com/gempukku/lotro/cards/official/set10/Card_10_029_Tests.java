package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_29");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("drawing", "10_29");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("runner", "1_178");     // Goblin Runner: a card for the Shadow player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrawingHisEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Drawing His Eye
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: For each [gondor] companion you spot, reveal 1 card at random from a Shadow player's hand. Choose a revealed Shadow card to be discarded, then its owner draws a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Drawing His Eye", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DrawingHisEyeTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void IsNotPlayableWithTheShadowHandEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var drawing = scn.GetFreepsCard("drawing");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(drawing);

		scn.StartGame();

		// A [gondor] companion to spot, and no card in the Shadow hand to reveal: the reveal must be able to happen
		// for the rest to read it, so the event is not playable (Q-0336: B, applied by engine pass 53).
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.FreepsPlayAvailable(drawing));

		// One card there, and it is playable.
		scn.MoveCardsToHand(runner);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(drawing));
		assertInZone(Zone.HAND, drawing);
	}
}
