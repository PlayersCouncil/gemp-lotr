package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_030_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// No Stranger to the Shadows, a Free Peoples condition, and Axe Strike, a Free Peoples
					// event: twelve cards to draw, so that a reconcile leaves a draw deck behind it
					put("nostranger1", "1_108");
					put("nostranger2", "1_108");
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
					put("filler5", "1_3");
					put("filler6", "1_3");
					put("filler7", "1_3");
					put("filler8", "1_3");
					put("filler9", "1_3");
					put("filler10", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_30"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// No Stranger to the Shadows, a Free Peoples condition, and Axe Strike, a Free Peoples
					// event: twelve cards to draw, so that a reconcile leaves a draw deck behind it
					put("nostranger1", "1_108");
					put("nostranger2", "1_108");
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
					put("filler5", "1_3");
					put("filler6", "1_3");
					put("filler7", "1_3");
					put("filler8", "1_3");
					put("filler9", "1_3");
					put("filler10", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_30", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_30
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: At the start of each of your Shadow phases, you may remove (2) to draw a card.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_30", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void RemovesTwilightToDrawAtStartOfShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();
		int deckBefore = scn.GetShadowDeckCount();
		int twilightBefore = scn.GetTwilight();
		// 5 set + 1 companion + 2 for the site
		assertEquals(8, twilightBefore);

		// Optional trigger at start of shadow phase — accept it
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(handBefore + 1, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertEquals(twilightBefore - 2, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanDeclineOptionalTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();
		int deckBefore = scn.GetShadowDeckCount();
		int twilightBefore = scn.GetTwilight();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(handBefore, scn.GetShadowHandCount());
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertEquals(twilightBefore, scn.GetTwilight());
		// "You may" is the modifier's owner and nobody else: the other player is not offered the draw
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotEnoughTwilightMeansNoTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		// The first Shadow phase of the turn begins with 3, and the offer is left alone
		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// The second begins at Rivendell Valley, whose Shadow number is 0, with the pool emptied before the move
		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		int handBefore = scn.GetShadowHandCount();
		assertTrue(scn.GetShadowDeckCount() > 0);
		scn.FreepsChooseToMove();

		// 0 set + 1 companion + 0 for the site
		assertEquals(1, scn.GetTwilight());

		// With insufficient twilight, the optional trigger shouldn't fire
		// (no decision presented — goes straight to shadow actions)
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(handBefore, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheSecondShadowPhaseOfTheTurnAndNotAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 companion + 2 for the site
		assertEquals(8, scn.GetTwilight());
		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(6, scn.GetTwilight());
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With no minion the turn goes on to the regroup phase: 6 in the pool, and no offer at its start
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(6, scn.GetTwilight());
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// The fellowship moves again, to Rivendell Valley: a second Shadow phase, begun with exactly 2
		scn.SkipToMovementDecision();
		scn.SetTwilight(1);
		hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		scn.FreepsChooseToMove();

		// 1 set + 1 companion + 0 for the site
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Freeps owns the modifier; Shadow should not get the trigger
		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		// 5 set + 1 companion + 2 for the site: the pool is there, and it is neither player's to spend this way
		assertEquals(8, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnerInTheFirstSeatMayDrawWhenHeIsTheShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		// Player 2's turn: the Shadow phase that follows his move is player 1's
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		int otherHand = scn.GetShadowHandCount();
		scn.ShadowPass();

		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(otherHand, scn.GetShadowHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
