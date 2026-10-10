package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Names;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.List;

public interface GameProperties extends TableBase {
	/**
	 * The burdens of the player whose turn it is, the player AddBurdens and RemoveBurdens go by as well; one
	 * player's burdens whoever's turn it is are read with FreepsGetBurdens() and ShadowGetBurdens().
	 */
	default int GetBurdens() { return gameState().getBurdens(); }
	/**
	 * The burdens of the Free Peoples player, whoever's turn it is: the burdens on his own Ring-bearer.  This is
	 * player 1 on every turn.  See GetBurdens(String).
	 */
	default int FreepsGetBurdens() { return GetBurdens(P1()); }
	/**
	 * The burdens of the Shadow player, whoever's turn it is: the burdens on his own Ring-bearer.  This is
	 * player 2 on every turn.  See GetBurdens(String).
	 */
	default int ShadowGetBurdens() { return GetBurdens(P2()); }
	/**
	 * The burdens of one player, read whoever's turn it is: the burdens on that player's own Ring-bearer.
	 * GetBurdens() with no player reads the burdens of the player whose turn it is, so from the moment a turn ends
	 * it reads the other player's; this is the reading for a burden added as a turn ends ("at the end of each
	 * turn"), and for one player's bid before the game starts.
	 * @param player The player whose burdens are wanted.
	 * @return The number of burdens that player has.
	 */
	default int GetBurdens(String player) {
		Verify(P1().equals(player) || P2().equals(player),
				"GetBurdens was asked for the burdens of '" + player + "', who is not a player at this table.");
		return gameState().getPlayerBurdens(player);
	}
	default void AddBurdens(int count) {
		gameState().addBurdens(count);
	}

	default void RemoveBurdens(int count) {
		gameState().removeBurdens(count);
	}

	default int GetFreepsCultureCount() { return GameUtils.getSpottableFPCulturesCount(game(), P1()); }
	default int GetShadowCultureCount() { return GameUtils.getSpottableShadowCulturesCount(game(), P1()); }

	default int GetThreats() {
		return gameState().getThreats();
	}
	default int GetThreatLimit() {
		return PlayConditions.getThreatLimit(game());
	}
	default void AddThreats(int count) {
		gameState().addThreats(gameState().getCurrentPlayerId(), count);
	}

	default void RemoveThreats(int count) {
		gameState().removeThreats(gameState().getCurrentPlayerId(), count);
	}

	default boolean FreepsHasInitiative() { return PlayConditions.hasInitiative(game(), Side.FREE_PEOPLE); }

	default boolean ShadowHasInitiative() { return PlayConditions.hasInitiative(game(), Side.SHADOW); }

	default int GetFreepsArcheryTotal() { return RuleUtils.calculateFellowshipArcheryTotal(game()); }
	default int GetShadowArcheryTotal() { return RuleUtils.calculateShadowArcheryTotal(game()); }

	default int GetTwilight() { return gameState().getTwilightPool(); }
	default void SetTwilight(int amount) { gameState().setTwilight(amount); }

	default int GetMoveCount() { return gameState().getMoveCount(); }

	default int GetMoveLimit() { return game().getModifiersQuerying().getMoveLimit(game(), 2); }

	/**
	 * @return Whether the game has finished one way or another.
	 */
	default boolean GameIsFinished() { return game().isFinished(); }

	/**
	 * Whether the Free Peoples player has won the game: false while it is going on, and for both players of a
	 * game that was cancelled.  This is player 1, whichever side he had on the turn the game ended.  See PlayerHasWon.
	 */
	default boolean FreepsHasWon() { return PlayerHasWon(P1()); }
	/**
	 * Whether the Shadow player has won the game: false while it is going on, and for both players of a
	 * game that was cancelled.  This is player 2, whichever side he had on the turn the game ended.  See PlayerHasWon.
	 */
	default boolean ShadowHasWon() { return PlayerHasWon(P2()); }
	/**
	 * Whether the given player has won the game: false while the game is going on, and false for both players
	 * of a game that was cancelled.  GameIsFinished() says that a game is over; this says for whom it ended well.
	 * It goes by the player and not by the side that player had on the turn the game ended.  Why the game ended
	 * is in the game log, in the game's own words ("... lost due to: ...", "... is the winner due to: ...").
	 * @param player The player asked about.
	 * @return True if the game is over and that player is its winner.
	 */
	default boolean PlayerHasWon(String player) {
		Verify(P1().equals(player) || P2().equals(player),
				"PlayerHasWon was asked about '" + player + "', who is not a player at this table.");
		return player.equals(game().getWinnerPlayerId());
	}

