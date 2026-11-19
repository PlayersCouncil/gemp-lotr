package com.gempukku.lotro.logic;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.GameRandom;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.*;

/**
 * A fixed seed for every game's random draws, for test servers only.
 * <p>
 * Normally a game draws from {@link GameRandom#DEFAULT}. When the JVM is started with
 * {@code -Dgemp.game.randomSeed=<number>}, every game gets its own {@link GameRandom} drawing from a {@link Random}
 * with that seed, so the same moves give the same game (the game screen's visual check plays the same game on two
 * pages).
 */
public final class GameSeed {
    public static final String SEED_PROPERTY = "gemp.game.randomSeed";

    private GameSeed() {
    }

    /**
     * True when the JVM was started with a seed: then whatever else would make two games differ, such as the order of
     * a table's players, is fixed too.
     */
    public static boolean isSeeded() {
        return Long.getLong(SEED_PROPERTY) != null;
    }

    /** A new game's random draws: seeded on a seeded server (each game from the same seed), the default otherwise. */
    public static GameRandom newGameRandom() {
        Long seed = Long.getLong(SEED_PROPERTY);
        return seed == null ? GameRandom.DEFAULT : seeded(seed);
    }

    /** Random draws from a {@link Random} with this seed, as {@link GameRandom#DEFAULT} draws from its own. */
    public static GameRandom seeded(long seed) {
        Random random = new Random(seed);
        return new GameRandom() {
            @Override
            public void shuffleDeck(String playerId, List<? extends PhysicalCard> deck) {
                Collections.shuffle(deck, random);
            }

            @Override
            public List<PhysicalCard> pickCards(String playerId, Zone zone, Collection<? extends PhysicalCard> cards, int count) {
                List<PhysicalCard> randomizedCards = new ArrayList<>(cards);
                Collections.shuffle(randomizedCards, random);
                return new LinkedList<>(randomizedCards.subList(0, Math.min(count, randomizedCards.size())));
            }

            @Override
            public void shufflePlayers(List<String> players) {
                Collections.shuffle(players, random);
            }
        };
    }

    /**
     * The actions of a decision in the order it lists them: as given, or on a seeded server by their card's id and then
     * their text, since the given order can follow object hashes (e.g. several required responses at once).
     */
    public static <T extends Action> List<T> stableActionOrder(LotroGame game, List<T> actions) {
        if (!isSeeded())
            return actions;
        List<T> result = new ArrayList<>(actions);
        result.sort(Comparator
                .comparingInt((T action) -> action.getActionAttachedToCard() != null ? action.getActionAttachedToCard().getCardId() : -1)
                .thenComparing(action -> String.valueOf(action.getText(game))));
        return result;
    }

    /**
     * The cards of a decision in the order it lists them. A list keeps its order; a set's order follows object hashes,
     * so on a seeded server it is put in card id order instead (elsewhere it stays as it was).
     */
    public static <T extends PhysicalCard> List<T> stableOrder(Collection<T> cards) {
        List<T> result = new LinkedList<>(cards);
        if (isSeeded() && !(cards instanceof List))
            result.sort(Comparator.comparingInt(PhysicalCard::getCardId));
        return result;
    }
}
