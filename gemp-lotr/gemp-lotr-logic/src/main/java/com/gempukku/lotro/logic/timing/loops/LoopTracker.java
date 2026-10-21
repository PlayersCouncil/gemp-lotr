package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Finds the voluntary loop of CR 4.2 "loops" (Ketura on Q-0321 and Q-0329): two players who take the same two
 * actions in turn, each round giving the board back as it was, so that either could go on for ever.  One tracker
 * watches one window in which the players take actions in turn: one run of the maneuver, archery, assignment or
 * regroup actions (PlayersPlayPhaseActionsInOrderGameProcess) or one skirmish (SkirmishActionProcedureAction).
 * <p>
 * The window tells it each action chosen ({@link #chose}) and each pass ({@link #passed}), and asks it, before
 * offering a player his actions, whether a loop is about to begin a third round ({@link #findLoop}).  A loop is
 * found when all of these hold:
 * <ul>
 *     <li>the last two rounds are the same two actions in the same order, one of each player (the same ability
 *     of the same card, used by the same player), with no pass among them, and the player about to be offered
 *     his actions is the one who opened each round;</li>
 *     <li>the board at the start of the second round (taken when its first action was chosen) is the board now,
 *     at the start of the third (BoardFingerprint): a round gives back everything it spends;</li>
 *     <li>the round's first action is on offer again now and is not known to do nothing, and neither ability has
 *     a limit on its uses ({@link ActionSource#isLimited}): an ability with a limit cannot go on for ever.</li>
 * </ul>
 * <p>
 * A round may also be one player's action with the other player's action taken INSIDE it (a Response, or anything
 * else he chose while it was carried out), and then the other player's pass (Ketura on F-0565, answers/2026-10-20-d.md:
 * Hrethel, Rider of Rohan 6_95 against a warg-rider bearing a Warg 5_65, whose Response cancels Hrethel's ability).
 * It is found as the two-action round is, the pass standing as the other player's half: the last two rounds are the
 * same action of the opener, each with an action of the same card chosen inside it by the other player, each
 * followed by that player's pass, and the board is as it was.  It is put to the players with the same prompts.  Its
 * run takes the opener's action, the other player's choices inside it made as in the round played by hand, and counts
 * the pass (LoopRun); afterwards only the opener's ability is barred, and the other player's Response is not, since
 * it may answer something else (her words: "the Response should not be barred, only Hrethel").
 * <p>
 * A pump war that spends something (exertions, cards, twilight, tokens) changes the board each round and is never
 * found, however long it runs; nor is one player using his own ability again and again while the other passes in
 * between with nothing chosen inside it (F-0565's report names that case and leaves it alone).  Longer chains (three or more actions to a round) are left alone, as she said ("we'll leave
 * off complicated chains of actions while trying to figure out the simple 2-action case"), and so is any loop
 * whose board only grows or that has a shuffle or a random choice in it: those go on as before.
 * <p>
 * The step is for two players: CR 4.2 names the Free Peoples player and "the Shadow player".  With more players
 * in the order the tracker is off and finds nothing.  (No table in the tests seats more than two, so that case is
 * not exercised by any test.)
 * <p>
 * The tracker keeps the last four choices and a fingerprint only for a choice that repeats the one two before it,
 * so a game in which nobody repeats an action pays for a short list and nothing else.  With each choice it keeps the
 * answers given to the decisions asked while that action was carried out (LoopFeedback records them), so that a
 * loop's run can give them again.
 * <p>
 * Once the players have settled how many more times the loop goes ({@link #agreed}), the tracker holds the run of
 * it (LoopRun), which the window carries out before it asks anybody anything else.
 * <p>
 * Not exercised by any card: a search of the definitions (engine pass 51) found no printed pair of a Free Peoples
 * and a Shadow ability that loops outside a skirmish, so the finding in the maneuver, archery, assignment and
 * regroup actions is shown only by a war that is no loop (FiniteRepeatsAreNoLoopTests, in the archery phase); and
 * no printed loop has an ability with a limit in it, so the limit guard is reached by no card either.
 */
public class LoopTracker {
    private final boolean _enabled;
    private final Deque<Entry> _entries = new ArrayDeque<>();
    private LoopNegotiation.Agreement _lastAgreement;
    // The round of the loop last found: its second round, as it was played by hand
    private List<Entry> _foundRound;
    // The other player's card in the loop last found (for a round closed by a pass, the card of his action inside)
    private PhysicalCard _foundOtherCard;
    private LoopRun _run;

    /**
     * @param playerCount How many players take actions in turn in the window; the tracker is off unless two.
     */
    public LoopTracker(int playerCount) {
        _enabled = (playerCount == 2);
    }

    /**
     * A player passed.  The pass is kept as a choice of its own: it is the other half of a round in which he took an
     * action inside the opener's (see the class's description), and in any other round it breaks the pattern.
     * @param playerId The player who passed.
     */
    public void passed(String playerId) {
        if (!_enabled)
            return;
        _entries.addLast(new Entry(Key.pass(playerId), null, null, null, List.of()));
        while (_entries.size() > 4)
            _entries.removeFirst();
    }

    /**
     * A player chose an action in the window.  Called as the choice is made, before the action is carried out, so
     * that the board is the board the action is chosen on.
     * @param game The game.
     * @param playerId The player who chose it.
     * @param action The action chosen.
     */
    public void chose(LotroGame game, String playerId, Action action) {
        if (!_enabled)
            return;
        final Key key = Key.of(game, playerId, action);
        BoardFingerprint fingerprint = null;
        if (_entries.size() >= 2 && secondLast().key.equals(key))
            fingerprint = BoardFingerprint.of(game);
        final Entry entry = new Entry(key, action.getActionAttachedToCard(), action.getAbility(), fingerprint, new ArrayList<>());
        _entries.addLast(entry);
        while (_entries.size() > 4)
            _entries.removeFirst();
        final LoopFeedback feedback = LoopFeedback.of(game);
        if (feedback != null)
            feedback.recordAnswersInto(entry.answers);
    }

    /**
     * The window is about to offer a player his actions (or to carry out the next step of a run): the action chosen
     * last is over, and nothing answered from now on is part of it.
     * @param game The game.
     */
    public void offering(LotroGame game) {
        final LoopFeedback feedback = LoopFeedback.of(game);
        if (feedback != null)
            feedback.recordAnswersInto(null);
    }

    /**
     * Whether the given player, about to be offered the given actions, would be offered the first action of a
     * loop for the third time (see the class's description).
     * @param game The game.
     * @param playerId The player about to be offered his actions.
     * @param offered The actions he would be offered.
     * @return The loop, or null if there is none.
     */
    public Loop findLoop(LotroGame game, String playerId, List<? extends Action> offered) {
        if (!_enabled || _entries.size() < 4)
            return null;
        final Iterator<Entry> iterator = _entries.iterator();
        final Entry first = iterator.next();
        final Entry second = iterator.next();
        final Entry third = iterator.next();
        final Entry fourth = iterator.next();

        if (!first.key.equals(third.key) || !second.key.equals(fourth.key) || first.key.isPass())
            return null;
        if (!first.key.player().equals(playerId) || second.key.player().equals(playerId))
            return null;
        // A round closed by the other player's pass: his half is the action he chose inside the opener's, the same
        // card's in both rounds (F-0565).
        PhysicalCard otherCard = second.card;
        if (second.key.isPass()) {
            final PhysicalCard inside = chosenInside(game, first, second.key.player());
            if (inside == null || inside != chosenInside(game, third, second.key.player()))
                return null;
            otherCard = inside;
        }
        if (isLimited(first) || isLimited(second))
            return null;
        if (third.fingerprint == null)
            return null;

        boolean onOffer = false;
        for (Action action : offered) {
            if (Key.of(game, playerId, action).equals(first.key) && action.getNoEffect() == null) {
                onOffer = true;
                break;
            }
        }
        if (!onOffer)
            return null;

        if (!third.fingerprint.equals(BoardFingerprint.of(game)))
            return null;

        _foundRound = List.of(third, fourth);
        _foundOtherCard = otherCard;
        return new Loop(first.key.player(), first.card, second.key.player(), otherCard);
    }

    // The card of the first action the given player chose while the entry's action was carried out, or null.
    private static PhysicalCard chosenInside(LotroGame game, Entry entry, String playerId) {
        for (LoopRun.Answer answer : entry.answers) {
            final int cardId = answer.chosenActionCardId();
            if (answer.player().equals(playerId) && cardId >= 0)
                return game.getGameState().findCardById(cardId);
        }
        return null;
    }

    /**
     * The players have settled how many more times the loop runs: the run of it is made, to be carried out by the
     * window ({@link #getRun}).  What has been chosen so far no longer counts towards a loop.
     * @param agreement What they settled.
     */
    public void agreed(LoopNegotiation.Agreement agreement) {
        _lastAgreement = agreement;
        final Entry first = _foundRound.get(0);
        final Entry second = _foundRound.get(1);
        _run = new LoopRun(
                new LoopRun.Step(first.key, first.card, first.answers),
                new LoopRun.Step(second.key, second.key.isPass() ? _foundOtherCard : second.card, second.answers),
                agreement);
        _foundRound = null;
        _entries.clear();
    }

    /**
     * @return The run of the loop the players last settled, while it is still to be carried out or under way; null
     * when there is none.  The window carries it out step by step (LoopRun.nextAction) and, when that gives no more
     * actions, asks the player the run says is next and calls {@link #runOver}.
     */
    public LoopRun getRun() {
        return _run;
    }

    /**
     * The run is over and the window has gone back to asking the players.
     */
    public void runOver() {
        _run = null;
    }

    /**
     * @return What the players settled the last time a loop was found in this window, or null if none was.
     */
    public LoopNegotiation.Agreement getLastAgreement() {
        return _lastAgreement;
    }

    private Entry secondLast() {
        final Iterator<Entry> iterator = _entries.descendingIterator();
        iterator.next();
        return iterator.next();
    }

    private static boolean isLimited(Entry entry) {
        return entry.ability != null && entry.ability.isLimited();
    }

    /**
     * A loop found: the two actions of a round, in the order they are taken.
     * @param opener The player who takes the first action of each round.
     * @param openerCard The card whose action that is.
     * @param other The player who takes the second.
     * @param otherCard The card whose action that is.
     */
    public record Loop(String opener, PhysicalCard openerCard, String other, PhysicalCard otherCard) {
    }

    /**
     * One ability of one card: the card's id, and the piece of its definition that built the action (the action's
     * ActionSource), or, for an action no definition built, its type and its words.  It is what a loop's run looks
     * for among the actions on offer, and what is barred once the loop is over (ActionsEnvironment.barUntilEndOfPhase):
     * the looping ability of that card, and not its other abilities, nor the same ability of another copy.
     * @param cardId The card's id, or -1 for an action of no card.
     * @param ability The ActionSource, or the type and words.
     */
    public record Ability(int cardId, Object ability) {
        /**
         * @param game The game.
         * @param action An action.
         * @return The ability that action is a use of.
         */
        public static Ability of(LotroGame game, Action action) {
            final PhysicalCard card = action.getActionAttachedToCard();
            final Object ability = action.getAbility() != null
                    ? action.getAbility()
                    : action.getType() + ":" + action.getText(game);
            return new Ability(card == null ? -1 : card.getCardId(), ability);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Ability other && cardId == other.cardId
                    && (ability instanceof ActionSource ? ability == other.ability : Objects.equals(ability, other.ability));
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(cardId);
        }
    }

    /**
     * What tells one use of an action from another: who took it, and the ability of which card.
     * @param player The player who took it.
     * @param ability The ability.
     */
    record Key(String player, Ability ability) {
        static Key of(LotroGame game, String playerId, Action action) {
            return new Key(playerId, Ability.of(game, action));
        }

        // A pass by the player: no ability.
        static Key pass(String playerId) {
            return new Key(playerId, null);
        }

        boolean isPass() {
            return ability == null;
        }
    }

    // One action chosen (or a pass, with no card and no ability): what it was a use of, its card, the ability that built it, the board as it was chosen (only
    // where it repeats the choice two before it), and the answers to the decisions asked while it was carried out.
    private record Entry(Key key, PhysicalCard card, ActionSource ability, BoardFingerprint fingerprint, List<LoopRun.Answer> answers) {
    }
}
