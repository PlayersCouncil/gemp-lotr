package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.game.state.PreGameInfo;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameStats;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * What one seat at the table is sent by the game.  Inside is a GameCommunicationChannel, the connection the server
 * keeps for each client, which decides what of each thing that happens the player in that seat is told; so what is
 * read here is what the server would send, and not the table's own idea of it.  Two things are kept of what the
 * channel passes on, as it arrives: which cards the seat has been sent as cards in a hand and has not since been
 * sent the removal of, and the game stats it was last sent.  Everything else a client is sent is not asked of the
 * channel at all, so nothing gathers in it however long the game goes on.
 *
 * A table keeps one for each seat only if it says so before its game starts (VirtualTable.KeepSeatChannels).  The
 * readings built on it (GameProperties.HandIsRevealed, PileProperties.PlayerSeesCardInHand) each also join a new
 * channel to the seat as a client joining at that moment is joined, and fail if the two were sent different things.
 *
 * Nothing here is for callers of the table: the class is public only because TableBase names it.
 */
public final class SeatChannel implements GameStateListener {
	private final String _player;
	private final GameCommunicationChannel _channel;
	// Cards and not card ids: a card that goes from a hand into play is given a new id on the way.
	private final Set<PhysicalCard> _cardsInHands = Collections.newSetFromMap(new IdentityHashMap<>());
	private GameStats _gameStats;

	SeatChannel(String player, LotroFormat format) {
		_player = player;
		// Not a live connection: one of those takes the place of any other under the same player's name.
		_channel = new GameCommunicationChannel(player, 0, false, format);
	}

	/**
	 * Whether the game has told every client that the given player's hand is revealed: the two seats that have been
	 * at the table from the start, and a client joining either seat now.
	 * @param player Whose hand is asked about.
	 */
	static boolean handIsRevealed(TableBase table, String player) {
		boolean freeps = seatSaysHandIsRevealed(table, table.P1(), player);
		boolean shadow = seatSaysHandIsRevealed(table, table.P2(), player);
		table.Verify(freeps == shadow, "The game stats sent to " + table.P1() + " say the hand of " + player + " is "
				+ (freeps ? "" : "not ") + "revealed, and those sent to " + table.P2() + " say it is "
				+ (shadow ? "" : "not ") + "revealed.");
		return freeps;
	}

	private static boolean seatSaysHandIsRevealed(TableBase table, String seat, String player) {
		boolean sent = kept(table, seat).saysHandIsRevealed(player);
		boolean joining = joinedNow(table, seat).saysHandIsRevealed(player);
		table.Verify(sent == joining, "The game stats last sent to " + seat + " say the hand of " + player + " is "
				+ (sent ? "" : "not ") + "revealed, but a client joining that seat now is told it is "
				+ (joining ? "" : "not ") + "revealed.");
		return sent;
	}

	/**
	 * Whether the given seat has been sent the given card as a card in a hand (its own or another player's) and
	 * has not since been sent that card's removal, and a client joining the seat now is sent the same.
	 */
	static boolean seesCardInHand(TableBase table, String seat, PhysicalCard card) {
		table.Verify(card != null, "No card was given to ask whether " + seat + " sees it in a hand.");
		if(card == null)
			return false;
		boolean sent = kept(table, seat)._cardsInHands.contains(card);
		boolean joining = joinedNow(table, seat)._cardsInHands.contains(card);
		table.Verify(sent == joining, seat + " has " + (sent ? "" : "not ") + "been sent " + GameUtils.getFullName(card)
				+ " as a card in a hand" + (sent ? " with no removal since" : "")
				+ ", but a client joining that seat now is " + (joining ? "" : "not ") + "sent it as one.");
		return sent;
	}

	private static SeatChannel kept(TableBase table, String seat) {
		SeatChannel channel = table.seatChannel(seat);
		table.Verify(channel != null, "This table keeps no channel for " + seat
				+ ", so what that seat has been sent cannot be read (see VirtualTable.KeepSeatChannels).");
		// Only when Verify does not throw: a channel that was sent nothing answers no to everything.
		return channel != null ? channel : new SeatChannel(seat, table.game().getFormat());
	}

