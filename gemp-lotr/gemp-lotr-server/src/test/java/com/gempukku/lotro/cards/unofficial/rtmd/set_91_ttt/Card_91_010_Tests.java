package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_010_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		// Gimli, Dwarf of Erebor: 6 str, 3 vit
		put("gimli", "1_13");
		// Ulaire Enquea: Nazgul, 11 str, 4 vit
		put("enquea", "1_231");
		// Filler cards to discard from hand
		put("filler1", "1_3");
		put("filler2", "1_3");
		put("filler3", "1_3");
		put("filler4", "1_3");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_10", null
		);
	}

	@Test
	public void SkirmishPumpStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_10
		 * Type: MetaSite
		 * Game Text: Skirmish: Discard 3 cards from hand to make your companion or minion strength +1.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_10", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsCanPumpCompanionDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_10: "Skirmish: Discard 3 cards from hand to make your companion or minion strength +1."

		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToFreepsHand("filler1", "filler2", "filler3");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, enquea);

		// Now in skirmish
		int gimliStr = scn.GetStrength(gimli);

		// Use the modifier's activated ability
		assertTrue(scn.FreepsActionAvailable(freepsMod));
		scn.FreepsUseCardAction(freepsMod);

		// Choose Gimli as the target
		scn.FreepsChooseCard(gimli);

		// Gimli should be strength +1
		assertEquals(gimliStr + 1, scn.GetStrength(gimli));
		// Should have discarded 3 cards
		assertEquals(0, scn.GetFreepsHandCount());

		//Shadow should not be able to use it
		assertFalse(scn.ShadowActionAvailable(freepsMod));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotUseWithoutThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_10: Should not be usable without 3 cards in hand.

		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(enquea);
		// Only 2 filler cards in hand, not enough
		scn.MoveCardsToFreepsHand("filler1", "filler2");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, enquea);

		// In skirmish, the action should not be available with only 2 cards in hand
		assertFalse(scn.FreepsActionAvailable(freepsMod));
	}
}
