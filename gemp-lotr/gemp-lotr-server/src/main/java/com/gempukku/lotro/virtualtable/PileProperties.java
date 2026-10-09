package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;

import java.util.List;

/**
 * These functions will help you determine pile counts or add/remove a card to a pile at a particular point.
 */
public interface PileProperties extends TableBase {
	/**
	 * @return Gets the current number of cards in the Free Peoples player's hand.
	 */
	default int GetFreepsHandCount() { return GetFreepsHand().size(); }
	/**
	 * @return Gets the current number of cards in the Shadow player's hand.
	 */
	default int GetShadowHandCount() { return GetShadowHand().size(); }

	/**
	 * @return Gets all the cards currently in the Free Peoples player's hand.
	 */
	default List<? extends PhysicalCard> GetFreepsHand() { return GetHand(P1()); }
	/**
	 * @return Gets all the cards currently in the Shadow player's hand.
	 */
	default List<? extends PhysicalCard> GetShadowHand() { return GetHand(P2()); }
	/**
	 *
	 * @param player The player whose hand you are interested in.
	 * @return Gets all the cards currently in the given player's hand.
	 */
	default List<? extends PhysicalCard> GetHand(String player)
	{
		return gameState().getHand(player);
	}

	/**
	 * Whether the Free Peoples player's client has the given card before it as a card in a hand: his own hand, or
	 * another player's that is revealed to him.  See PlayerSeesCardInHand.
	 */
	default boolean FreepsSeesCardInHand(PhysicalCardImpl card) { return PlayerSeesCardInHand(P1(), card); }
	/**
	 * Whether the Shadow player's client has the given card before it as a card in a hand: his own hand, or
	 * another player's that is revealed to him.  See PlayerSeesCardInHand.
	 */
	default boolean ShadowSeesCardInHand(PhysicalCardImpl card) { return PlayerSeesCardInHand(P2(), card); }
	/**
	 * Whether the given player's client has the given card before it as a card in a hand: true when that seat has
	 * been sent the card as a card in a hand (the server's own channel for the seat decides what it is sent) and
	 * has not since been sent the card's removal.  A player is always sent the cards of his own hand; he is sent
	 * another player's only while that hand is revealed to him ("You must play with your hand revealed"), each
	 * card as it arrives there, and its removal as it leaves, played or discarded or put back on a deck.  Whose
	 * hand the clients are told is revealed is another reading: FreepsHandIsRevealed / ShadowHandIsRevealed.
	 *
	 * The table reads this from a channel it keeps for the seat from before the game's first step, and also joins
	 * a client to the seat as the reading is made, which is sent the game as it stands; it fails if the one has
	 * the card as a card in a hand and the other has not.  So one reading shows both what a client at the table
	 * is sent as things happen and what a client who joins or refreshes is sent.  It fails as well on a table
	 * that keeps no such channels (the unit test table keeps them).
	 * @param player The player whose seat is asked about.
	 * @param card The card, wherever it lies and whoever owns it.
	 * @return True if that seat has the card before it as a card in a hand.
	 */
	default boolean PlayerSeesCardInHand(String player, PhysicalCardImpl card) {
		return SeatChannel.seesCardInHand(this, player, card);
	}

	/**
	 * @return Gets the number of cards in the Free Peoples player's Draw Deck.
	 */
	default int GetFreepsDeckCount() { return GetFreepsDrawDeck().size(); }
	/**
	 * @return Gets the number of cards in the Shadow player's Draw Deck.
	 */
	default int GetShadowDeckCount() { return GetShadowDrawDeck().size(); }

	/**
	 * @return How many times the Free Peoples player's draw deck has been shuffled so far this game.  Read it before
	 * and after an action to see whether the action shuffled the deck.
	 */
	default int GetFreepsDeckShuffleCount() { return GetDeckShuffleCount(P1()); }
	/**
	 * @return How many times the Shadow player's draw deck has been shuffled so far this game.  Read it before and
	 * after an action to see whether the action shuffled the deck.
	 */
	default int GetShadowDeckShuffleCount() { return GetDeckShuffleCount(P2()); }
	/**
	 * @param player The player whose draw deck is asked about.
	 * @return How many times that player's draw deck has been shuffled so far this game, the shuffle before the
	 * opening hand included.
	 */
	default int GetDeckShuffleCount(String player) { return gameState().getDeckShuffleCount(player); }

	/**
	 * @return Gets all the cards in the Free Peoples player's DRaw Deck.
	 */
	default List<? extends PhysicalCard> GetFreepsDrawDeck() { return GetDrawDeck(P1()); }
	/**
	 * @return Gets all the cards in the Shadow player's Draw Deck.
	 */
	default List<? extends PhysicalCard> GetShadowDrawDeck() { return GetDrawDeck(P2()); }

	/**
	 * @param player The player whose draw deck you are interested in.
	 * @return Gets all the cards in the given player's Draw Deck.
	 */
	default List<? extends PhysicalCard> GetDrawDeck(String player)
	{
		return gameState().getDeck(player);
	}

	default PhysicalCardImpl GetFreepsBottomOfDeck() { return GetPlayerBottomOfDeck(P1()); }
	default PhysicalCardImpl GetShadowBottomOfDeck() { return GetPlayerBottomOfDeck(P2()); }
	default PhysicalCardImpl GetFromBottomOfFreepsDeck(int index) { return GetFromBottomOfPlayerDeck(P1(), index); }
	default PhysicalCardImpl GetFromBottomOfShadowDeck(int index) { return GetFromBottomOfPlayerDeck(P2(), index); }
	default PhysicalCardImpl GetPlayerBottomOfDeck(String player) { return GetFromBottomOfPlayerDeck(player, 1); }
	default PhysicalCardImpl GetFromBottomOfPlayerDeck(String player, int index)
	{
		var deck = gameState().getDeck(player);
		return (PhysicalCardImpl) deck.get(deck.size() - index);
	}

	default PhysicalCardImpl GetFreepsTopOfDeck() { return GetPlayerTopOfDeck(P1()); }
	default PhysicalCardImpl GetShadowTopOfDeck() { return GetPlayerTopOfDeck(P2()); }
	default PhysicalCardImpl GetFromTopOfFreepsDeck(int index) { return GetFromTopOfPlayerDeck(P1(), index); }
	default PhysicalCardImpl GetFromTopOfShadowDeck(int index) { return GetFromTopOfPlayerDeck(P2(), index); }
	default PhysicalCardImpl GetPlayerTopOfDeck(String player) { return GetFromTopOfPlayerDeck(player, 1); }

	/**
	 * Index is 1-based (1 is first, 2 is second, etc)
	 */
	default PhysicalCardImpl GetFromTopOfPlayerDeck(String player, int index)
	{
		var deck = gameState().getDeck(player);
		if(deck.isEmpty())
			return null;

		return (PhysicalCardImpl) deck.get(index - 1);
	}
	default int GetFreepsDiscardCount() { return GetPlayerDiscardCount(P1()); }
	default int GetShadowDiscardCount() { return GetPlayerDiscardCount(P2()); }
	default int GetPlayerDiscardCount(String player) { return gameState().getDiscard(player).size(); }

	default int GetFreepsDeadCount() { return gameState().getDeadPile(P1()).size(); }
}
