package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Everything that steered a game, written down in the order it happened: every answer a player gave, and every
 * random outcome.  Nothing else decides how a game goes, so a game started the same way and fed the same journal
 * arrives in exactly the same place.  That is what makes it possible to take a game back to an earlier point, to
 * pick one up after it was lost, and to re-run a reported bug with a debugger attached.  (The engine itself cannot
 * save or restore a game.)
 *
 * To keep a journal of a game: add the journal to the game's DefaultUserFeedback as a decision observer, and give
 * the game journal.random(whatever random it would have used) as its random.
 *
 * To follow an earlier journal: call replayRandomFrom(earlier) before the game starts, so that its random outcomes
 * come out as they did before, then give the game each of earlier.answers() in turn, checking each against the
 * decision it is for with matches().  The new journal fills up as the game is replayed, so at the end it is a
 * complete journal in its own right.
 *
 * Players are written down by whatever keyOf says, so that a journal does not have to be replayed by the same people
 * (a scenario writes "freeps" and "shadow").  Cards are written down by card ID.  Those are handed out in a fixed
 * order as a game goes, so the same game gives the same card the same ID.
 */
public class GameJournal implements DecisionObserver {
    public enum Kind {
        /** A player's answer to a decision.  detail is the decision's fingerprint; value the answer. */
        ANSWER("A"),
        /** A deck was shuffled.  value is the card IDs in their new order, top first. */
        SHUFFLE("S"),
        /** Cards were picked at random.  detail is the zone they were picked from; value the IDs picked, in order. */
        PICK("P"),
        /** The players were put in a random order.  value is that order. */
        ORDER("O"),
        /** A point somebody wanted to be able to find again.  value is its name.  Has no effect on a replay. */
        MARK("M");

        private final String code;

        Kind(String code) {
            this.code = code;
        }

        static Kind of(String code) {
            for (Kind kind : values())
                if (kind.code.equals(code))
                    return kind;
            throw new IllegalArgumentException("'" + code + "' is not a kind of journal entry");
        }
    }

    /**
     * @param player The key of the player concerned; "-" if none is.
     */
    public record Entry(Kind kind, String player, String detail, String value) {
        boolean random() {
            return kind == Kind.SHUFFLE || kind == Kind.PICK || kind == Kind.ORDER;
        }
    }

    /** Turns a player's name into what the journal calls them, and back. */
    public interface PlayerKeys {
        String keyOf(String player);

        PlayerKeys NAMES = player -> player;
    }

    private final PlayerKeys _keys;
    private final List<Entry> _entries = new ArrayList<>();
    /** Random outcomes of an earlier game that this one is to repeat, soonest first. */
    private final LinkedList<Entry> _toRepeat = new LinkedList<>();
    private String _divergence;

    public GameJournal() {
        this(PlayerKeys.NAMES);
    }

    public GameJournal(PlayerKeys keys) {
        _keys = keys;
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(_entries);
    }

    /** The answers, in the order they were given. */
    public List<Entry> answers() {
        return _entries.stream().filter(entry -> entry.kind() == Kind.ANSWER).toList();
    }

    /**
     * Notes a point in the game by name, to be found again with upTo.
     */
    public void mark(String name) {
        _entries.add(new Entry(Kind.MARK, "-", "", name));
    }

    /**
     * @return Whether the journal has a mark of this name.
     */
    public boolean hasMark(String name) {
        return _entries.stream().anyMatch(entry -> entry.kind() == Kind.MARK && entry.value().equals(name));
    }

    /**
     * @return A journal of everything up to the last mark of this name: replaying it leaves a game at the point
     * where the mark was made.  Null if there is no such mark.
     */
    public GameJournal upTo(String mark) {
        int last = -1;
        for (int i = 0; i < _entries.size(); i++)
            if (_entries.get(i).kind() == Kind.MARK && _entries.get(i).value().equals(mark))
                last = i;
        if (last < 0)
            return null;
        GameJournal result = new GameJournal(_keys);
        result._entries.addAll(_entries.subList(0, last));
        return result;
    }

