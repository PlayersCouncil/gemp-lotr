package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_9: Fellowship: Add (3) to play a companion from your draw deck; they come into play exhausted.
 *
 * Owner-gated to Freeps.
 * Cost: add 3 twilight.
 * Effect: play a companion from draw deck, then exhaust that companion.
 *
 * Test cards:
 * - Aragorn (1_89): companion in draw deck to be played
 * - Boromir (1_96): second companion to prove selectivity
 */
public class Card_93_009_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89");
		put("boromir", "1_96");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_9", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_9"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_9", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void PlaysCompanionFromDeckExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");

		// Place Aragorn in draw deck
		scn.MoveCardsToTopOfDeck(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Should see the deck contents and be able to select Aragorn
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(aragorn);

		// Aragorn should now be in play, exhausted (vitality 4 = 3 wounds)
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// Cost was 3 twilight, +4 from Aragorn
		assertEquals(3 + 4, scn.GetTwilight());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OwnerGatingShadowCantUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToTopOfDeck(aragorn);

		scn.StartGame();

		// Shadow owns the mod — Freeps can't use it
		assertFalse(scn.FreepsActionAvailable("mod"));
	}
}
