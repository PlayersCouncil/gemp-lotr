package com.gempukku.lotro.game;

import java.util.ArrayList;
import java.util.List;

public class ParticipantTurnOrder {
    private final List<String> _participants;
    private int _currentPlayerIndex = -1;

    public ParticipantTurnOrder(List<String> participants) {
        _participants = new ArrayList<>(participants);
    }

    public void startNewTurn() {
        _currentPlayerIndex++;
        if (_participants.size() == _currentPlayerIndex)
            _currentPlayerIndex = 0;
    }

    public String getCurrentParticipant() {
        if (_currentPlayerIndex == -1)
            return null;
        return _participants.get(_currentPlayerIndex);
    }
}
