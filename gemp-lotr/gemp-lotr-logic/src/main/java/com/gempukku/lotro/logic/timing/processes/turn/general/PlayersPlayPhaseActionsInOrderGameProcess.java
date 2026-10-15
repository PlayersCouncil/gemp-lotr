package com.gempukku.lotro.logic.timing.processes.turn.general;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.loops.LoopNegotiation;
import com.gempukku.lotro.logic.timing.loops.LoopTracker;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.List;

/**
 * The players take actions of the phase in turn (maneuver, archery, assignment and regroup) until each in a row has
 * passed.  Each step is a new instance; what carries over goes through the constructor, the loop step's tracker
 * (CR 4.2 "loops", LoopTracker) included.
 */
public class PlayersPlayPhaseActionsInOrderGameProcess implements GameProcess {
    private final PlayOrder _playOrder;
    private int _consecutivePasses;
    private final GameProcess _followingGameProcess;
    private final LoopTracker _loopTracker;
    // The player to be asked, where it is already settled (after the loop step's prompts); null to take the next
    // player of the order.
    private final String _playerToAsk;

    private GameProcess _nextProcess;

    public PlayersPlayPhaseActionsInOrderGameProcess(PlayOrder playOrder, int consecutivePasses, GameProcess followingGameProcess) {
        this(playOrder, consecutivePasses, followingGameProcess, new LoopTracker(playOrder.getPlayerCount()), null);
    }

    private PlayersPlayPhaseActionsInOrderGameProcess(PlayOrder playOrder, int consecutivePasses, GameProcess followingGameProcess,
                                                      LoopTracker loopTracker, String playerToAsk) {
        _playOrder = playOrder;
        _consecutivePasses = consecutivePasses;
        _followingGameProcess = followingGameProcess;
        _loopTracker = loopTracker;
        _playerToAsk = playerToAsk;
    }

    @Override
    public void process(final LotroGame game) {
        Skirmish skirmish = game.getGameState().getSkirmish();
        if (game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && (game.getGameState().getSkirmish().isCancelled()
                || skirmish.getFellowshipCharacter() == null || skirmish.getShadowCharacters().size() == 0)) {
            // If the skirmish is cancelled or one side of the skirmish is missing, no more phase actions can be played
            _nextProcess = _followingGameProcess;
        } else {
            String playerId;
            if (_playerToAsk != null) {
                playerId = _playerToAsk;
            } else if (game.getGameState().isConsecutiveAction()) {
                playerId = _playOrder.getLastPlayer();
                game.getGameState().setConsecutiveAction(false);
            } else {
                playerId = _playOrder.getNextPlayer();
            }

            final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(playerId);

            // CR 4.2 "loops": the player would be offered the first action of a loop for the third time.  The
            // players settle how many more times it runs, and then he is asked as he would have been.
            final LoopTracker.Loop loop = (_playerToAsk == null) ? _loopTracker.findLoop(game, playerId, playableActions) : null;
            if (loop != null) {
                game.getActionsEnvironment().addActionToStack(new LoopNegotiation(game, loop, _loopTracker::agreed));
                _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, _consecutivePasses, _followingGameProcess, _loopTracker, playerId);
                return;
            }

            if (playableActions.size() == 0 && game.shouldAutoPass(playerId, game.getGameState().getCurrentPhase())) {
                playerPassed();
            } else {
                game.getUserFeedback().sendAwaitingDecision(playerId,
                        new CardActionSelectionDecision(game, 1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _loopTracker.chose(game, playerId, action);
                                    _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, 0, _followingGameProcess, _loopTracker, null);
                                    game.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    playerPassed();
                                }
                            }
                        });
            }
        }
    }

    private void playerPassed() {
        _loopTracker.passed();
        _consecutivePasses++;
        if (_consecutivePasses >= _playOrder.getPlayerCount())
            _nextProcess = _followingGameProcess;
        else
            _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, _consecutivePasses, _followingGameProcess, _loopTracker, null);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
