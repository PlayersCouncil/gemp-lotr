package com.gempukku.lotro.hall;

import java.util.Map;

public interface HallChannelVisitor {
    public void channelNumber(int channelNumber);
    public void motdChanged(String motd);
    
    public void serverTime(String serverTime);

    /**
     * Sent with every hall update (like the server time): whether the server is in shutdown mode.  A default no-op so
     * visitors that do not care need not implement it.
     */
    public default void shutdownMode(boolean shutdown) {
    }
    public void newPlayerGame(String gameId);

    public void addTournamentQueue(String queueId, Map<String, String> props);
    public void updateTournamentQueue(String queueId, Map<String, String> props);
    public void removeTournamentQueue(String queueId);

    public void addTournament(String tournamentId, Map<String, String> props);
    public void updateTournament(String tournamentId, Map<String, String> props);
    public void removeTournament(String tournamentId);

    public void addTable(String tableId, Map<String, String> props);
    public void updateTable(String tableId, Map<String, String> props);
    public void removeTable(String tableId);
}
