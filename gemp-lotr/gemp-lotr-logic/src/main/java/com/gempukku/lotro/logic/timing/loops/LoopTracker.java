package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.ArrayDeque;
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
 * A pump war that spends something (exertions, cards, twilight, tokens) changes the board each round and is never
 * found, however long it runs; nor is one player using his own ability again and again, since the other player
 * passes in between.  Longer chains (three or more actions to a round) are left alone, as she said ("we'll leave
 * off complicated chains of actions while trying to figure out the simple 2-action case"), and so is any loop
 * whose board only grows or that has a shuffle or a random choice in it: those go on as before.
 * <p>
 * The step is for two players: CR 4.2 names the Free Peoples player and "the Shadow player".  With more players
 * in the order the tracker is off and finds nothing.  (No table in the tests seats more than two, so that case is
 * not exercised by any test.)
 * <p>
 * The tracker keeps the last four choices and a fingerprint only for a choice that repeats the one two before it,
 * so a game in which nobody repeats an action pays for a short list and nothing else.
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

    /**
     * @param playerCount How many players take actions in turn in the window; the tracker is off unless two.
     */
    public LoopTracker(int playerCount) {
        _enabled = (playerCount == 2);
    }

    /**
     * A player passed: he has declined to go on, and nothing chosen before counts towards a loop.
     */
    public void passed() {
        _entries.clear();
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
        _entries.addLast(new Entry(key, action.getActionAttachedToCard(), action.getAbility(), fingerprint));
        while (_entries.size() > 4)
            _entries.removeFirst();
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

        if (!first.key.equals(third.key) || !second.key.equals(fourth.key))
            return null;
        if (!first.key.player.equals(playerId) || second.key.player.equals(playerId))
            return null;
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

        return new Loop(first.key.player, first.card, second.key.player, second.card);
    }

    /**
     * The players have settled how many more times the loop runs.  What has been chosen so far no longer counts
     * towards a loop.
     * @param agreement What they settled.
     */
    public void agreed(LoopNegotiation.Agreement agreement) {
        _lastAgreement = agreement;
        _entries.clear();
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

    // What tells one use of an action from another: who took it, of which card, and which of the card's abilities
    // (or, for an action no definition built, its type and its words).
    private record Key(String player, int cardId, Object ability) {
        private static Key of(LotroGame game, String playerId, Action action) {
            final PhysicalCard card = action.getActionAttachedToCard();
            final Object ability = action.getAbility() != null
                    ? action.getAbility()
                    : action.getType() + ":" + action.getText(game);
            return new Key(playerId, card == null ? -1 : card.getCardId(), ability);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key other && player.equals(other.player) && cardId == other.cardId
                    && (ability instanceof ActionSource ? ability == other.ability : Objects.equals(ability, other.ability));
        }

        @Override
        public int hashCode() {
            return Objects.hash(player, cardId);
        }
    }

    private record Entry(Key key, PhysicalCard card, ActionSource ability, BoardFingerprint fingerprint) {
    }
}
