package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_027_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cards to hold in hand, none of them ever played
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Free Peoples companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					// Axe Strike, a Free Peoples event: a draw deck to read as untouched, and a second hand
					put("filler1", "1_3");
					put("filler2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_27", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cards to hold in hand, none of them ever played
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Free Peoples companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					// Axe Strike, a Free Peoples event: a draw deck to read as untouched, and a second hand
					put("filler1", "1_3");
					put("filler2", "1_3");
				}},
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
		 * Intensity: 6
		 * Game Text: Each time your fellowship moves, discard your hand.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_27", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(6, card.getBlueprint().getIntensity());
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
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.DISCARD, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWholeHandOfBothSidesGoesAtEachMoveAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ownRunner = scn.GetFreepsCard("runner");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var theirGimli = scn.GetShadowCard("gimli");
		var theirRunner = scn.GetShadowCard("runner");

		// "Your hand": Free Peoples cards and a Shadow card of the owner's, and a hand of the other player's.
		scn.MoveCardsToHand(gimli, aragorn, ownRunner);
		scn.MoveCardsToHand(theirGimli, theirRunner);

		scn.StartGame();

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// The first move of the turn, from site 1 to site 2: nothing is asked, the hand is gone
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInDiscard(gimli, aragorn, ownRunner);
		// His draw deck is no part of his hand, and the other player's hand is not his
		assertEquals(2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, filler1, filler2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInHand(theirGimli, theirRunner);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A new hand before the second move, from site 2 to site 3 at the end of the regroup phase
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(filler1, filler2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		int theirHand = scn.GetShadowHandCount();
		int theirDiscard = scn.GetShadowDiscardCount();

		scn.FreepsChooseToMove();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertInDiscard(filler1, filler2);
		assertEquals(theirHand, scn.GetShadowHandCount());
		assertEquals(theirDiscard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(gimli, aragorn);
		// The owner's hand: it is his fellowship's moves that take it, not the other player's
		scn.MoveCardsToShadowHand("gimli", "runner");

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPass();

		// Shadow owns the modifier, Freeps hand should not be discarded
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// Nor is the owner's, when the fellowship that moves is not his
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerTwoLosesHisHandWhenHisOwnFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetShadowCard("gimli");
		var aragorn = scn.GetShadowCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		// Player 2's turn: he is the Free Peoples player now, and it is his fellowship that moves
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.SetShadowHand(gimli, aragorn, runner);
		int theirHand = scn.GetFreepsHandCount();
		int theirDiscard = scn.GetFreepsDiscardCount();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowPass();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertInDiscard(gimli, aragorn, runner);
		// The other player's hand is not his
		assertEquals(theirHand, scn.GetFreepsHandCount());
		assertEquals(theirDiscard, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
