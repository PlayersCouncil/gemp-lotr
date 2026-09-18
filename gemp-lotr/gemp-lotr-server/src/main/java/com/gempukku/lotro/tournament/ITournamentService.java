package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITournamentService {
    void clearCache();

    void addPlayer(String tournamentId, String playerName, LotroDeck deck);

    void dropPlayer(String tournamentId, String playerName);

    Set<String> getPlayers(String tournamentId);

    Map<String, LotroDeck> getPlayerDecks(String tournamentId, String format);

    Set<String> getDroppedPlayers(String tournamentId);

    LotroDeck getPlayerDeck(String tournamentId, String player, String format);

    void addMatch(String tournamentId, int round, String playerOne, String playerTwo);

    void setMatchResult(String tournamentId, int round, String winner);

    void setPlayerDeck(String tournamentId, String player, LotroDeck deck);

    List<TournamentMatch> getMatches(String tournamentId);

    Tournament addTournament(TournamentInfo info);

    void updateTournamentStage(String tournamentId, Tournament.Stage stage);

    void updateTournamentRound(String tournamentId, int round);

    List<Tournament> getOldTournaments(ZonedDateTime since);

    List<Tournament> getLiveTournaments();

    Tournament getTournamentById(String tournamentId);

    void addRoundBye(String tournamentId, String player, int round);

    Map<String, Integer> getPlayerByes(String tournamentId);

    List<DBDefs.ScheduledTournament> getUnstartedScheduledTournamentQueues(ZonedDateTime tillDate);

    List<DBDefs.ScheduledTournament> getScheduledTournamentsBetween(ZonedDateTime from, ZonedDateTime to);

    void updateScheduledTournamentStarted(String scheduledTournamentId);
}
