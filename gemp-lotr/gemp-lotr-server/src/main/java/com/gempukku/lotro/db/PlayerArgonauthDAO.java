package com.gempukku.lotro.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Gemp's copy of which accounts belong to which PC account (table `player_argonauth`). Argonauth is the authority:
 * this copy is brought up to date at every sign-in and whenever Argonauth is asked about one account, and it decides
 * only what is shown, never who may enter (PC-ACCOUNT.md).
 */
public interface PlayerArgonauthDAO {
    /** The ids of the accounts this PC account owns, lowest first. */
    List<Integer> getPlayerIds(String sub) throws SQLException;

    /** The PC account that owns this account, or null. */
    String getSub(int playerId) throws SQLException;

    /** Records that this account belongs to this PC account, whoever had it before. */
    void setLink(int playerId, String sub, String proof) throws SQLException;

    /** Records that this account belongs to nobody. */
    void removeLink(int playerId) throws SQLException;

    /**
     * Makes the copy say that this PC account owns exactly these accounts (each with the proof Argonauth has for
     * it): the ones it no longer owns are let go, and the ones it owns now are taken from whoever had them here.
     */
    void replaceLinks(String sub, Map<Integer, String> proofByPlayerId) throws SQLException;
}
