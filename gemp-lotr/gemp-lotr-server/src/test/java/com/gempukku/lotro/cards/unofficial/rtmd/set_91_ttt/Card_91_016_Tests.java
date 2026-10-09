package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_91_016_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Axe Strike, a Free Peoples Skirmish event: cards to hold in hand
					put("filler1", "1_3");
					put("filler2", "1_3");
					// Goblin Runner. Player 2's is the minion on the table, so that the turn does not skip
					// straight to the regroup phase; player 1's is a Shadow card to hold in hand.
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_16", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Axe Strike, a Free Peoples Skirmish event: cards to hold in hand
					put("filler1", "1_3");
					put("filler2", "1_3");
					// Goblin Runner. Player 2's is the minion on the table, so that the turn does not skip
					// straight to the regroup phase; player 1's is a Shadow card to hold in hand.
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_16"
		);
	}

	@Test
	public void DiscardAtRegroupStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_16
		 * Type: MetaSite
		 * Intensity: 2
		 * Game Text: At the start of each regroup phase, you must discard a card from your hand.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_16", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsDiscardsAtStartOfRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_16: "At the start of each regroup phase, you must discard a card from your hand."
		// FP has the modifier. At the start of regroup, FP should be forced to discard 1 card.

		var scn = GetFreepsScenario();

		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToFreepsHand("filler1", "filler2");

		scn.StartGame();
		scn.FreepsPass(); // move
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		// At start of regroup, FP should be forced to discard a card
		assertEquals(2, scn.GetFreepsHandCount());
		scn.FreepsChooseAnyCard();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOwnerChoosesOneCardOfHisOwnHandAndAnotherAtTheNextRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var ownRunner = scn.GetFreepsCard("runner");
		var shadowFiller = scn.GetShadowCard("filler1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(filler1, filler2, ownRunner);
		scn.MoveCardsToHand(shadowFiller);

		scn.StartGame();
		scn.FreepsPass(); // move
		scn.MoveMinionsToTable(runner);

		// The start of another phase takes nothing.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		// "A card from your hand": exactly one, Free Peoples or Shadow, and none of the other player's.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(filler1, filler2, ownRunner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shadowFiller));

		scn.FreepsChooseCard(ownRunner);
		assertInDiscard(ownRunner);
		assertInHand(filler1, filler2, shadowFiller);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move brings a second regroup phase in the same turn, and a second discard.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetFreepsHandCount());
		// The Shadow player reconciled before the second move: he drew the one card left in his deck.
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(filler1, filler2));

		scn.FreepsChooseCard(filler1);
		assertInDiscard(filler1);
		assertInHand(filler2, shadowFiller);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowDiscardsAtStartOfRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_16: "At the start of each regroup phase, you must discard a card from your hand."
		// Shadow has the modifier. At the start of regroup, Shadow should be forced to discard 1 card.

		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToFreepsHand("filler1", "filler2");
		scn.MoveCardsToShadowHand("filler1", "filler2");

		scn.StartGame();
		scn.FreepsPass(); // move
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		// At start of regroup, Shadow should be forced to discard a card
		assertEquals(2, scn.GetShadowHandCount());
		scn.ShadowChooseAnyCard();
		assertEquals(1, scn.GetShadowHandCount());

		// FP should NOT be forced to discard — modifier is on Shadow's copy
		assertEquals(2, scn.GetFreepsHandCount());
		// Should be at regroup actions, not a discard decision
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
