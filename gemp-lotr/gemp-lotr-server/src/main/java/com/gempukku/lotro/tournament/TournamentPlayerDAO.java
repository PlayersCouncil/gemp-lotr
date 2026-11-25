package com.gempukku.lotro.tournament;

import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.Map;
import java.util.Set;

public interface TournamentPlayerDAO {
    public void addPlayer(String tournamentId, String playerName, LotroDeck deck);

    public void updatePlayerDeck(String tournamentId, String playerName, LotroDeck deck);

    public void dropPlayer(String tournamentId, String playerName);

    /** Deletes every player recorded for the tournament; only for undoing a tournament start that failed. */
    public void removePlayers(String tournamentId);

    public Map<String, LotroDeck> getPlayerDecks(String tournamentId, String format);

    public Set<String> getDroppedPlayers(String tournamentId);

    public LotroDeck getPlayerDeck(String tournamentId, String playerName, String format);

    public Set<String> getPlayers(String tournamentId);
}
