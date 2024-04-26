package com.gempukku.lotro.hall;

import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroGameParticipant;

import java.util.*;

public class GameTable {
    private final GameSettings gameSettings;
    private final Map<String, LotroGameParticipant> players = new HashMap<>();

    private LotroGameMediator lotroGameMediator;
    private final int capacity;

    public GameTable(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
        this.capacity = gameSettings.getLotroFormat().getAdventure().isSolo() ? 1 : 2;
    }

    public void startGame(LotroGameMediator lotroGameMediator) {
        this.lotroGameMediator = lotroGameMediator;
    }

    public LotroGameMediator getLotroGameMediator() {
        return lotroGameMediator;
    }

    public boolean wasGameStarted() {
        return lotroGameMediator != null;
    }

    public boolean addPlayer(LotroGameParticipant player) {
        players.put(player.getPlayerId(), player);
        return players.size() == capacity;
    }

    public boolean removePlayer(String playerId) {
        players.remove(playerId);
        return players.isEmpty();
    }

    public boolean hasPlayer(String playerId) {
        return players.containsKey(playerId);
    }

    public List<String> getPlayerNames() {
        return new LinkedList<>(players.keySet());
    }

    public Set<LotroGameParticipant> getPlayers() {
        return Collections.unmodifiableSet(new HashSet<>(players.values()));
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }
}
