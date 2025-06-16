package com.gempukku.lotro.framework;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;

import java.util.List;

/**
 * These functions will help you determine pile counts or add/remove a card to a pile at a particular point.
 */
public interface PileProperties extends TestBase{
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
	default List<? extends PhysicalCard> GetFreepsHand() { return GetHand(P1); }
	/**
	 * @return Gets all the cards currently in the Shadow player's hand.
	 */
	default List<? extends PhysicalCard> GetShadowHand() { return GetHand(P2); }
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
	 * @return Gets the number of cards in the Free Peoples player's Draw Deck.
	 */
	default int GetFreepsDeckCount() { return GetFreepsDrawDeck().size(); }
	/**
	 * @return Gets the number of cards in the Shadow player's Draw Deck.
	 */
	default int GetShadowDeckCount() { return GetShadowDrawDeck().size(); }

	/**
	 * @return Gets all the cards in the Free Peoples player's DRaw Deck.
	 */
	default List<? extends PhysicalCard> GetFreepsDrawDeck() { return GetDrawDeck(P1); }
	/**
	 * @return Gets all the cards in the Shadow player's Draw Deck.
	 */
	default List<? extends PhysicalCard> GetShadowDrawDeck() { return GetDrawDeck(P2); }

	/**
	 * @param player The player whose draw deck you are interested in.
	 * @return Gets all the cards in the given player's Draw Deck.
	 */
	default List<? extends PhysicalCard> GetDrawDeck(String player)
	{
		return gameState().getDeck(player);
	}

	default PhysicalCardImpl GetFreepsBottomOfDeck() { return GetPlayerBottomOfDeck(P1); }
	default PhysicalCardImpl GetShadowBottomOfDeck() { return GetPlayerBottomOfDeck(P2); }
	default PhysicalCardImpl GetFromBottomOfFreepsDeck(int index) { return GetFromBottomOfPlayerDeck(P1, index); }
	default PhysicalCardImpl GetFromBottomOfShadowDeck(int index) { return GetFromBottomOfPlayerDeck(P2, index); }
	default PhysicalCardImpl GetPlayerBottomOfDeck(String player) { return GetFromBottomOfPlayerDeck(player, 1); }
	default PhysicalCardImpl GetFromBottomOfPlayerDeck(String player, int index)
	{
		var deck = gameState().getDeck(player);
		return (PhysicalCardImpl) deck.get(deck.size() - index);
	}

	default PhysicalCardImpl GetFreepsTopOfDeck() { return GetPlayerTopOfDeck(P1); }
	default PhysicalCardImpl GetShadowTopOfDeck() { return GetPlayerTopOfDeck(P2); }
	default PhysicalCardImpl GetFromTopOfFreepsDeck(int index) { return GetFromTopOfPlayerDeck(P1, index); }
	default PhysicalCardImpl GetFromTopOfShadowDeck(int index) { return GetFromTopOfPlayerDeck(P2, index); }
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
	default int GetFreepsDiscardCount() { return GetPlayerDiscardCount(P1); }
	default int GetShadowDiscardCount() { return GetPlayerDiscardCount(P2); }
	default int GetPlayerDiscardCount(String player) { return gameState().getDiscard(player).size(); }

	default int GetFreepsDeadCount() { return gameState().getDeadPile(P1).size(); }
}
