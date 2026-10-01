package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Every random outcome a game of LOTR-TCG depends on goes through one of these, so that a game which needs its
 * outcomes decided ahead of time (or written down as they happen) can swap in its own.
 *
 * A game uses {@link #DEFAULT} unless told otherwise; see GameState.setRandom.
 */
public interface GameRandom {
    /**
     * Shuffles a player's draw deck in place.
     * @param playerId Whose deck it is.
     * @param deck The deck itself, top card first.  This list is reordered.
     */
    void shuffleDeck(String playerId, List<? extends PhysicalCard> deck);

    /**
     * Picks cards at random out of a pile.
     * @param playerId Whose pile it is.
     * @param zone Which pile it is.
     * @param cards The cards to pick from.  This collection is left alone.
     * @param count How many to pick.  If there are not that many cards, all of them are picked.
     * @return The picked cards, in the order they were picked.
     */
    List<PhysicalCard> pickCards(String playerId, Zone zone, Collection<? extends PhysicalCard> cards, int count);

    /**
     * Puts the players in a random order.  This settles who gets to choose their seat first when bids are tied.
     * @param players The players to reorder.  This list is reordered.
     */
    void shufflePlayers(List<String> players);

    GameRandom DEFAULT = new GameRandom() {
        @Override
        public void shuffleDeck(String playerId, List<? extends PhysicalCard> deck) {
            Collections.shuffle(deck, ThreadLocalRandom.current());
        }

        @Override
        public List<PhysicalCard> pickCards(String playerId, Zone zone, Collection<? extends PhysicalCard> cards, int count) {
            List<PhysicalCard> randomizedCards = new ArrayList<>(cards);
            Collections.shuffle(randomizedCards, ThreadLocalRandom.current());

            return new LinkedList<>(randomizedCards.subList(0, Math.min(count, randomizedCards.size())));
        }

        @Override
        public void shufflePlayers(List<String> players) {
            Collections.shuffle(players, ThreadLocalRandom.current());
        }
    };
}
