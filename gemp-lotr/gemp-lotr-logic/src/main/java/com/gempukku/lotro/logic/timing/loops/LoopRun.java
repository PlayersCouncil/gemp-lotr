package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.timing.Action;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The carrying out of a voluntary loop the players have settled (CR 4.2 "loops": the loop "continues that number of
 * times", "adding one final action at the end of the loop, if necessary"; Ketura on Q-0329, answers/2026-10-15-c.md
 * and -d.md).  The window that found the loop (LoopTracker) takes its actions from here instead of asking the
 * players, one at a time, until there are none left.
 * <p>
 * <b>The actions.</b>  One iteration is one round as it was found: the opener's action and then the other player's.
 * The loop runs the number of iterations settled.  The player who takes the last action is the Shadow player if he
 * agreed to the number named and the Free Peoples player if he chose a lower one; where the last round ends with the
 * other player, the opener's action is taken once more at the end.  So a loop the Free Peoples player opened ends on
 * the round as found when the Shadow player agrees, and one action out of step when he lowers the number; a loop the
 * Shadow player opened, the other way about.  The player who did not take the last action takes the next.
 * <p>
 * <b>The choices inside them.</b>  Each decision asked while one of the actions is carried out is answered as it
 * was answered in the second round played by hand, which is the round the loop was found on: the same player, the
 * same kind of decision, the same words and the same options, or the run stops (LoopFeedback asks {@link #answer}).
 * <p>
 * <b>What the players are sent.</b>  Every round but the last one or two actions is carried out unseen: nothing is
 * sent to the clients or to the replay while it runs (GameState.holdBackEvents), and since each round gives the
 * board back as it found it, the clients' board stays right.  What a round would have sent is dropped once the next
 * round is seen to begin on the board the loop was found on.  Then the game stats are sent as they now stand, with
 * one line of the game log for the iterations not shown, and the last action (where one is added) or the last round
 * is carried out and sent as usual.
 * <p>
 * <b>Stopping short.</b>  The run stops where a round does not begin on the board the loop was found on, where the
 * next action is not on offer, where a decision inside an action is not as it was or none was answered then, or
 * where the window or the game ends under it.  What was held back since the last round that came out right is then
 * sent after all (each event read as the board now stands), the game log says why, and play goes on from there:
 * the decision that did not fit is asked of its player, and the player who did not take the last action is asked
 * next.  Not exercised by any card: in every printed loop each round comes out the same (engine pass 51).  Nor is
 * a run in the maneuver, archery, assignment or regroup actions (PlayersPlayPhaseActionsInOrderGameProcess): no
 * printed loop is found there (LoopTracker); the skirmish's window is the one a card reaches.
 * <p>
 * <b>Afterwards.</b>  However the run ends, the two abilities it used are barred to the end of the phase
 * (ActionsEnvironment.barUntilEndOfPhase), each skirmish being a phase of its own; they are still offered, flagged
 * "loopBanned", and refused if taken.
 * <p>
 * <b>A loop of one player's actions</b> (OnePlayerLoopTracker; Ketura, answers/2026-10-16-c.md: "perform that many,
 * and then put the tag on that card action to block its future use").  One iteration is one round as found, one
 * action or two, all of the one player's; the run is that many iterations and no action more, the last round shown
 * and the rest carried out unseen as above, and the board a round must give back is compared without the order of
 * the draw decks (BoardFingerprint.ignoringDeckOrder).  Then every ability of the round is barred to the end of the
 * phase, as above, and the player is asked for his actions again.
 */
public class LoopRun {
    /**
     * One action of a round: whose and which ability, its card, and the answers given while it was carried out
     * in the round played by hand.
     */
    record Step(LoopTracker.Key key, PhysicalCard card, List<Answer> answers) {
    }

    /**
     * An answer a player gave to a decision, with what the decision was, so that the same decision can be known
     * again.
     * @param player The player who answered.
     * @param type The kind of decision.
     * @param text Its words.
     * @param parameters What it offered, as it was sent.
     * @param answer The answer.
     */
    public record Answer(String player, AwaitingDecisionType type, String text, Map<String, List<String>> parameters, String answer) {
        static Answer of(String player, AwaitingDecision decision, String answer) {
            return new Answer(player, decision.getDecisionType(), decision.getText(), parametersOf(decision), answer);
        }

        boolean fits(String player, AwaitingDecision decision) {
            if (!this.player.equals(player) || type != decision.getDecisionType() || !text.equals(decision.getText()))
                return false;
            final Map<String, List<String>> now = parametersOf(decision);
            if (parameters.equals(now))
                return true;
            // A choice of cards answered with none (a look through a deck that finds nothing to take, Sam, Faithful
            // Companion's): the same cards in another order, after a shuffle, are the same decision.
            return answer.isEmpty() && (type == AwaitingDecisionType.ARBITRARY_CARDS || type == AwaitingDecisionType.CARD_SELECTION)
                    && optionsOf(parameters, type).equals(optionsOf(now, type));
        }

        // The options of a decision as a sorted list, each the values its parameters give it at one index, so that
        // the same options in another order compare equal; a parameter that is not one value per option stays whole.
        // The card ids of a choice of arbitrary cards are left out: they are "temp" and the card's place in the list.
        private static List<String> optionsOf(Map<String, List<String>> parameters, AwaitingDecisionType type) {
            int count = -1;
            for (List<String> values : parameters.values())
                count = Math.max(count, values.size());
            final List<String> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final StringBuilder option = new StringBuilder();
                for (Map.Entry<String, List<String>> parameter : parameters.entrySet()) {
                    final List<String> values = parameter.getValue();
                    if (values.size() == count && !(type == AwaitingDecisionType.ARBITRARY_CARDS && parameter.getKey().equals("cardId")))
                        option.append(parameter.getKey()).append('=').append(values.get(i)).append(';');
                }
                result.add(option.toString());
            }
            result.sort(null);
            for (Map.Entry<String, List<String>> parameter : parameters.entrySet()) {
                if (parameter.getValue().size() != count)
                    result.add(parameter.getKey() + "=" + parameter.getValue());
            }
            return result;
        }

        private static Map<String, List<String>> parametersOf(AwaitingDecision decision) {
            final Map<String, List<String>> result = new LinkedHashMap<>();
            for (Map.Entry<String, String[]> parameter : decision.getDecisionParameters().entrySet())
                result.put(parameter.getKey(), Arrays.asList(parameter.getValue().clone()));
            return result;
        }
    }

    private final Step[] _round;
    // Whether the board a round gives back is compared with the order of the draw decks
    private final boolean _deckOrderCounts;
    // The actions the run takes, the last one added if needed, and how many of them are taken unseen
    private final int _actions;
    private final int _unseen;

    private int _taken;
    private boolean _started;
    private boolean _over;
    private boolean _holding;
    private BoardFingerprint _board;
    private Deque<Answer> _answers = new ArrayDeque<>();

    LoopRun(Step opening, Step closing, LoopNegotiation.Agreement agreement) {
        _round = new Step[]{opening, closing};
        _deckOrderCounts = true;
        final boolean oneMore = !agreement.nextActor().equals(opening.key().player());
        _actions = 2 * agreement.iterations() + (oneMore ? 1 : 0);
        final int shown = oneMore ? 1 : Math.min(2, _actions);
        _unseen = _actions - shown;
    }

    /**
     * The run of a loop of one player's actions (see the class's description).
     * @param round The round as found: one action, or two.
     * @param iterations How many times the player chose to repeat it, 0 or more.
     */
    LoopRun(List<Step> round, int iterations) {
        _round = round.toArray(new Step[0]);
        _deckOrderCounts = false;
        _actions = _round.length * iterations;
        _unseen = _actions - Math.min(_round.length, _actions);
    }

    private BoardFingerprint boardNow(LotroGame game) {
        return _deckOrderCounts ? BoardFingerprint.of(game) : BoardFingerprint.ignoringDeckOrder(game);
    }

    /**
     * The next action of the run, taken from the actions on offer to its player.  Called by the window each time
     * it would otherwise ask a player for an action; the window stacks the action as if he had chosen it.
     * @param game The game.
     * @return The action, or null when the run is over (all its actions taken, or stopped short), and the window is
     * to ask {@link #getNextActor} as usual.
     */
    public Action nextAction(LotroGame game) {
        if (_over)
            return null;
        final GameState gameState = game.getGameState();
        if (!_started) {
            _started = true;
            _board = boardNow(game);
            final LoopFeedback feedback = LoopFeedback.of(game);
            if (feedback != null)
                feedback.beginRun(this);
            if (_unseen > 0) {
                gameState.holdBackEvents();
                _holding = true;
            }
        }
        if (_taken == _actions) {
            finish(game, null);
            return null;
        }
        if (_taken > 0 && _taken % _round.length == 0) {
            if (!_board.equals(boardNow(game))) {
                stop(game, "the board was not as it was when the loop was found");
                return null;
            }
            if (_holding)
                gameState.dropHeldBackEvents();
        }
        if (_holding && _taken == _unseen) {
            _holding = false;
            gameState.releaseHeldBackEvents();
            final int unseenRounds = _unseen / _round.length;
            gameState.sendMessage("The loop " + (_round.length == 1 ? "of " : "between ") + linksOf() + " was carried out " + unseenRounds
                    + (unseenRounds == 1 ? " time" : " times") + " without being shown");
        }

        final Step step = _round[_taken % _round.length];
        Action found = null;
        for (Action action : game.getActionsEnvironment().getPhaseActions(step.key().player())) {
            if (LoopTracker.Key.of(game, step.key().player(), action).equals(step.key())
                    && !game.getActionsEnvironment().isLoopBanned(action)) {
                found = action;
                break;
            }
        }
        if (found == null) {
            stop(game, GameUtils.getCardLink(step.card()) + " could not be used again");
            return null;
        }
        _answers = new ArrayDeque<>(step.answers());
        _taken++;
        return found;
    }

    /**
     * The answer to give to a decision asked while one of the run's actions is carried out: the next answer given
     * to that action's decisions in the round played by hand, if it was given by the same player to the same
     * decision.
     * @param playerId The player the decision is for.
     * @param decision The decision.
     * @return The answer, or null if there is none that fits, and the run is to stop.
     */
    String answer(String playerId, AwaitingDecision decision) {
        final Answer next = _answers.poll();
        if (next == null || !next.fits(playerId, decision))
            return null;
        return next.answer();
    }

    /**
     * Stops the run short, wherever it has got to (see the class's description).  Does nothing once it is over.
     * @param game The game.
     * @param why What the game log is to say stopped it.
     */
    public void stop(LotroGame game, String why) {
        if (_over)
            return;
        if (_holding) {
            _holding = false;
            game.getGameState().releaseHeldBackEvents();
        }
        finish(game, why);
    }

    private void finish(LotroGame game, String why) {
        _over = true;
        final LoopFeedback feedback = LoopFeedback.of(game);
        if (feedback != null)
            feedback.endRun(this);
        for (Step step : _round)
            game.getActionsEnvironment().barUntilEndOfPhase(step.key().ability());
        final String loop = "The loop " + (_round.length == 1 ? "of " : "between ") + linksOf();
        if (why == null)
            game.getGameState().sendMessage(loop
                    + " is over: the abilities it used cannot be used again this phase");
        else
            game.getGameState().sendMessage(loop + " stopped short, as " + why
                    + ": the abilities it used cannot be used again this phase");
    }

    /**
     * @return Whether the run is over: all its actions taken, or stopped short.
     */
    public boolean isOver() {
        return _over;
    }

    /**
     * @return The player to be asked for an action once the run is over: the one who did not take its last action,
     * or the opener, who was about to be asked, if it took none.  As the run planned, that is the player the
     * players settled on (LoopNegotiation.Agreement.nextActor).
     */
    public String getNextActor() {
        if (_taken == 0)
            return _round[0].key().player();
        return _round[_taken % _round.length].key().player();
    }

    private String linksOf() {
        final PhysicalCard first = _round[0].card();
        final PhysicalCard second = _round.length > 1 ? _round[1].card() : null;
        if (second == null || second == first)
            return GameUtils.getCardLink(first);
        return GameUtils.getCardLink(first) + " and " + GameUtils.getCardLink(second);
    }
}
