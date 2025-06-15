
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
                    put("counsel", "101_13");
                    put("gandalf", "1_364");
                    put("elrond", "1_40");
                    put("galadriel", "1_45");
                    put("orophin", "1_56");
                }},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CounselStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: Counsel of the Wise
		 * Side: Free Peoples
		 * Culture: elven
		 * Twilight Cost: 0
		 * Type: event
		 * Subtype: Fellowship
		 * Game Text: Add (X) to take an [elven] ally with a twilight cost of X or less into hand from your draw deck.
		 */

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl counsel = scn.GetFreepsCard("counsel");

		assertEquals(Side.FREE_PEOPLE, counsel.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, counsel.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, counsel.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(counsel, Timeword.FELLOWSHIP)); // test for keywords as needed
		//assertTrue(scn.HasKeyword(counsel, Keyword.TALE));
		assertEquals(0, counsel.getBlueprint().getTwilightCost());

	}

	@Test
	public void CounselRequiresGandalfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl counsel = scn.GetFreepsCard("counsel");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(counsel, gandalf);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(counsel));
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(counsel));
	}

	@Test
	public void CounseloftheWiseChoosing4PermitsTakingElrondIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl counsel = scn.GetFreepsCard("counsel");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(counsel);
		scn.MoveCompanionToTable(gandalf);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(counsel);
		scn.FreepsDismissRevealedCards();
        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));
		// Choices available should be 1 Elrond, 1 Galadriel, 1 Orophin
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(scn.GetFreepsCard("elrond"));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to pay"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(Zone.HAND, elrond.getZone());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(4, scn.GetTwilight());
	}

	@Test
	public void CanRevealWithoutPaying() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl counsel = scn.GetFreepsCard("counsel");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(counsel);
		scn.MoveCompanionToTable(gandalf);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(counsel);
		scn.FreepsDismissRevealedCards();
        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));
		// Choices available should be 1 Elrond, 1 Galadriel, 1 Orophin
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(scn.GetFreepsCard("elrond"));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to pay"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(Zone.DECK, elrond.getZone());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetTwilight());
	}
}



