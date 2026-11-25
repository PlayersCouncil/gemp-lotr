package com.gempukku.lotro.logic.timing.processes.turn.general;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.loops.LoopRun;
import com.gempukku.lotro.logic.timing.loops.OnePlayerLoopPrompt;
import com.gempukku.lotro.logic.timing.loops.OnePlayerLoopTracker;
import com.gempukku.lotro.logic.timing.processes.AsksAgain;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.List;

/**
 * One player takes actions of the phase until he passes (the fellowship phase, and a Shadow player's Shadow phase).
 * Each step is a new instance; the loop step's tracker for one player's actions (CR 4.2 "loops",
 * OnePlayerLoopTracker) is carried through the constructor, and with it the run of a loop the player has settled
 * (LoopRun), which is carried out before he is asked anything.
 */
public class PlayerPlaysPhaseActionsUntilPassesGameProcess implements GameProcess, AsksAgain {
    private final String _playerId;
    private final GameProcess _followingGameProcess;
    private final OnePlayerLoopTracker _loopTracker;

    private GameProcess _nextProcess;
    // Whether the choice of actions this step put to the player is still waiting for an answer (AsksAgain)
    private boolean _asking;

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

        offer(game, playableActions);
    }

    // The player passes by himself if he has nothing to play in a phase he is passed through; otherwise he is asked.
    private void offer(final LotroGame game, List<Action> playableActions) {
        if (countUsable(game, playableActions) == 0 && game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
            playerPassed();
        } else {
            _asking = true;
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(game, 1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            _asking = false;
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

    @Override
    public boolean isAsking() {
        return _asking;
    }

    @Override
    public String askedPlayer() {
        return _playerId;
    }

    /**
     * Asks the player again, the actions on offer worked out afresh (AsksAgain).  The loop step (CR 4.2) is told
     * nothing and not looked at: a decision is asked again only after the board was changed by hand (a scenario's
     * sandbox), which chose nothing and passed nothing, and a loop's rounds are compared by the board each began on,
     * so a changed board breaks any round in progress by itself.  The tracker is left as it was, so a game rebuilt
     * from its journal, which asks again at the same points, tracks exactly as this one did.
     */
    @Override
    public void askAgain(LotroGame game) {
        _asking = false;
        offer(game, game.getActionsEnvironment().getPhaseActions(_playerId));
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
