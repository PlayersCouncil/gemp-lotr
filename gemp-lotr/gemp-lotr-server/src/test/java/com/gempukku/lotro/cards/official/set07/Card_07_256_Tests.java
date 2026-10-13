package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_256_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_256");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two Household Guards, each on a Rider's Mount, and two minions that are still in play in the regroup phase.
	protected VirtualTableScenario GetTwoRidersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sang", "7_256");
					put("guard1", "5_83");      // Household Guard: [rohan] Man, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
					put("mount1", "4_287");     // Rider's Mount: on a [rohan] Man; its text acts at the start of a skirmish
					put("mount2", "4_287");
					put("savage1", "1_151");    // Uruk Savage: vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheySangasTheySlewStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: They Sang as They Slew
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert X mounted companions to wound X minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("They Sang as They Slew", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "Exert X mounted companions to wound X minions": an X of exertions is at least 1 (Q-0076), and CR 4.2
	// "exert" says of this card "You cannot pay for the cost of They Sang As They Slew by exerting a character zero
	// times."  The definition let the choice of companions be answered with none.
	// F-0447.
	@Test
	public void AtLeastOneMountedCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoRidersScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, scn.GetFreepsCard("mount1"));
		scn.AttachCardsTo(guard2, scn.GetFreepsCard("mount2"));
		scn.MoveCardsToHand(sang);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		// Nobody is assigned, so both minions are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);

		// Both riders exhausted (Frodo, who is not mounted, could exert): the event cannot be played.
		scn.AddWoundsToChar(guard1, 2);
		scn.AddWoundsToChar(guard2, 2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sang));

		scn.RemoveWoundsFromChar(guard1, 2);
		scn.RemoveWoundsFromChar(guard2, 2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sang));
		scn.FreepsPlayCard(sang);

		// One rider at the least, and Frodo is not among them.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(guard1, guard2);

		// Two exerted, so two minions wounded: there are two, and nothing is asked.
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TheySangasTheySlewTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
