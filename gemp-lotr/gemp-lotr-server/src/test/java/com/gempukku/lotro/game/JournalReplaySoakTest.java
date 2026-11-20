package com.gempukku.lotro.game;

import com.gempukku.lotro.bots.AssignmentLegality;
import com.gempukku.lotro.bots.BotDecisionFallback;
import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.db.DeckSerialization;
import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Plays whole games of the ordinary kind (two decks, bids, starting fellowships, mulligans, turns to the end) with a
 * journal kept, then builds each game again from nothing but its journal and checks that it arrives in the same
 * place.  This is what taking a game back to an earlier decision rests on, so it says three things about it: whether
 * a journal brings a game back exactly, how long that takes, and how big a journal is.
 *
 * The players answer at random, which is the hardest kind of game on a journal: every sort of decision gets every
 * sort of answer.  The games are shorter than people's, so the time that matters is the time for each answer.
 *
 * How many games: -Dgemp.journalSoak=N (2 by default, enough to notice a journal that no longer replays; a
 * measurement wants a few hundred).  Each game's figures are printed.
 */
public class JournalReplaySoakTest {
    private static final String FORMAT = "fotr_block";
    private static final int MOST_ANSWERS = 20000;

    /** One game, with its journal and what is needed to play it. */
    private static class Table implements JournalFollower.Table {
        final DefaultUserFeedback feedback = new DefaultUserFeedback();
        final GameJournal journal = new GameJournal();
        final DefaultLotroGame game;

        /**
         * @param toRepeat The journal of the game this one is to be a repeat of; null for a game of its own.
         */
        Table(Map<String, LotroDeck> decks, GameJournal toRepeat) {
            feedback.addDecisionObserver(journal);
            game = new DefaultLotroGame(LoadedLibraries._formatLibrary.getFormat(FORMAT), decks, feedback, LoadedLibraries._cardLibrary);
            feedback.setGame(game);
            if (toRepeat != null)
                journal.replayRandomFrom(toRepeat);
            game.getGameState().setRandom(journal.random(GameRandom.DEFAULT));
        }

        @Override
        public String playerOf(String key) {
            return key;
        }

        @Override
        public AwaitingDecision awaiting(String player) {
            return feedback.getAwaitingDecision(player);
        }

        @Override
        public void answer(String player, AwaitingDecision decision, String answer) {
            feedback.participantDecided(player);
            try {
                decision.decisionMade(answer);
            } catch (DecisionResultInvalidException exp) {
                feedback.sendAwaitingDecision(player, decision);
                throw new RuntimeException(exp);
            }
            feedback.decisionAccepted(player, decision, answer);
            game.carryOutPendingActionsUntilDecisionNeeded();
        }

        String board() {
            return Boards.of(game);
        }
    }

    private record Played(Map<String, LotroDeck> decks, GameJournal journal, List<String> boardBeforeAnswer,
                          String boardAtEnd, boolean finished, long nanos) {
        int answers() {
            return boardBeforeAnswer.size();
        }
    }

    private static Map<String, LotroDeck> decks(int game) {
        LotroDeck aragorn = DeckSerialization.buildDeckFromContents("Aragorn Starter", Boards.ARAGORN_STARTER, FORMAT, "Aragorn Starter");
        LotroDeck gandalf = DeckSerialization.buildDeckFromContents("Gandalf Starter", Boards.GANDALF_STARTER, FORMAT, "Gandalf Starter");
        Map<String, LotroDeck> decks = new LinkedHashMap<>();
        decks.put("Alice", game % 2 == 0 ? aragorn : gandalf);
        decks.put("Bob", game % 4 < 2 ? gandalf : aragorn);
        return decks;
    }

    private static Played play(Map<String, LotroDeck> decks) throws DecisionResultInvalidException {
        Table table = new Table(decks, null);
        Map<String, BotPlayer> bots = new LinkedHashMap<>();
        for (String player : decks.keySet())
            bots.put(player, new RandomDecisionBot(player));
        AssignmentLegality legality = new AssignmentLegality(table.game);
        List<String> boards = new ArrayList<>();

        long started = System.nanoTime();
        table.game.startGame();
        while (!table.game.isFinished() && boards.size() < MOST_ANSWERS) {
            String asked = null;
            for (String player : decks.keySet())
                if (asked == null && table.awaiting(player) != null)
                    asked = player;
            if (asked == null)
                break;
            AwaitingDecision decision = table.awaiting(asked);
            boards.add(table.board());
            String answer = bots.get(asked).chooseAction(table.game.getGameState(), decision, legality);
            table.feedback.participantDecided(asked);
            String accepted = BotDecisionFallback.decide(table.game, asked, decision, answer);
            table.feedback.decisionAccepted(asked, decision, accepted);
            table.game.carryOutPendingActionsUntilDecisionNeeded();
        }
        return new Played(decks, table.journal, boards, table.board(), table.game.isFinished(), System.nanoTime() - started);
    }