    /**
     * Has this game's random outcomes come out as an earlier game's did, for as long as that game's journal lasts
     * and this game keeps asking for the same ones.  After that, chance takes over again.
     */
    public void replayRandomFrom(GameJournal earlier) {
        _toRepeat.clear();
        for (Entry entry : earlier._entries)
            if (entry.random())
                _toRepeat.add(entry);
    }

    /**
     * @return Why this game stopped following the earlier one's random outcomes, if it did before they ran out.
     */
    public String divergence() {
        return _divergence;
    }

    /* ---- answers ---- */

    @Override
    public void decisionIssued(String playerId, AwaitingDecision decision) { }

    @Override
    public void decisionAnswered(String playerId, AwaitingDecision decision, String answer) {
        _entries.add(new Entry(Kind.ANSWER, _keys.keyOf(playerId), fingerprint(decision), answer == null ? "" : answer));
    }

    /**
     * @return Whether a decision is the one an answer in the journal was given to.
     */
    public static boolean matches(Entry answer, AwaitingDecision decision) {
        return answer.kind() == Kind.ANSWER && decision != null && answer.detail().equals(fingerprint(decision));
    }

    /**
     * A short stand-in for everything a decision asks: what kind it is, its text, and every option it offers.  Two
     * decisions with the same fingerprint are, for all practical purposes, the same question.  The kind is left
     * readable because it is the first thing anybody wants to know when two do not match.
     */
    public static String fingerprint(AwaitingDecision decision) {
        StringBuilder all = new StringBuilder(String.valueOf(decision.getText()));
        for (Map.Entry<String, String[]> parameter : new TreeMap<>(decision.getDecisionParameters()).entrySet())
            all.append('\u0001').append(parameter.getKey()).append('=').append(String.join("\u0002", parameter.getValue()));
        return decision.getDecisionType().name() + ":" + Integer.toHexString(all.toString().hashCode());
    }

    /* ---- random outcomes ---- */

    /**
     * @param chance Where the game's random outcomes would otherwise come from.  It is still asked every time, so
     *               that anything it keeps count of stays in step; its outcome is overridden only while an earlier
     *               journal is being followed.
     * @return The random to give the game.
     */
    public GameRandom random(GameRandom chance) {
        return new GameRandom() {
            @Override
            public void shuffleDeck(String playerId, List<? extends PhysicalCard> deck) {
                chance.shuffleDeck(playerId, deck);
                String player = _keys.keyOf(playerId);
                Entry earlier = repeat(Kind.SHUFFLE, player, "");
                if (earlier != null) {
                    List<PhysicalCard> ordered = inOrder(earlier.value(), deck);
                    if (ordered != null && ordered.size() == deck.size())
                        reorder(deck, ordered);
                    else
                        diverged("a shuffle of " + player + "'s deck, which no longer holds the same cards");
                }
                _entries.add(new Entry(Kind.SHUFFLE, player, "", ids(deck)));
            }

            @Override
            public List<PhysicalCard> pickCards(String playerId, Zone zone, Collection<? extends PhysicalCard> cards, int count) {
                List<PhysicalCard> picked = chance.pickCards(playerId, zone, cards, count);
                String player = _keys.keyOf(playerId);
                Entry earlier = repeat(Kind.PICK, player, zone.name());
                if (earlier != null) {
                    List<PhysicalCard> ordered = inOrder(earlier.value(), cards);
                    if (ordered != null && ordered.size() == picked.size())
                        picked = new LinkedList<>(ordered);
                    else
                        diverged("a pick from " + player + "'s " + zone.name() + ", which no longer holds the cards that were picked");
                }
                _entries.add(new Entry(Kind.PICK, player, zone.name(), ids(picked)));
                return picked;
            }

            @Override
            public void shufflePlayers(List<String> players) {
                chance.shufflePlayers(players);
                Entry earlier = repeat(Kind.ORDER, "-", "");
                if (earlier != null) {
                    List<String> wanted = earlier.value().isEmpty() ? List.of() : Arrays.asList(earlier.value().split(","));
                    List<String> ordered = new ArrayList<>();
                    for (String key : wanted)
                        for (String candidate : players)
                            if (_keys.keyOf(candidate).equals(key))
                                ordered.add(candidate);
                    if (ordered.size() == players.size()) {
                        players.clear();
                        players.addAll(ordered);
                    } else {
                        diverged("the order of the players, who are not the same players");
                    }
                }
                List<String> keys = new ArrayList<>();
                for (String candidate : players)
                    keys.add(_keys.keyOf(candidate));
                _entries.add(new Entry(Kind.ORDER, "-", "", String.join(",", keys)));
            }
        };
    }

