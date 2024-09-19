package com.gempukku.lotro.hall;

import java.util.List;

public interface HallInfoVisitor {
    public enum TableStatus {
        WAITING, PLAYING, FINISHED
    }

    public void serverTime(String time);

    public void motd(String motd);

    public void visitTable(String tableId, String gameId, boolean watchable, TableStatus status, String statusDescription, String formatName, String tournamentName, String userDesc, List<String> playerIds, boolean playing, boolean isPrivate, boolean isInviteOnly, String winner);

    public void visitTournamentQueue(String tournamentQueueKey, int cost, String collectionName, String formatName, String type, String tournamentQueueName, String tournamentPrizes,
                                     String pairingDescription, String startCondition, int playerCount, String playerList, boolean playerSignedUp, boolean joinable);

    public void visitTournament(String tournamentKey, String collectionName, String formatName, String tournamentName, String type,
                                String pairingDescription, String tournamentStage, int round, int playerCount, String playerList, boolean playerInCompetition, boolean abandoned);

    public void runningPlayerGame(String gameId);
}
