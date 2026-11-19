package com.gempukku.lotro.game;

import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultUserFeedback implements UserFeedback {
    private final Map<String, AwaitingDecision> _awaitingDecisionMap = new HashMap<>();

    private LotroGame _game;

    public void setGame(LotroGame game) {
        _game = game;
    }

    /**
     * To be called once a decision has taken an answer without complaint, and before the game is told to carry on.
     * The game's own feedback keeps the answer if a loop being run needs it (LoopFeedback).
     */
    public void decisionAccepted(String playerId, AwaitingDecision decision, String answer) {
        if (_game != null && _game.getUserFeedback() != this)
            _game.getUserFeedback().decisionAnswered(playerId, decision, answer);
    }

    public void participantDecided(String playerId) {
        _awaitingDecisionMap.remove(playerId);
        _game.getGameState().playerDecisionFinished(playerId);
    }

    public AwaitingDecision getAwaitingDecision(String playerId) {
        return _awaitingDecisionMap.get(playerId);
    }

    @Override
    public void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision) {
        _awaitingDecisionMap.put(playerId, awaitingDecision);
        _game.getGameState().playerDecisionStarted(playerId, awaitingDecision);
    }

    @Override
    public boolean hasPendingDecisions() {
        return _awaitingDecisionMap.size() > 0;
    }

    public Set<String> getUsersPendingDecision() {
        return _awaitingDecisionMap.keySet();
    }
}