    /**
     * @return The earlier outcome to repeat for this random event, or null if there is none (any more).
     */
    private Entry repeat(Kind kind, String player, String detail) {
        Entry next = _toRepeat.peek();
        if (next == null)
            return null;
        if (next.kind() != kind || !next.player().equals(player) || !next.detail().equals(detail)) {
            diverged("a " + kind.name().toLowerCase() + " for " + player + " where the earlier game had a "
                    + next.kind().name().toLowerCase() + " for " + next.player());
            return null;
        }
        return _toRepeat.poll();
    }

    private void diverged(String what) {
        if (_divergence == null)
            _divergence = "The game stopped following the earlier one at " + what;
        _toRepeat.clear();
    }

    private static String ids(Collection<? extends PhysicalCard> cards) {
        StringBuilder sb = new StringBuilder();
        for (PhysicalCard card : cards)
            sb.append(sb.isEmpty() ? "" : ",").append(card.getCardId());
        return sb.toString();
    }

    /**
     * @return The cards with these IDs, in this order; null if any of them is not among the cards given.
     */
    private static List<PhysicalCard> inOrder(String ids, Collection<? extends PhysicalCard> cards) {
        Map<Integer, PhysicalCard> byId = new LinkedHashMap<>();
        for (PhysicalCard card : cards)
            byId.put(card.getCardId(), card);
        List<PhysicalCard> result = new ArrayList<>();
        if (ids.isEmpty())
            return result;
        for (String id : ids.split(",")) {
            PhysicalCard card = byId.remove(Integer.parseInt(id));
            if (card == null)
                return null;
            result.add(card);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends PhysicalCard> void reorder(List<T> deck, List<PhysicalCard> ordered) {
        deck.clear();
        for (PhysicalCard card : ordered)
            deck.add((T) card);
    }

    /* ---- as text ---- */

    /**
     * One entry to a line: the kind's letter, the player, the detail and the value, separated by tabs.
     */
    public String write() {
        StringBuilder sb = new StringBuilder();
        for (Entry entry : _entries)
            sb.append(entry.kind().code).append('\t').append(escape(entry.player())).append('\t')
                    .append(escape(entry.detail())).append('\t').append(escape(entry.value())).append('\n');
        return sb.toString();
    }

    /**
     * @throws IllegalArgumentException if the text is not a journal.
     */
    public static GameJournal read(String text, PlayerKeys keys) {
        GameJournal journal = new GameJournal(keys);
        for (String line : text.split("\n")) {
            if (line.isEmpty())
                continue;
            String[] parts = line.split("\t", -1);
            if (parts.length != 4)
                throw new IllegalArgumentException("Not a journal entry: " + line);
            journal._entries.add(new Entry(Kind.of(parts[0]), unescape(parts[1]), unescape(parts[2]), unescape(parts[3])));
        }
        return journal;
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String unescape(String text) {
        if (text.indexOf('\\') < 0)
            return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                sb.append(next == 't' ? '\t' : next == 'n' ? '\n' : next == 'r' ? '\r' : next);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
