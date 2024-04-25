package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.Map;

public class PlayRingBearerRingAndAddBurdersGameProcess implements GameProcess {
    private final Map<String, Integer> _bids;
    private final String _firstPlayer;

    public PlayRingBearerRingAndAddBurdersGameProcess(Map<String, Integer> bids, String firstPlayer) {
        _bids = bids;
        _firstPlayer = firstPlayer;
    }

    @Override
    public void process(LotroGame game) {
        var gameState = game.getGameState();

        for (String playerId : gameState.getPlayerOrder().getAllPlayers()) {
            var ringBearer = gameState.getRingBearer(playerId);
            gameState.addCardToZone(game, ringBearer, Zone.FREE_CHARACTERS);

            PhysicalCard ring = gameState.getRing(playerId);
            if (ring != null)
                gameState.attachCard(game, ring, ringBearer);

            gameState.startPlayerTurn(playerId);
            gameState.addBurdens(_bids.get(playerId));
        }
        gameState.setCurrentPhase(Phase.PLAY_STARTING_FELLOWSHIP);
    }

    @Override
    public GameProcess getNextProcess() {
        return new CheckForCorruptionGameProcess(_firstPlayer);
    }
}
