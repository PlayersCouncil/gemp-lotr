package com.gempukku.lotro.game;

import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultUserFeedback implements UserFeedback {
    private final Map<String, AwaitingDecision> _awaitingDecisionMap = new HashMap<>();

    private final List<DecisionObserver> _observers = new ArrayList<>();
    private DecisionArbiter _arbiter;
    /** With an arbiter: the decisions that have been asked but not yet put to their players, in the order asked. */
    private final Map<String, AwaitingDecision> _held = new LinkedHashMap<>();

    private LotroGame _game;

    public void setGame(LotroGame game) {
        _game = game;
    }

    public void addDecisionObserver(DecisionObserver observer) {
        _observers.add(observer);
    }

    /**
     * From now on, decisions are held back until the game comes to rest, and the arbiter says which are put to their
     * players then.  See DecisionArbiter.
     */
    public void setDecisionArbiter(DecisionArbiter arbiter) {
        _arbiter = arbiter;
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
        _held.remove(playerId);
        _game.getGameState().playerDecisionFinished(playerId);
    }

    public AwaitingDecision getAwaitingDecision(String playerId) {
        return _awaitingDecisionMap.get(playerId);
    }

    @Override
    public void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision) {
        _awaitingDecisionMap.put(playerId, awaitingDecision);
        if (_arbiter == null) {
            announce(playerId, awaitingDecision);
        } else {
            _held.remove(playerId);
            _held.put(playerId, awaitingDecision);
        }
    }

    /**
     * With an arbiter, this is when decisions reach their players.  The game calls it whenever it stops working.
     * Anybody who asks a decision again outside the game's own working (its answer was rejected) has to call it too.
     */
    @Override
    public void atRest() {
        if (_arbiter == null || _held.isEmpty())
            return;
        List<String> released = _arbiter.release(Collections.unmodifiableMap(new LinkedHashMap<>(_held)));
        for (String playerId : released) {
            AwaitingDecision decision = _held.remove(playerId);
            if (decision != null)
                announce(playerId, decision);
        }
    }

    private void announce(String playerId, AwaitingDecision awaitingDecision) {
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
