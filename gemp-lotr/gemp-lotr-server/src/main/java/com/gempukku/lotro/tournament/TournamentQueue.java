package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.io.IOException;
import java.sql.SQLException;

public interface TournamentQueue {
    String getID();
    int getCost();

    String getFormat();

    CollectionType getCollectionType();

    String getTournamentQueueName();

    String getPrizesDescription();

    String getPairingDescription();

    String getStartCondition();

    boolean isRequiresDeck();

    boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) throws SQLException, IOException;

    void joinPlayer(CollectionsManager collectionsManager, Player player, LotroDeck deck) throws SQLException, IOException;

    void leavePlayer(CollectionsManager collectionsManager, Player player) throws SQLException, IOException;

    void leaveAllPlayers(CollectionsManager collectionsManager) throws SQLException, IOException;

    int getPlayerCount();
    String getPlayerList();

    boolean isPlayerSignedUp(String player);

    boolean isJoinable();
}
