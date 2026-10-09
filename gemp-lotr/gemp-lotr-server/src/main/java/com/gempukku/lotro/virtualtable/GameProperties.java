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
	default int GetBurdens() { return gameState().getBurdens(); }
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
	 * @return Gets the player who is currently making a decision.
	 */
	default String GetDecidingPlayer() { return userFeedback().getUsersPendingDecision().stream().findFirst().get(); }

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
