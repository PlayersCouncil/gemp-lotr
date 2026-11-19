package com.gempukku.lotro.game;

import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.game.state.GameExtraInfo;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Players joining a game at the same moment (the game page's first request, GET /game/{id}) must all get a channel of
 * their own. signupUserForGame changes the game's listeners and the channel counter: under a shared read lock two
 * joins ran at once, and one failed with a ConcurrentModificationException (HTTP 500), seen by the game check.
 */
public class LotroGameMediatorSignupTest {
    private static final LotroCardBlueprintLibrary LIBRARY = new LotroCardBlueprintLibrary();
    private static final LotroFormatLibrary FORMATS = new LotroFormatLibrary(new DefaultAdventureLibrary(), LIBRARY);

    private static LotroDeck deck() {
        LotroDeck deck = new LotroDeck("Test deck");
        deck.setNotes("");
        deck.setRingBearer("1_290");
        deck.setRing("1_2");
        for (String site : List.of("1_319", "1_327", "1_337", "1_343", "1_349", "1_350", "1_353", "1_356", "1_360"))
            deck.addSite(site);
        for (String card : List.of("1_7", "1_53", "1_178", "1_181", "1_185", "1_191"))
            for (int i = 0; i < 10; i++)
                deck.addCard(card);
        return deck;
    }

    private static LotroGameMediator startedGame() {
        LotroGameParticipant[] participants = {
                new LotroGameParticipant("alice", deck()),
                new LotroGameParticipant("bob", deck()),
        };
        LotroGameMediator mediator = new LotroGameMediator("1", FORMATS.getFormat("fotr_block"), participants, LIBRARY,
                GameTimer.DEFAULT_TIMER, true, true, false, "Test", new MarkdownParser(), null, false,
                new GameExtraInfo(), null);
        mediator.startGame();
        return mediator;
    }

    private static Player player(int id, String name) {
        return new Player(id, name, "pass", "u", null, null, null, null, false);
    }

    /** Records the channel number it is given. */
    private static ParticipantCommunicationVisitor visitor(List<Integer> channels) {
        return new ParticipantCommunicationVisitor() {
            @Override
            public void visitChannelNumber(int channelNumber) {
                channels.add(channelNumber);
            }

            @Override
            public void visitClock(Map<String, Integer> secondsLeft) {
            }

            @Override
            public void visitGameEvent(GameEvent gameEvent) {
            }
        };
    }

    @Test
    public void playersJoiningAtTheSameMomentEachGetAChannelOfTheirOwn() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            for (int round = 0; round < 100; round++) {
                LotroGameMediator mediator = startedGame();
                List<Integer> channels = Collections.synchronizedList(new ArrayList<>());
                CyclicBarrier together = new CyclicBarrier(8);
                List<Future<?>> joins = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    // The two players and six spectators (the game allows spectators)
                    Player player = player(i, i == 0 ? "alice" : i == 1 ? "bob" : "watcher" + i);
                    joins.add(pool.submit(() -> {
                        together.await();
                        mediator.signupUserForGame(player, visitor(channels));
                        return null;
                    }));
                }
                for (Future<?> join : joins)
                    join.get(30, TimeUnit.SECONDS);
                assertEquals("round " + round + ": " + channels, 8, new HashSet<>(channels).size());
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
