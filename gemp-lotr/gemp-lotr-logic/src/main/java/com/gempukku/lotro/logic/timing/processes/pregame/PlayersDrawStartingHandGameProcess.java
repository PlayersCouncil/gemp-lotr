package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.BetweenTurnsProcess;

public class PlayersDrawStartingHandGameProcess implements GameProcess {
    private final String _firstPlayer;
    private GameProcess _followingGameProcess;

    public PlayersDrawStartingHandGameProcess(String firstPlayer) {
        _firstPlayer = firstPlayer;
    }

    @Override
    public void process(LotroGame game) {
        GameState gameState = game.getGameState();
        for (String player : gameState.getPlayerOrder().getAllPlayers()) {
            int handSize = game.getModifiersQuerying().getHandSize(game, player);
            gameState.shuffleDeck(player);
            for (int i = 0; i < handSize; i++)
                gameState.playerDrawsCard(player);
        }
        gameState.setCurrentPhase(Phase.BETWEEN_TURNS);
        if (game.getFormat().hasMulliganRule())
            _followingGameProcess = new MulliganProcess(game.getGameState().getPlayerOrder().getClockwisePlayOrder(_firstPlayer, false));
        else
            _followingGameProcess = new BetweenTurnsProcess();

    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
