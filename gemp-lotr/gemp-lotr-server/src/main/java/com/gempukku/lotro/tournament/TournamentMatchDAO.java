package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;
import java.util.Map;

public interface TournamentMatchDAO {
    int addMatch(String tournamentId, int round, String playerOne, String playerTwo);

    void setMatchResult(String tournamentId, int round, String winner);

    List<DBDefs.TournamentMatch> getMatches(String tournamentId);

    int addBye(String tournamentId, String player, int round);

    Map<String, Integer> getPlayerByes(String tournamentId);
}
