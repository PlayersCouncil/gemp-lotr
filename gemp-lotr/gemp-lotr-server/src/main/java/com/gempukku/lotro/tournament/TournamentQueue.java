package com.gempukku.lotro.tournament;

import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
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

    void joinPlayer(Player player, LotroDeck deck) throws SQLException, IOException;

    void joinPlayer(Player player) throws SQLException, IOException;

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
}
