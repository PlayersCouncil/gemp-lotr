package com.gempukku.lotro.logic.timing.processes.turn.regroup;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayerReconcilesAction;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

public class PlayerReconcilesGameProcess implements GameProcess {
    private final String _playerId;
    private final GameProcess _followingGameProcess;

    public PlayerReconcilesGameProcess(String playerId, GameProcess followingGameProcess) {
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(LotroGame game) {
        game.getActionsEnvironment().addActionToStack(new PlayerReconcilesAction(game, _playerId));
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
