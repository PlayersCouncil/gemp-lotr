package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_91_008_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("boromir", "1_97");
					put("guard", "1_7");        // Dwarf Guard: vitality 2; to play, spot a Dwarf
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a companion for Durin's Secret to spot; a fellowship ability used only on purpose
					put("secret", "1_71");      // Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("saved", "8_20");       // Saved From the Fire: Fellowship event, spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take up to 3 cards of his culture from the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_8", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("boromir", "1_97");
					put("guard", "1_7");        // Dwarf Guard: vitality 2; to play, spot a Dwarf
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_8"
		);
	}

	@Test
	public void MoveLimitMinusOneStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_8
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: While you can spot 5 or more companions, your move limit is -1.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_8", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getIntensity());
	}

	@Test
	public void MoveLimitReducedWith5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: With 5 companions (Frodo + 4), move limit should be reduced from 2 to 1.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		// Normal move limit is 2, should be reduced to 1
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAFifthCompanionLowersTheMoveLimitAndTheTurnEndsAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		// Frodo + 3 = 4 companions, and an ally beside them
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas");
		scn.MoveAlliesToTable("bounder");
		scn.MoveCardsToHand(boromir, guard);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		// The fifth companion
		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(1, scn.GetMoveLimit());

		// "5 or more": a sixth changes nothing
		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WhenAFifthCompanionDiesTheMoveLimitIsStillOneAndTheTurnEndsAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var guard = scn.GetFreepsCard("guard");
		var slayer = scn.GetShadowCard("slayer");
		// Frodo + 4 = 5 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "guard");

		scn.StartGame();
		// Vitality 2: one wound more kills him.
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		// Four companions are left, and the limit is still 1: a change to the move limit holds for the whole turn
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The other player's turn: the penalty went with the turn it was given in
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ThePenaltyHoldsThroughASecondMoveAfterTheFifthCompanionHasGoneAndEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var guard = scn.GetFreepsCard("guard");
		var secret = scn.GetFreepsCard("secret");
		var saved = scn.GetFreepsCard("saved");
		// Frodo + 4 = 5 companions; Gandalf, Gimli and Legolas are the three Durin's Secret needs
		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf", "guard");
		scn.MoveCardsToHand(secret, saved);

		scn.StartGame();

		assertEquals(1, scn.GetMoveLimit()); // 2 - 1
		scn.FreepsPlayCard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1

		// The fifth companion is placed in the dead pile in the fellowship phase, the first of the turn
		scn.FreepsPlayCard(saved);
		scn.FreepsChooseCard(guard);
		// The search of the deck: shown, and no [dwarven] card is in it to take.
		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DEAD, guard);
		// Four companions are left, and the limit is still 2
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// One move made of two: the fellowship is asked about another, and makes it
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// The Shadow phase after the second move: the penalty is still there
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Two moves of two: the turn ends without the question of a third
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.ShadowPass();
		// The Shadow player drew at the reconcile before the second move, so he is asked to reconcile again
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The other player's turn: 2 again
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void MoveLimitUnchangedWithFewerThan5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: Move limit should remain normal with < 5 companions.

		var scn = GetFreepsScenario();

		// Only Frodo + 3 = 4 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas");
		// A fifth character who is no companion
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();

		// Normal move limit should be unchanged
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowCopyDoesNotAffectFreePeoples() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: Shadow's copy should not reduce FP's move limit.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheMoveLimitIsLoweredByExactlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var secret = scn.GetFreepsCard("secret");
		// Frodo and these four are five companions; Gimli is the Dwarf and Legolas the Elf that Durin's Secret needs
		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf", "aragorn");
		scn.MoveCardsToHand(secret);

		scn.StartGame();

		// The limit cannot go below 1, so from 2 a penalty of 1 and a penalty of 2 read the same: raise the limit first
		assertEquals(1, scn.GetMoveLimit()); // 2 - 1
		scn.FreepsPlayCard(secret);
		assertInDiscard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThePenaltyIsGivenAgainOnTheOwnersNextTurnAndNotOnTheOtherPlayersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		// Frodo + 4 = 5 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();
		assertEquals(1, scn.GetMoveLimit());

		// Player 2's turn: the five are not his, and the penalty of the turn before has ended with it
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The owner's next turn: the five are still there, and the limit is 1 again
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOwnersMoveLimitIsLoweredOnHisOwnTurnWhenHeIsPlayerTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 owns the modifier, and his own fellowship is the five: Frodo and these four
		scn.MoveCompanionsToTable(scn.GetShadowCard("gimli"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("legolas"), scn.GetShadowCard("boromir"));

		scn.StartGame();

		// Player 1's turn: his own fellowship is not the five
		assertEquals(2, scn.GetMoveLimit());

		// Player 2's turn
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
