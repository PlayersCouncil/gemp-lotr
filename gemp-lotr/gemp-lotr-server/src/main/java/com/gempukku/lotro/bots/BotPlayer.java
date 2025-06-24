package com.gempukku.lotro.bots;

import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface BotPlayer {
    String chooseAction(GameState gameState, AwaitingDecision awaitingDecision);
    String getName();
}
