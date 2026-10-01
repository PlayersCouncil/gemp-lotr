package com.gempukku.lotro.game;

import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultUserFeedback implements UserFeedback {
    private final Map<String, AwaitingDecision> _awaitingDecisionMap = new HashMap<>();

    private final List<DecisionObserver> _observers = new ArrayList<>();

    private LotroGame _game;

    public void setGame(LotroGame game) {
        _game = game;
    }

    public void addDecisionObserver(DecisionObserver observer) {
        _observers.add(observer);
    }

    /**
     * To be called once a decision has taken an answer without complaint, and before the game is told to carry on.
     * @param decision The decision, which by now is no longer awaiting anything.
     */
    public void decisionAccepted(String playerId, AwaitingDecision decision, String answer) {
        for (DecisionObserver observer : _observers)
            observer.decisionAnswered(playerId, decision, answer);
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
        for (DecisionObserver observer : _observers)
            observer.decisionIssued(playerId, awaitingDecision);
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
