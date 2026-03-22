package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class Card_92_008_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Aragorn, Ranger of the North (1_89): Gondor Man companion, twilight 4
		put("aragorn", "1_89");
		// Gimli, Dwarf of Erebor (1_13): Dwarven companion, twilight 2
		put("gimli", "1_13");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_8", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_8"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_8
		 * Type: MetaSite
		 * Game Text: Each time you play a companion, you must exert the Ring-bearer.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_8", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void PlayingCompanionExertsRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		// Playing multiple companions should trigger multiple exertions.

		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToHand(aragorn, gimli);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetWoundsOn(frodo));
	}

	@Test
	public void OpponentPlayingCompanionDoesNotExert() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_8: "you play" — Shadow has the modifier, so Freeps playing a companion should not trigger it.

		var scn = GetShadowScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(aragorn);

		// Frodo should NOT be exerted — the modifier belongs to Shadow, not Freeps
		assertEquals(0, scn.GetWoundsOn(frodo));
	}
}
