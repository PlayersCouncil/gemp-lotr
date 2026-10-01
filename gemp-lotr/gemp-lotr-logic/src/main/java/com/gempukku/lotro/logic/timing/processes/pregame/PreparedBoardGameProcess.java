package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PregameSetup;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.BetweenTurnsProcess;

/**
 * Takes the place of the starting fellowships, the opening draw and the mulligans in a game that begins with a
 * prepared position (see {@link PregameSetup}), then hands over to the first turn.
 */
public class PreparedBoardGameProcess implements GameProcess {
    private final String _firstPlayer;
    private final PregameSetup _setup;

    public PreparedBoardGameProcess(String firstPlayer, PregameSetup setup) {
        _firstPlayer = firstPlayer;
        _setup = setup;
    }

    @Override
    public void process(LotroGame game) {
        GameState gameState = game.getGameState();

        //Nothing is affecting the game at this point (the corruption check stops each player's cards as it goes), and
        // in this phase cards do not start affecting the game as they are placed.  The start of the first turn will
        // start everything that is on the table, exactly as it does after an ordinary opening.
        gameState.setCurrentPhase(Phase.BETWEEN_TURNS);

        //Burdens and threats belong to whoever the current player is, so the board is arranged from the first
        // player's point of view.
        gameState.startPlayerTurn(_firstPlayer, false);
        _setup.arrangeBoard(game);

        //The turn that follows goes to whoever comes after the current player, so leave the last player current.
        PlayOrder playOrder = gameState.getPlayerOrder().getClockwisePlayOrder(_firstPlayer, false);
        String lastPlayer = _firstPlayer;
        String nextPlayer;
        while ((nextPlayer = playOrder.getNextPlayer()) != null)
            lastPlayer = nextPlayer;
        gameState.startPlayerTurn(lastPlayer, false);
    }

    @Override
    public GameProcess getNextProcess() {
        return new BetweenTurnsProcess();
    }
}
