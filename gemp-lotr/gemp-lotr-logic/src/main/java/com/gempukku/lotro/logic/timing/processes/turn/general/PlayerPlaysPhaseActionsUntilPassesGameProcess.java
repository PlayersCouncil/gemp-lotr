package com.gempukku.lotro.logic.timing.processes.turn.general;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.List;

public class PlayerPlaysPhaseActionsUntilPassesGameProcess implements GameProcess {
    private final String _playerId;
    private final GameProcess _followingGameProcess;

    private GameProcess _nextProcess;

    public PlayerPlaysPhaseActionsUntilPassesGameProcess(String playerId, GameProcess followingGameProcess) {
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(final LotroGame game) {
        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_playerId);

        if (playableActions.isEmpty() && game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
            playerPassed();
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(game, 1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new PlayerPlaysPhaseActionsUntilPassesGameProcess(_playerId, _followingGameProcess);
                                game.getActionsEnvironment().addActionToStack(action);
                            } else
                                playerPassed();
                        }
                    });
        }
    }

    private void playerPassed() {
        _nextProcess = _followingGameProcess;
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
