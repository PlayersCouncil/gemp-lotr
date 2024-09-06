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

    void updateScheduledTournamentStarted(String scheduledTournamentId);
}