	/**
	 * Whether the Free Peoples player has lost the game: false while it is going on, and true exactly when the
	 * other player has won.  This is player 1, whichever side he had on the turn the game ended.  See PlayerHasLost.
	 */
	default boolean FreepsHasLost() { return PlayerHasLost(P1()); }
	/**
	 * Whether the Shadow player has lost the game: false while it is going on, and true exactly when the
	 * other player has won.  This is player 2, whichever side he had on the turn the game ended.  See PlayerHasLost.
	 */
	default boolean ShadowHasLost() { return PlayerHasLost(P2()); }
	/**
	 * Whether the given player has lost the game: false while the game is going on, and false for both players
	 * of a game that was cancelled.  At a table of two the game is over the moment one player loses and the other
	 * is then its winner, so a player has lost exactly when the other has won.  It goes by the player and not by
	 * the side that player had on the turn the game ended.
	 * @param player The player asked about.
	 * @return True if the game is over and the other player is its winner.
	 */
	default boolean PlayerHasLost(String player) {
		Verify(P1().equals(player) || P2().equals(player),
				"PlayerHasLost was asked about '" + player + "', who is not a player at this table.");
		String winner = game().getWinnerPlayerId();
		return winner != null && !winner.equals(player);
	}

	/**
	 * @return Gets the current game phase
	 */
	default Phase GetCurrentPhase() { return gameState().getCurrentPhase(); }

	/**
	 * Whether the game is in its fierce round: true from the game's "Fierce skirmishes" until the last fierce
	 * skirmish is over, and false in the first assignment and skirmish phases and in every other phase.  After
	 * the ordinary skirmishes, if a fierce minion is still in play, the game goes through an assignment phase and
	 * skirmish phases again, and GetCurrentPhase() reads ASSIGNMENT and then SKIRMISH there exactly as it did the
	 * first time: this is what tells the two apart.
	 */
	default boolean InFierceRound() { return gameState().isFierceSkirmishes(); }

	/**
	 * Whether the game is in its relentless round: true from the game's "Relentless skirmishes" until the last
	 * relentless skirmish is over, and false in the first round, in the fierce round and in every other phase.
	 * The relentless round is the assignment phase and skirmish phases that follow the last of the others while a
	 * relentless minion is in play (after the fierce round, or after the first where no minion is fierce), and
	 * GetCurrentPhase() reads ASSIGNMENT and then SKIRMISH there as it did before, with InFierceRound() false:
	 * this is what tells it from the first round.
	 */
	default boolean InRelentlessRound() { return gameState().isRelentlessSkirmishes(); }

	/**
	 * @return Gets the player who is currently playing their turn.
	 */
	default String GetCurrentPlayer() { return gameState().getCurrentPlayerId(); }

	/**
	 * @return Gets the player whose turn it isn't.
	 */
	default String GetOffPlayer() {
		var order = gameState()
				.getPlayerOrder()
				.getCounterClockwisePlayOrder(GetCurrentPlayer(), true);

		//Skip the first player in the order, as that is the decider.
		order.getNextPlayer();
		return order.getNextPlayer();
	}

