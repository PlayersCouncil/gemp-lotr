package com.gempukku.lotro.events;

/**
 * One completed event as the event browser lists it: a plain value object with no reference to a live
 * {@code League} or {@code Tournament}, so that the per-month cache holding it cannot be invalidated out from
 * under those services' own caches (the {@code CacheManager} clears its registered caches in no defined order).
 * <p>
 * Nothing admin-only lives here.  EventHistoryService derives the admin links from {@link #kind} and {@link #id} for the
 * viewers that are allowed them, so one cached month serves admins and non-admins alike.
 */
public class EventSummary {
    /** "league" or "tournament".  Not sent per event (the response echoes the requested kind once); kept to derive the admin links. */
    public final String kind;
    /** The league code, or the tournament id. */
    public final String id;
    public final String name;
    /** ISO-8601 date, always present. */
    public final String startDate;
    /** ISO-8601 date, or null when the event has no recorded end (every tournament - see EventHistoryService). */
    public final String endDate;
    /** Human-readable where the format is still known, otherwise the raw code.  Never null. */
    public final String format;
    /** Null when it could not be determined cheaply. */
    public final Integer playerCount;
    /** Rounds played, for tournaments; null for leagues. */
    public final Integer rounds;

    public EventSummary(String kind, String id, String name, String startDate, String endDate, String format,
                        Integer playerCount, Integer rounds) {
        this.kind = kind;
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.format = format;
        this.playerCount = playerCount;
        this.rounds = rounds;
    }
}
