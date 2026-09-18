package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;

public interface PrizePlaceholderDAO {
    DBDefs.PrizePlaceholder getPlaceholder(int id);

    /**
     * @return every placeholder that has not been resolved yet, oldest first
     */
    List<DBDefs.PrizePlaceholder> getUnresolved();

    /**
     * @return the most recently resolved placeholders, newest first, at most {@code limit} of them
     */
    List<DBDefs.PrizePlaceholder> getResolved(int limit);

    /**
     * Finds the placeholder for an event's promise (keyed by event kind, event id and label), so that the same
     * promise is never represented by two placeholders.  An unresolved row wins over a resolved one; among
     * resolved rows the newest wins.  {@code eventId} may be null (manual promises).
     */
    DBDefs.PrizePlaceholder findPromise(String eventKind, String eventId, String label);

    /**
     * Inserts the placeholder; {@code created} is filled in when missing.
     * @return the id of the new row
     */
    int createPlaceholder(DBDefs.PrizePlaceholder placeholder);

    /**
     * Records that the placeholder has been swapped for a real card.
     */
    void markResolved(int id, String blueprintId, String resolvedBy);

    /**
     * Every placeholder (resolved or not) recorded for an event.
     */
    List<DBDefs.PrizePlaceholder> findByEvent(String eventKind, String eventId);

    /**
     * Removes a placeholder row; only meant for unresolved promises nobody holds (a promise dropped from an event
     * before it ended).
     */
    void deletePlaceholder(int id);
}
