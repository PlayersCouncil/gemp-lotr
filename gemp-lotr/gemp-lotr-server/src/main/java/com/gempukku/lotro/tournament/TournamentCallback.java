package com.gempukku.lotro.tournament;

import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.Collection;

public interface TournamentCallback {
    void createGame(String playerOne, LotroDeck deckOne, String playerTwo, LotroDeck deckTwo);

    void broadcastMessage(String message, Collection<String> toWhom);
}
