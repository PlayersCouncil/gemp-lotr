package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.CollectionType;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public interface TournamentDAO {
    void addTournament(DBDefs.Tournament info);

    void addScheduledTournament(DBDefs.ScheduledTournament info);

    List<DBDefs.Tournament> getUnfinishedTournaments();

    DBDefs.Tournament getTournament(String tournamentId);

    List<DBDefs.Tournament> getFinishedTournamentsSince(ZonedDateTime time);

    DBDefs.Tournament getTournamentById(String tournamentId);

    void updateTournamentStage(String tournamentId, Tournament.Stage stage);

    void updateTournamentRound(String tournamentId, int round);

    List<DBDefs.ScheduledTournament> getUnstartedScheduledTournamentQueues(ZonedDateTime tillDate);

    /**
     * Every scheduled tournament (started or not) whose start falls within [from, to], for calendar display.
     */
    List<DBDefs.ScheduledTournament> getScheduledTournamentsBetween(ZonedDateTime from, ZonedDateTime to);
    DBDefs.ScheduledTournament getScheduledTournament(String tournamentId);

    void updateScheduledTournamentStarted(String scheduledTournamentId);
}
