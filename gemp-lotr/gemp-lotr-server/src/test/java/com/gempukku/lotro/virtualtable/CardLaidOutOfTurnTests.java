package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: what becomes of the text of a card that a test lays on the table (MoveCompanionsToTable,
 * MoveCardsToSupportArea and the rest, which all go through GameState.addCardToZone) when the card is not active
 * as it is laid.  "During your turn, only these cards in play are active: ... your Free Peoples cards ... your
 * opponent's Shadow cards" (CR 4.2 "active"): the Free Peoples cards of the player whose turn it is not, and the
 * Shadow cards of the player whose turn it is, are in play and not active.
 *
 * The game starts a card's text as the card comes into play only if the card is active, and starts the text of
 * every active card as a turn begins (GameState.startAffectingCardsForCurrentPlayer).  So a card laid out of turn
 * has no text in force until a turn begins in which it is active, and has it in force once from then on.  Before
 * engine pass 28 (G-0055) its text was started as it was laid and again as that turn began: the second player's
 * Ranger of the South, laid before his turn, read strength 7 on the first player's turn where 5 is right and 9 on
 * his own where 7 is right.
 *
 * No play of the game puts a card on the table that is not active, so only a cheat, or a scenario step that lays
 * a card for the player whose turn it is not, meets this.
 */
public class CardLaidOutOfTurnTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ranger of the South: strength 5, not unique.  "While you can spot a [gondor] token, this
					// companion is strength +2."
					put("ranger", "18_56");
					// Garrison of Osgiliath: "When you play this condition, you may spot a [gondor] Man to place 2
					// [gondor] tokens here."  Its skirmish ability is not used.
					put("garrison", "6_52");
					// Heirs of Gondor: "When you play this, add a [gondor] token here."  Its skirmish ability is
					// not used.
					put("heirs", "13_69");

					// Orc Bowmen: a Shadow condition, not unique.  "Plays to your support area.  While you can
					// spot a [sauron] Orc, add 1 to the minion archery total."
					put("bowmen", "1_264");
					// Orc Soldier: a [sauron] Orc; its skirmish ability is not used
					put("soldier", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AFreePeoplesCardLaidOnItsOwnersTurnHasItsTextInForceAtOnceAndOnEachOfHisTurnsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstPlayersRanger = scn.GetFreepsCard("ranger");
		var firstPlayersGarrison = scn.GetFreepsCard("garrison");
		var secondPlayersRanger = scn.GetShadowCard("ranger");
		var secondPlayersHeirs = scn.GetShadowCard("heirs");

		scn.StartGame();

		// Player 1's turn, and his own Free Peoples cards: active as they are laid.
		scn.MoveCompanionsToTable(firstPlayersRanger);
		scn.MoveCardsToSupportArea(firstPlayersGarrison);
		assertEquals(5, scn.GetStrength(firstPlayersRanger));
		scn.AddTokensToCard(firstPlayersGarrison, 2);
		assertEquals(7, scn.GetStrength(firstPlayersRanger));

		// Player 2's turn, and his own Free Peoples cards: active as they are laid.  The first player's Ranger is
		// no stronger for the token on a card that is active now: his own text is not in force.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCompanionsToTable(secondPlayersRanger);
		scn.MoveCardsToSupportArea(secondPlayersHeirs);
		assertEquals(5, scn.GetStrength(secondPlayersRanger));
		scn.AddTokensToCard(secondPlayersHeirs, 1);
		assertEquals(7, scn.GetStrength(secondPlayersRanger));
		assertEquals(5, scn.GetStrength(firstPlayersRanger));

		// Each is +2, and no more, on each turn of his owner's after that.
		scn.SkipToOtherPlayersTurn();
		assertEquals(7, scn.GetStrength(firstPlayersRanger));
		assertEquals(5, scn.GetStrength(secondPlayersRanger));

		scn.SkipToOtherPlayersTurn();
		assertEquals(5, scn.GetStrength(firstPlayersRanger));
		assertEquals(7, scn.GetStrength(secondPlayersRanger));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheSecondPlayersFreePeoplesCardLaidBeforeStartGameHasItsTextInForceOnHisOwnTurnsOnlyAndOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstPlayersGarrison = scn.GetFreepsCard("garrison");
		var secondPlayersRanger = scn.GetShadowCard("ranger");
		var secondPlayersHeirs = scn.GetShadowCard("heirs");

		// Before StartGame() the table rests in the first player's part of the opening: the second player's Free
		// Peoples cards are not active as they are laid.
		scn.MoveCardsToSupportArea(firstPlayersGarrison);
		scn.MoveCompanionsToTable(secondPlayersRanger);
		scn.MoveCardsToSupportArea(secondPlayersHeirs);

		scn.StartGame();
		scn.AddTokensToCard(firstPlayersGarrison, 2);
		scn.AddTokensToCard(secondPlayersHeirs, 1);

		// Player 1's turn.  The second player's Ranger has no text in force: he is no stronger for the tokens on
		// the first player's Garrison, which is active (7 before engine pass 28).
		assertEquals(5, scn.GetStrength(secondPlayersRanger));

		// Player 2's turn.  His Ranger's text is started as his turn begins, and once: +2 for the token on his own
		// Heirs of Gondor (9 before engine pass 28).
		scn.SkipToOtherPlayersTurn();
		assertEquals(7, scn.GetStrength(secondPlayersRanger));

		// And so on the turns that follow.
		scn.SkipToOtherPlayersTurn();
		assertEquals(5, scn.GetStrength(secondPlayersRanger));

		scn.SkipToOtherPlayersTurn();
		assertEquals(7, scn.GetStrength(secondPlayersRanger));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheSecondPlayersFreePeoplesCardLaidDuringTheFirstPlayersTurnHasItsTextInForceOnHisOwnTurnsOnlyAndOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstPlayersGarrison = scn.GetFreepsCard("garrison");
		var secondPlayersRanger = scn.GetShadowCard("ranger");
		var secondPlayersHeirs = scn.GetShadowCard("heirs");

		scn.StartGame();

		// Player 1's turn: the second player's Free Peoples cards are not active as they are laid.
		scn.MoveCardsToSupportArea(firstPlayersGarrison);
		scn.MoveCompanionsToTable(secondPlayersRanger);
		scn.MoveCardsToSupportArea(secondPlayersHeirs);
		scn.AddTokensToCard(firstPlayersGarrison, 2);
		scn.AddTokensToCard(secondPlayersHeirs, 1);

		// No text of his is in force (7 before engine pass 28).
		assertEquals(5, scn.GetStrength(secondPlayersRanger));

		// Player 2's turn: +2, once (9 before engine pass 28).
		scn.SkipToOtherPlayersTurn();
		assertEquals(7, scn.GetStrength(secondPlayersRanger));

		// And so on the turns that follow.
		scn.SkipToOtherPlayersTurn();
		assertEquals(5, scn.GetStrength(secondPlayersRanger));

		scn.SkipToOtherPlayersTurn();
		assertEquals(7, scn.GetStrength(secondPlayersRanger));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AShadowCardLaidOnItsOwnersOwnTurnHasItsTextInForceOnTheOtherPlayersTurnOnlyAndOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstPlayersBowmen = scn.GetFreepsCard("bowmen");
		var firstPlayersOrc = scn.GetFreepsCard("soldier");
		var secondPlayersBowmen = scn.GetShadowCard("bowmen");
		var secondPlayersOrc = scn.GetShadowCard("soldier");

		scn.StartGame();

		// Player 1's turn.  The second player is the Shadow player: his Orc and his Orc Bowmen are active as they
		// are laid, and the Bowmen adds 1 at once.
		scn.MoveMinionsToTable(secondPlayersOrc);
		assertEquals(0, scn.GetShadowArcheryTotal());
		scn.MoveCardsToSupportArea(secondPlayersBowmen);
		assertEquals(1, scn.GetShadowArcheryTotal());

		// The first player's own Orc Bowmen is a Shadow card of the player whose turn it is.  It is not active
		// and adds nothing, though there is a [sauron] Orc to spot (2 before engine pass 28).
		scn.MoveCardsToSupportArea(firstPlayersBowmen);
		assertEquals(1, scn.GetShadowArcheryTotal());

		// Player 2's turn.  The first player is the Shadow player now: his Orc Bowmen's text is started as the
		// turn begins, and once (2 before engine pass 28); the second player's Bowmen is the one that adds nothing.
		scn.MoveCardsToDiscard(secondPlayersOrc);
		scn.SkipToOtherPlayersTurn();
		assertEquals(0, scn.GetShadowArcheryTotal());
		scn.MoveMinionsToTable(firstPlayersOrc);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
