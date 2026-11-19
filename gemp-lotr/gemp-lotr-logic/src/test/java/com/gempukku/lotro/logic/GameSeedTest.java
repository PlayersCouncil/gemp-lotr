package com.gempukku.lotro.logic;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.GameRandom;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class GameSeedTest {
    /** A 60-card "deck" of placeholder cards, shuffled as a game shuffles a draw deck: their ids in the order dealt. */
    private static List<Integer> shuffled(GameRandom random) {
        List<PhysicalCard> deck = new ArrayList<>(IntStream.range(0, 60).mapToObj(GameSeedTest::card).toList());
        random.shuffleDeck("alice", deck);
        return ids(deck);
    }

    @Test
    public void withoutASeedEveryGameUsesTheDefault() {
        withSeed(null, () -> assertSame(GameRandom.DEFAULT, GameSeed.newGameRandom()));
    }

    @Test
    public void theSameSeedGivesEveryGameTheSameDraws() {
        GameRandom first = GameSeed.seeded(42L);
        GameRandom second = GameSeed.seeded(42L);
        assertEquals(shuffled(first), shuffled(second));
        // and keeps doing so, draw after draw, whatever is drawn
        assertEquals(shuffled(first), shuffled(second));
        List<PhysicalCard> hand = IntStream.range(0, 8).mapToObj(GameSeedTest::card).toList();
        assertEquals(ids(first.pickCards("alice", Zone.HAND, hand, 2)), ids(second.pickCards("alice", Zone.HAND, hand, 2)));
        List<String> a = new ArrayList<>(List.of("alice", "bob", "carol"));
        List<String> b = new ArrayList<>(a);
        first.shufflePlayers(a);
        second.shufflePlayers(b);
        assertEquals(a, b);
    }

    @Test
    public void eachGameHasItsOwnSequence() {
        GameRandom first = GameSeed.seeded(42L);
        GameRandom second = GameSeed.seeded(42L);
        shuffled(first);
        // a draw in one game doesn't move the other one on
        assertEquals(shuffled(GameSeed.seeded(42L)), shuffled(second));
    }

    @Test
    public void theSeedComesFromTheSystemProperty() {
        withSeed("7", () -> assertEquals(shuffled(GameSeed.seeded(7L)), shuffled(GameSeed.newGameRandom())));
    }

    private static PhysicalCard card(int id) {
        return (PhysicalCard) Proxy.newProxyInstance(PhysicalCard.class.getClassLoader(), new Class<?>[]{PhysicalCard.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getCardId" -> id;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "card " + id;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static List<Integer> ids(List<PhysicalCard> cards) {
        return cards.stream().map(PhysicalCard::getCardId).toList();
    }

    private static void withSeed(String seed, Runnable test) {
        String before = System.getProperty(GameSeed.SEED_PROPERTY);
        try {
            if (seed == null)
                System.clearProperty(GameSeed.SEED_PROPERTY);
            else
                System.setProperty(GameSeed.SEED_PROPERTY, seed);
            test.run();
        } finally {
            if (before == null)
                System.clearProperty(GameSeed.SEED_PROPERTY);
            else
                System.setProperty(GameSeed.SEED_PROPERTY, before);
        }
    }

    @Test
    public void aSeededServerListsTheCardsOfASetByCardId() {
        Set<PhysicalCard> set = new HashSet<>(List.of(card(147), card(9), card(143), card(31)));
        withSeed("1", () -> assertEquals(List.of(9, 31, 143, 147), ids(GameSeed.stableOrder(set))));
    }

    @Test
    public void aListKeepsItsOrderEvenOnASeededServer() {
        List<PhysicalCard> list = List.of(card(147), card(9), card(143));
        withSeed("1", () -> assertEquals(List.of(147, 9, 143), ids(GameSeed.stableOrder(list))));
    }

    @Test
    public void withoutASeedASetKeepsItsOwnOrder() {
        Set<PhysicalCard> set = new HashSet<>(List.of(card(147), card(9), card(143), card(31)));
        List<Integer> asTheSetHasThem = set.stream().map(PhysicalCard::getCardId).toList();
        withSeed(null, () -> assertEquals(asTheSetHasThem, ids(GameSeed.stableOrder(set))));
    }

    private static Action action(PhysicalCard card, String text) {
        return (Action) Proxy.newProxyInstance(Action.class.getClassLoader(), new Class<?>[]{Action.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getActionAttachedToCard" -> card;
                    case "getText" -> text;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> text;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    public void aSeededServerListsActionsByTheirCardThenTheirText() {
        PhysicalCard axe = card(49);
        PhysicalCard condition = card(50);
        List<Action> actions = List.of(action(condition, "Discard the Orc"), action(null, "Resolve skirmish damage"),
                action(axe, "Discard the top card"), action(axe, "A second action"));
        withSeed("1", () -> assertEquals(
                List.of("Resolve skirmish damage", "A second action", "Discard the top card", "Discard the Orc"),
                GameSeed.stableActionOrder(null, actions).stream().map(a -> a.getText(null)).toList()));
        withSeed(null, () -> assertSame(actions, GameSeed.stableActionOrder(null, actions)));
    }
}
