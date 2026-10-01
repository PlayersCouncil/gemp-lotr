package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.CollectionType;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TournamentDAO {
    void addTournament(DBDefs.Tournament info);

    void addScheduledTournament(DBDefs.ScheduledTournament info);

    List<DBDefs.Tournament> getUnfinishedTournaments();

    DBDefs.Tournament getTournament(String tournamentId);

    List<DBDefs.Tournament> getFinishedTournamentsSince(ZonedDateTime time);

    /**
     * The raw rows of every FINISHED tournament whose {@code start_date} falls in the half-open range [from, to),
     * newest first.  Half-open, never BETWEEN: {@code start_date} is a DATETIME, so an inclusive upper bound would
     * drop everything after 00:00:00 on the last day of the month.
     * <p>
     * Raw rows, not {@link Tournament} objects: hydrating a Tournament costs ~6 queries and parses every stored
     * decklist, which a month listing must not pay.
     */
    List<DBDefs.Tournament> getFinishedTournamentsBetween(ZonedDateTime from, ZonedDateTime to);

    /**
     * How many players took part in each FINISHED tournament starting in [from, to), keyed by tournament_id.
     * One grouped query for the whole month rather than one query per tournament.
     */
    Map<String, Integer> getFinishedTournamentPlayerCountsBetween(ZonedDateTime from, ZonedDateTime to);

    /**
     * Every {@code yyyy-MM} in which at least one FINISHED tournament with a played round started, unordered.
     */
    List<String> getFinishedTournamentMonths();

    DBDefs.Tournament getTournamentById(String tournamentId);

    void updateTournamentStage(String tournamentId, Tournament.Stage stage);

    void updateTournamentRound(String tournamentId, int round);

    List<DBDefs.ScheduledTournament> getUnstartedScheduledTournamentQueues(ZonedDateTime tillDate);

    /**
     * Every scheduled tournament (started or not) whose start falls within [from, to], for calendar display.
     */
    List<DBDefs.ScheduledTournament> getScheduledTournamentsBetween(ZonedDateTime from, ZonedDateTime to);
    DBDefs.ScheduledTournament getScheduledTournament(String tournamentId);

    /**
     * Rewrites the name, format, start, type and parameters of the not-yet-started scheduled tournament with
     * {@code info.tournament_id}.  The id itself never changes.  Returns the number of rows changed: 0 when there
     * is no such unstarted row (it started in the meantime, or never existed).
     */
    int updateScheduledTournament(DBDefs.ScheduledTournament info);

    void updateScheduledTournamentStarted(String scheduledTournamentId);
}
