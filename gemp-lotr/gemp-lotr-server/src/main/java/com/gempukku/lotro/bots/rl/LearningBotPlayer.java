package com.gempukku.lotro.bots.rl;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface LearningBotPlayer extends BotPlayer {
    void observe(GameState gameState, AwaitingDecision decision, String playerId, String chosenAction, double reward, boolean terminal);
    void endEpisode(double reward); // call after each game ends
}
