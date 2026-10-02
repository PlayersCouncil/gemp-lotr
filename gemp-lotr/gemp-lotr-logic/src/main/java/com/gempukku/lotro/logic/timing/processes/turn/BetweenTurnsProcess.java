package com.gempukku.lotro.logic.timing.processes.turn;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

public class BetweenTurnsProcess implements GameProcess {
    @Override
    public void process(LotroGame game) {
        String nextPlayer = game.getGameState().getFixedFreePeoplePlayer();
        if (nextPlayer == null) {
            PlayOrder playOrder = game.getGameState().getPlayerOrder().getClockwisePlayOrder(game.getGameState().getCurrentPlayerId(), false);
            playOrder.getNextPlayer();

            nextPlayer = playOrder.getNextPlayer();
        }
        game.getGameState().startPlayerTurn(nextPlayer, true);
    }

    @Override
    public GameProcess getNextProcess() {
        return new StartOfTurnGameProcess();
    }
}