	/**
	 * A channel joined to the game under the seat's name as a client's is, sent the whole state as the game stands,
	 * and taken off again.  Only ever reached on a table that keeps seat channels, which has no person connected.
	 */
	private static SeatChannel joinedNow(TableBase table, String seat) {
		SeatChannel channel = new SeatChannel(seat, table.game().getFormat());
		table.game().addGameStateListener(seat, channel);
		table.game().removeGameStateListener(channel);
		return channel;
	}

	private boolean saysHandIsRevealed(String player) {
		return _gameStats != null && _gameStats.getRevealedHands().contains(player);
	}

	/*
	 * The three things the table reads: each is handed to the server's channel, and what the channel made of it
	 * (an event for this seat, or none) is taken out again at once.
	 */

	@Override
	public void cardCreated(PhysicalCard card, boolean overrideOwnerVisibility, boolean forceVisible) {
		_channel.cardCreated(card, overrideOwnerVisibility, forceVisible);
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY && event.getZone() == Zone.HAND)
				_cardsInHands.add(card);
		}
	}

	@Override
	public void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards, boolean forceVisible) {
		_channel.cardsRemoved(playerPerforming, cards, forceVisible);
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() != GameEvent.Type.REMOVE_CARD_FROM_PLAY)
				continue;
			for (PhysicalCard card : cards) {
				if (Arrays.stream(event.getOtherCardIds()).anyMatch(removedId -> removedId == card.getCardId()))
					_cardsInHands.remove(card);
			}
		}
	}

	@Override
	public void sendGameStats(GameStats gameStats) {
		_channel.sendGameStats(gameStats);
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() == GameEvent.Type.GAME_STATS)
				_gameStats = event.getGameStats();
		}
	}

	@Override
	public boolean isLiveConnection() { return false; }
	@Override
	public String getAssignedPlayerId() { return _player; }

	/*
	 * The rest of what a client is sent, of which the table reads nothing.
	 */

	@Override
	public void cardMoved(PhysicalCard card) { }
	@Override
	public void cardsFlipped(Collection<PhysicalCard> cards, boolean down) { }
	@Override
	public void initializeBoard(List<String> playerIds, boolean discardIsPublic) { }
	@Override
	public void initializePregameBoard(PreGameInfo preGameInfo) { }
	@Override
	public void setPlayerPosition(String playerId, int i) { }
	@Override
	public void setTwilight(int twilightPool) { }
	@Override
	public void setCurrentPlayerId(String playerId, Set<PhysicalCard> inactiveCards) { }
	@Override
	public void setCurrentPhase(String currentPhase) { }
	@Override
	public void addAssignment(PhysicalCard fp, Set<PhysicalCard> minions) { }
	@Override
	public void addPendingAssignment(PhysicalCard fp, PhysicalCard minion) { }
	@Override
	public void removeAssignment(PhysicalCard fp) { }
	@Override
	public void removePendingAssignment(PhysicalCard freePeople, PhysicalCard minion) { }
	@Override
	public void startSkirmish(PhysicalCard fp, Set<PhysicalCard> minions) { }
	@Override
	public void addToSkirmish(PhysicalCard card) { }
	@Override
	public void removeFromSkirmish(PhysicalCard card) { }
	@Override
	public void finishSkirmish() { }
	@Override
	public void addTokens(PhysicalCard card, Token token, int count) { }
	@Override
	public void removeTokens(PhysicalCard card, Token token, int count) { }
	@Override
	public void sendMessage(String message) { }
	@Override
	public void setSite(PhysicalCard card) { }
	@Override
	public void cardAffectedByCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCard) { }
	@Override
	public void eventPlayed(PhysicalCard card) { }
	@Override
	public void cardActivated(String playerPerforming, PhysicalCard card) { }
	@Override
	public void decisionRequired(String playerId, AwaitingDecision awaitingDecision) { }
	@Override
	public void sendWarning(String playerId, String warning) { }
	@Override
	public void endGame() { }
}
