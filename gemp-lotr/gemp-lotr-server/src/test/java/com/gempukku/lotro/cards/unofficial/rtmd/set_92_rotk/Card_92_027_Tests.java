package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.assertEquals;

public class Card_92_027_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("gimli", "1_13");
		put("aragorn", "1_89");
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_27", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_27"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_27
		 * Type: MetaSite
		 * Game Text: Each time your fellowship moves, discard your hand.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_27", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void DiscardsHandOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(gimli, aragorn);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());

		// Move to site 2
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.DISCARD, aragorn);
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(gimli, aragorn);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsPassCurrentPhaseAction();

		// Shadow owns the modifier, Freeps hand should not be discarded
		assertEquals(2, scn.GetFreepsHandCount());
	}
}
