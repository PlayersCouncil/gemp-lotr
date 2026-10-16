package com.gempukku.lotro.logic.timing.processes.turn.general;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.loops.LoopRun;
import com.gempukku.lotro.logic.timing.loops.OnePlayerLoopPrompt;
import com.gempukku.lotro.logic.timing.loops.OnePlayerLoopTracker;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.List;

/**
 * One player takes actions of the phase until he passes (the fellowship phase, and a Shadow player's Shadow phase).
 * Each step is a new instance; the loop step's tracker for one player's actions (CR 4.2 "loops",
 * OnePlayerLoopTracker) is carried through the constructor, and with it the run of a loop the player has settled
 * (LoopRun), which is carried out before he is asked anything.
 */
public class PlayerPlaysPhaseActionsUntilPassesGameProcess implements GameProcess {
    private final String _playerId;
    private final GameProcess _followingGameProcess;
    private final OnePlayerLoopTracker _loopTracker;

    private GameProcess _nextProcess;

    public PlayerPlaysPhaseActionsUntilPassesGameProcess(String playerId, GameProcess followingGameProcess) {
        this(playerId, followingGameProcess, new OnePlayerLoopTracker());
    }

    private PlayerPlaysPhaseActionsUntilPassesGameProcess(String playerId, GameProcess followingGameProcess, OnePlayerLoopTracker loopTracker) {
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
        _loopTracker = loopTracker;
    }

    @Override
    public void process(final LotroGame game) {
        _loopTracker.offering(game);

        final LoopRun run = _loopTracker.getRun();
        if (run != null) {
            // CR 4.2 "loops": the loop the player settled is carried out, an action at a time, as if each were
            // chosen; then he is asked again.
            final Action step = run.nextAction(game);
            if (step != null) {
                _nextProcess = new PlayerPlaysPhaseActionsUntilPassesGameProcess(_playerId, _followingGameProcess, _loopTracker);
                game.getActionsEnvironment().addActionToStack(step);
                return;
            }
            _loopTracker.runOver();
        }

        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_playerId);

        // CR 4.2 "loops": the player has gone round a loop three times and would be offered it a fourth.  He
        // chooses how many more times it is repeated, and the run is carried out before he is asked anything.
        final List<PhysicalCard> loop = (run == null) ? _loopTracker.findLoop(game, _playerId, playableActions) : null;
        if (loop != null) {
            game.getActionsEnvironment().addActionToStack(new OnePlayerLoopPrompt(game, _playerId, loop, _loopTracker::agreed));
            _nextProcess = new PlayerPlaysPhaseActionsUntilPassesGameProcess(_playerId, _followingGameProcess, _loopTracker);
            return;
        }

        if (countUsable(game, playableActions) == 0 && game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
            playerPassed();
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(game, 1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _loopTracker.chose(game, _playerId, action);
                                _nextProcess = new PlayerPlaysPhaseActionsUntilPassesGameProcess(_playerId, _followingGameProcess, _loopTracker);
                                game.getActionsEnvironment().addActionToStack(action);
                            } else
                                playerPassed();
                        }
                    });
        }
    }

    // The actions the player can take: those not barred after a loop (they are offered, and refused if taken).
    private static int countUsable(LotroGame game, List<Action> actions) {
        int count = 0;
        for (Action action : actions) {
            if (!game.getActionsEnvironment().isLoopBanned(action))
                count++;
        }
        return count;
    }

    private void playerPassed() {
        _nextProcess = _followingGameProcess;
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