	/**
	 * The player who is currently making a decision; fails, saying whether the game is over and who won it, when
	 * nobody is being asked anything.  Every helper that passes for both players starts here.
	 * @return Gets the player who is currently making a decision.
	 */
	default String GetDecidingPlayer() {
		var deciding = userFeedback().getUsersPendingDecision().stream().findFirst();
		if(deciding.isEmpty()) {
			String winner = game().getWinnerPlayerId();
			Verify(false, "Nobody is being asked anything, so no player can be passed for or answered for: "
					+ (!GameIsFinished() ? "the game is at rest with no decision pending."
						: winner != null ? "the game is over (" + winner + " has won).  A helper that plays a turn out cannot "
								+ "play on through the turn that ends the game."
						: "the game is over (it was cancelled)."));
		}
		return deciding.orElse(null);
	}

	/**
	 * @return Gets the player who is not currently making a decision.
	 */
	default String GetNextDecider() {
		var order = gameState()
			.getPlayerOrder()
			.getCounterClockwisePlayOrder(GetDecidingPlayer(), true);

		//Skip the first player in the order, as that is the decider.
		order.getNextPlayer();
		return order.getNextPlayer();
	}



	/**
	 * Starts reading the game log from here: the messages the game sends from now on are kept, and any kept before
	 * are dropped.  Call it just before the action whose log lines a test means to read.
	 */
	default void MarkGameLog() { gameState().startMessageRecord(); }

	/**
	 * @return Every message the game has sent to the players since MarkGameLog() was last called, oldest first, as
	 * the client is sent them (a card is named by a link, HTML that holds its blueprint id and its full name).  For
	 * a failure message; to ask whether a card was named, use GameLogSinceMarkMentions.
	 */
	default List<String> GetGameLogSinceMark() {
		var record = gameState().getMessageRecord();
		Verify(record != null, "The game log is not being read: call MarkGameLog() before the action whose messages are wanted.");
		return record == null ? List.of() : List.copyOf(record);
	}

	/**
	 * Whether the game log has named each of the given cards since MarkGameLog() was last called.  This is the
	 * reading for a card that is made known by the log alone: one taken from a draw deck by its type, or revealed
	 * from a hand to a player who is shown no pop-up.  A card is named by its link (blueprint id and full name),
	 * as every line of the log names a card, so two copies of one card cannot be told apart, and a card the log
	 * names for another reason (the event that was just played) is named too: set the mark after that.
	 * @param cards The cards to look for.
	 * @return True if every one of the cards is named in some message sent since the mark.
	 */
	default boolean GameLogSinceMarkMentions(PhysicalCardImpl... cards) {
		var log = GetGameLogSinceMark();
		for (var card : cards) {
			var link = GameUtils.getCardLink(card);
			if (log.stream().noneMatch(message -> message.contains(link)))
				return false;
		}
		return true;
	}

	/**
	 * Whether the game log has said the given words since MarkGameLog() was last called.  This is the reading for
	 * something the game tells the players by a line in the log and by nothing else, with no card to name: a reveal
	 * from a hand that had no card to show ("no cards in").  Give the fewest words that tell the line from every
	 * other; a card is asked after with GameLogSinceMarkMentions.
	 * @param text Words to look for, in any mix of capitals and small letters.
	 * @return True if some one message sent since the mark contains the words.
	 */
	default boolean GameLogSinceMarkSays(String text) {
		Verify(text != null && !text.isBlank(), "GameLogSinceMarkSays was given no words to look for.");
		var words = text == null ? "" : text.toLowerCase();
		return GetGameLogSinceMark().stream().anyMatch(message -> message.toLowerCase().contains(words));
	}

