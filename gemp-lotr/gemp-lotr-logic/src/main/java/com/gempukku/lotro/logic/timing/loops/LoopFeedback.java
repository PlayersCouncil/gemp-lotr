package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * The game's own feedback (DefaultLotroGame.getUserFeedback), standing between the game and the feedback that puts
 * decisions to the players (the server's, or a virtual table's), for the loop step (CR 4.2 "loops"; Q-0329).  It
 * does two things for it, and otherwise hands everything on.
 * <ul>
 *     <li>While an action chosen in a window of actions taken in turn is carried out, it keeps each answer the
 *     players give to the decisions asked meanwhile (LoopTracker, which names the list; the answers reach it through
 *     {@link #decisionAnswered}, which the players' feedback calls).</li>
 *     <li>While a loop's run is under way (LoopRun), a decision asked is not put to its player: it is held here, and
 *     when the game stops for it ({@link #answerHeldDecision}) it is answered as it was in the round played by hand.
 *     A decision that cannot be answered so stops the run, and is then put to its player as any other.</li>
 * </ul>
 */
public class LoopFeedback implements UserFeedback {
    private final LotroGame _game;
    private final UserFeedback _players;

    private List<LoopRun.Answer> _recording;
    // The decisions put to the players while a recording was kept, whose answers belong in it
    private final Set<AwaitingDecision> _recordable = Collections.newSetFromMap(new IdentityHashMap<>());

    private LoopRun _run;
    private String _heldPlayer;
    private AwaitingDecision _held;

    /**
     * @param game The game.
     * @param players The feedback that puts decisions to the players.
     */
    public LoopFeedback(LotroGame game, UserFeedback players) {
        _game = game;
        _players = players;
    }

    /**
     * @param game A game.
     * @return The game's loop feedback, or null for a game that has none.
     */
    public static LoopFeedback of(LotroGame game) {
        return game.getUserFeedback() instanceof LoopFeedback feedback ? feedback : null;
    }

    @Override
    public void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision) {
        if (_run != null) {
            if (_held == null) {
                _heldPlayer = playerId;
                _held = awaitingDecision;
                return;
            }
            // Two decisions at once: no round of a loop is played so.
            final String firstPlayer = _heldPlayer;
            final AwaitingDecision first = _held;
            _held = null;
            _run.stop(_game, "two choices came at once");
            _players.sendAwaitingDecision(firstPlayer, first);
        }
        if (_recording != null)
            _recordable.add(awaitingDecision);
        _players.sendAwaitingDecision(playerId, awaitingDecision);
    }

    @Override
    public boolean hasPendingDecisions() {
        return _held != null || _players.hasPendingDecisions();
    }

    /**
     * The game has stopped working.  A run that is still under way then (the game ended in the middle of it) is
     * stopped, so that what it held back is sent.
     */
    @Override
    public void atRest() {
        if (_run != null)
            _run.stop(_game, "the game stopped");
        _players.atRest();
    }

    @Override
    public void decisionAnswered(String playerId, AwaitingDecision decision, String answer) {
        if (_recording != null && _recordable.remove(decision))
            _recording.add(LoopRun.Answer.of(playerId, decision, answer));
    }

    /**
     * Answers the decision held for a run, if there is one, as the run says.  The game calls this each time it has
     * stopped for a decision, and goes on if it answers.
     * @return True if a decision was answered and the game is to go on; false if there was none, or it was put to
     * its player after all.
     */
    public boolean answerHeldDecision() {
        if (_held == null)
            return false;
        final String playerId = _heldPlayer;
        final AwaitingDecision decision = _held;
        _held = null;
        _heldPlayer = null;
        final String answer = (_run != null) ? _run.answer(playerId, decision) : null;
        if (answer != null) {
            try {
                decision.decisionMade(answer);
                return true;
            } catch (DecisionResultInvalidException exp) {
                // Not answered: put to the player below.
            }
        }
        if (_run != null)
            _run.stop(_game, "a choice in it was not as it had been");
        _players.sendAwaitingDecision(playerId, decision);
        return false;
    }

    /**
     * Keeps the answers given from now on in the list given, or in none.
     * @param answers The list, or null to keep none.
     */
    void recordAnswersInto(List<LoopRun.Answer> answers) {
        _recording = answers;
        _recordable.clear();
    }

    void beginRun(LoopRun run) {
        _run = run;
        _recording = null;
        _recordable.clear();
    }

    void endRun(LoopRun run) {
        if (_run == run)
            _run = null;
    }
}
