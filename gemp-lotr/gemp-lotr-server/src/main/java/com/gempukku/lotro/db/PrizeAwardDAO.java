package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;

public interface PrizeAwardDAO {
    /**
     * @return every award logged for the event (a league code, tournament id or campaign tag), oldest first
     */
    List<DBDefs.PrizeAward> getAwards(String eventKind, String eventId);

    /**
     * Logs an award; {@code awarded_on} is filled in when missing.
     * @return the id of the new row
     */
    int addAward(DBDefs.PrizeAward award);
}
