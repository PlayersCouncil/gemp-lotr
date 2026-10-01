package com.gempukku.lotro.tournament;

import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.hall.HallException;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.io.IOException;
import java.sql.SQLException;

public interface TournamentQueue {
    String getID();
    int getCost();

    String getFormatCode();

    CollectionType getCollectionType();
    TournamentInfo getInfo();

    String getTournamentQueueName();

    String getPrizesDescription();

    String getPairingDescription();

    String getStartCondition();

    boolean isRequiresDeck();

    boolean process() throws SQLException, IOException ;

    /**
     * Signs the player up (taking the entry cost).
     * @throws HallException with a message for the player when they were not signed up: already in the queue, sign-up
     * not open, queue full, not enough currency...
     */
    void joinPlayer(Player player, LotroDeck deck) throws SQLException, IOException, HallException;

    /** As {@link #joinPlayer(Player, LotroDeck)}, for queues that do not take a deck at sign-up. */
    void joinPlayer(Player player) throws SQLException, IOException, HallException;

    void leavePlayer(Player player) throws SQLException, IOException;

    void leaveAllPlayers() throws SQLException, IOException;

    int getPlayerCount();
    String getPlayerList();

    boolean isPlayerSignedUp(String player);

    boolean isJoinable();

    boolean isStartable(String byWhom);

    boolean requestStart(String byWhom);

    int getSecondsRemainingForReadyCheck();

    boolean confirmReadyCheck(String player);

    boolean hasConfirmedReadyCheck(String player);

    boolean isWC();

    String getDraftCode();

    boolean shouldBeDisplayedAsWaiting();
}
