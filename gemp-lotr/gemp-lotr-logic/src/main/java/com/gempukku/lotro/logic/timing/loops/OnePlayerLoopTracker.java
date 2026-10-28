package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Finds a loop of one player's actions (CR 4.2 "loops", whose first example is Sam, Faithful Companion: "Fellowship:
 * Play Bill the Pony from your draw deck", with no Bill the Pony to play), in a window in which one player takes
 * actions until he passes: the fellowship phase, and a Shadow player's Shadow phase
 * (PlayerPlaysPhaseActionsUntilPassesGameProcess).  Ketura's word (answers/2026-10-16-c.md): "it should let the
 * player do it three times and then prompt with: "LOOP DETECTED: Choose a number of times to repeat this action."
 * It should then perform that many, and then put the tag on that card action to block its future use."
 * <p>
 * The window tells it each action chosen ({@link #chose}) and asks it, before offering the player his actions,
 * whether a loop has gone round three times ({@link #findLoop}).  A loop is found when all of these hold:
 * <ul>
 *     <li>the last three rounds are the same round: one action, used three times, or two actions of different
 *     abilities taken one after the other, three times (two Hobbit Party Guests, each exerting to heal the
 *     other); the same ability of the same card each time ({@link LoopTracker.Ability});</li>
 *     <li>the board at the start of each of the three rounds, and now, is the same, the order of the draw decks
 *     left out (BoardFingerprint.ignoringDeckOrder): Sam looks through the deck and shuffles it, and has done
 *     nothing;</li>
 *     <li>the round's first action is on offer again now and is not known to do nothing, and no ability of the
 *     round has a limit on its uses ({@link ActionSource#isLimited}).</li>
 * </ul>
 * So the fourth use is not offered: the prompt is asked in its place.  An action that moves a card between zones,
 * spends a wound, a token, a threat or twilight that it does not give back, changes the board and is never part of
 * a loop: Base of Mindolluin and Gondorian Captain playing and discarding fortifications go on until the draw deck
 * has none, unasked.  Rounds of three or more actions are left alone, as for two players (LoopTracker).
 * <p>
 * Once the player has chosen how many times the round is repeated ({@link #agreed}), the tracker holds the run of
 * it (LoopRun), which the window carries out before it asks him anything else; the run bars every ability of the
 * round to the end of the phase.
 */
public class OnePlayerLoopTracker {
    /** The prompt, in Ketura's words. */
    public static final String PROMPT = "LOOP DETECTED: Choose a number of times to repeat this action.";
    /** How many times a round goes unasked before the prompt. */
    public static final int TIMES_UNASKED = 3;
    /** The most actions a round of a loop is made of. */
    private static final int LONGEST_ROUND = 2;

    private final Deque<Entry> _entries = new ArrayDeque<>();
    // The round of the loop last found: its third round, as it was played by hand
    private List<Entry> _foundRound;
    private LoopRun _run;

    /**
     * The player chose an action in the window.  Called as the choice is made, before the action is carried out, so
     * that the board is the board the action is chosen on.
     * @param game The game.
     * @param playerId The player who chose it.
     * @param action The action chosen.
     */
    public void chose(LotroGame game, String playerId, Action action) {
        final Entry entry = new Entry(LoopTracker.Key.of(game, playerId, action), action.getActionAttachedToCard(),
                action.getAbility(), BoardFingerprint.ignoringDeckOrder(game), new ArrayList<>());
        _entries.addLast(entry);
        while (_entries.size() > TIMES_UNASKED * LONGEST_ROUND)
            _entries.removeFirst();
        final LoopFeedback feedback = LoopFeedback.of(game);
        if (feedback != null)
            feedback.recordAnswersInto(entry.answers);
    }

    /**
     * The window is about to offer the player his actions (or to carry out the next step of a run): the action chosen
     * last is over, and nothing answered from now on is part of it.
     * @param game The game.
     */
    public void offering(LotroGame game) {
        final LoopFeedback feedback = LoopFeedback.of(game);
        if (feedback != null)
            feedback.recordAnswersInto(null);
    }

    /**
     * Whether the player, about to be offered the given actions, has gone round a loop three times (see the class's
     * description).
     * @param game The game.
     * @param playerId The player about to be offered his actions.
     * @param offered The actions he would be offered.
     * @return The cards of the loop's round, in the order the round takes them (one card where it is one action, or
     * both actions are of one card), or null if there is no loop.
     */
    public List<PhysicalCard> findLoop(LotroGame game, String playerId, List<? extends Action> offered) {
        final List<Entry> entries = new ArrayList<>(_entries);
        BoardFingerprint now = null;
        for (int length = 1; length <= LONGEST_ROUND; length++) {
            final int needed = TIMES_UNASKED * length;
            if (entries.size() < needed)
                continue;
            final List<Entry> last = entries.subList(entries.size() - needed, entries.size());
            if (!isRepeated(last, length, playerId))
                continue;

            boolean onOffer = false;
            for (Action action : offered) {
                if (LoopTracker.Key.of(game, playerId, action).equals(last.get(0).key) && action.getNoEffect() == null) {
                    onOffer = true;
                    break;
                }
            }
            if (!onOffer)
                continue;

            if (now == null)
                now = BoardFingerprint.ignoringDeckOrder(game);
            boolean sameBoard = true;
            for (int round = 0; round < TIMES_UNASKED; round++) {
                if (!last.get(round * length).fingerprint.equals(now))
                    sameBoard = false;
            }
            if (!sameBoard)
                continue;

            _foundRound = new ArrayList<>(last.subList(needed - length, needed));
            final List<PhysicalCard> cards = new ArrayList<>();
            for (Entry entry : _foundRound) {
                if (entry.card != null && !cards.contains(entry.card))
                    cards.add(entry.card);
            }
            return cards;
        }
        return null;
    }

    // Whether the entries are one round of the given length, of the player's, three times over, with no ability in
    // it limited, and (for two actions) of two different abilities.
    private static boolean isRepeated(List<Entry> entries, int length, String playerId) {
        for (int i = 0; i < length; i++) {
            final Entry first = entries.get(i);
            if (!first.key.player().equals(playerId) || isLimited(first))
                return false;
            for (int round = 1; round < TIMES_UNASKED; round++) {
                if (!entries.get(round * length + i).key.equals(first.key))
                    return false;
            }
        }
        return length == 1 || !entries.get(0).key.equals(entries.get(1).key);
    }

    /**
     * The player has chosen how many times the loop's round is repeated: the run of it is made, to be carried out by
     * the window ({@link #getRun}).  What has been chosen so far no longer counts towards a loop.
     * @param iterations The number chosen, 0 or more.
     */
    public void agreed(int iterations) {
        final List<LoopRun.Step> round = new ArrayList<>();
        for (Entry entry : _foundRound)
            round.add(new LoopRun.Step(entry.key, entry.card, entry.answers));
        _run = new LoopRun(round, iterations);
        _foundRound = null;
        _entries.clear();
    }

    /**
     * @return The run of the loop the player last settled, while it is still to be carried out or under way; null
     * when there is none.
     */
    public LoopRun getRun() {
        return _run;
    }

    /**
     * The run is over and the window has gone back to asking the player.
     */
    public void runOver() {
        _run = null;
    }

    private static boolean isLimited(Entry entry) {
        return entry.ability != null && entry.ability.isLimited();
    }

    // One action chosen: what it was a use of, its card, the ability that built it, the board as it was chosen, and
    // the answers to the decisions asked while it was carried out.
    private record Entry(LoopTracker.Key key, PhysicalCard card, ActionSource ability, BoardFingerprint fingerprint,
                         List<LoopRun.Answer> answers) {
    }
}