    /**
     * Builds the game again up to the moment its answer of this number (from 0) was waiting to be given; with the
     * number of its answers, up to its end.
     * @return How long that took, in nanoseconds.
     */
    private static long replay(Played played, int answer, List<String> problems) {
        List<GameJournal.Entry> entries = played.journal.entries();
        int cut = entries.size();
        int seen = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).kind() != GameJournal.Kind.ANSWER)
                continue;
            if (seen == answer) {
                cut = i;
                break;
            }
            seen++;
        }
        GameJournal prefix = played.journal.upTo(cut);
        String where = "replay to answer " + answer + " of " + played.answers() + ": ";

        long started = System.nanoTime();
        Table table = new Table(played.decks, prefix);
        table.game.startGame();
        JournalFollower follower = new JournalFollower().follow(prefix, table);
        long nanos = System.nanoTime() - started;

        if (follower.problem() != null)
            problems.add(where + follower.problem());
        else if (table.journal.divergence() != null)
            problems.add(where + table.journal.divergence());
        else if (!table.journal.write().equals(prefix.write()))
            problems.add(where + "the game that was built wrote a different journal" + difference(prefix.write(), table.journal.write()));
        else if (cut < entries.size()) {
            GameJournal.Entry next = entries.get(cut);
            if (!GameJournal.matches(next, table.awaiting(next.player())))
                problems.add(where + "the game is not waiting on the decision that answer was given to");
            else if (!table.board().equals(played.boardBeforeAnswer.get(answer)))
                problems.add(where + "the board is not the board the answer was given on" + difference(played.boardBeforeAnswer.get(answer), table.board()));
        } else if (!table.board().equals(played.boardAtEnd)) {
            problems.add(where + "the board at the end is not the board the game ended on" + difference(played.boardAtEnd, table.board()));
        }
        return nanos;
    }

    /** Where two texts first part company, with a little of each from there. */
    private static String difference(String was, String is) {
        int at = 0;
        while (at < was.length() && at < is.length() && was.charAt(at) == is.charAt(at))
            at++;
        int from = Math.max(0, at - 60);
        return " (at " + at + ": was \"" + was.substring(from, Math.min(was.length(), at + 100)).replace("\n", "\\n").replace("\t", " ")
                + "\", is \"" + is.substring(from, Math.min(is.length(), at + 100)).replace("\n", "\\n").replace("\t", " ") + "\")";
    }

    private static int gzipped(String text) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return bytes.size();
    }

    private static double ms(long nanos) {
        return nanos / 1e6;
    }

    @Test
    public void AGameBuiltAgainFromItsJournalArrivesWhereTheGameWas() throws Exception {
        int games = Integer.getInteger("gemp.journalSoak", 2);
        List<String> problems = new ArrayList<>();
        int answers = 0;
        long fullReplayNanos = 0;
        long worstFullReplay = 0;
        int mostAnswers = 0;
        long journalBytes = 0;
        long journalGzipped = 0;

        //The first game pays for everything the JVM has not done before; it is played and replayed once unmeasured
        Played warmUp = play(decks(0));
        replay(warmUp, warmUp.answers(), new ArrayList<>());

        System.out.println("game  answers  entries  finished  play ms  replay ms: 25%  50%  75%  100%  ms/answer  journal B  gzipped B");
        for (int game = 0; game < games; game++) {
            Played played = play(decks(game));
            int count = played.answers();
            List<String> found = new ArrayList<>();
            long quarter = replay(played, count / 4, found);
            long half = replay(played, count / 2, found);
            long threeQuarters = replay(played, count * 3 / 4, found);
            long whole = replay(played, count, found);
            String text = played.journal.write();
            int raw = text.getBytes(StandardCharsets.UTF_8).length;
            int zipped = gzipped(text);

            System.out.printf("%4d  %7d  %7d  %8s  %7.0f  %14.0f  %3.0f  %3.0f  %4.0f  %9.3f  %9d  %9d%n",
                    game, count, played.journal.entries().size(), played.finished, ms(played.nanos),
                    ms(quarter), ms(half), ms(threeQuarters), ms(whole), count == 0 ? 0 : ms(whole) / count, raw, zipped);
            for (String problem : found)
                problems.add("game " + game + ": " + problem);

            answers += count;
            fullReplayNanos += whole;
            worstFullReplay = Math.max(worstFullReplay, whole);
            mostAnswers = Math.max(mostAnswers, count);
            journalBytes += raw;
            journalGzipped += zipped;
        }
        System.out.printf("%d games, %d answers: a whole game replays in %.0f ms on average (the slowest %.0f ms, the longest game %d answers), "
                        + "%.3f ms an answer; a journal is %d bytes on average, %d gzipped%n",
                games, answers, ms(fullReplayNanos) / Math.max(games, 1), ms(worstFullReplay), mostAnswers,
                answers == 0 ? 0 : ms(fullReplayNanos) / answers, journalBytes / Math.max(games, 1), journalGzipped / Math.max(games, 1));
        for (String problem : problems)
            System.out.println("PROBLEM " + problem);

        assertTrue("No game was played", games == 0 || answers > 0);
        assertEquals(String.join("\n", problems), 0, problems.size());
    }
}
