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
import java.util.Map;
import java.util.Set;

/**
 * What one seat at the table is sent by the game.  Inside is a GameCommunicationChannel, the connection the server
 * keeps for each client, which decides what of each thing that happens the player in that seat is told; so what is
 * read here is what the server would send, and not the table's own idea of it.  Three things are kept of what the
 * channel passes on, as it arrives: where the seat was last told each card lies that it has been sent and has not
 * since been sent the removal of (the zone, and the card that bears it), which cards it has been told are in the
 * skirmish being fought, and the game stats it was last sent.  Everything else a client is sent is not asked of
 * the channel at all, so nothing gathers in it however long the game goes on.
 *
 * A table keeps one for each seat only if it says so before its game starts (VirtualTable.KeepSeatChannels).  The
 * readings built on it (GameProperties.HandIsRevealed, PileProperties.PlayerSeesCardInHand,
 * CardProperties.PlayerSeesCardInZone and PlayerSeesCardBorneBy, Skirmishes.PlayerSeesCardInSkirmish) each also
 * join a new channel to the seat as a client joining at that moment is joined, and fail if the two were sent
 * different things.
 *
 * Nothing here is for callers of the table: the class is public only because TableBase names it.
 */
public final class SeatChannel implements GameStateListener {
	private final String _player;
	private final GameCommunicationChannel _channel;
	// Where the seat was last told a card lies: its zone, and the id of the card it was shown on (the one that
	// bears it or has it stacked on it), null for a card shown on no other.
	private record Place(Zone zone, Integer onCardId) { }
	// Cards and not card ids: a card that goes from a hand into play is given a new id on the way.
	private final Map<PhysicalCard, Place> _placesSent = new IdentityHashMap<>();
	// The cards the seat has been told are in the skirmish, kept as a client keeps them: the start of a skirmish
	// and an addition to it each mark their cards, a removal from it or from play unmarks one, its end unmarks all.
	private final Set<PhysicalCard> _cardsInSkirmish = Collections.newSetFromMap(new IdentityHashMap<>());
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
		boolean sent = kept(table, seat).zoneSent(card) == Zone.HAND;
		boolean joining = joinedNow(table, seat).zoneSent(card) == Zone.HAND;
		table.Verify(sent == joining, seat + " has " + (sent ? "" : "not ") + "been sent " + GameUtils.getFullName(card)
				+ " as a card in a hand" + (sent ? " with no removal since" : "")
				+ ", but a client joining that seat now is " + (joining ? "" : "not ") + "sent it as one.");
		return sent;
	}

	/**
	 * The zone the given seat was last told the given card lies in, with no removal of the card sent since; null
	 * if it has the card nowhere before it.  A client joining the seat now must be sent the same.
	 */
	static Zone zoneSeen(TableBase table, String seat, PhysicalCard card) {
		table.Verify(card != null, "No card was given to ask where " + seat + " sees it.");
		if(card == null)
			return null;
		Zone sent = kept(table, seat).zoneSent(card);
		Zone joining = joinedNow(table, seat).zoneSent(card);
		table.Verify(sent == joining, seat + " was last sent " + GameUtils.getFullName(card) + " "
				+ (sent == null ? "nowhere" : "as a card in " + sent) + ", but a client joining that seat now is sent it "
				+ (joining == null ? "nowhere" : "as a card in " + joining) + ".");
		return sent;
	}

	/**
	 * The card the given seat was last told bears the given card, with no removal of the card sent since; null if
	 * it was told of no bearer (or has the card nowhere before it).  A client joining the seat now must be sent
	 * the same.
	 */
	static PhysicalCard bearerSeen(TableBase table, String seat, PhysicalCard card) {
		table.Verify(card != null, "No card was given to ask on which card " + seat + " sees it.");
		if(card == null)
			return null;
		PhysicalCard sent = kept(table, seat).bearerSent(table, card);
		PhysicalCard joining = joinedNow(table, seat).bearerSent(table, card);
		table.Verify(sent == joining, seat + " was last sent " + GameUtils.getFullName(card) + " as borne by "
				+ (sent == null ? "no card" : GameUtils.getFullName(sent)) + ", but a client joining that seat now is"
				+ " sent it as borne by " + (joining == null ? "no card" : GameUtils.getFullName(joining)) + ".");
		return sent;
	}

	/**
	 * Whether the given seat has been told the given card is in the skirmish being fought and has not since been
	 * told otherwise, and a client joining the seat now is told the same.
	 */
	static boolean seesCardInSkirmish(TableBase table, String seat, PhysicalCard card) {
		table.Verify(card != null, "No card was given to ask whether " + seat + " sees it in the skirmish.");
		if(card == null)
			return false;
		boolean sent = kept(table, seat)._cardsInSkirmish.contains(card);
		boolean joining = joinedNow(table, seat)._cardsInSkirmish.contains(card);
		table.Verify(sent == joining, seat + " has " + (sent ? "" : "not ") + "been told " + GameUtils.getFullName(card)
				+ " is in the skirmish, but a client joining that seat now is " + (joining ? "" : "not ") + "told so.");
		return sent;
	}

	private Zone zoneSent(PhysicalCard card) {
		Place place = _placesSent.get(card);
		return place != null ? place.zone() : null;
	}

	private PhysicalCard bearerSent(TableBase table, PhysicalCard card) {
		Place place = _placesSent.get(card);
		if (place == null || place.zone() != Zone.ATTACHED || place.onCardId() == null)
			return null;
		return table.gameState().findCardById(place.onCardId());
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
	 * The things the table reads: each is handed to the server's channel, and what the channel made of it (an
	 * event for this seat, or none) is taken out again at once.
	 */

	@Override
	public void cardCreated(PhysicalCard card, boolean overrideOwnerVisibility, boolean forceVisible) {
		_channel.cardCreated(card, overrideOwnerVisibility, forceVisible);
		keepPlaceSent(card, GameEvent.Type.PUT_CARD_INTO_PLAY);
	}

	@Override
	public void cardMoved(PhysicalCard card) {
		_channel.cardMoved(card);
		keepPlaceSent(card, GameEvent.Type.MOVE_CARD_IN_PLAY);
	}

	private void keepPlaceSent(PhysicalCard card, GameEvent.Type sentAs) {
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() == sentAs)
				_placesSent.put(card, new Place(event.getZone(), event.getTargetCardId()));
		}
	}

	@Override
	public void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards, boolean forceVisible) {
		_channel.cardsRemoved(playerPerforming, cards, forceVisible);
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() != GameEvent.Type.REMOVE_CARD_FROM_PLAY)
				continue;
			for (PhysicalCard card : cards) {
				if (Arrays.stream(event.getOtherCardIds()).anyMatch(removedId -> removedId == card.getCardId())) {
					_placesSent.remove(card);
					_cardsInSkirmish.remove(card);
				}
			}
		}
	}

	@Override
	public void startSkirmish(PhysicalCard fp, Set<PhysicalCard> minions) {
		_channel.startSkirmish(fp, minions);
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() != GameEvent.Type.START_SKIRMISH)
				continue;
			if (fp != null && event.getCardId() != null)
				_cardsInSkirmish.add(fp);
			for (PhysicalCard minion : minions) {
				if (Arrays.stream(event.getOtherCardIds()).anyMatch(minionId -> minionId == minion.getCardId()))
					_cardsInSkirmish.add(minion);
			}
		}
	}

	@Override
	public void addToSkirmish(PhysicalCard card) {
		_channel.addToSkirmish(card);
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() == GameEvent.Type.ADD_TO_SKIRMISH)
				_cardsInSkirmish.add(card);
		}
	}

	@Override
	public void removeFromSkirmish(PhysicalCard card) {
		_channel.removeFromSkirmish(card);
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() == GameEvent.Type.REMOVE_FROM_SKIRMISH)
				_cardsInSkirmish.remove(card);
		}
	}

	@Override
	public void finishSkirmish() {
		_channel.finishSkirmish();
		for (GameEvent event : _channel.consumeGameEvents()) {
			if (event.getType() == GameEvent.Type.END_SKIRMISH)
				_cardsInSkirmish.clear();
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
	public void addTokens(PhysicalCard card, Token token, int count) { }
	@Override
	public void removeTokens(PhysicalCard card, Token token, int count) { }
	@Override
	public void sendMessage(String message) { }
	// Not kept, and so not handed on: a site as it is laid on the adventure path.  The readings of where a card
	// lies are therefore refused for a site on the path (a client joining is sent it there, this seat nothing).
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
