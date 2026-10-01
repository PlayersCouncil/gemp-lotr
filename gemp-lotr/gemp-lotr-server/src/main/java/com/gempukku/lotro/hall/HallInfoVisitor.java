package com.gempukku.lotro.hall;

import java.util.List;

public interface HallInfoVisitor {
    public enum TableStatus {
        WAITING, PLAYING, FINISHED
    }

    public void serverTime(String time);

    public void motd(String motd);

    /**
     * Whether the server is in shutdown mode (an admin put it there ahead of a restart): games in progress carry on, but
     * no new table, bot game or tournament queue can be started or joined.
     */
    public void shutdownMode(boolean shutdown);

    /**
     * @param createdAt  when the table was opened (epoch ms, server clock)
     * @param invitedYou the table is invite-only and the player being visited is its invitee
     */
    public void visitTable(String tableId, String gameId, boolean watchable, TableStatus status, String statusDescription, String formatName, String tournamentName, String userDesc, List<String> playerIds, boolean playing, boolean isPrivate, boolean isInviteOnly, String winner,
                           long createdAt, boolean invitedYou);

    public void visitTournamentQueue(String tournamentQueueKey, int cost, String collectionName, String formatName, String type, String tournamentQueueName, String tournamentPrizes,
                                     String pairingDescription, String startCondition, int playerCount, String playerList, boolean playerSignedUp, boolean joinable, boolean startable,
                                     int readyCheckSecsRemaining, boolean confirmedReadyCheck, boolean wc, String draftCode, boolean recurring, boolean scheduled,
                                     boolean displayInWaitingTables);

    public void visitTournament(String tournamentKey, String collectionName, String formatName, String tournamentName, String type,
                                String pairingDescription, String tournamentStage, int round, int playerCount, String playerList, boolean playerInCompetition, boolean abandoned, boolean joinable,
                                long secsRemaining, boolean wc);

    public void runningPlayerGame(String gameId);
}
