package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

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
 * come out as they did before, then go through earlier.entries() in order.  Give the game each answer, checking it
 * against the decision it is for with matches() and turning it back into an answer to that decision with concrete();
 * and do again whatever each note says was done.  The new journal fills up as the game is replayed, so at the end
 * it is a complete journal in its own right.
 *
 * Players are written down by whatever keyOf says, so that a journal does not have to be replayed by the same people
 * (a scenario writes "freeps" and "shadow").  Cards are written down by card ID.  Those are handed out in a fixed
 * order as a game goes, so the same game gives the same card the same ID.
 *
 * What is NOT the same from one run of a game to the next is the order a decision lists its options in.  The engine
 * collects cards and actions in hash sets of objects that have no hash of their own, so the same three companions
 * come out in a different order each time.  Two things follow.  A decision's fingerprint must not depend on the
 * order of its options.  And an answer that is a position in the list ("action 2", "the card called temp1", "option
 * 0") means nothing in another run, so answers are written down by what was chosen instead: see stable().
 */
public class GameJournal implements DecisionObserver {
    public enum Kind {
        /** A player's answer to a decision.  detail is the decision's fingerprint; value the answer, in its stable form. */
        ANSWER("A"),
        /** A deck was shuffled.  value is the card IDs in their new order, top first. */
        SHUFFLE("S"),
        /** Cards were picked at random.  detail is the zone they were picked from; value the IDs picked, in order. */
        PICK("P"),
        /** The players were put in a random order.  value is that order. */
        ORDER("O"),
        /** A point somebody wanted to be able to find again.  value is its name.  Has no effect on a replay. */
        MARK("M"),
        /**
         * Something that steered whatever is running the game, not the game itself (a scenario's reader choosing
         * which way through it to take).  detail says what kind of thing; value what.  The game takes no notice;
         * whoever follows the journal is expected to do the same thing again at the same point.
         */
        NOTE("N");

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
     * Writes down something that happened outside the game and has to happen again when the journal is followed.
     */
    public void note(String playerId, String detail, String value) {
        _entries.add(new Entry(Kind.NOTE, playerId == null ? "-" : _keys.keyOf(playerId), detail, value));
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
        _entries.add(new Entry(Kind.ANSWER, _keys.keyOf(playerId), fingerprint(decision), stable(decision, answer == null ? "" : answer)));
    }

    /**
     * @return Whether a decision is the one an answer in the journal was given to.
     */
    public static boolean matches(Entry answer, AwaitingDecision decision) {
        return answer.kind() == Kind.ANSWER && decision != null && answer.detail().equals(fingerprint(decision));
    }

    /**
     * A short stand-in for everything a decision asks: what kind it is, its text, and every option it offers, in no
     * particular order.  Two decisions with the same fingerprint are, for all practical purposes, the same question.
     * The kind is left readable because it is the first thing anybody wants to know when two do not match.
     */
    public static String fingerprint(AwaitingDecision decision) {
        Map<String, String[]> parameters = new TreeMap<>(decision.getDecisionParameters());
        //Position in the list is not part of the question
        switch (decision.getDecisionType()) {
            case CARD_ACTION_CHOICE, ACTION_CHOICE -> parameters.remove("actionId");
            case ARBITRARY_CARDS -> {
                String[] cards = arbitraryCards(decision);
                if (cards != null)
                    parameters.put("cardId", cards);
                else
                    parameters.remove("cardId");
            }
            default -> { }
        }

        //Some parameters describe the same options, one option to a position ("cardId" and "actionText" of a choice
        // of actions).  Those are put together, each option on a line, and the lines sorted.  Every other parameter
        // is a list of its own (the characters and the minions of an assignment) and is sorted by itself.
        List<String> together = switch (decision.getDecisionType()) {
            case CARD_ACTION_CHOICE -> List.of("cardId", "blueprintId", "actionText");
            case ACTION_CHOICE -> List.of("blueprintId", "actionText");
            case ARBITRARY_CARDS -> List.of("cardId", "blueprintId", "selectable");
            default -> List.of();
        };
        StringBuilder all = new StringBuilder(String.valueOf(decision.getText()));
        int options = 0;
        for (String name : together)
            if (parameters.containsKey(name))
                options = Math.max(options, parameters.get(name).length);
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < options; i++) {
            StringBuilder line = new StringBuilder();
            for (String name : together) {
                String[] values = parameters.get(name);
                line.append(name).append('=').append(values != null && i < values.length ? values[i] : "").append('\u0002');
            }
            lines.add(line.toString());
        }
        Collections.sort(lines);
        all.append('\u0001').append(String.join("\u0001", lines));
        for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
            if (together.contains(parameter.getKey()))
                continue;
            String[] values = parameter.getValue().clone();
            Arrays.sort(values);
            all.append('\u0001').append(parameter.getKey()).append('=').append(String.join("\u0002", values));
        }
        return decision.getDecisionType().name() + ":" + Integer.toHexString(all.toString().hashCode());
    }

    /**
     * @return For a choice from an arbitrary list of cards: something that identifies each card on offer, in the
     * order they are offered.  The card's ID if the decision knows its cards, otherwise all that can be said is
     * which card it is a copy of.  Null if the decision is not that kind.
     */
    private static String[] arbitraryCards(AwaitingDecision decision) {
        String[] blueprints = decision.getDecisionParameters().get("blueprintId");
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS || blueprints == null)
            return null;
        List<PhysicalCard> cards = decision instanceof ArbitraryCardsSelectionDecision arbitrary && arbitrary.getPhysicalCards() != null
                ? new ArrayList<>(arbitrary.getPhysicalCards()) : null;
        String[] result = new String[blueprints.length];
        for (int i = 0; i < result.length; i++)
            result[i] = cards != null && i < cards.size() ? "c" + cards.get(i).getCardId() : "b" + blueprints[i];
        return result;
    }

    private static final String PART = "|";

    /**
     * Turns an answer into a form that means the same thing in another run of the same game.  Most answers already
     * do (card IDs, numbers).  The ones that are positions in the decision's list of options do not:
     *
     *     a choice of a card's action      "2"            becomes   "&lt;card ID&gt;|&lt;the action's text&gt;"
     *     a choice between actions         "2"            becomes   "&lt;blueprint&gt;|&lt;the action's text&gt;"
     *     cards from an arbitrary list     "temp1,temp3"  becomes   "c&lt;card ID&gt;,c&lt;card ID&gt;" (or "b&lt;blueprint&gt;")
     *     a multiple choice                "0"            becomes   the text of the option
     *
     * An answer that is not one of the decision's options is left as it is; the game will have refused it.
     */
    public static String stable(AwaitingDecision decision, String answer) {
        Map<String, String[]> parameters = decision.getDecisionParameters();
        try {
            switch (decision.getDecisionType()) {
                case CARD_ACTION_CHOICE -> {
                    if (answer.isEmpty())
                        return answer;
                    int index = indexOf(parameters.get("actionId"), answer);
                    return index < 0 ? answer : parameters.get("cardId")[index] + PART + parameters.get("actionText")[index];
                }
                case ACTION_CHOICE -> {
                    int index = indexOf(parameters.get("actionId"), answer);
                    return index < 0 ? answer : parameters.get("blueprintId")[index] + PART + parameters.get("actionText")[index];
                }
                case ARBITRARY_CARDS -> {
                    if (answer.isEmpty())
                        return answer;
                    String[] cards = arbitraryCards(decision);
                    List<String> chosen = new ArrayList<>();
                    for (String id : answer.split(",")) {
                        int index = indexOf(parameters.get("cardId"), id);
                        if (index < 0)
                            return answer;
                        chosen.add(cards[index]);
                    }
                    return String.join(",", chosen);
                }
                case MULTIPLE_CHOICE -> {
                    int index = Integer.parseInt(answer);
                    String[] options = parameters.get("results");
                    return index < 0 || index >= options.length ? answer : options[index];
                }
                default -> {
                    return answer;
                }
            }
        } catch (RuntimeException exp) {
            return answer;
        }
    }

    /**
     * The other way: turns a stable answer into the answer to give this decision.
     * @return The answer, or null if what was chosen is not among what this decision offers.
     */
    public static String concrete(AwaitingDecision decision, String stable) {
        Map<String, String[]> parameters = decision.getDecisionParameters();
        switch (decision.getDecisionType()) {
            case CARD_ACTION_CHOICE -> {
                if (stable.isEmpty())
                    return stable;
                return optionWith(parameters.get("actionId"), parameters.get("cardId"), parameters.get("actionText"), stable);
            }
            case ACTION_CHOICE -> {
                return optionWith(parameters.get("actionId"), parameters.get("blueprintId"), parameters.get("actionText"), stable);
            }
            case ARBITRARY_CARDS -> {
                if (stable.isEmpty())
                    return stable;
                String[] cards = arbitraryCards(decision);
                String[] ids = parameters.get("cardId");
                boolean[] used = new boolean[ids.length];
                List<String> chosen = new ArrayList<>();
                for (String card : stable.split(",")) {
                    int found = -1;
                    for (int i = 0; i < cards.length && found < 0; i++)
                        if (!used[i] && cards[i].equals(card))
                            found = i;
                    if (found < 0)
                        return null;
                    used[found] = true;
                    chosen.add(ids[found]);
                }
                return String.join(",", chosen);
            }
            case MULTIPLE_CHOICE -> {
                int index = indexOf(parameters.get("results"), stable);
                return index < 0 ? null : String.valueOf(index);
            }
            default -> {
                return stable;
            }
        }
    }

    private static String optionWith(String[] ids, String[] firsts, String[] seconds, String stable) {
        int bar = stable.indexOf(PART);
        if (bar < 0 || ids == null)
            return null;
        String first = stable.substring(0, bar);
        String second = stable.substring(bar + 1);
        for (int i = 0; i < ids.length; i++)
            if (firsts[i].equals(first) && seconds[i].equals(second))
                return ids[i];
        return null;
    }

    private static int indexOf(String[] values, String value) {
        if (values != null)
            for (int i = 0; i < values.length; i++)
                if (values[i].equals(value))
                    return i;
        return -1;
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