	/**
	 * Whether the game has told the clients that the Free Peoples player's hand is revealed.  See HandIsRevealed.
	 */
	default boolean FreepsHandIsRevealed() { return HandIsRevealed(P1()); }
	/**
	 * Whether the game has told the clients that the Shadow player's hand is revealed.  See HandIsRevealed.
	 */
	default boolean ShadowHandIsRevealed() { return HandIsRevealed(P2()); }
	/**
	 * Whether the game has told the clients that the given player plays with a revealed hand ("You must play with
	 * your hand revealed").  This is read from the game stats, where every client is told whose hands are open to
	 * it, and not from the modifier that reveals the hand: true only if the stats last sent to each seat say so.
	 * It goes by the player and not by the side that player has this turn.  Which cards of such a hand a seat has
	 * in fact been sent is another reading: FreepsSeesCardInHand / ShadowSeesCardInHand.
	 *
	 * The table reads this from a channel it keeps for each seat from before the game's first step, and from a
	 * client joined to each seat as the reading is made; it fails, saying which, if any two of them were told
	 * different things.  It fails as well on a table that keeps no such channels (the unit test table keeps them).
	 * @param player The player whose hand is asked about.
	 * @return True if every client has been told that player's hand is revealed.
	 */
	default boolean HandIsRevealed(String player) { return SeatChannel.handIsRevealed(this, player); }

	default boolean RBWearingOneRing() { return game().getGameState().isWearingRing(); }
	default PhysicalCardImpl GetRing() {
		return (PhysicalCardImpl)gameState().getRing(P1());
	}
	default PhysicalCardImpl GetShadowRing() { return (PhysicalCardImpl)gameState().getRing(P2()); }
	/**
	 * @return The Map the first player's deck brought to the table, or null in a format that does not use Maps
	 * (there the Map is not in the game at all).  A Map is in no pile and has no alias: this is the way to it.
	 */
	default PhysicalCardImpl GetFreepsMap() { return (PhysicalCardImpl)gameState().getMap(P1()); }
	/**
	 * @return The Map the second player's deck brought to the table, or null in a format that does not use Maps.
	 */
	default PhysicalCardImpl GetShadowMap() { return (PhysicalCardImpl)gameState().getMap(P2()); }
	default PhysicalCardImpl GetRingBearer() { return (PhysicalCardImpl)gameState().getRingBearer(P1()); }
	default PhysicalCardImpl GetShadowRingBearer() { return (PhysicalCardImpl)gameState().getRingBearer(P2()); }

	default int GetFreepsControlledSiteCount() { return Filters.countActive(game(), Filters.siteControlledByPlayer(P1())); }
	default int GetShadowControlledSiteCount() { return Filters.countActive(game(), Filters.siteControlledByPlayer(P2())); }

	default PhysicalCardImpl GetCurrentSite() { return (PhysicalCardImpl)gameState().getCurrentSite(); }
	default int GetCurrentSiteNumber() { return GetCurrentSite().getSiteNumber(); }

	default PhysicalCardImpl GetFreepsSite(int siteNum) { return GetSite(P1(), siteNum); }
	default PhysicalCardImpl GetShadowSite(int siteNum) { return GetSite(P2(), siteNum); }
	default PhysicalCardImpl GetSite(String playerID, int siteNum)
	{
		PhysicalCardImpl site = (PhysicalCardImpl)gameState().getSite(siteNum);
		if(site != null && site.getOwner().equals(playerID))
			return site;

		List<PhysicalCardImpl> advDeck = (List<PhysicalCardImpl>)gameState().getAdventureDeck(playerID);
		return advDeck.stream().filter(x -> x.getBlueprint().getSiteNumber() == siteNum).findFirst().get();
	}
	default PhysicalCardImpl GetSite(int siteNum) { return (PhysicalCardImpl) gameState().getSite(siteNum);	}
	default PhysicalCardImpl GetFreepsSite(String name) { return GetSiteByName(P1(), name); }
	default PhysicalCardImpl GetShadowSite(String name) { return GetSiteByName(P2(), name); }
	default PhysicalCardImpl GetSiteByName(String player, String name)
	{
		var attempt = GetCard(player, name);
		if(attempt != null)
			return attempt;

		final String lowername = Names.SanitizeName(name);
		List<PhysicalCardImpl> advDeck = (List<PhysicalCardImpl>)gameState().getAdventureDeck(player);
		return advDeck.stream().filter(x -> x.getBlueprint().getSanitizedTitle().contains(lowername)).findFirst().get();
	}
}
