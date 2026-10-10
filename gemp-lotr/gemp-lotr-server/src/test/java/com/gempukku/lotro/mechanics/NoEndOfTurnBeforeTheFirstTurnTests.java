package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * No turn ends before the first turn begins: a text that acts "at the end of the turn" is not carried out
 * between the mulligans and turn 1.
 *
 * Comprehensive Rules 4.2, "mulligan": "After all players have drawn their starting hands (before the first turn
 * begins), each player (in player order) may shuffle his or her hand into his or her draw deck and draw 6
 * cards."  The starting fellowships and the mulligans are before the first turn, and the first end of a turn is
 * the end of the first turn.
 *
 * Before F-0327 the game ran its whole end-of-turn step once after the mulligans, to end what the starting
 * fellowships had left "until the end of the turn" (Théoden, The Renowned 13_137, whose test of that is in
 * Card_13_137_Tests), and with it told every card that a turn had ended.  The player who will go second counts as
 * the Free Peoples player at that moment, so it was his cards that answered: a card of his with an "end of turn"
 * text, in play before the first turn, was carried out there.  It was found on Race Text 94_17 ("At the end of
 * each of your turns, choose an opponent who may take control of a site"), which asked player 1 its question
 * before anybody's turn; that card has since been given a requirement that hides it.
 *
 * It is shown here with Enraged 6_26, "Bearer must be an Ent. Limit 1 per bearer. Discard this condition at the
 * end of the turn", whose trigger has no requirement at all.  A condition is in play before the first turn only
 * through Race Text 94_3 (Race to Mount Doom): "You may include items, conditions, allies, and followers in your
 * starting fellowship."  Forest Guardian 5_17 (an Ent of cost 2 with nothing needed to play him) bears it: 2 and
 * 2 are the 4 a starting fellowship may cost.
 */
public class NoEndOfTurnBeforeTheFirstTurnTests
{
	// Race to Mount Doom, with player 1 holding Race Text 94_3.
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guardian", "5_17");    // Forest Guardian: [gandalf] Ent companion, cost 2
					put("enraged", "6_26");     // Enraged: [gandalf] condition, cost 2, on an Ent
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_3", null
		);
	}

	// The same table with player 2 holding Race Text 94_3.
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guardian", "5_17");
					put("enraged", "6_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_3"
		);
	}

	@Test
	public void AnEndOfTurnTextInTheSecondPlayersStartingFellowshipIsNotCarriedOutBeforeTheFirstTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var guardian = scn.GetShadowCard("guardian");
		var enraged = scn.GetShadowCard("enraged");

		// Player 1 takes nobody; player 2 puts down an Ent and Enraged on him
		scn.FreepsDeclineChoosing();
		scn.ShadowChooseCard(guardian);
		scn.ShadowChooseCard(enraged);
		assertAttachedTo(enraged, guardian);

		// The hands are dealt and both players keep them
		assertTrue(scn.FreepsDecisionAvailable("Do you wish to mulligan"));
		scn.StartGame(true, false);

		// Turn 1 has begun, player 1's, and no turn has ended: Enraged is where it was played
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(enraged, guardian);
		assertEquals(0, scn.GetShadowDiscardCount());
	}

	@Test
	public void ThatTextIsCarriedOutAtTheEndOfItsOwnersFirstTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var guardian = scn.GetShadowCard("guardian");
		var enraged = scn.GetShadowCard("enraged");

		scn.FreepsDeclineChoosing();
		scn.ShadowChooseCard(guardian);
		scn.ShadowChooseCard(enraged);
		scn.StartGame(true, false);

		// Turn 1 is player 1's: player 2's Free Peoples cards are not active in it.  Player 1 was dealt his
		// own two copies, and keeps them as he reconciles
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		// Turn 2, player 2's own, begins with Enraged still on its bearer
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertAttachedTo(enraged, guardian);

		scn.SkipToMovementDecision();
		assertAttachedTo(enraged, guardian);
		scn.ShadowChooseToStay();

		// His turn has ended, and Enraged with it
		assertInZone(Zone.DISCARD, enraged);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFirstEndOfTurnIsTheEndOfTheFirstTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var enraged = scn.GetFreepsCard("enraged");

		// Player 1 puts down an Ent and Enraged on him; player 2 takes nobody
		scn.FreepsChooseCard(guardian);
		scn.FreepsChooseCard(enraged);
		scn.ShadowDeclineChoosing();
		scn.StartGame(true, false);

		// Turn 1 has begun, his own
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(enraged, guardian);
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.SkipToMovementDecision();
		assertAttachedTo(enraged, guardian);
		scn.FreepsChooseToStay();

		// The first turn has ended, and Enraged with it
		assertInZone(Zone.DISCARD, enraged);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
