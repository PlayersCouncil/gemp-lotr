package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.TestConstants;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_007_Tests implements TestConstants
{
	private final HashMap<String, String> cards = new HashMap<>() {{
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_7"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_7
		 * Type: MetaSite
		 * Game Text: You must play with your hand revealed.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_7", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ModifierIsActiveForFreepsOwner() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_7: The card owner's hand should be revealed.
		// This tests the modifier is registered; actual visibility is a client/channel concern
		// that can't be verified in the test rig.

		var scn = GetFreepsScenario();

		scn.StartGame();

		// The modifier should report the freeps player's hand as revealed
		var game = scn.game();
		assertTrue(game.getModifiersQuerying().isHandRevealed(game, P1));
		assertFalse(game.getModifiersQuerying().isHandRevealed(game, P2));
	}

	@Test
	public void ModifierIsActiveForShadowOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		var game = scn.game();
		assertTrue(game.getModifiersQuerying().isHandRevealed(game, P2));
		assertFalse(game.getModifiersQuerying().isHandRevealed(game, P1));
	}
}
